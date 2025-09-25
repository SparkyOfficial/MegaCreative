# MegaCreative
**MegaCreative** ist eine revolutionäre visuelle Programmierplattform für Minecraft. Vergessen Sie alte, langsame Plugins, die die Welt nach Blöcken durchsuchen. Wir präsentieren eine Architektur der nächsten Generation, bei der Ihr Code ein lebendiger Graph von Objekten ist, der mit Blitzgeschwindigkeit ausgeführt wird.
**Einfach und elegant.** Erstellen Sie komplexe Minispiele, interaktive Quests und lebendige Welten, indem Sie einfach Blöcke platzieren.

### Hauptmerkmale

*   🚀 **Unglaubliche Leistung:** Kein Welt-Scanning. Die gesamte Logik besteht aus leichtgewichtigen Objekten im Speicher, die dank einer modernen `ScriptEngine` sofort ausgeführt werden.
*   **⚙️ Fantastisch flexibel:** Alle Blöcke, ihre Materialien, ihr Verhalten und ihre Parameter sind über YAML-Dateien konfigurierbar. Fügen Sie neue Aktionen hinzu, ohne eine einzige Zeile Java-Code zu schreiben.
*   **🧩 Moderne Architektur:** Das Plugin basiert auf `Service Registry` und `Dependency Injection`, was Stabilität, Modularität und einfache Erweiterbarkeit gewährleistet.
*   **🛠️ Profi-Werkzeuge:** Integrierter **Visual Debugger** zum Debuggen, **Code Block Clipboard** zum Kopieren von Code-Ketten und **Performance Monitor** zur Analyse der Leistung Ihrer Kreationen.
*   **🌐 Plattform für Schöpfer:** Erstellen Sie Welten, teilen Sie sie über den integrierten **World Browser**, spielen Sie und bewerten Sie Projekte anderer Spieler.

### 🆚 MegaCreative vs. Andere Plattformen

Viele kennen OpenCreative oder haben auf Mineland und DiamondFire gespielt. Was ist der entscheidende Unterschied?

| Aspekt | MegaCreative | Andere Plattformen (OpenCreative, Server-Implementierungen) |
| :--- | :--- | :--- |
| **Leistung** | **Objektorientierte Ausführung.** Code wird einmal in eine praktische Struktur kompiliert und sofort ausgeführt. Kein Echtzeit-Welt-Scanning. | **Scanning oder veraltete Methoden.** Alte Plugins (OpenCreative) scannen physisch bei jedem Ereignis nach Blöcken, was zu Lag führt. Server-Pendants können schnell sein, aber ihre Technologien sind geschlossen. |
| **Flexibilität** | **100% anpassbar über YAML.** Sie können Ihre eigenen Codeblöcke erstellen und deren Logik und Aussehen über `coding_blocks.yml` definieren, ohne den Kern zu berühren. | **Fest kodiert.** In den meisten Systemen werden neue Blöcke nur von Plugin-/Server-Entwicklern hinzugefügt. Sie sind auf das beschränkt, was sie erhalten. |
| **Format** | **Vollständiges Plugin.** Sie können MegaCreative auf **Ihren** Paper/Spigot-Server installieren und ihn vollständig kontrollieren. | **Geschlossenes Server-System.** Plattformen wie Mineland, YottaCraft, JustMC oder DiamondFIRE sind Funktionen bestimmter Server. Sie können ihr System nicht nehmen und zu sich bringen. |
| **Architektur** | **Moderne Technologie:** Dependency Injection, Service Registry, Asynchronität. Code ist sauber, modular und leicht von der Community erweiterbar. | **Veraltete Ansätze:** Oft wird "God Object" verwendet (eine Klasse, die alles macht), was die Wartung und das Hinzufügen neuer Funktionen erschwert. |

---

### 🚀 Schnellstart

1.  **/create <Typ>** — Erstellen Sie Ihre Welt.
2.  **/dev** — Wechseln Sie in den Entwicklungsmodus.
3.  **Erschaffen!** Platzieren Sie Blöcke aus Ihrem Inventar.
4.  **/play** — Kehren Sie zum Spielmodus zurück und testen Sie Ihren Code.
5.  **/worldsettings** — Konfigurieren Sie Ihre Welt und machen Sie sie öffentlich!

---

### 💬 Gemeinschaft

Treten Sie unserem Discord bei, um Ideen auszutauschen, Hilfe zu erhalten und sich an der Entwicklung der besten kreativen Programmierplattform zu beteiligen!

https://discord.gg/gz8KUkWWMj


---

### ❤️ Beitrag

Möchten Sie MegaCreative noch besser machen? Schauen Sie sich unsere [CONTRIBUTING.md](CONTRIBUTING.md) an.