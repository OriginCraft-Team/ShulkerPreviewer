package xuan.cat.shulker_previewer.code;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comphenix.protocol.PacketType.Play.Server.SET_SLOT;
import static com.comphenix.protocol.PacketType.Play.Server.WINDOW_ITEMS;
import static com.comphenix.protocol.ProtocolLibrary.getProtocolManager;
import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class PluginMain extends JavaPlugin {
    private static final @NotNull TranslatableComponent LINE_START = translatable("", text("ShulkerPreviewerLore")).color(WHITE).decoration(ITALIC, false);
    private static final int LINE_ITEMS = 4;

    private @Nullable PacketListen listen;

    @Override
    public void onEnable() {
        super.onEnable();
        getProtocolManager().addPacketListener(listen = new PacketListen(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (listen != null) {
            getProtocolManager().removePacketListener(listen);
        }
        listen = null;
    }

    private static final class PacketListen extends PacketAdapter {
        public PacketListen(@NotNull Plugin plugin) {
            super(plugin, ListenerPriority.MONITOR, SET_SLOT, WINDOW_ITEMS);
        }

        @Override
        public void onPacketSending(@NotNull PacketEvent event) {
            PacketType type = event.getPacketType();
            if (type.equals(SET_SLOT)) {
                StructureModifier<ItemStack> modifier = event.getPacket().getItemModifier();
                modifier.write(0, replaceItems(modifier.read(0)));
            } else if (type.equals(WINDOW_ITEMS)) {
                StructureModifier<List<ItemStack>> modifier = event.getPacket().getItemListModifier();
                modifier.write(0, modifier.read(0).stream().map(PacketListen::replaceItems).toList());
            } else {
                throw new RuntimeException("Strange handling");
            }
        }

        private static @Nullable ItemStack replaceItems(@Nullable ItemStack item) {
            if (item == null || item.isEmpty() || !(item.getItemMeta() instanceof BlockStateMeta block) || !block.hasBlockState() || !(block.getBlockState() instanceof ShulkerBox box)) {
                return item;
            }
            AtomicInteger count = new AtomicInteger();
            Map<Integer, Component> lore = new LinkedHashMap<>();
            Stream.of(box.getInventory().getStorageContents())
                    .filter(Objects::nonNull)
                    .filter(content -> !content.isEmpty())
                    .map(content -> {
                        ItemMeta meta = content.getItemMeta();
                        Component material = translatable(content.getType().translationKey()); // TODO 1.20.6 ~ meta.hasItemName() ? meta.itemName() : translatable(content.getType().translationKey());
                        Component component;
                        if (meta.hasDisplayName()) {
                            component = requireNonNullElseGet(meta.displayName(), () -> text("")).append(text(" (").append(material).append(text(")")));
                        } else {
                            component = material;
                        }
                        if (content.getType().getMaxStackSize() > 1) { // TODO 1.20.6 ~ if ((meta.hasMaxStackSize() ? meta.getMaxStackSize() : content.getType().getMaxStackSize()) > 1) {
                            component = component.append(text(" x" + content.getAmount()));
                        }
                        return component;
                    })
                    .forEach(component -> {
                        int index = count.getAndIncrement();
                        int group = index / LINE_ITEMS;
                        Component line = lore.getOrDefault(group, LINE_START);
                        if (index % LINE_ITEMS > 0) {
                            line = line.append(text(" , "));
                        }
                        lore.put(group, line.append(component));
                    });
            List<Component> merger = requireNonNullElseGet(block.lore(), List::of)
                    .stream()
                    .map(component -> (Component) component)
                    .filter(component -> !component.equals(LINE_START)).collect(Collectors.toList());
            if (!merger.isEmpty()) {
                merger.add(LINE_START);
            }
            merger.addAll(lore.values());
            block.lore(merger);
            block.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS); // TODO 1.20.6 ~ block.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(block);
            return item;
        }
    }
}