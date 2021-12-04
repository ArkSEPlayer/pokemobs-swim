package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.NewRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.Proxy;
import thut.core.common.ThutCore;

public class CommonProxy implements Proxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistry event)
        {
            if (PokecubeCore.proxy == null)
            {
                PokecubeCore.proxy = new CommonProxy();
                NBTEdit.proxy = new pokecube.nbtedit.forge.CommonProxy();
                MinecraftForge.EVENT_BUS.addListener(PokecubeCore.proxy::handleServerAboutToStart);
                MinecraftForge.EVENT_BUS.addListener(NBTEdit.proxy::handleServerAboutToStart);
            }
        }
    }

    @Override
    public void setServer(final MinecraftServer server)
    {
    }

    @Override
    public MinecraftServer getServer()
    {
        return ThutCore.proxy.getServer();
    }

    public Player getPlayer(final UUID uuid)
    {
        return this.getServer().getPlayerList().getPlayer(uuid);
    }

    public ResourceLocation getPlayerSkin(final String name)
    {
        return null;
    }

    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        return null;
    }

    public Level getWorld()
    {
        return this.getServer().getLevel(Level.OVERWORLD);
    }

    public Player getPlayer()
    {
        return null;
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

}
