package versoma.dimension.lore.maintenance;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.UUID;

public class MaintenanceManager {

    // private static final long CLEAR_INTERVAL_MS = 30 * 60 * 1000L; // 30 минут
    // private static final long RESTART_INTERVAL_MS = 12 * 60 * 60 * 1000L; // 12 часов
    // private static final long WARNING_TIME_MS = 60 * 1000L; // 1 минута

    private static final long CLEAR_INTERVAL_MS = 90 * 1000L; // 30 минут
    private static final long RESTART_INTERVAL_MS = 30 * 1000L; // 12 часов
    private static final long WARNING_TIME_MS = 60 * 1000L; // 1 минута

    private static long nextClearTime = System.currentTimeMillis() + CLEAR_INTERVAL_MS;
    private static long nextRestartTime = System.currentTimeMillis() + RESTART_INTERVAL_MS;

    private static boolean isClearWarningActive = false;
    private static boolean isRestartWarningActive = false;

    // В новых версиях конструктор требует UUID первым параметром
    private static final ServerBossEvent clearBar = new ServerBossEvent(
            UUID.randomUUID(),
            Component.literal("Очистка предметов..."),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private static final ServerBossEvent restartBar = new ServerBossEvent(
            UUID.randomUUID(),
            Component.literal("Рестарт сервера..."),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();

        handleClear(server, now);
        // handleRestart(server, now);
    }

    private static void handleClear(MinecraftServer server, long now) {
        long timeUntilClear = nextClearTime - now;

        if (timeUntilClear <= WARNING_TIME_MS && timeUntilClear > 0) {
            if (!isClearWarningActive) {
                isClearWarningActive = true;

                // Создаем интерактивный компонент программно
                Component cancelBtn = Component.literal("[ОТМЕНИТЬ]")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.RED)
                                .withBold(true)
                                .withClickEvent(new ClickEvent.RunCommand("/lore_cancel_clear")));

                Component msg = Component.literal("Очистка предметов через 60 секунд. ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(cancelBtn);

                server.getPlayerList().broadcastSystemMessage(msg, false);
            }

            clearBar.setProgress((float) timeUntilClear / WARNING_TIME_MS);
            syncBossBarPlayers(server, clearBar);

        } else if (timeUntilClear <= 0) {
            executeClear(server);
        }
    }

    private static void handleRestart(MinecraftServer server, long now) {
        long timeUntilRestart = nextRestartTime - now;

        if (timeUntilRestart <= WARNING_TIME_MS && timeUntilRestart > 0) {
            if (!isRestartWarningActive) {
                isRestartWarningActive = true;
                server.getPlayerList().broadcastSystemMessage(Component.literal("Внимание! Плановый рестарт сервера через 1 минуту.").withStyle(ChatFormatting.RED), false);
            }

            restartBar.setProgress((float) timeUntilRestart / WARNING_TIME_MS);
            syncBossBarPlayers(server, restartBar);

        } else if (timeUntilRestart <= 0) {
            forceRestart(server);
        }
    }

    private static void executeClear(MinecraftServer server) {
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity) {
                    entity.discard();
                    count++;
                }
            }
        }

        server.getPlayerList().broadcastSystemMessage(Component.literal("Удалено " + count + " предметов."), false);
        resetClearTimer();
    }

    private static void forceRestart(MinecraftServer server) {
        java.util.List<ServerPlayer> playersToKick = java.util.List.copyOf(server.getPlayerList().getPlayers());

        for (ServerPlayer player : playersToKick) {
            player.connection.disconnect(Component.literal("Плановый рестарт. Сервер скоро поднимется."));
        }

        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "stop");
    }

    public static void cancelClear() {
        resetClearTimer();
    }

    private static void resetClearTimer() {
        nextClearTime = System.currentTimeMillis() + CLEAR_INTERVAL_MS;
        isClearWarningActive = false;
        clearBar.removeAllPlayers();
    }

    private static void syncBossBarPlayers(MinecraftServer server, ServerBossEvent bossBar) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            // Команда отмены для обычных игроков
            dispatcher.register(Commands.literal("lore_cancel_clear").executes(context -> {
                MaintenanceManager.cancelClear();
                context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("Очистка предметов отменена игроком " + context.getSource().getTextName()).withStyle(ChatFormatting.GREEN), false
                );
                return 1;
            }));

            // Форсированная очистка для админов
            dispatcher.register(Commands.literal("lore_force_clear")
                    .requires(source -> {
                        if (source.getServer() == null) return false; // Защита от NPE при входе
                        var stopCmd = source.getServer().getCommands().getDispatcher().getRoot().getChild("stop");
                        return stopCmd != null && stopCmd.canUse(source);
                    })
                    .executes(context -> {
                        executeClear(context.getSource().getServer());
                        return 1;
                    }));

            // Форсированный рестарт для админов
            /*
            dispatcher.register(Commands.literal("lore_force_restart")
                    .requires(source -> {
                        if (source.getServer() == null) return false; // Защита от NPE при входе
                        var stopCmd = source.getServer().getCommands().getDispatcher().getRoot().getChild("stop");
                        return stopCmd != null && stopCmd.canUse(source);
                    })
                    .executes(context -> {
                        forceRestart(context.getSource().getServer());
                        return 1;
                    }));
             */
        });
    }
}