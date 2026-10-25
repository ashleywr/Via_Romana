<p align="center"><img src="https://i.imgur.com/fJ1HxSM.png" width="200" height="200"> 
  
<h1 align="center">Via Romana<br>
<a href="https://www.curseforge.com/minecraft/mc-mods/via-romana"><img src="https://img.shields.io/badge/CurseForge-1.20.1 & 1.21.1-orange"></a>
<a href="https://modrinth.com/mod/via-romana"><img src="https://img.shields.io/badge/Modrinth-1.20.1 & 1.21.1-green"></a>
<a href="https://twitter.com/Rasa_Novum"><img src="https://img.shields.io/badge/Socials-Xitter-black"></a>
<a href="https://discord.gg/WGh4mq6W5U"><img src="https://img.shields.io/badge/Socials-Discord-5865F2"></a>
</h1>

Via Romana is a mod which allows fast travel only after a road connecting the destinations has been made, incentivizing players to build and see the world.

![Landing Image](https://i.imgur.com/dHqT8mo.png)

If you have any questions that aren't [answered in our Wiki](https://github.com/RasaNovum/Via_Romana/wiki), feel free to [ask on our Discord](https://discord.com/invite/WGh4mq6W5U)!

## Dependencies

- **[Fabric API](https://modrinth.com/mod/fabric-api)** - Core Fabric mod loader [Fabric Only]

- **[Data Anchor](https://modrinth.com/mod/data-anchor)** - Provides data saving/loading and networking
- **[Moonlight Lib](https://modrinth.com/mod/moonlight)** - Provides runtime resource pack modification
- **[MidnightLib](https://modrinth.com/mod/midnightlib)** - Provides config

## Destination Icons

Via Romana includes destination icons for signposts, houses, shops, towers, caves, crops, portals, and books.

Resource packs can add more icons by placing 16x16 PNG files under `assets/<namespace>/textures/screens/destination_icons/`. For example, `assets/example_pack/textures/screens/destination_icons/lighthouse.png` registers the icon ID `example_pack:lighthouse`. Custom icons must be valid 16x16 PNG files and must not exceed 16 KiB.

The destination icon field also accepts registered item IDs such as `minecraft:compass`, with autocomplete in the sign link screen.

Resource packs can register item-backed icon aliases under `assets/<namespace>/via_romana/destination_icons/`. For example, `assets/example_pack/via_romana/destination_icons/emerald.json` registers `example_pack:emerald`:

```json
{
  "type": "item",
  "item": "minecraft:emerald"
}
```

Item icon definitions must be valid JSON, must not exceed 4 KiB, and must reference a registered namespaced item ID.

## License

**Via Romana** is licensed under the **Responsive Source License (RSL) v1.0**. See the [LICENSE](LICENSE.md) file for full details.

Feel free to include this mod in your modpack as long as you download it from our official sources.

<!-- ashley-release-information -->

## Ashley's builds

This is an unofficial maintenance fork. Original authors retain ownership and credit. See the [upstream project](https://github.com/RasaNovum/Via_Romana) for the original mod. Local changes do not imply upstream endorsement.

Licensing: **Restricted: RSL-1.0 public binary distribution requires permission**. Existing license files, copyright notices, attribution and any separate asset terms remain in force. This fork does not relicense upstream work.

Provided **AS IS**, without warranty of any kind. Maintenance, updates and support are best effort; compatibility with every modpack is not guaranteed.

### Distribution status

Public binary publication is pending license/provenance review: Restricted: RSL-1.0 public binary distribution requires permission. An AS IS notice does not grant redistribution rights. Use the original author's distribution channels where applicable.

### Building and reporting problems

Use Java 21 and the checked-in Gradle wrapper:

```powershell
.\gradlew.bat :1.21.1-fabric:build :1.21.1-neoforge:build
```

Build outputs are under `build/libs/` or each loader subproject's `build/libs/`. Some projects require the local development dependencies described above; those dependencies are not included in the download.

For a problem report, include the exact release filename, Minecraft and loader versions, relevant mod versions, `logs/latest.log`, and a crash report if one was written. Remove private information before sharing logs. Remove the JAR to uninstall; worlds containing this mod's blocks, entities or world generation may require restoring a backup.
