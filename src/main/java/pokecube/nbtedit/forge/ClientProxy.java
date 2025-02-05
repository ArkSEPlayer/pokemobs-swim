package pokecube.nbtedit.forge;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.PokecubeAPI;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.gui.GuiEditNBTTree;
import pokecube.nbtedit.nbt.SaveStates;
import pokecube.nbtedit.packets.EntityRequestPacket;
import pokecube.nbtedit.packets.PacketHandler;
import pokecube.nbtedit.packets.TileRequestPacket;
import thut.core.common.network.Packet;
import thut.lib.TComponent;

@OnlyIn(value = Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static KeyMapping NBTEditKey;

    @SubscribeEvent
    public void onKey(final InputEvent.KeyInputEvent event)
    {
        if (ClientProxy.NBTEditKey.consumeClick())
        {
            final HitResult pos = Minecraft.getInstance().hitResult;
            if (pos != null)
            {
                Packet ret = null;
                switch (pos.getType())
                {
                case BLOCK:
                    ret = new TileRequestPacket(((BlockHitResult) pos).getBlockPos());
                    break;
                case ENTITY:
                    ret = new EntityRequestPacket(((EntityHitResult) pos).getEntity().getId());
                    break;
                case MISS:
                    NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", ChatFormatting.RED);
                    return;
                default:
                    NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", ChatFormatting.RED);
                    return;
                }
                PacketHandler.INSTANCE.sendToServer(ret);
            }
        }
    }

    @Override
    public void openEditGUI(final BlockPos pos, final CompoundTag tag)
    {
        Minecraft.getInstance().setScreen(new GuiEditNBTTree(pos, tag));
    }

    @Override
    public void openEditGUI(final int entityID, final CompoundTag tag)
    {
        Minecraft.getInstance().setScreen(new GuiEditNBTTree(entityID, tag));
    }

    @Override
    public void openEditGUI(final int entityID, final String customName, final CompoundTag tag)
    {
        Minecraft.getInstance().setScreen(new GuiEditNBTTree(entityID, customName, tag));
    }

    @Override
    public void sendMessage(final Player player, final String message, final ChatFormatting color)
    {
        final Component component = TComponent.literal(message);
        component.getStyle().withColor(TextColor.fromLegacyFormat(color));
        thut.lib.ChatHelper.sendSystemMessage(Minecraft.getInstance().player, component);
    }

    @Override
    public void setupClient()
    {
        MinecraftForge.EVENT_BUS.register(this);
        try
        {
            final SaveStates save = NBTEdit.getSaveStates();
            save.load();
            save.save();
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.catching(e);
        }
    }
}
