package com.dynamiceconomy.commands;

import com.dynamiceconomy.DynamicEconomyEvents;
import com.dynamiceconomy.events.EconomyEvent;
import com.dynamiceconomy.managers.EventManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyEventCommand implements CommandExecutor, TabCompleter {

    private final DynamicEconomyEvents plugin;

    public EconomyEventCommand(DynamicEconomyEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfigManager().getPrefix();

        if (!sender.hasPermission("economyevent.admin")) {
            sender.sendMessage(prefix + plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, prefix);
            return true;
        }

        EventManager em = plugin.getEventManager();

        switch (args[0].toLowerCase()) {

            // /ee start <event>
            case "start" -> {
                if (args.length < 2) {
                    sender.sendMessage(prefix + ChatColor.RED + "Usage: /ee start <event>");
                    sender.sendMessage(prefix + ChatColor.GRAY + "Events: gold_rush, recession, market_boom, tax_event, inflation, deflation");
                    return true;
                }
                EconomyEvent event = EconomyEvent.fromString(args[1]);
                if (event == null || event == EconomyEvent.NONE) {
                    sender.sendMessage(prefix + ChatColor.RED + "Unknown event: " + args[1]);
                    return true;
                }
                em.startEvent(event);
                sender.sendMessage(prefix + ChatColor.GREEN + "Started event: " + event.getFullName());
            }

            // /ee stop
            case "stop" -> {
                if (em.getCurrentEvent() == EconomyEvent.NONE) {
                    sender.sendMessage(prefix + plugin.getConfigManager().getMessage("no-event"));
                    return true;
                }
                em.endEvent();
                sender.sendMessage(prefix + ChatColor.YELLOW + "Economy event stopped.");
            }

            // /ee status
            case "status" -> {
                EconomyEvent current = em.getCurrentEvent();
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                sender.sendMessage(ChatColor.YELLOW + "  Economy Event Status");
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                if (current == EconomyEvent.NONE) {
                    sender.sendMessage(ChatColor.GRAY + "  Active Event: " + ChatColor.WHITE + "None");
                } else {
                    sender.sendMessage(ChatColor.GRAY + "  Active Event: " + ChatColor.WHITE + current.getFullName());
                    sender.sendMessage(ChatColor.GRAY + "  Description:  " + ChatColor.WHITE + current.getDescription());
                }
                sender.sendMessage(ChatColor.GRAY + "  Vault:        " + (plugin.isVaultEnabled()
                    ? ChatColor.GREEN + "Connected (" + plugin.getVaultEconomy().getName() + ")"
                    : ChatColor.RED + "Not found (fallback active)"));
                sender.sendMessage(ChatColor.GRAY + "  History:      " + ChatColor.WHITE + em.getHistory().size() + " entries");
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            // /ee reload
            case "reload" -> {
                plugin.getConfigManager().reload();
                sender.sendMessage(prefix + plugin.getConfigManager().getMessage("reloaded"));
            }

            // /ee history
            case "history" -> {
                List<String> hist = em.getHistory();
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                sender.sendMessage(ChatColor.YELLOW + "  Last " + Math.min(10, hist.size()) + " Events");
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                if (hist.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "  No events recorded yet.");
                } else {
                    int start = Math.max(0, hist.size() - 10);
                    for (int i = start; i < hist.size(); i++) {
                        sender.sendMessage(ChatColor.GRAY + "  " + hist.get(i));
                    }
                }
                sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }

            default -> sendHelp(sender, prefix);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String prefix) {
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "  Dynamic Economy Events Help");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.AQUA + "  /ee start <event>" + ChatColor.GRAY + " - Start an event");
        sender.sendMessage(ChatColor.AQUA + "  /ee stop"          + ChatColor.GRAY + " - Stop current event");
        sender.sendMessage(ChatColor.AQUA + "  /ee status"        + ChatColor.GRAY + " - Show current status");
        sender.sendMessage(ChatColor.AQUA + "  /ee history"       + ChatColor.GRAY + " - Show event history");
        sender.sendMessage(ChatColor.AQUA + "  /ee reload"        + ChatColor.GRAY + " - Reload config");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "status", "history", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return Arrays.stream(EconomyEvent.values())
                .filter(e -> e != EconomyEvent.NONE)
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
