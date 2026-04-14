package com.dynamiceconomy.listeners;

import com.dynamiceconomy.DynamicEconomyEvents;
import com.dynamiceconomy.events.EconomyEvent;
import com.dynamiceconomy.managers.EconomyManager;
import com.dynamiceconomy.managers.EventManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {

    private final DynamicEconomyEvents plugin;

    public EconomyListener(DynamicEconomyEvents plugin) {
        this.plugin = plugin;
    }

    /**
     * When a player kills a mob, apply the current event multiplier to their reward.
     * This demonstrates how the economy system hooks into gameplay.
     */
    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;

        EventManager  em  = plugin.getEventManager();
        EconomyManager eco = plugin.getEconomyManager();
        EconomyEvent current = em.getCurrentEvent();

        if (current == EconomyEvent.NONE) return;

        // Base reward for killing a mob
        double baseReward = 5.0;
        double finalReward;

        if (current == EconomyEvent.TAX_EVENT) {
            // Tax event: apply tax deduction
            if (player.hasPermission("economyevent.bypass.tax")) {
                finalReward = baseReward;
                player.sendMessage(ChatColor.GREEN + "Tax bypassed! +" + eco.format(finalReward));
            } else {
                finalReward = eco.applyTax(baseReward);
                double taxed = baseReward - finalReward;
                player.sendMessage(ChatColor.YELLOW + "⚡ Tax deducted: " + eco.format(taxed)
                    + ChatColor.GREEN + " | Net: +" + eco.format(finalReward));
            }
        } else {
            // All other events: apply multiplier
            finalReward = eco.applyMultiplier(baseReward, current);
            String multiplierInfo = getMultiplierLabel(current);
            player.sendMessage(ChatColor.GOLD + current.getIcon() + " " + multiplierInfo
                + ChatColor.GREEN + " +" + eco.format(finalReward));
        }

        eco.deposit(player, finalReward);
    }

    /**
     * When a player joins, show them the current active event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("economyevent.notify")) return;

        EconomyEvent current = plugin.getEventManager().getCurrentEvent();
        if (current == EconomyEvent.NONE) return;

        // Delay message slightly so it appears after join messages
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.YELLOW + "⚡ Active Economy Event: " + ChatColor.WHITE + current.getFullName());
            player.sendMessage(ChatColor.GRAY + current.getDescription());
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Add to BossBar
            if (plugin.getConfigManager().isBossBarEnabled()) {
                plugin.getEventManager().getBossBar().addPlayer(player);
            }
        }, 20L);
    }

    private String getMultiplierLabel(EconomyEvent event) {
        return switch (event) {
            case GOLD_RUSH   -> ChatColor.YELLOW + "Gold Rush x" + plugin.getConfigManager().getGoldRushMultiplier();
            case RECESSION   -> ChatColor.RED    + "Recession x" + plugin.getConfigManager().getRecessionMultiplier();
            case MARKET_BOOM -> ChatColor.GREEN  + "Market Boom x" + plugin.getConfigManager().getMarketBoomMultiplier();
            case INFLATION   -> ChatColor.LIGHT_PURPLE + "Inflation x" + plugin.getConfigManager().getInflationRate();
            case DEFLATION   -> ChatColor.AQUA   + "Deflation x" + plugin.getConfigManager().getDeflationRate();
            default          -> "";
        };
    }
}
