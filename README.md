# 📈 DynamicEconomyEvents — Minecraft Plugin

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen)
![Version](https://img.shields.io/badge/Version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Vault](https://img.shields.io/badge/Vault-Supported-gold)

> A live economy system with random global events that affect in-game money, trading, and rewards.

---

## ⚙️ Features

- 🎲 **Random economy events** fire every X minutes (configurable)
- 💰 **6 unique events** with different multipliers and effects
- 🔌 **Vault integration** with automatic fallback economy
- 📊 **BossBar + ActionBar** showing the active event
- 📢 **Broadcast messages** on event start/end
- 💾 **Persistent data** — economy state and event history saved to YAML
- 🔒 **Permission system** for notifications and admin commands
- ⚡ **Tab-complete** on all commands

---

## 📅 Economy Events

| Event | Icon | Effect |
|-------|------|--------|
| Gold Rush | 💰 | 2× money from all activities |
| Recession | 📉 | 0.5× rewards |
| Market Boom | 🛒 | 1.5× sell profit |
| Tax Event | ⚡ | 10% tax deducted from earnings |
| Inflation | 📈 | 1.3× prices |
| Deflation | 📊 | 0.7× prices |

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/ee start <event>` | Start a specific event |
| `/ee stop` | Stop the current event |
| `/ee status` | Show current event + Vault status |
| `/ee history` | Show last 10 events |
| `/ee reload` | Reload config.yml |

---

## 🔒 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `economyevent.admin` | OP | Access all commands |
| `economyevent.notify` | Everyone | Receive event notifications |
| `economyevent.bypass.tax` | Nobody | Skip tax during Tax Event |

---

## 📦 Installation

1. Drop `DynamicEconomyEvents.jar` into your `plugins/` folder
2. *(Optional)* Install [Vault](https://www.spigotmc.org/resources/vault.34315/) + an economy plugin
3. Restart your server
4. Edit `plugins/DynamicEconomyEvents/config.yml` to your liking
5. Use `/ee reload` to apply changes live

---

## 🛠️ Building from Source

```bash
git clone <repo-url>
cd DynamicEconomyEvents
mvn clean package
```

Output: `target/DynamicEconomyEvents.jar`

---

## 📁 Data Files

| File | Purpose |
|------|---------|
| `config.yml` | All settings, multipliers, chances |
| `economy.yml` | Fallback player balances |
| `event-history.yml` | Last 100 event log entries |

---

## ⚙️ config.yml Overview

```yaml
events:
  interval-minutes: 15   # How often random events fire
  duration-minutes: 5    # How long each event lasts
  random-events-enabled: true

  chances:               # Relative weight for each event
    gold-rush: 25
    recession: 20
    market-boom: 25
    tax-event: 15
    inflation: 10
    deflation: 5

multipliers:
  gold-rush: 2.0
  recession: 0.5
  market-boom: 1.5
  tax-rate: 0.10
  inflation-rate: 1.3
  deflation-rate: 0.7

display:
  bossbar-enabled: true
  actionbar-enabled: true
  broadcast-enabled: true
```

---

## 📝 Changelog

### v1.0.0
- Initial release
- 6 economy events
- Vault + fallback economy
- BossBar, ActionBar, broadcast support
- Event history persistence
- Full command suite with tab-complete

---

**Made with ❤️ for the Minecraft community**
