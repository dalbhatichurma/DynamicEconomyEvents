package com.dynamiceconomy.managers;

import com.dynamiceconomy.DynamicEconomyEvents;
import com.dynamiceconomy.events.EconomyEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EventManager {

    private final DynamicEconomyEvents plugin;

    private EconomyEvent currentEvent = EconomyEvent.NONE;
    private BukkitTask schedulerTask;
    private BukkitTask endTask;
    private BukkitTask actionBarTask;
    private BossBar bossBar;

    private File historyFile;
    private FileConfiguration historyData;
    private final List<String> history = new ArrayList<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventManager(DynamicEconomyEvents plugin) {
        this.plugin = plugin;
        loadHistory();
        initBossBar();
    }

    // ── Scheduler ───────────────────────────────────────────────────────────────

    public void startScheduler() {
        if (!plugin.getConfigManager().isRandomEnabled()) return;

        long intervalTicks = plugin.getConfigManager().getEventInterval() * 60L * 20L;
        schedulerTask = new BukkitRunnable() {
            @Override public void run() {
                if (currentEvent == EconomyEvent.NONE) startRandom();
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void stopScheduler() {
        cancel(schedulerTask);
        cancel(endTask);
        cancel(actionBarTask);
        if (bossBar != null) bossBar.removeAll();
    }

    // ── Event Lifecycle ─────────────────────────────────────────────────────────

    public void startRandom() {
        startEvent(pickRandom());
    }

    public void startEvent(EconomyEvent event) {
        if (event == EconomyEvent.NONE) return;

        // Stop any running event first
        if (currentEvent != EconomyEvent.NONE) endEvent();

        currentEvent = event;
        int durationMin = plugin.getConfigManager().getEventDuration();

        broadcast(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "  ⚡ ECONOMY EVENT STARTED ⚡");
        broadcast(ChatColor.WHITE + "  " + event.getFullName());
        broadcast(ChatColor.GRAY + "  " + event.getDescription());
        broadcast(ChatColor.AQUA + "  Duration: " + ChatColor.WHITE + durationMin + " minutes");
        broadcast(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        showBossBar(event);
        startActionBar(event);
        log(event, "STARTED");

        long durationTicks = durationMin * 60L * 20L;
        endTask = new BukkitRunnable() {
            @Override public void run() { endEvent(); }
        }.runTaskLater(plugin, durationTicks);
    }

    public void endEvent() {
        if (currentEvent == EconomyEvent.NONE) return;

        EconomyEvent ended = currentEvent;
        currentEvent = EconomyEvent.NONE;

        broadcast(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        broadcast(ChatColor.YELLOW + "" + ChatColor.BOLD + "  ⏰ ECONOMY EVENT ENDED");
        broadcast(ChatColor.WHITE + "  " + ended.getFullName() + ChatColor.GRAY + " has concluded.");
        broadcast(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (bossBar != null) bossBar.removeAll();
        cancel(actionBarTask);
        cancel(endTask);
        log(ended, "ENDED");
    }

    // ── BossBar ─────────────────────────────────────────────────────────────────

    private void initBossBar() {
        bossBar = Bukkit.createBossBar(
            ChatColor.GOLD + "No Active Economy Event",
            org.bukkit.boss.BarColor.WHITE,
            BarStyle.SOLID
        );
    }

    private void showBossBar(EconomyEvent event) {
        if (!plugin.getConfigManager().isBossBarEnabled()) return;
        bossBar.setTitle(ChatColor.GOLD + "⚡ " + ChatColor.YELLOW + event.getFullName()
                + ChatColor.GRAY + " — " + event.getDescription());
        bossBar.setColor(event.getBarColor());
        bossBar.setProgress(1.0);
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("economyevent.notify"))
            .forEach(bossBar::addPlayer);
    }

    // ── ActionBar ───────────────────────────────────────────────────────────────

    private void startActionBar(EconomyEvent event) {
        if (!plugin.getConfigManager().isActionBarEnabled()) return;
        actionBarTask = new BukkitRunnable() {
            @Override public void run() {
                if (currentEvent == EconomyEvent.NONE) { cancel(); return; }
                String msg = ChatColor.GOLD + "⚡ " + ChatColor.YELLOW + event.getDisplayName()
                           + ChatColor.GRAY + " | " + ChatColor.WHITE + event.getDescription();
                Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("economyevent.notify"))
                    .forEach(p -> p.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR, new TextComponent(msg)));
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }

    // ── Random Picker ────────────────────────────────────────────────────────────

    private EconomyEvent pickRandom() {
        Map<EconomyEvent, Integer> pool = new LinkedHashMap<>();
        pool.put(EconomyEvent.GOLD_RUSH,   plugin.getConfigManager().getChance("gold-rush"));
        pool.put(EconomyEvent.RECESSION,   plugin.getConfigManager().getChance("recession"));
        pool.put(EconomyEvent.MARKET_BOOM, plugin.getConfigManager().getChance("market-boom"));
        pool.put(EconomyEvent.TAX_EVENT,   plugin.getConfigManager().getChance("tax-event"));
        pool.put(EconomyEvent.INFLATION,   plugin.getConfigManager().getChance("inflation"));
        pool.put(EconomyEvent.DEFLATION,   plugin.getConfigManager().getChance("deflation"));

        int total = pool.values().stream().mapToInt(i -> i).sum();
        int roll  = new Random().nextInt(total);
        int acc   = 0;
        for (Map.Entry<EconomyEvent, Integer> e : pool.entrySet()) {
            acc += e.getValue();
            if (roll < acc) return e.getKey();
        }
        return EconomyEvent.GOLD_RUSH;
    }

    // ── History ──────────────────────────────────────────────────────────────────

    private void log(EconomyEvent event, String action) {
        String entry = LocalDateTime.now().format(FMT) + " | " + event.getDisplayName() + " | " + action;
        history.add(entry);
        if (history.size() > 100) history.remove(0);
    }

    public void loadHistory() {
        historyFile = new File(plugin.getDataFolder(), "event-history.yml");
        plugin.getDataFolder().mkdirs();
        if (!historyFile.exists()) {
            try { historyFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Cannot create event-history.yml");
            }
        }
        historyData = YamlConfiguration.loadConfiguration(historyFile);
        if (historyData.isList("history")) history.addAll(historyData.getStringList("history"));
    }

    public void saveHistory() {
        historyData.set("history", history);
        try { historyData.save(historyFile); } catch (IOException e) {
            plugin.getLogger().severe("Cannot save event-history.yml");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void broadcast(String message) {
        if (!plugin.getConfigManager().isBroadcastEnabled()) return;
        Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("economyevent.notify"))
            .forEach(p -> p.sendMessage(message));
    }

    private void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) task.cancel();
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public EconomyEvent getCurrentEvent() { return currentEvent; }
    public List<String> getHistory()      { return Collections.unmodifiableList(history); }
    public BossBar getBossBar()           { return bossBar; }
}
