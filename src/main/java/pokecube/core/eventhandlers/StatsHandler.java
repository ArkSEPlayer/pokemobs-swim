package pokecube.core.eventhandlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.EggEvent;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.api.events.pokemobs.TradeEvent;
import pokecube.api.events.pokemobs.combat.KillEvent;
import pokecube.api.items.IPokecube;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.init.Config;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;
import thut.lib.TComponent;

public class StatsHandler
{
    public static void register()
    {
        // This checks if the capture is allowed, and cancels the event
        // otherwise. It checks things such as: if the pokecube can capture it,
        // if the player is allowed to capture it, and if the pokemob is already
        // tamed, etc.
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.HIGHEST, StatsHandler::canCapture);

        // From here down, they are lowest, false to allow addons to override
        // the behaviour.
        // These just record the given event for use in things like pokedex
        // stats, etc
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordCapture);
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordEvolve);
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordHatch);
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordKill);
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, StatsHandler::recordTrade);

    }

    private static void canCapture(final CaptureEvent.Pre evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.getFilledCube());
        if (IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehaviour cube = IPokecube.PokecubeBehaviour.BEHAVIORS.get(id);
            cube.onPreCapture(evt);
        }
        if (evt.getCaught() == null || !(evt.pokecube instanceof EntityPokecube cube)) return;
        final PokedexEntry entry = evt.getCaught().getPokedexEntry();
        if (evt.getCaught().getGeneralState(GeneralStates.TAMED)) evt.setResult(Result.DENY);
        if (evt.getCaught().getGeneralState(GeneralStates.DENYCAPTURE)) evt.setResult(Result.DENY);
        final Entity catcher = cube.shootingEntity;
        if (!EntityPokecubeBase.canCaptureBasedOnConfigs(evt.getCaught()))
        {
            evt.setCanceled(true);
            if (catcher instanceof Player player)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
            CaptureManager.onCaptureDenied(cube);
            return;
        }
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (catcher instanceof ServerPlayer player && (config.permsCapture || config.permsCaptureSpecific))
        {
            boolean denied = false;
            if (config.permsCapture && !PermNodes.getBooleanPerm(player, Permissions.CATCHPOKEMOB)) denied = true;
            if (config.permsCaptureSpecific && !denied
                    && !PermNodes.getBooleanPerm(player, Permissions.CATCHSPECIFIC.get(entry)))
                denied = true;
            if (denied)
            {
                evt.setCanceled(true);
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
                CaptureManager.onCaptureDenied(cube);
                return;
            }
        }

        if (ISpecialCaptureCondition.captureMap.containsKey(entry))
        {
            boolean deny = true;
            final ISpecialCaptureCondition condition = ISpecialCaptureCondition.captureMap.get(entry);
            try
            {
                deny = !condition.canCapture(catcher, evt.getCaught());
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error checking capture for " + entry, e);
            }

            if (deny)
            {
                evt.setCanceled(true);
                if (catcher instanceof Player player)
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
                condition.onCaptureFail(catcher, evt.getCaught());
                CaptureManager.onCaptureDenied(cube);
                return;
            }
        }
    }

    private static void recordCapture(final CaptureEvent.Post evt)
    {
        final ResourceLocation id = PokecubeItems.getCubeId(evt.getFilledCube());
        if (IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(id))
        {
            final PokecubeBehaviour cube = IPokecube.PokecubeBehaviour.BEHAVIORS.get(id);
            cube.onPostCapture(evt);
        }
        if (evt.getCaught() == null || evt.isCanceled() || evt.getCaught().isShadow()) return;
        StatsCollector.addCapture(evt.getCaught());
    }

    private static void recordEvolve(final EvolveEvent.Post evt)
    {
        if (evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }

    private static void recordHatch(final EggEvent.Hatch evt)
    {
        StatsCollector.addHatched(evt.egg);
    }

    private static void recordKill(final KillEvent evt)
    {
        if (!evt.killed.isShadow()) StatsCollector.addKill(evt.killed, evt.killer);
    }

    private static void recordTrade(final TradeEvent evt)
    {
        if (evt.mob == null || evt.mob.isShadow()) return;
        StatsCollector.addCapture(evt.mob);
    }
}
