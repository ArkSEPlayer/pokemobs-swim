package pokecube.core.inventory.pc;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;

public class PCSlot extends Slot
{

    public boolean release = false;

    public PCSlot(final Container inventory, final int slotIndex, final int xDisplay, final int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }

    /** Return whether this slot's stack can be taken from this slot. */
    @Override
    public boolean mayPickup(final Player par1PlayerEntity)
    {
        return !this.release;
    }

    @Override
    public boolean mayPlace(final ItemStack itemstack)
    {
        return this.container.canPlaceItem(this.getSlotIndex(), itemstack);
    }

    /** Called when the stack in a Slot changes */
    @Override
    public void setChanged()
    {
        if (this.getItem().isEmpty()) this.container.setItem(this.getSlotIndex(), ItemStack.EMPTY);
        this.container.setChanged();
    }

    /** Helper method to put a stack in the slot. */
    @Override
    public void set(final ItemStack par1ItemStack)
    {
        this.container.setItem(this.getSlotIndex(), par1ItemStack);
        PlayerPokemobCache.UpdateCache(par1ItemStack, true, false);
        this.setChanged();
    }

    @Override
    public void onTake(final Player thePlayer, final ItemStack stack)
    {
        PlayerPokemobCache.UpdateCache(stack, false, false);
        super.onTake(thePlayer, stack);
    }
}
