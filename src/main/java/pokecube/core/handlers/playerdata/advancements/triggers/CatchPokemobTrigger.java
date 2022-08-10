package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.utils.TagNames;

public class CatchPokemobTrigger implements CriterionTrigger<CatchPokemobTrigger.Instance>
{
    public static class Instance extends AbstractCriterionTriggerInstance
    {
        final PokedexEntry entry;
        boolean            lenient = false;
        int                number  = -1;
        int                sign    = 0;

        public Instance(final EntityPredicate.Composite player, final PokedexEntry entry, final boolean lenient, final int number, final int sign)
        {
            super(CatchPokemobTrigger.ID, player);
            this.entry = entry != null ? entry : Database.missingno;
            this.lenient = lenient;
            this.number = number;
            this.sign = sign;
        }

        public boolean test(final ServerPlayer player, final IPokemob pokemob)
        {
            PokedexEntry entry = this.entry;
            PokedexEntry testEntry = pokemob.getPokedexEntry();
            boolean numCheck = true;
            if (this.lenient)
            {
                entry = entry.base ? entry : entry.getBaseForme();
                testEntry = testEntry.base ? testEntry : testEntry.getBaseForme();
            }
            if (this.number != -1)
            {
                int num = -1;
                if (entry == Database.missingno) num = CaptureStats.getNumberUniqueCaughtBy(player.getUUID());
                else num = CaptureStats.getTotalNumberOfPokemobCaughtBy(player.getUUID(), entry);
                if (num == -1) return false;
                numCheck = num * this.sign > this.number;
            }
            if (pokemob.getEntity().getPersistentData().getBoolean(TagNames.HATCHED)) return false;
            return numCheck && (entry == Database.missingno || testEntry == entry) && pokemob.getOwner() == player;
        }

    }

    static class Listeners
    {
        private final PlayerAdvancements                                            playerAdvancements;
        private final Set<CriterionTrigger.Listener<CatchPokemobTrigger.Instance>> listeners = Sets.<CriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newHashSet();

        public Listeners(final PlayerAdvancements playerAdvancementsIn)
        {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public void add(final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.add(listener);
        }

        public boolean isEmpty()
        {
            return this.listeners.isEmpty();
        }

        public void remove(final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
        {
            this.listeners.remove(listener);
        }

        public void trigger(final ServerPlayer player, final IPokemob pokemob)
        {
            List<CriterionTrigger.Listener<CatchPokemobTrigger.Instance>> list = null;

            for (final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener : this.listeners)
                if (listener.getTriggerInstance().test(player, pokemob))
                {
                    if (list == null)
                        list = Lists.<CriterionTrigger.Listener<CatchPokemobTrigger.Instance>> newArrayList();

                    list.add(listener);
                }
            if (list != null) for (final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener1 : list)
                listener1.run(this.playerAdvancements);
        }
    }

    public static ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "catch");

    private final Map<PlayerAdvancements, CatchPokemobTrigger.Listeners> listeners = Maps.<PlayerAdvancements, CatchPokemobTrigger.Listeners> newHashMap();

    public CatchPokemobTrigger()
    {
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners == null)
        {
            bredanimalstrigger$listeners = new CatchPokemobTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, bredanimalstrigger$listeners);
        }

        bredanimalstrigger$listeners.add(listener);
    }

    @Override
    public ResourceLocation getId()
    {
        return CatchPokemobTrigger.ID;
    }

    @Override
    public void removePlayerListeners(final PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements playerAdvancementsIn,
            final CriterionTrigger.Listener<CatchPokemobTrigger.Instance> listener)
    {
        final CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (bredanimalstrigger$listeners != null)
        {
            bredanimalstrigger$listeners.remove(listener);

            if (bredanimalstrigger$listeners.isEmpty()) this.listeners.remove(playerAdvancementsIn);
        }
    }

    public void trigger(final ServerPlayer player, final IPokemob pokemob)
    {
        final CatchPokemobTrigger.Listeners bredanimalstrigger$listeners = this.listeners.get(player.getAdvancements());
        if (bredanimalstrigger$listeners != null) bredanimalstrigger$listeners.trigger(player, pokemob);
    }

    @Override
    public Instance createInstance(final JsonObject json, final DeserializationContext conditions)
    {
        final EntityPredicate.Composite pred = EntityPredicate.Composite.fromJson(json, "player", conditions);
        final String name = json.has("entry") ? json.get("entry").getAsString() : "";
        final int number = json.has("number") ? json.get("number").getAsInt() : -1;
        final int sign = json.has("sign") ? json.get("sign").getAsInt() : 0;
        final boolean lenient = json.has("lenient") ? json.get("lenient").getAsBoolean() : false;
        return new CatchPokemobTrigger.Instance(pred, Database.getEntry(name), lenient, number, sign);
    }
}
