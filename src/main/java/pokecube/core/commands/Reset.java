package pokecube.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import pokecube.api.PokecubeAPI;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.PermNodes.DefaultPermissionLevel;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.lib.TComponent;

public class Reset
{

    public static int execute(final CommandSourceStack source, final ServerPlayer target) throws CommandSyntaxException
    {
        PokecubeSerializer.getInstance().setHasStarter(target, false);
        EventsHandler.sendInitInfo(target);
        source.sendSuccess(TComponent.translatable("pokecube.command.reset", target.getDisplayName()), true);
        thut.lib.ChatHelper.sendSystemMessage(target, TComponent.translatable("pokecube.command.canchoose"));
        PokecubeAPI.LOGGER.info("Reset Starter for {}", target.getGameProfile());
        return 0;
    }

    public static void register(final LiteralArgumentBuilder<CommandSourceStack> command)
    {
        final String perm = "command.pokecube.reset";
        PermNodes.registerNode(perm, DefaultPermissionLevel.OP,
                "Is the player allowed to reset the starter status of a player");
        command.then(Commands.literal("reset").requires(Tools.hasPerm(perm))
                .then(Commands.argument("target_player", EntityArgument.player()).executes(
                        (ctx) -> Reset.execute(ctx.getSource(), EntityArgument.getPlayer(ctx, "target_player")))));
    }
}
