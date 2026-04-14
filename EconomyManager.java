package com.dynamiceconomy.managers;

import com.dynamiceconomy.DynamicEconomyEvents;
import com.dynamiceconomy.events.EconomyEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final DynamicEconomyEvents plugin;

    // Fallback balances when Vault is not present
    private final Map<UUID, Double> balances = new HashMap<>();
    private File economyFile;
    private FileConfiguration economyData;

    public EconomyManager(DynamicEconomyEvents plugin) {
        this.plugin = plugin;
        loadState();
    }

    // ── Balance API ─────────────────────────────────────────────────────────────

    public double getBalance(OfflinePlayer player) {
        if (plugin.isVaultEnabled()) {
            return plugin.getVaultEconomy().getBalance(player);
        }
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (amount <= 0) return;
        if (plugin.isVaultEnabled()) {
            plugin.getVaultEconomy().depositPlayer(player, amount);
        } else {
            balances.merge(player.getUniqueId(), amount, Double::sum);
        }
    }

    public void withdraw(OfflinePlayer player, double amount) {
        if (amount <= 0) return;
        if (plugin.isVaultEnabled()) {
            plugin.getVaultEconomy().withdrawPlayer(player, amount);
        } else {
            double current = balances.getOrDefault(player.getUniqueId(), 0.0);
            balances.put(player.getUniqueId(), Math.max(0, current - amount));
        }
    }

    // ── Event Multiplier ────────────────────────────────────────────────────────

    /**
     * Apply the current event multiplier to a base reward amount.
     */
    public double applyMultiplier(double base, EconomyEvent event) {
        double multiplier = switch (event) {
            case GOLD_RUSH   -> plugin.getConfigManager().getGoldRushMultiplier();
            case RECESSION   -> plugin.getConfigManager().getRecessionMultiplier();
            case MARKET_BOOM -> plugin.getConfigManager().getMarketBoomMultiplier();
            case INFLATION   -> plugin.getConfigManager().getInflationRate();
            case DEFLATION   -> plugin.getConfigManager().getDeflationRate();
            default          -> 1.0;
        };
        return base * multiplier;
    }

    /**
     * Deduct tax from an amount and return the net value.
     */
    public double applyTax(double amount) {
        return amount * (1.0 - plugin.getConfigManager().getTaxRate());
    }

    /**
     * Format a money value using Vault or a default pattern.
     */
    public String format(double amount) {
        if (plugin.isVaultEnabled()) {
            return plugin.getVaultEconomy().format(amount);
        }
        return String.format("$%.2f", amount);
    }

    // ── Persistence ─────────────────────────────────────────────────────────────

    public void loadState() {
        economyFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!economyFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { economyFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Cannot create economy.yml: " + e.getMessage());
            }
        }
        economyData = YamlConfiguration.loadConfiguration(economyFile);

        if (economyData.isConfigurationSection("balances")) {
            for (String key : economyData.getConfigurationSection("balances").getKeys(false)) {
                balances.put(UUID.fromString(key), economyData.getDouble("balances." + key));
            }
        }
    }

    public void saveEconomyState() {
        balances.forEach((uuid, bal) -> economyData.set("balances." + uuid, bal));
        try { economyData.save(economyFile); } catch (IOException e) {
            plugin.getLogger().severe("Cannot save economy.yml: " + e.getMessage());
        }
    }
}
