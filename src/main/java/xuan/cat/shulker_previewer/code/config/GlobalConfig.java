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
        formatSingleItem = new ItemFormat(applyColorCodes(config.getString("format.single.item", "")));
        formatSingleItemRenamed = new ItemFormat(applyColorCodes(config.getString("format.single.item-renamed", "")));
        formatMultiItem = new ItemFormat(applyColorCodes(config.getString("format.multi.item", "")));
        formatMultiItemRenamed = new ItemFormat(applyColorCodes(config.getString("format.multi.item-renamed", "")));
        formatSeparator = applyColorCodes(config.getString("format.separator", ""));
        formatHead = applyColorCodes(config.getString("format.head", ""));
        rowItems = max(1, config.getInt("row-items", 1));
        commandDefault = config.getBoolean("command.default", true);
        commandMessagesToOn = applyColorCodes(config.getString("command.messages.to-on", ""));
        commandMessagesToOff = applyColorCodes(config.getString("command.messages.to-off", ""));
        commandMessagesNonPlayer = applyColorCodes(config.getString("command.messages.non-player", ""));
        commandMessagesNeedHelp = applyColorCodes(config.getString("command.messages.need-help", ""));
        commandMessagesDoReload = applyColorCodes(config.getString("command.messages.do-reload", ""));
        commandMessagesNoPermission = applyColorCodes(config.getString("command.messages.no-permission", ""));
    }

    private static @NotNull String applyColorCodes(@NotNull String text) {
        return text.replace('&', '§');
    }
}