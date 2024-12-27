package xuan.cat.shulker_previewer.code.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xuan.cat.shulker_previewer.code.PluginMain;

import static java.util.Objects.requireNonNullElseGet;

public final class CommanderExecutor implements CommandExecutor {
    private final @NotNull PluginMain main;

    public CommanderExecutor(@NotNull PluginMain main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(main.getGlobalConfig().commandMessagesNeedHelp);
            return true;
        }
        switch (args[0]) {
            case "reload" -> {
                if (sender.hasPermission("command.shulkerview.reload")) {
                    main.reloadGlobalConfig();
                    sender.sendMessage(main.getGlobalConfig().commandMessagesDoReload);
                    Bukkit.getOnlinePlayers().forEach(player -> player.getScheduler().execute(main, player::updateInventory, null, 1L));
                } else {
                    sender.sendMessage(main.getGlobalConfig().commandMessagesNoPermission);
                }
            }
            case "switch" -> doSwitch(sender);
            default -> sender.sendMessage(main.getGlobalConfig().commandMessagesNeedHelp);
        }
        return true;
    }

    private void doSwitch(@NotNull CommandSender sender) {
        if (!sender.hasPermission("command.shulkerview.switch")) {
            sender.sendMessage(main.getGlobalConfig().commandMessagesNoPermission);
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(main.getGlobalConfig().commandMessagesNonPlayer);
            return;
        }
        boolean to = !player.getPersistentDataContainer().getOrDefault(main.statusKey, PersistentDataType.BOOLEAN, !main.getGlobalConfig().commandDefault);
        player.getPersistentDataContainer().set(main.statusKey, PersistentDataType.BOOLEAN, to);
        sender.sendMessage(to ? main.getGlobalConfig().commandMessagesToOn : main.getGlobalConfig().commandMessagesToOff);
        player.updateInventory();
    }
}
