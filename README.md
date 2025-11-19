# QuickReport

**A quick and efficient reporting system for Minecraft servers**

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20+-brightgreen.svg)](https://www.spigotmc.org/)
[![Java Version](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Description

QuickReport is a powerful and user-friendly reporting system designed for Minecraft servers. It allows players to report cheaters, rule-breakers, or any misconduct with ease, while providing administrators with comprehensive tools to manage and process reports efficiently.

### ✨ Key Features

- **Easy Report Submission** - Players can report others with a simple command
- **Admin Review System** - Accept or reject reports with customizable rewards
- **Report History** - Players can view their submitted reports
- **Advanced Query System** - Detailed report information for staff members
- **Cooldown Protection** - Prevents spam with configurable cooldown timers
- **Clickable Messages** - Interactive admin notifications with click-to-view functionality
- **SQLite Database** - Lightweight and efficient data storage
- **Reward System** - Automatically reward players for accepted reports
- **Multi-Language Support** - Built-in English and Turkish language support
- **PlaceholderAPI Integration** - Statistics and leaderboard placeholders

---

## 🔧 Requirements

- **Minecraft Server**: Paper/Spigot 1.20+
- **Java**: 17 or higher
- **Dependencies** (Optional):
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - For placeholder support

---

## 📥 Installation

1. Download the latest `QuickReport-1.0.0.jar` from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use a plugin manager to load it
4. Configure the plugin by editing `config.yml` and `messages.yml` in the `plugins/QuickReport/` folder
5. Reload the plugin with `/reload confirm` or restart the server

---

## 🎮 Commands

### Player Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/report` | Submit a report against a player | `quickreport.report` | `/report <player> <reason> [details]` |
| `/myreports` | View your submitted reports | `quickreport.report` | `/myreports [page]` |
| `/queryreport` | Query a specific report by ID | `quickreport.report` | `/queryreport <id>` |

### Admin Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/reports` | View all pending reports | `quickreport.admin` | `/reports [page]` |
| `/reportaction` | Accept or reject a report | `quickreport.admin` | `/reportaction <accept\|reject> <id> [reward-code\|reason]` |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `quickreport.report` | Allows players to submit and view reports | `true` (all players) |
| `quickreport.admin` | Allows staff to manage and process reports | `op` (operators only) |

---

## ⚙️ Configuration

### config.yml

```yaml
# QuickReport Configuration File

# General Settings
report-cooldown-seconds: 60 # Cooldown for submitting new reports
max-reports-per-cooldown: 1 # Maximum reports within cooldown period

# Report Reasons (Tab-completion support)
report-reasons:
  - "Fly"
  - "KillAura"
  - "Speed"
  - "Xray"
  - "Griefing"
  - "Swearing"

# Admin Notification Settings
admin-permission: "quickreport.admin"
notification-sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
notification-volume: 1.0
notification-pitch: 1.0

# Reward System Configuration
rewards:
  diamond-reward:
    name: "&bDiamond Reward"
    command: "give %player% diamond 5"
  money-reward:
    name: "&eMoney Reward"
    command: "eco give %player% 1000"
  key-reward:
    name: "&6Crate Key Reward"
    command: "crate give %player% common 1"
```

### Customizing Report Reasons

Edit the `report-reasons` list in `config.yml` to add or remove valid reasons:

```yaml
report-reasons:
  - "Hacking"
  - "Exploiting"
  - "Toxicity"
  - "Scamming"
```

### Admin Notification Sound

Change the sound played to admins when a new report is submitted. See [Spigot Sound List](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) for available sounds.

---

## 🎁 Reward System

The reward system allows you to automatically reward players when their reports are accepted.

### Adding a New Reward

1. Open `config.yml`
2. Add a new reward under the `rewards` section:

```yaml
rewards:
  custom-reward:
    name: "&aCustom Reward"
    command: "give %player% diamond_block 1"
```

3. Use the reward code when accepting a report:

```
/reportaction accept 123 custom-reward
```

The `%player%` placeholder will be replaced with the reporter's username.

---

## 📊 PlaceholderAPI Support

QuickReport integrates with PlaceholderAPI to provide statistics and leaderboard placeholders.

### Available Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%quickreport_kabul_edilen%` | Number of accepted reports for a player | `5` |
| `%quickreport_reddedilen%` | Number of rejected reports for a player | `2` |
| `%quickreport_top_kabul_isim_<rank>%` | Top reporter name (accepted) | `Player1` |
| `%quickreport_top_kabul_sayi_<rank>%` | Top reporter count (accepted) | `10` |
| `%quickreport_top_red_isim_<rank>%` | Top reporter name (rejected) | `Player2` |
| `%quickreport_top_red_sayi_<rank>%` | Top reporter count (rejected) | `3` |

### Usage Example

```yaml
# In a scoreboard or hologram
&6Top Reporter: &e%quickreport_top_kabul_isim_1%
&7Reports: &a%quickreport_top_kabul_sayi_1%
```

---

## 🌐 Multi-Language Support

QuickReport supports multiple languages out of the box. The primary language is English (`en`), with Turkish (`tr`) as a secondary language.

### Customizing Messages

Edit `messages.yml` to customize messages:

```yaml
messages:
  report-success:
    en: "&aYour report has been submitted successfully!"
    tr: "&aRaporunuz başarıyla gönderildi!"
```

### Adding a New Language

1. Open `messages.yml`
2. Add your language code to any message:

```yaml
messages:
  report-success:
    en: "&aYour report has been submitted!"
    tr: "&aRaporunuz gönderildi!"
    de: "&aDein Bericht wurde eingereicht!"
```

---

## 📖 Usage Examples

### Player Workflow

1. **Submit a report:**
   ```
   /report Cheater123 Fly He was flying in spawn
   ```

2. **Check your reports:**
   ```
   /myreports
   ```

3. **Query a specific report:**
   ```
   /queryreport 5
   ```

### Admin Workflow

1. **View pending reports:**
   ```
   /reports
   ```

2. **Click on a report** or use:
   ```
   /queryreport 5
   ```

3. **Accept a report with reward:**
   ```
   /reportaction accept 5 diamond-reward
   ```

4. **Reject a report with reason:**
   ```
   /reportaction reject 5 Insufficient evidence
   ```

---

## 🗄️ Database

QuickReport uses SQLite for data storage. The database file is located at:

```
plugins/QuickReport/reports.db
```

### Database Schema

```sql
CREATE TABLE reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_uuid TEXT NOT NULL,
    reporter_name TEXT NOT NULL,
    reported_uuid TEXT NOT NULL,
    reported_name TEXT NOT NULL,
    reason TEXT NOT NULL,
    details TEXT,
    timestamp INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    admin_uuid TEXT,
    admin_name TEXT,
    rejection_reason TEXT
);
```

---

## 🔄 Report Status

Reports can have three statuses:

- **PENDING** - Awaiting admin review
- **ACCEPTED** - Approved by staff (reporter gets rewarded)
- **REJECTED** - Denied by staff

---

## 🚀 Building from Source

If you want to compile the plugin yourself:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/QuickReport.git
   cd QuickReport
   ```

2. **Build with Maven:**
   ```bash
   mvn clean package
   ```

3. **Find the JAR file:**
   ```
   target/QuickReport-1.0.0.jar
   ```

---

## 🐛 Troubleshooting

### Reports not saving

- Check console for database connection errors
- Ensure the plugin has write permissions to the `plugins/QuickReport/` folder

### Admin notifications not working

- Verify the `admin-permission` in `config.yml` matches your permissions plugin
- Check that the notification sound name is valid

### PlaceholderAPI placeholders not working

- Make sure PlaceholderAPI is installed and running
- Use `/papi parse me %quickreport_kabul_edilen%` to test placeholders

---

## 📞 Support

For issues, suggestions, or contributions:

- **Issues**: Open an issue on the GitHub repository
- **Wiki**: Check the documentation for detailed guides
- **Discord**: Join our community server (if available)

---

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🙏 Credits

- **Developer**: Melut
- **Contributors**: Community contributions welcome!

---

<details>
<summary><b>🇹🇷 Türkçe Açıklama</b></summary>

## 📋 Açıklama

QuickReport, Minecraft sunucuları için tasarlanmış güçlü ve kullanıcı dostu bir raporlama sistemidir. Oyuncuların hile yapanları, kural ihlallerini veya herhangi bir yanlış davranışı kolayca raporlamasını sağlarken, yöneticilere raporları verimli bir şekilde yönetme ve işleme araçları sunar.

## 🎮 Komutlar

### Oyuncu Komutları

- `/report <oyuncu> <sebep> [detaylar]` - Bir oyuncuyu raporla
- `/myreports [sayfa]` - Gönderdiğiniz raporları görüntüle
- `/queryreport <id>` - Belirli bir raporu sorgula

### Admin Komutları

- `/reports [sayfa]` - Bekleyen tüm raporları görüntüle
- `/reportaction <accept|reject> <id> [ödül-kodu|sebep]` - Raporu kabul et veya reddet

## ⚙️ Yapılandırma

### Rapor Sebeplerini Özelleştirme

`config.yml` dosyasındaki `report-reasons` listesini düzenleyerek geçerli sebepleri ekleyin veya kaldırın:

```yaml
report-reasons:
  - "Hile"
  - "Exploit"
  - "Küfür"
  - "Dolandırıcılık"
```

## 🎁 Ödül Sistemi

Oyuncuların raporları kabul edildiğinde otomatik olarak ödüllendirin.

### Yeni Ödül Ekleme

1. `config.yml` dosyasını açın
2. `rewards` bölümüne yeni ödül ekleyin:

```yaml
rewards:
  ozel-odul:
    name: "&aÖzel Ödül"
    command: "give %player% diamond_block 1"
```

3. Raporu kabul ederken ödül kodunu kullanın:

```
/reportaction accept 123 ozel-odul
```

## 📊 PlaceholderAPI Desteği

### Kullanılabilir Placeholderlar

- `%quickreport_kabul_edilen%` - Oyuncunun kabul edilen rapor sayısı
- `%quickreport_reddedilen%` - Oyuncunun reddedilen rapor sayısı
- `%quickreport_top_kabul_isim_<sıra>%` - En çok kabul edilen rapor gönderen oyuncu
- `%quickreport_top_kabul_sayi_<sıra>%` - En çok kabul edilen rapor sayısı
- `%quickreport_top_red_isim_<sıra>%` - En çok reddedilen rapor gönderen oyuncu
- `%quickreport_top_red_sayi_<sıra>%` - En çok reddedilen rapor sayısı

## 🌐 Çoklu Dil Desteği

QuickReport, kutusundan çıktığı gibi İngilizce ve Türkçe dil desteği sunar. `messages.yml` dosyasını düzenleyerek mesajları özelleştirebilirsiniz.

## 📖 Kullanım Örnekleri

### Oyuncu İş Akışı

1. **Rapor gönder:**
   ```
   /report Cheater123 Fly Spawn bölgesinde uçuyordu
   ```

2. **Raporlarını kontrol et:**
   ```
   /myreports
   ```

3. **Belirli bir raporu sorgula:**
   ```
   /queryreport 5
   ```

### Admin İş Akışı

1. **Bekleyen raporları görüntüle:**
   ```
   /reports
   ```

2. **Bir rapora tıklayın** veya şunu kullanın:
   ```
   /queryreport 5
   ```

3. **Raporu ödülle birlikte kabul et:**
   ```
   /reportaction accept 5 diamond-reward
   ```

4. **Raporu sebep belirterek reddet:**
   ```
   /reportaction reject 5 Yetersiz kanıt
   ```

</details>

---

**Made with ❤️ for the Minecraft community**
