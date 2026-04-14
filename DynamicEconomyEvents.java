package com.dynamiceconomy;

import com.dynamiceconomy.commands.EconomyEventCommand;
import com.dynamiceconomy.listeners.EconomyListener;
import com.dynamiceconomy.managers.ConfigManager;
import com.dynamiceconomy.managers.EconomyManager;
import com.dynamiceconomy.managers.EventManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicEconomyEvents extends JavaPlugin {

    private static DynamicEconomyEvents instance;

    private ConfigManager  configManager;
    private EconomyManager economyManager;
    private EventManager   eventManager;

    private Economy vaultEconomy;
    private boolean vaultEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config
        configManager = new ConfigManager(this);
        configManager.load();

        // 2. Vault (optional)
        setupVault();

        // 3. Managers
        economyManager = new EconomyManager(this);
        eventManager   = new EventManager(this);

        // 4. Commands & Listeners
        getCommand("economyevent").setExecutor(new EconomyEventCommand(this));
        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);

        // 5. Start scheduler
        eventManager.startScheduler();

        getLogger().info("DynamicEconomyEvents enabled! Vault: " + (vaultEnabled ? "YES" : "NO (fallback)"));
    }

    @Override
    public void onDisable() {
        if (eventManager  != null) { eventManager.stopScheduler(); eventManager.saveHistory(); }
        if (economyManager != null) economyManager.saveEconomyState();
        getLogger().info("DynamicEconomyEvents disabled.");
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp =
            getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;
        vaultEconomy = rsp.getProvider();
        vaultEnabled = true;
        getLogger().info("Hooked into Vault: " + vaultEconomy.getName());
    }

    // ── Static accessor ──────────────────────────────────────────────────────────
    public static DynamicEconomyEvents getInstance() { return instance; }

    // ── Getters ──────────────────────────────────────────────────────────────────
    public ConfigManager  getConfigManager()  { return configManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public EventManager   getEventManager()   { return eventManager; }
    public Economy        getVaultEconomy()   { return vaultEconomy; }
    public boolean        isVaultEnabled()    { return vaultEnabled; }
}
