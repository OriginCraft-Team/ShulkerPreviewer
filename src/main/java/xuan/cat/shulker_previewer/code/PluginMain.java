package xuan.cat.shulker_previewer.code;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuan.cat.shulker_previewer.code.command.CommandSpecialCompleter;
import xuan.cat.shulker_previewer.code.command.CommandSpecialExecutor;
import xuan.cat.shulker_previewer.code.config.GlobalConfig;

import static com.comphenix.protocol.ProtocolLibrary.getProtocolManager;
import static java.util.Objects.requireNonNull;

public final class PluginMain extends JavaPlugin {
    public final @NotNull NamespacedKey statusKey = new NamespacedKey(this, "show_status");

    private @Nullable PacketListen listen = null;
    private @Nullable GlobalConfig config = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        super.onEnable();
        config = new GlobalConfig(getConfig());
        getProtocolManager().addPacketListener(listen = new PacketListen(this));
        PluginCommand command = Bukkit.getPluginCommand("shulkerview");
        if (command == null) {
            throw new RuntimeException("No registration command 'shulkerview' in plugin.yml");
        }
        command.setExecutor(new CommandSpecialExecutor(this));
        command.setTabCompleter(new CommandSpecialCompleter());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (listen != null) {
            getProtocolManager().removePacketListener(listen);
        }
        listen = null;
    }

    public @NotNull GlobalConfig getGlobalConfig() {
        return requireNonNull(config);
    }

    public void reloadGlobalConfig() {
        reloadConfig();
        config = new GlobalConfig(getConfig());
    }
}