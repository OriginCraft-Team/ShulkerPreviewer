package xuan.cat.shulker_previewer.code.config;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNullElse;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public final class ItemFormat {
    private final @NotNull String value;

    public ItemFormat(@NotNull String value) {
        this.value = value;
    }

    public @NotNull Component apply(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return apply(Map.of(
                "material", translatable(item.getType().translationKey()),  // TODO 1.20.6 ~ meta.hasItemName() ? meta.itemName() : translatable(content.getType().translationKey());
                "name", requireNonNullElse(meta.hasDisplayName() ? meta.displayName() : null, text("")),
                "amount", text(item.getAmount())
        ));
    }
    private @NotNull Component apply(@NotNull Map<String, Component> args) {
        Component components = text("");
        StringBuilder buffer = new StringBuilder();
        StringBuilder placeholder = null;
        for (int code : value.codePoints().toArray()) {
            if (code == '%') {
                if (placeholder == null) {
                    placeholder = new StringBuilder();
                } else if (placeholder.isEmpty()) {
                    buffer.append('%'); // %% == %
                    placeholder = null;
                } else {
                    Component arg = args.get(valueOf(placeholder).toLowerCase());
                    if (arg != null) {
                        components = components.append(text(valueOf(buffer))).append(arg);
                        buffer = new StringBuilder();
                    } else {
                        buffer.append('%').append(placeholder).append('%');
                    }
                    placeholder = null;
                }
            } else {
                requireNonNullElse(placeholder, buffer).appendCodePoint(code);
            }
        }
        if (!buffer.isEmpty()) {
            components = components.append(text(valueOf(buffer)));
        }
        return components;
    }
}
