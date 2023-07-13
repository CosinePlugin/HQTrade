package kr.hqservice.trade.data

import kr.hqservice.trade.inventory.container.button.TradeButtonBuilder
import org.bukkit.Material

data class TemporaryItemStack(
    private val material: Material,
    private val displayName: String?,
    private val lore: List<String>?,
    private val customModelData: Int
) {

    private val button by lazy {
        TradeButtonBuilder(material).setDisplayName(displayName).setLore(lore).setCustomModelData(customModelData)
    }

    fun getTradeButtonBuilder() = button
}