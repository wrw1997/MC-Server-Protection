/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/MC-Server-Protection
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.TheElm.project.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.TheElm.project.CoreMod;
import net.TheElm.project.config.SewingMachineConfig;
import net.TheElm.project.enums.ChatRooms;
import net.TheElm.project.interfaces.PlayerChat;
import net.TheElm.project.utilities.MessageUtils;
import net.TheElm.project.utilities.TranslatableServerSide;
import net.minecraft.command.arguments.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ChatroomCommands {
    
    private ChatroomCommands() {}
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (!SewingMachineConfig.INSTANCE.CHAT_MODIFY.get())
            return;
        
        LiteralCommandNode<ServerCommandSource> townChat = dispatcher.register(CommandManager.literal("t")
            .requires(ClaimCommand::sourceInTown)
            .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.TOWN)))
            .executes((context -> switchToChatRoom(context, ChatRooms.TOWN)))
        );
        CoreMod.logDebug( "- Registered Town chat command" );
        
        LiteralCommandNode<ServerCommandSource> globalChat = dispatcher.register(CommandManager.literal("g")
            .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.GLOBAL)))
            .executes((context -> switchToChatRoom(context, ChatRooms.GLOBAL)))
        );
        CoreMod.logDebug( "- Registered Global chat command" );
        
        LiteralCommandNode<ServerCommandSource> localChat = dispatcher.register(CommandManager.literal("l")
            .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.LOCAL)))
            .executes((context -> switchToChatRoom(context, ChatRooms.LOCAL)))
        );
        CoreMod.logDebug( "- Registered Local chat command" );
        
        dispatcher.register(CommandManager.literal("chat")
            .then(CommandManager.literal("town")
                .requires(ClaimCommand::sourceInTown)
                .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.TOWN)))
                .executes((context -> switchToChatRoom(context, ChatRooms.TOWN)))
            )
            .then(CommandManager.literal("global")
                .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.GLOBAL)))
                .executes((context -> switchToChatRoom(context, ChatRooms.GLOBAL)))
            )
            .then(CommandManager.literal("local")
                .then(CommandManager.argument("text", MessageArgumentType.message()).executes((context) -> sendToChatRoom(context, ChatRooms.LOCAL)))
                .executes((context -> switchToChatRoom(context, ChatRooms.LOCAL)))
            )
        );
        
    }
    
    private static int sendToChatRoom(final CommandContext<ServerCommandSource> context, final ChatRooms chatRoom) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        
        // Format the text
        Text chatText = MessageUtils.formatPlayerMessage(
            player,
            chatRoom,
            MessageArgumentType.getMessage(context, "text")
        );
        
        // Send the new chat message to the currently selected chat room
        MessageUtils.sendTo(chatRoom, player, chatText);
        
        return Command.SINGLE_SUCCESS;
    }
    private static int switchToChatRoom(final CommandContext<ServerCommandSource> context, final ChatRooms chatRoom) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        
        // Update the chat room
        ((PlayerChat) player).setChatRoom(chatRoom);
        
        // Tell the player
        player.sendMessage(TranslatableServerSide.text(player, "chat.change." + chatRoom.name().toLowerCase()));
        
        return Command.SINGLE_SUCCESS;
    }
    
}
