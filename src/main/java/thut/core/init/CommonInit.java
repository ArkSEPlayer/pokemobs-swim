package thut.core.init;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import thut.api.Tracker;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.EntityCraft;
import thut.lib.TComponent;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class CommonInit
{
    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || evt.getWorld().isClientSide || evt.getItemStack().isEmpty()
                || !evt.getPlayer().isShiftKeyDown() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER.get())
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AABB box = new AABB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min;
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > 30 || dw > 2 * 20 + 1)
            {
                final String message = "msg.craft.toobig";
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                return;
            }
            if (!worldIn.isClientSide)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        ThutCrafts.CRAFTTYPE.get());
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
            }
            itemstack.getTag().remove("min");
            evt.setCanceled(true);
        }
        else
        {
            if (!itemstack.hasTag()) itemstack.setTag(new CompoundTag());
            final CompoundTag min = new CompoundTag();
            new Vector3().set(pos).writeToNBT(min, "");
            itemstack.getTag().put("min", min);
            final String message = "msg.craft.setcorner";
            if (!worldIn.isClientSide)
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", Tracker.instance().getTick());
        }
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == InteractionHand.OFF_HAND || evt.getWorld().isClientSide || evt.getItemStack().isEmpty()
                || !evt.getPlayer().isShiftKeyDown() || evt.getItemStack().getItem() != ThutCrafts.CRAFTMAKER.get())
            return;
        final ItemStack itemstack = evt.getItemStack();
        final Player playerIn = evt.getPlayer();
        final Level worldIn = evt.getWorld();
        final long now = Tracker.instance().getTick();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min")
                && itemstack.getTag().getLong("time") != now)
        {
            final CompoundTag minTag = itemstack.getTag().getCompound("min");
            final Vec3 loc = playerIn.position().add(0, playerIn.getEyeHeight(), 0)
                    .add(playerIn.getLookAngle().scale(2));
            final BlockPos pos = new BlockPos(loc);
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AABB box = new AABB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min;
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > 30 || dw > 2 * 20 + 1)
            {
                final String message = "msg.craft.toobig";
                if (!worldIn.isClientSide)
                    thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
                return;
            }
            if (!worldIn.isClientSide)
            {
                final EntityCraft craft = IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, mid,
                        ThutCrafts.CRAFTTYPE.get());
                final String message = craft != null ? "msg.craft.create" : "msg.craft.fail";
                thut.lib.ChatHelper.sendSystemMessage(playerIn, TComponent.translatable(message));
            }
            itemstack.getTag().remove("min");
        }
    }

    @SubscribeEvent
    public static void logout(final PlayerLoggedOutEvent event)
    {
        if (event.getPlayer().isPassenger() && event.getPlayer().getRootVehicle() instanceof EntityCraft)
            event.getPlayer().stopRiding();
    }

    @SubscribeEvent
    /**
     * Sends update packet to the mob.
     *
     * @param evt
     */
    public static void startTracking(final StartTracking evt)
    {
        if (evt.getTarget() instanceof IEntityAdditionalSpawnData && evt.getTarget() instanceof BlockEntityBase)
            EntityUpdate.sendEntityUpdate(evt.getTarget());
    }
}
