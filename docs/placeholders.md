# 🎆 FrameLand-Style Placeholder System Documentation

MegaCreative now supports a comprehensive placeholder system inspired by FrameLand, with full backwards compatibility.

## Supported Placeholder Formats

### 1. FrameLand Style (Recommended): `prefix[content]~`

#### Variables
- `apple[variable_name]~` - Get player variable value
- `var[variable_name]~` - Alternative variable syntax
- `apple[variable|default]~` - Variable with default value

#### Player Information
- `player[name]~` - Player's username
- `player[display_name]~` - Player's display name
- `player[uuid]~` - Player's UUID
- `player[world]~` - Player's current world
- `player[health]~` - Current health
- `player[max_health]~` - Maximum health
- `player[food]~` - Food level
- `player[level]~` - Experience level
- `player[gamemode]~` - Current game mode

#### World Information
- `world[name]~` - World name
- `world[time]~` - World time
- `world[weather]~` - Weather (clear/storm)
- `world[difficulty]~` - World difficulty
- `world[seed]~` - World seed

#### Location
- `location[x]~` - Block X coordinate
- `location[y]~` - Block Y coordinate
- `location[z]~` - Block Z coordinate
- `location[yaw]~` - Player's yaw
- `location[pitch]~` - Player's pitch
- `location[world]~` - Location's world
- `location[formatted]~` - \"X, Y, Z\" format

#### Mathematics
- `math[5+3]~` - Simple addition
- `math[10*2]~` - Simple multiplication
- `math[apple[var1]~+apple[var2]~]~` - Variable-based math

#### Time & Date
- `time[HH:mm]~` - Custom time format
- `time[short]~` - HH:mm format
- `time[medium]~` - HH:mm:ss format
- `time[long]~` - yyyy-MM-dd HH:mm:ss format
- `time[date]~` - yyyy-MM-dd format

#### Random Numbers
- `random[1-100]~` - Random number between 1-100
- `random[50]~` - Random number between 0-50
- `random[]~` - Random decimal 0.0-1.0

#### Server Information
- `server[online]~` - Online player count
- `server[max]~` - Maximum players
- `server[version]~` - Server version
- `server[name]~` - Server name
- `server[motd]~` - Server MOTD

#### Colors & Formatting
- `color[red]~` - §c (red color)
- `color[green]~` - §a (green color)
- `color[blue]~` - §9 (blue color)
- `color[yellow]~` - §e (yellow color)
- `color[bold]~` - §l (bold)
- `color[italic]~` - §o (italic)
- `color[reset]~` - §r (reset formatting)

#### Number Formatting
- `format[1234.567|2]~` - \"1234.57\" (2 decimals)
- `format[apple[money]~|currency]~` - \"$1234.57\"
- `format[0.75|percent]~` - \"75.0%\"

### 2. Modern Style: `${variable}`
- `${player_name}` - Player's name
- `${player_world}` - Player's world
- `${variable_name}` - Variable value
- `${timestamp}` - Current timestamp
- `${random}` - Random number

### 3. Classic Style: `%variable%`
- `%player%` - Player's name
- `%world%` - Player's world
- `%x%`, `%y%`, `%z%` - Player coordinates

## Usage Examples

### Basic Variable Display
```
Your score is apple[score]~ points!
```

### Complex Player Stats
```
color[gold]~=== player[name]~'s Stats ===
color[green]~❤ Health: player[health]~/player[max_health]~
color[blue]~📍 Location: location[formatted]~
color[yellow]~🌍 World: world[name]~ at time[short]~
color[reset]~Updated: time[medium]~
```

### Math with Variables
```
Total damage: math[apple[base_damage]~*apple[multiplier]~]~
Level progress: format[math[apple[current_exp]~/apple[required_exp]~*100]|1]~%
```

### Conditional with Defaults
```
Welcome apple[player_nickname|player[name]~]~!
Your rank: apple[rank|Newcomer]~
```

### Rich Formatting
```
color[cyan]~💰 Money: format[apple[balance]~|currency]~
color[green]~🎯 Accuracy: format[math[apple[hits]~/apple[shots]~]|percent]~
color[red]~🔥 Streak: apple[kill_streak|0]~ kills
```

## Advanced Features

### Nested Placeholders
Placeholders can be nested for complex expressions:
```
math[apple[base_value]~+math[apple[bonus]~*2]~]~
```

### Default Values
Provide fallback values when variables are missing:
```
apple[player_title|Unknown Adventurer]~
```

### Multiple Formats
Mix different placeholder styles in the same text:
```
FrameLand: apple[score]~, Modern: ${player_name}, Classic: %world%
```

## Performance Notes

- FrameLand-style placeholders are processed first (highest priority)
- Modern `${}` placeholders are processed second
- Classic `%%` placeholders are processed last
- Nested placeholders are resolved recursively
- Variable lookups are cached per execution context
- Math operations are limited to basic arithmetic for security

## Migration Guide

### From Classic to FrameLand Style
- `%player%` → `player[name]~`
- `%world%` → `world[name]~`
- `%x%` → `location[x]~`
- `${variable}` → `apple[variable]~`

### Backwards Compatibility
All existing placeholder formats continue to work. You can gradually migrate to FrameLand style for better features and readability.

## Security Features

- Math expressions are sandboxed (only basic arithmetic)
- No script execution or file access
- Variable access respects player permissions
- Format strings are validated
- Recursion depth is limited to prevent infinite loops

This system provides the flexibility and power of FrameLand's placeholder system while maintaining full backwards compatibility with existing MegaCreative scripts.
