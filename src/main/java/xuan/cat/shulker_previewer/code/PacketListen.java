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
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuan.cat.shulker_previewer.code.config.GlobalConfig;
import xuan.cat.shulker_previewer.code.config.ItemFormat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.comphenix.protocol.PacketType.Play.Client.SET_CREATIVE_SLOT;
import static com.comphenix.protocol.PacketType.Play.Server.SET_SLOT;
import static com.comphenix.protocol.PacketType.Play.Server.WINDOW_ITEMS;
import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class PacketListen extends PacketAdapter {
    private static final @NotNull TranslatableComponent LINE_START = translatable("", text("ShulkerPreviewerLore"))
            .color(WHITE)
            .decoration(ITALIC, false)
            .children(List.of());

    public PacketListen(@NotNull PluginMain plugin) {
        super(plugin, ListenerPriority.MONITOR, SET_SLOT, WINDOW_ITEMS, SET_CREATIVE_SLOT);
    }

    @Override
    public @NotNull PluginMain getPlugin() {
        return (PluginMain) super.getPlugin();
    }

    private @NotNull GlobalConfig getConfig() {
        return getPlugin().getGlobalConfig();
    }

    @Override
    public void onPacketSending(@NotNull PacketEvent event) {
        if (!requireNonNullElseGet(event.getPlayer().getPersistentDataContainer().get(getPlugin().statusKey, PersistentDataType.BOOLEAN), () -> getConfig().commandDefault)) {
            return;
        }
        PacketType type = event.getPacketType();
        if (type.equals(SET_SLOT)) {
            StructureModifier<ItemStack> modifier = event.getPacket().getItemModifier();
            modifier.write(0, replaceItems(modifier.read(0)));
        } else if (type.equals(WINDOW_ITEMS)) {
            StructureModifier<List<ItemStack>> modifier = event.getPacket().getItemListModifier();
            modifier.write(0, modifier.read(0).stream().map(this::replaceItems).toList());
        } else {
            throw new RuntimeException("Strange handling");
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketType type = event.getPacketType();
        if (type.equals(SET_CREATIVE_SLOT)) {
            StructureModifier<ItemStack> modifier = event.getPacket().getItemModifier();
            modifier.write(0, replaceItems(modifier.read(0), false));
        } else {
            throw new RuntimeException("Strange handling");
        }
    }

    private @Nullable ItemStack replaceItems(@Nullable ItemStack item) {
        return replaceItems(item, true);
    }
    private @Nullable ItemStack replaceItems(@Nullable ItemStack item, boolean write) {
        if (item == null || item.isEmpty() || !(item.getItemMeta() instanceof BlockStateMeta block) || !block.hasBlockState() || !(block.getBlockState() instanceof ShulkerBox box)) {
            return item;
        }
        AtomicInteger count = new AtomicInteger();
        Map<Integer, Component> lore = new LinkedHashMap<>();
        Stream.of(write ? box.getInventory().getStorageContents() : new ItemStack[0])
                .filter(Objects::nonNull)
                .filter(content -> !content.isEmpty())
                .map(content -> {
                    ItemMeta meta = content.getItemMeta();
                    ItemFormat format;
                    if (content.getMaxStackSize() > 1) {
                        format = meta.hasDisplayName() ? getConfig().formatMultiItemRenamed : getConfig().formatMultiItem;
                    } else {
                        format = meta.hasDisplayName() ? getConfig().formatSingleItemRenamed : getConfig().formatSingleItem;
                    }
                    return format.apply(content);
                })
                .forEach(component -> {
                    int index = count.getAndIncrement();
                    int group = index / getConfig().rowItems;
                    Component line = lore.get(group);
                    if (line == null) {
                        line = LINE_START.append(text(getConfig().formatHead));
                    }
                    if (index % getConfig().rowItems > 0) {
                        line = line.append(text(getConfig().formatSeparator));
                    }
                    lore.put(group, line.append(component));
                });
        List<Component> merger = requireNonNullElseGet(block.lore(), List::of)
                .stream()
                .map(component -> (Component) component)
                .filter(component -> !component.children(List.of()).equals(LINE_START))
                .collect(Collectors.toList());
        if (!merger.isEmpty() && !lore.isEmpty()) {
            merger.addFirst(LINE_START);
        }
        lore.values()
                .stream()
                .toList()
                .reversed()
                .forEach(merger::addFirst);
        block.lore(merger);
        block.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS); // TODO 1.20.6 ~ block.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(block);
        return item;
    }
}