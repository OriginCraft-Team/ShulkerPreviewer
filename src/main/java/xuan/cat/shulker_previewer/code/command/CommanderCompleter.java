package xuan.cat.shulker_previewer.code.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CommanderCompleter implements TabCompleter {
    public CommanderCompleter() {
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("help");
            if (sender.hasPermission("command.shulkerview.reload")) {
                list.add("reload");
            }
            if (sender.hasPermission("command.shulkerview.switch") && sender instanceof Player) {
                list.addAll(List.of("on", "off"));
            }
            return list.stream().filter(str -> str.startsWith(args[0])).sorted(StringUtils::compareIgnoreCase).toList();
        } else {
            return List.of();
        }
    }
}
