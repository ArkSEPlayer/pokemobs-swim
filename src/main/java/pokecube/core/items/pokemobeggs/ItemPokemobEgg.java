package pokecube.core.items.pokemobeggs;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/** @author Manchou */
public class ItemPokemobEgg extends Item
{
    public static double                   PLAYERDIST = 2;
    public static double                   MOBDIST    = 4;
    static HashMap<PokedexEntry, IPokemob> fakeMobs   = new HashMap<>();
    public static Item                     EGG        = null;

    public static byte[] getColour(final int[] fatherColours, final int[] motherColours)
    {
        final byte[] ret = new byte[] { 127, 127, 127, 127 };
        if (fatherColours.length < 3 && motherColours.length < 3) return ret;
        for (int i = 0; i < 3; i++)
            ret[i] = (byte) ((fatherColours[i] + motherColours[i]) / 2 - 128);
        return ret;
    }

    public static ItemStack getEggStack(final IPokemob pokemob)
    {
        final ItemStack stack = ItemPokemobEgg.getEggStack(pokemob.getPokedexEntry());
        ItemPokemobEgg.initStack(pokemob.getEntity(), pokemob, stack);
        return stack;
    }

    public static ItemStack getEggStack(final PokedexEntry entry)
    {
        final ItemStack eggItemStack = new ItemStack(ItemPokemobEgg.EGG);
        eggItemStack.setTag(new CompoundNBT());
        eggItemStack.getTag().putString("pokemob", entry.getName());
        return eggItemStack;
    }

    public static PokedexEntry getEntry(final ItemStack stack)
    {
        if (stack.isEmpty() || stack.getTag() == null) return null;
        if (stack.getTag().contains("pokemob")) return Database.getEntry(stack.getTag().getString("pokemob"));
        genes:
        if (stack.getTag().contains(GeneticsManager.GENES))
        {
            final INBT genes = stack.getTag().get(GeneticsManager.GENES);
            final IMobGenetics eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
            GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, eggs, null, genes);
            final Alleles<SpeciesInfo, SpeciesGene> gene = eggs.getAlleles(GeneticsManager.SPECIESGENE);
            if (gene == null) break genes;
            final SpeciesGene sgene = gene.getExpressed();
            final SpeciesInfo info = sgene.getValue();
            // Lets cache this for easier lookup.
            stack.getTag().putString("pokemob", info.entry.getTrimmedName());
            return info.entry;
        }
        return Database.getEntry(stack.getTag().getString("pokemob"));
    }

    public static IPokemob getFakePokemob(final World world, final Vector3 location, final ItemStack stack)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        IPokemob pokemob = ItemPokemobEgg.fakeMobs.get(entry);
        if (pokemob == null)
        {
            pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
            if (pokemob == null) return null;
            ItemPokemobEgg.fakeMobs.put(entry, pokemob);
        }
        location.moveEntity(pokemob.getEntity());
        return pokemob;
    }

    private static void getGenetics(final IPokemob mother, final IPokemob father, final CompoundNBT nbt)
    {
        final IMobGenetics eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
        final IMobGenetics mothers = mother.getEntity().getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        final IMobGenetics fathers = father.getEntity().getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        GeneticsManager.initEgg(eggs, mothers, fathers);
        final INBT tag = GeneRegistry.GENETICS_CAP.getStorage().writeNBT(GeneRegistry.GENETICS_CAP, eggs, null);
        nbt.put(GeneticsManager.GENES, tag);
        try
        {
            final Alleles<SpeciesInfo, SpeciesGene> alleles = eggs.getAlleles(GeneticsManager.SPECIESGENE);
            final SpeciesGene gene = alleles.getExpressed();
            final SpeciesInfo info = gene.getValue();
            nbt.putString("pokemob", info.entry.getName());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        nbt.putString("motherId", mother.getEntity().getStringUUID());
        return;
    }

    public static IPokemob getPokemob(final World world, final ItemStack stack)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry == null) return null;
        final IPokemob ret = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
        return ret;
    }

    public static float getSize(final float fatherSize, final float motherSize)
    {
        float ret = 1;
        ret = (fatherSize + motherSize) * 0.5f * (1 + 0.075f * (float) ThutCore.newRandom().nextGaussian());
        ret = Math.min(Math.max(0.1f, ret), 2);
        return ret;
    }

    private static LivingEntity imprintOwner(final IPokemob mob)
    {
        final Vector3 location = Vector3.getNewVector().set(mob.getEntity());
        PlayerEntity player = mob.getEntity().getCommandSenderWorld().getNearestPlayer(location.x, location.y,
                location.z, ItemPokemobEgg.PLAYERDIST, EntityPredicates.NO_SPECTATORS);
        LivingEntity owner = player;
        final AxisAlignedBB box = location.getAABB().inflate(ItemPokemobEgg.MOBDIST, ItemPokemobEgg.MOBDIST,
                ItemPokemobEgg.MOBDIST);
        if (owner == null)
        {
            final List<LivingEntity> list = mob.getEntity().getCommandSenderWorld().getEntitiesOfClass(
                    LivingEntity.class, box, (Predicate<LivingEntity>) input -> !(input instanceof EntityPokemobEgg));
            final LivingEntity closestTo = mob.getEntity();
            LivingEntity t = null;
            double d0 = Double.MAX_VALUE;
            for (final LivingEntity t1 : list)
                if (t1 != closestTo && EntityPredicates.NO_SPECTATORS.test(t1))
                {
                    final double d1 = closestTo.distanceToSqr(t1);

                    if (d1 <= d0)
                    {
                        t = t1;
                        d0 = d1;
                    }
                }
            owner = t;
        }
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(owner);
        final IOwnable ownable = OwnableCaps.getOwnable(owner);
        if (owner == null || pokemob != null || ownable != null)
        {
            if (pokemob != null && pokemob.getOwner() instanceof PlayerEntity) player = (PlayerEntity) pokemob
                    .getOwner();
            else if (ownable != null && ownable.getOwner() instanceof PlayerEntity) player = (PlayerEntity) ownable
                    .getOwner();
            owner = player;
        }
        return owner;
    }

    public static void tryImprint(final IPokemob mob)
    {
        final LivingEntity owner = ItemPokemobEgg.imprintOwner(mob);
        final Config config = PokecubeCore.getConfig();
        // Check permissions
        if (owner instanceof PlayerEntity && (config.permsHatch || config.permsHatchSpecific))
        {
            final PokedexEntry entry = mob.getPokedexEntry();
            final PlayerEntity player = (PlayerEntity) owner;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            if (config.permsHatch && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTPOKEMOB,
                    context)) return;
            if (config.permsHatchSpecific && !handler.hasPermission(player.getGameProfile(), Permissions.SENDOUTSPECIFIC
                    .get(entry), context)) return;
        }
        if (owner != null)
        {
            mob.setOwner(owner.getUUID());
            mob.setGeneralState(GeneralStates.TAMED, true);
            mob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            mob.setHeldItem(ItemStack.EMPTY);
        }
        else mob.getEntity().getPersistentData().remove(TagNames.HATCHED);
    }

    public static void initPokemobGenetics(final IPokemob mob, final CompoundNBT nbt, final boolean imprint)
    {
        mob.setForSpawn(10);
        if (imprint) mob.getEntity().getPersistentData().putBoolean(TagNames.HATCHED, true);
        if (nbt.contains(GeneticsManager.GENES))
        {
            final INBT genes = nbt.get(GeneticsManager.GENES);
            final IMobGenetics eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
            GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, eggs, null, genes);
            GeneticsManager.initFromGenes(eggs, mob);
        }
    }

    public static void initStack(final Entity mother, final IPokemob father, final ItemStack stack)
    {
        if (!stack.hasTag()) stack.setTag(new CompoundNBT());
        final IPokemob mob = CapabilityPokemob.getPokemobFor(mother);
        if (mob != null && father != null) ItemPokemobEgg.getGenetics(mob, father, stack.getTag());
    }

    public static IPokemob make(final World world, final ItemStack stack, final EntityPokemobEgg egg)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        final MobEntity entity = PokecubeCore.createPokemob(entry, world);
        if (entity != null)
        {
            final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, true);
            mob.setHealth(mob.getMaxHealth());
            int exp = Tools.levelToXp(mob.getExperienceMode(), 1);
            exp = Math.max(1, exp);
            mob.setForSpawn(exp);
            entity.moveTo(Math.floor(egg.getX()) + 0.5, Math.floor(egg.getY()) + 0.5, Math.floor(egg.getZ()) + 0.5,
                    world.random.nextFloat() * 360F, 0.0F);
            final CompoundNBT nbt = stack.getTag();
            final boolean hasNest = nbt.contains("nestLoc");
            if (stack.hasTag()) ItemPokemobEgg.initPokemobGenetics(mob, stack.getTag(), !hasNest);
            mob.spawnInit();
        }
        return CapabilityPokemob.getPokemobFor(entity);
    }

    public static void spawn(final IPokemob mob, final ItemStack stack, final World world, final EntityPokemobEgg egg)
    {
        final MobEntity entity = mob.getEntity();
        final CompoundNBT nbt = stack.getTag();
        world.addFreshEntity(entity);
        if (mob.getOwner() != null)
        {
            final LivingEntity owner = mob.getOwner();
            owner.sendMessage(new TranslationTextComponent("pokemob.hatch", mob.getDisplayName().getString()),
                    Util.NIL_UUID);
            if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) world.addFreshEntity(new ExperienceOrbEntity(
                    world, entity.getX(), entity.getY(), entity.getZ(), entity.getRandom().nextInt(7) + 1));
        }
        final EggEvent.Hatch evt = new EggEvent.Hatch(egg);
        PokecubeCore.POKEMOB_BUS.post(evt);
        if (nbt.contains("nestLoc"))
        {
            final BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound("nestLoc"));
            final TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof NestTile) ((NestTile) tile).addResident(mob);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, false);
        }
        entity.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        entity.playAmbientSound();
    }

    public ItemPokemobEgg(final Properties props)
    {
        super(props);
        ItemPokemobEgg.EGG = this;
    }

    /**
     * allows items to add custom lines of information to the mouseover
     * description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final World playerIn,
            final List<ITextComponent> tooltip, final ITooltipFlag advanced)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry != null) tooltip.add(1, new TranslationTextComponent("item.pokecube.pokemobegg.named", I18n.get(entry
                .getUnlocalizedName())));
    }

    /**
     * This function should return a new entity to replace the dropped item.
     * Returning null here will not kill the ItemEntity and will leave it to
     * function normally. Called when the item it placed in a world.
     *
     * @param world
     *            The world object
     * @param location
     *            The ItemEntity object, useful for getting the position of the
     *            entity
     * @param itemstack
     *            The current item stack
     * @return A new Entity object to spawn or null
     */
    @Override
    public Entity createEntity(final World world, final Entity oldItem, final ItemStack itemstack)
    {
        if (this.hasCustomEntity(itemstack))
        {
            final EntityPokemobEgg egg = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world).setStack(itemstack)
                    .setToPos(oldItem.getX(), oldItem.getY(), oldItem.getZ());
            egg.setDeltaMovement(oldItem.getDeltaMovement());
            return egg;
        }
        return null;
    }

    public boolean dropEgg(final World world, final ItemStack stack, final Vector3 location, final Entity placer)
    {
        final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
        if (entry == null) return false;
        final ItemStack eggItemStack = new ItemStack(ItemPokemobEgg.EGG, 1);
        if (stack.hasTag()) eggItemStack.setTag(stack.getTag());
        else eggItemStack.setTag(new CompoundNBT());
        final EntityPokemobEgg entity = new EntityPokemobEgg(EntityPokemobEgg.TYPE, world).setToPos(location).setStack(
                eggItemStack);
        final EggEvent.Place event = new EggEvent.Place(entity);
        MinecraftForge.EVENT_BUS.post(event);
        world.addFreshEntity(entity);
        return true;
    }

    /**
     * Determines if this Item has a special entity for when they are in the
     * world. Is called when a ItemEntity is spawned in the world, if true and
     * Item#createCustomEntity returns non null, the ItemEntity will be
     * destroyed and the new Entity will be added to the world.
     *
     * @param stack
     *            The current item stack
     * @return True of the item has a custom entity, If true,
     *         Item#createCustomEntity will be called
     */
    @Override
    public boolean hasCustomEntity(final ItemStack stack)
    {
        return ItemPokemobEgg.getEntry(stack) != null;
    }

    @Override
    public ActionResultType useOn(final ItemUseContext context)
    {
        final World worldIn = context.getLevel();
        if (worldIn.isClientSide) return ActionResultType.SUCCESS;
        final Vector3d hit = context.getClickLocation();
        final Vector3 loc = Vector3.getNewVector().set(hit);
        final ItemStack stack = context.getItemInHand();
        final PlayerEntity playerIn = context.getPlayer();
        if (this.dropEgg(worldIn, stack, loc, playerIn) && !playerIn.abilities.instabuild) stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }

}