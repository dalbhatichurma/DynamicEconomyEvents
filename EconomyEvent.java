package com.dynamiceconomy.events;

import org.bukkit.boss.BarColor;

public enum EconomyEvent {

    GOLD_RUSH("Gold Rush", "💰", "Players earn 2x money from all activities!", BarColor.YELLOW),
    RECESSION("Recession", "📉", "Shop prices drop but rewards decrease!", BarColor.RED),
    MARKET_BOOM("Market Boom", "🛒", "Selling items gives extra profit!", BarColor.GREEN),
    TAX_EVENT("Tax Event", "⚡", "All players pay a small tax on earnings!", BarColor.PURPLE),
    INFLATION("Inflation", "📈", "Prices are rising across the economy!", BarColor.PINK),
    DEFLATION("Deflation", "📊", "Prices are falling across the economy!", BarColor.BLUE),
    NONE("None", "", "No active event", BarColor.WHITE);

    private final String displayName;
    private final String icon;
    private final String description;
    private final BarColor barColor;

    EconomyEvent(String displayName, String icon, String description, BarColor barColor) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.barColor = barColor;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    public BarColor getBarColor() { return barColor; }
    public String getFullName() { return icon.isEmpty() ? displayName : icon + " " + displayName; }

    /** Parse event from string input (case-insensitive) */
    public static EconomyEvent fromString(String input) {
        for (EconomyEvent event : values()) {
            if (event.name().equalsIgnoreCase(input) || event.displayName.equalsIgnoreCase(input)) {
                return event;
            }
        }
        return null;
    }
}
