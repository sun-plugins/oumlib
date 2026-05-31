package dev.oum.example;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.command.Arguments;
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskGroup;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class ExampleAnnouncer {

    private static final boolean IS_PAPER = checkIsPaper();
    private static final long startTime = System.currentTimeMillis();
    private static final AtomicInteger joinCount = new AtomicInteger(0);
    private static final AtomicInteger broadcastCount = new AtomicInteger(0);

    private static ConfigManager<PluginConfig> configManager;
    private static TaskGroup announcerGroup;

    public record AnnouncerStats(
            int joinCount,
            int broadcastCount,
            long uptimeSeconds
    ) {}

    private static boolean checkIsPaper() {
        try {
            Class.forName("org.bukkit.Bukkit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void initialize() {
        configManager = ConfigManager.of(PluginConfig.class, "config.yml", () -> new PluginConfig(
                "<gold>[OumLib]</gold> ",
                "<gray>[<green>+</green>] <yellow>%player%</yellow> joined!</gray>",
                true,
                "<yellow>Reminder: You can reload this config in real-time!</yellow>"
        ));

        configManager.enableAutoReload();
        configManager.onReload(newConfig -> {
            OumLib.logError("Announcer config was reloaded from disk!");
            rescheduleTasks();
        });

        announcerGroup = Scheduler.newGroup();
        rescheduleTasks();

        OumLib.globalRegistry()
                .forNamespace("oumlib")
                .add("uptime", player -> String.valueOf((System.currentTimeMillis() - startTime) / 1000))
                .add("broadcasts", player -> String.valueOf(broadcastCount.get()))
                .add("joins", player -> String.valueOf(joinCount.get()));

        registerCommands();
    }

    public static boolean isAutoBroadcastEnabled() {
        return configManager.get().autoBroadcastEnabled();
    }

    public static int getJoinCount() {
        return joinCount.get();
    }

    public static int getBroadcastCount() {
        return broadcastCount.get();
    }

    public static void toggleAutoBroadcast() {
        PluginConfig old = configManager.get();
        PluginConfig updated = new PluginConfig(
                old.chatPrefix(),
                old.joinMessageFormat(),
                !old.autoBroadcastEnabled(),
                old.broadcastTemplate()
        );
        configManager.update(updated);
    }

    public static void triggerManualBroadcast() {
        PluginConfig config = configManager.get();
        broadcast(config.chatPrefix() + config.broadcastTemplate());
        broadcastCount.incrementAndGet();
    }

    private static void rescheduleTasks() {
        announcerGroup.cancelAll();

        PluginConfig config = configManager.get();
        if (!config.autoBroadcastEnabled()) return;

        announcerGroup.runRepeating(Duration.ofSeconds(30), Duration.ofSeconds(30), () -> {
            broadcast(config.chatPrefix() + config.broadcastTemplate());
            broadcastCount.incrementAndGet();
        });

        announcerGroup.runRepeating(Duration.ofSeconds(5), Duration.ofSeconds(5), () -> {
            String barMsg = "<gold>Uptime: <oumlib_uptime> seconds</gold>";
            if (IS_PAPER) {
                PaperAnnouncerHelper.sendActionbarToAll(barMsg);
            } else {
                VelocityAnnouncerHelper.sendActionbarToAll(barMsg);
            }
        });
    }

    private static void registerCommands() {
        var broadcastArg = Arguments.string("message");
        var onceArg = Arguments.string("message");

        Commands.create("oum")
                .permission("oumlib.admin")
                .executes(context -> {
                    Text.Preset.info(context.sender(), "OumLib Administration Dashboard");

                    Text.builder("<dark_gray> - </dark_gray><green>[Reload Config]</green>")
                            .clickRunCommand("/oum reload")
                            .hoverText("<gray>Click to reload config.yml</gray>")
                            .send(context.sender());

                    Text.builder("<dark_gray> - </dark_gray><aqua>[Show Stats]</aqua>")
                            .clickRunCommand("/oum stats")
                            .hoverText("<gray>Click to print current performance stats</gray>")
                            .send(context.sender());

                    if (IS_PAPER) {
                        Text.builder("<dark_gray> - </dark_gray><yellow>[Open Menu]</yellow>")
                                .clickRunCommand("/oum menu")
                                .hoverText("<gray>Click to open the chest GUI dashboard</gray>")
                                .send(context.sender());
                    }
                })
                .subcommand(sub -> sub
                        .label("reload")
                        .permission("oumlib.admin")
                        .executes(context -> {
                            configManager.reload();
                            Text.Preset.success(context.sender(), "Configuration reloaded successfully!");
                        })
                )
                .subcommand(sub -> sub
                        .label("stats")
                        .permission("oumlib.admin")
                        .executes(context -> {
                            AnnouncerStats stats = new AnnouncerStats(
                                    joinCount.get(),
                                    broadcastCount.get(),
                                    (System.currentTimeMillis() - startTime) / 1000
                            );
                            Text.Preset.info(context.sender(), "Current statistics for OumLib Announcer:");
                            Text.send(context.sender(), " <gray>Joins: <gold><joinCount></gold> | Broadcasts: <gold><broadcastCount></gold> | Uptime: <gold><uptimeSeconds>s</gold></gray>", stats);
                        })
                )
                .subcommand(sub -> sub
                        .label("broadcast")
                        .permission("oumlib.admin")
                        .argument(broadcastArg)
                        .executes(context -> {
                            String message = context.args().get(broadcastArg);
                            broadcast(configManager.get().chatPrefix() + message);
                            broadcastCount.incrementAndGet();
                        })
                )
                .subcommand(sub -> sub
                        .label("once")
                        .permission("oumlib.admin")
                        .argument(onceArg)
                        .executes(context -> {
                            String messageToDeliver = context.args().get(onceArg);
                            Text.Preset.info(context.sender(), "Registered join listener. The next player to join will receive this message.");

                            if (IS_PAPER) {
                                PaperAnnouncerHelper.registerOnceListener(messageToDeliver);
                            } else {
                                VelocityAnnouncerHelper.registerOnceListener(messageToDeliver);
                            }
                        })
                )
                .subcommand(sub -> sub
                        .label("menu")
                        .permission("oumlib.admin")
                        .executes(context -> {
                            if (context.isPlayer()) {
                                if (IS_PAPER) {
                                    PaperAnnouncerHelper.openMenu(context.sender());
                                } else {
                                    Text.Preset.error(context.sender(), "Menus are only supported on Paper servers!");
                                }
                            } else {
                                Text.Preset.error(context.sender(), "Only players can open menus!");
                            }
                        })
                )
                .register();
    }

    public static void handlePlayerJoin(String playerName) {
        joinCount.incrementAndGet();
        PluginConfig config = configManager.get();
        String joinMsg = config.joinMessageFormat().replace("%player%", playerName);
        broadcast(config.chatPrefix() + joinMsg);
    }

    private static void broadcast(String miniMessageText) {
        var component = MiniMessage.miniMessage().deserialize(miniMessageText);
        if (IS_PAPER) {
            PaperAnnouncerHelper.broadcast(component);
        } else {
            VelocityAnnouncerHelper.broadcast(component);
        }
    }
}
