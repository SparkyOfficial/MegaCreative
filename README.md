# MegaCreative
**MegaCreative** is a revolutionary visual programming platform for Minecraft. Forget about old, slow plugins that scan the world for blocks. We present a next-generation architecture where your code is a live graph of objects executed at lightning speed.
**Simple and elegant.** Create complex mini-games, interactive quests, and living worlds by simply placing blocks.

### Key Features

*   🚀 **Unmatched Performance:** No world scanning. All logic consists of lightweight in-memory objects executed instantly thanks to a modern `ScriptEngine`.
*   **⚙️ Fantastically Flexible:** All blocks, their materials, behavior, and parameters are configurable through YAML files. Add new actions without writing a single line of Java code.
*   **🧩 Modern Architecture:** The plugin is built on `Service Registry` and `Dependency Injection`, ensuring stability, modularity, and easy extensibility.
*   **🛠️ Pro-Level Tools:** Built-in **Visual Debugger** for debugging, **Code Block Clipboard** for copying code chains, and **Performance Monitor** for analyzing the performance of your creations.
*   **🌐 Creator Platform:** Create worlds, share them via the built-in **World Browser**, play, and rate other players' projects.

### 🆚 MegaCreative vs Other Platforms

Many know OpenCreative or have played on Mineland and DiamondFire. What's the key difference?

| Aspect | MegaCreative | Other Platforms (OpenCreative, server implementations) |
| :--- | :--- | :--- |
| **Performance** | **Object-oriented execution.** Code is compiled once into a convenient structure and executed instantly. No real-time world scanning. | **Scanning or outdated methods.** Old plugins (OpenCreative) physically scan blocks at every event, causing lag. Server counterparts may be fast, but their technologies are closed. |
| **Flexibility** | **100% customizable via YAML.** You can create your own code blocks, define their logic and appearance through `coding_blocks.yml` without touching the core. | **Hardcoded.** In most systems, new blocks are added only by plugin/server developers. You're limited to what they provide. |
| **Format** | **Full plugin.** You can install MegaCreative on **your** Paper/Spigot server and have complete control. | **Closed server system.** Platforms like Mineland, YottaCraft, JustMC, or DiamondFIRE are features of specific servers. You can't take their system and move it to yours. |
| **Architecture** | **Modern stack:** Dependency Injection, Service Registry, asynchronicity. Code is clean, modular, and easily extensible by the community. | **Outdated approaches:** Often uses "God Object" (one class doing everything), making maintenance and adding new functionality difficult. |

---

### 🚀 Quick Start

1.  **/create <type>** — Create your world.
2.  **/dev** — Enter development mode.
3.  **Create!** Place blocks from your inventory.
4.  **/play** — Return to play mode and test your code.
5.  **/worldsettings** — Configure your world and make it public!

---

### 💬 Community

Join our Discord to share ideas, get help, and participate in developing the best creative coding platform!

https://discord.gg/gz8KUkWWMj


---

### ❤️ Contribution

Want to make MegaCreative even better? Check out our [CONTRIBUTING.md](CONTRIBUTING.md).