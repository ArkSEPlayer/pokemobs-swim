package pokecube.legends.handlers;

import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.legends.blocks.properties.Strippables;
import pokecube.legends.blocks.properties.Tillables;
import pokecube.legends.init.ItemInit;
import thut.lib.RegHelper;

public class EventsHandler
{

    public static void register()
    {
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::toolModificationEvent);
    }

    public static void toolModificationEvent(final BlockEvent.BlockToolModificationEvent event)
    {
        Tillables.tillables(event);
        Strippables.strippables(event);
    }

    public static boolean isWearingUltraArmour(LivingEntity player)
    {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return (head.getItem() == ItemInit.ULTRA_HELMET.get() && chest.getItem() == ItemInit.ULTRA_CHESTPLATE.get()
                && legs.getItem() == ItemInit.ULTRA_LEGGINGS.get() && feet.getItem() == ItemInit.ULTRA_BOOTS.get());
    }

    public static boolean takesBiomeDamage(LivingEntity player)
    {
        return !isWearingUltraArmour(player);
    }

    public static void onPlayerTick(final PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayer player)
        {
            final Biome biome = event.player.getLevel().getBiome(player.getOnPos()).value();
            MobEffectInstance effect = null;

            if (RegHelper.getKey(biome).toString().equals("pokecube_legends:aquamarine_caves") && !player.isCreative()
                    && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.DIG_SLOWDOWN))
            {
                effect = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 480, 2);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:azure_badlands")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:eroded_azure_badlands")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:wooded_azure_badlands"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.WEAKNESS))
            {
                effect = new MobEffectInstance(MobEffects.WEAKNESS, 480, 1);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:blinding_deltas")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:dried_blinding_deltas")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:magmatic_blinding_deltas")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:shattered_blinding_deltas")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:volcanic_blinding_deltas"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.BLINDNESS))
            {
                effect = new MobEffectInstance(MobEffects.BLINDNESS, 480, 0);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:burnt_beach")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:meteorite_spikes"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.UNLUCK))
            {
                effect = new MobEffectInstance(MobEffects.UNLUCK, 480, 1);

                if (takesBiomeDamage(player))
                {
                    player.setSecondsOnFire(10);
                }
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:corrupted_caves")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:deep_frozen_polluted_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:deep_polluted_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:frozen_polluted_river")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:frozen_polluted_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:polluted_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:polluted_river")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:shattered_tainted_barrens")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:tainted_barrens"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.CONFUSION))
            {
                effect = new MobEffectInstance(MobEffects.CONFUSION, 480, 1);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:crystallized_beach")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:mirage_desert")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:rocky_mirage_desert")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:snowy_crystallized_beach"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.LEVITATION))
            {
                effect = new MobEffectInstance(MobEffects.LEVITATION, 120, 0);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:dead_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:dead_river")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:deep_dead_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:deep_frozen_dead_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:frozen_dead_ocean")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:frozen_dead_river"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.HUNGER))
            {
                effect = new MobEffectInstance(MobEffects.HUNGER, 480, 2);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:distorted_lands")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:small_distorted_islands"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SPEED))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 480, 2);
            }

            if (RegHelper.getKey(biome).toString().equals("pokecube_legends:dripstone_caves") && !player.isCreative()
                    && !player.isSpectator())
            {
                if (takesBiomeDamage(player) && player.tickCount % 20 == 0)
                {
                    // this is a massively nerfed version of the HARM effect.
                    // The default one is a bit too OP.
                    if (!player.isInvertedHealAndHarm()) player.hurt(DamageSource.MAGIC, (float) (2));
                    else player.heal((float) (2));
                }
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:forbidden_grove")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:forbidden_meadow")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:forbidden_taiga")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:old_growth_forbidden_taiga")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:snowy_forbidden_taiga")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:windswept_forbidden_taiga"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.WITHER))
            {
                effect = new MobEffectInstance(MobEffects.WITHER, 480, 1);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:frozen_peaks")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:jagged_peaks")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:snowy_slopes")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:ultra_stony_peaks"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SLOWDOWN))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 480, 2);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:fungal_flower_forest")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:fungal_forest")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:fungal_plains")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:fungal_sunflower_plains")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:snowy_fungal_plains"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SLOWDOWN))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 480, 0);
            }

            if ((RegHelper.getKey(biome).toString().equals("pokecube_legends:sparse_temporal_jungle")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:temporal_bamboo_jungle")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:temporal_jungle")
                    || RegHelper.getKey(biome).toString().equals("pokecube_legends:windswept_temporal_jungle"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.POISON))
            {
                effect = new MobEffectInstance(MobEffects.POISON, 480, 1);
            }

            if (RegHelper.getKey(biome).toString().equals("pokecube_legends:ultra_stony_shore") && !player.isCreative()
                    && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.UNLUCK))
            {
                effect = new MobEffectInstance(MobEffects.UNLUCK, 480, 0);
            }

            if (takesBiomeDamage(player) && effect != null)
            {
                effect.setCurativeItems(Lists.newArrayList(new ItemStack(ItemInit.ULTRA_HELMET.get()),
                        new ItemStack(ItemInit.ULTRA_CHESTPLATE.get()), new ItemStack(ItemInit.ULTRA_LEGGINGS.get()),
                        new ItemStack(ItemInit.ULTRA_BOOTS.get())));
                player.addEffect(effect);
            }

            if (!takesBiomeDamage(player))
            {
                player.clearFire();
                player.curePotionEffects(new ItemStack(ItemInit.ULTRA_HELMET.get()));
                player.setIsInPowderSnow(false);
            }

        }
    }
}
