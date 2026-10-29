package de.pizzapost.all_items.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.pizzapost.all_items.AllItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;

import java.net.URI;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    private static final PermissionCheck GAMEMASTERS_CHECK = new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS));

    public static void initializeCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("start").executes(context -> {
                MinecraftServer server = context.getSource().getServer();
                if (!AllItems.started) {
                    AllItems.startGame(server, false);
                } else {
                    context.getSource().sendSuccess(() -> Component.translatable("command.all_items.already_started"), false);
                }
                return 1;
            }));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("set_timer").requires(Commands.hasPermission(GAMEMASTERS_CHECK)).then(argument("days", IntegerArgumentType.integer(0)).then(argument("hours", IntegerArgumentType.integer(0, 23)).then(argument("minutes", IntegerArgumentType.integer(0, 59)).then(argument("seconds", IntegerArgumentType.integer(0, 59)).executes(context -> {
                CommandSourceStack source = context.getSource();
                int days = IntegerArgumentType.getInteger(context, "days");
                int hours = IntegerArgumentType.getInteger(context, "hours");
                int minutes = IntegerArgumentType.getInteger(context, "minutes");
                int seconds = IntegerArgumentType.getInteger(context, "seconds");
                AllItems.setGameTimer(source.getServer(), days, hours, minutes, seconds);
                return 1;
            }))))));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("skip_item").requires(Commands.hasPermission(GAMEMASTERS_CHECK)).executes(context -> {
            Component skippedItem = AllItems.skipItem();
            context.getSource().sendSuccess(() -> Component.translatable("command.all_items.skipped", skippedItem), false);
            return 1;
        })));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("obtaining").executes(context -> {
            if (!AllItems.isVanillaItem(AllItems.items.getFirst())) {
                context.getSource().sendSuccess(() -> Component.translatable("command.all_items.obtaining_failed"), false);
                return 1;
            } else {
                String link = AllItems.getObtainingLink();
                context.getSource().sendSuccess(() -> Component.translatable("command.all_items.obtaining").withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(link))).withUnderlined(true)), false);
                return 1;
            }
        })));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("update").executes(context -> {
            int added = AllItems.manuallyUpdate(context.getSource().getServer());
            context.getSource().sendSuccess(() -> Component.translatable("command.all_items.manually_updated", added), false);
            return 1;
        })));
    }

    public static void registerModCommands() {
        AllItems.LOGGER.info("Registering Mod Commands for " + AllItems.MOD_ID);
        initializeCommands();
    }
}