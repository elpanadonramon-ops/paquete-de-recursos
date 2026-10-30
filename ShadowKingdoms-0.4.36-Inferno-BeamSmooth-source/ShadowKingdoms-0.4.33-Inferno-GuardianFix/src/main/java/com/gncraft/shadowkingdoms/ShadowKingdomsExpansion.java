package com.gncraft.shadowkingdoms;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ShadowKingdomsExpansion extends PlaceholderExpansion {
    private final ShadowKingdoms plugin;

    public ShadowKingdomsExpansion(ShadowKingdoms plugin) { this.plugin = plugin; }
    @Override public @NotNull String getIdentifier() { return "shadowkingdoms"; }
    @Override public @NotNull String getAuthor() { return "GNcraft"; }
    @Override public @NotNull String getVersion() { return "0.4.8"; }
    @Override public boolean persist() { return true; }
    @Override public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        return plugin.placeholder(player, params);
    }
}
