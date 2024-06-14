package xuan.cat.shulker_previewer.code.config;

import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.max;

public final class GlobalConfig {
    public final @NotNull ItemFormat formatSingleItem;
    public final @NotNull ItemFormat formatSingleItemRenamed;
    public final @NotNull ItemFormat formatMultiItem;
    public final @NotNull ItemFormat formatMultiItemRenamed;
    public final @NotNull String formatSeparator;
    public final @NotNull String formatHead;
    public final int rowItems;
    public final boolean commandDefault;
    public final @NotNull String commandMessagesToOn;
    public final @NotNull String commandMessagesToOff;
    public final @NotNull String commandMessagesNonPlayer;
    public final @NotNull String commandMessagesNeedHelp;
    public final @NotNull String commandMessagesDoReload;
    public final @NotNull String commandMessagesNoPermission;

    public GlobalConfig(@NotNull Configuration config) {
        formatSingleItem = new ItemFormat(config.getString("format.single.item", ""));
        formatSingleItemRenamed = new ItemFormat(config.getString("format.single.item-renamed", ""));
        formatMultiItem = new ItemFormat(config.getString("format.multi.item", ""));
        formatMultiItemRenamed = new ItemFormat(config.getString("format.multi.item-renamed", ""));
        formatSeparator = config.getString("format.separator", "");
        formatHead = config.getString("format.head", "");
        rowItems = max(1, config.getInt("row-items", 1));
        commandDefault = config.getBoolean("command.default", true);
        commandMessagesToOn = config.getString("command.messages.to-on", "");
        commandMessagesToOff = config.getString("command.messages.to-off", "");
        commandMessagesNonPlayer = config.getString("command.messages.non-player", "");
        commandMessagesNeedHelp = config.getString("command.messages.need-help", "");
        commandMessagesDoReload = config.getString("command.messages.do-reload", "");
        commandMessagesNoPermission = config.getString("command.messages.no-permission", "");
    }
}
