package kr.hqservice.trade.config

import kr.hqservice.trade.enums.ButtonType
import kr.hqservice.trade.extension.applyColor
import kr.hqservice.trade.registry.InventoryInfoRegistry
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class TradeConfig(
    plugin: Plugin,
    private val inventoryInfoRegistry: InventoryInfoRegistry
) {

    companion object {
        internal var prefix = "§6[ 거래 ]§f"
            private set

        private const val path = "config.yml"
    }

    private val logger = plugin.logger

    private var file: File
    private var config: YamlConfiguration

    init {
        val newFile = File(plugin.dataFolder, path)
        if (!newFile.exists() && plugin.getResource(path) != null) {
            plugin.saveResource(path, false)
        }
        file = newFile
        config = YamlConfiguration.loadConfiguration(newFile)
    }

    fun load() {
        prefix = config.getString("prefix")?.applyColor() ?: "§6[ 거래 ]§f"
        config.getConfigurationSection("inventory-info")?.apply {
            val title = getString("title")?.applyColor() ?: "%partner%님과의 거래"
            inventoryInfoRegistry.setTitle(title)

            val headCustomModelData = getInt("head-custom-model-data")
            inventoryInfoRegistry.setHeadCustomModelData(headCustomModelData)

            getConfigurationSection("buttons")?.apply {
                getKeys(false).forEach { key ->
                    getConfigurationSection(key)?.apply {
                        val buttonType = ButtonType.getKey(key) ?: run {
                            logger.warning("${key}는 존재하지 않는 버튼 타입입니다.")
                            return@forEach
                        }
                        val materialText = getString("material") ?: run {
                            logger.warning("${key}의 material을 불러오지 못했습니다.")
                            return@forEach
                        }
                        val material = Material.getMaterial(materialText) ?: run {
                            logger.warning("$materialText(은)는 존재하지 않는 Material입니다.")
                            return@forEach
                        }
                        val display = getString("display")
                        val lore = getStringList("lore").ifEmpty { null }
                        val customModelData = getInt("custom-model-data")

                        val button = buttonType.newInstance(material, display, lore, customModelData)
                        inventoryInfoRegistry.setButton(buttonType, button)
                    }
                }
            }
        }
    }

    fun reload() {
        config.load(file)
        inventoryInfoRegistry.clear()
        load()
    }
}