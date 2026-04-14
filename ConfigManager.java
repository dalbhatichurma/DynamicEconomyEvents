package com.dynamiceconomy.managers;

import com.dynamiceconomy.DynamicEconomyEvents;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final DynamicEconomyEvents plugin;
    private FileConfiguration config;

    public ConfigManager(DynamicEconomyEvents plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    // ── Event Settings ──────────────────────────────────────────────────────────
    public int getEventInterval()  { return config.getInt("events.interval-minutes", 15); }
    public int getEventDuration()  { return config.getInt("events.duration-minutes", 5); }
    public boolean isRandomEnabled() { return config.getBoolean("events.random-events-enabled", true); }

    // ── Chances ─────────────────────────────────────────────────────────────────
    public int getChance(String key) { return config.getInt("events.chances." + key, 10); }

    // ── Multipliers ─────────────────────────────────────────────────────────────
    public double getGoldRushMultiplier()  { return config.getDouble("multipliers.gold-rush", 2.0); }
    public double getRecessionMultiplier() { return config.getDouble("multipliers.recession", 0.5); }
    public double getMarketBoomMultiplier(){ return config.getDouble("multipliers.market-boom", 1.5); }
    public double getTaxRate()             { return config.getDouble("multipliers.tax-rate", 0.10); }
    public double getInflationRate()       { return config.getDouble("multipliers.inflation-rate", 1.3); }
    public double getDeflationRate()       { return config.getDouble("multipliers.deflation-rate", 0.7); }

    // ── Display ─────────────────────────────────────────────────────────────────
    public boolean isBossBarEnabled()   { return config.getBoolean("display.bossbar-enabled", true); }
    public boolean isActionBarEnabled() { return config.getBoolean("display.actionbar-enabled", true); }
    public boolean isBroadcastEnabled() { return config.getBoolean("display.broadcast-enabled", true); }

    // ── Messages ─────────────────────────────────────────────────────────────────
    public String getMessage(String key) {
        String raw = config.getString("messages." + key, "&cMessage missing: " + key);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }
}
