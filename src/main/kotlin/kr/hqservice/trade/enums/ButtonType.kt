package kr.hqservice.trade.enums

import kr.hqservice.trade.data.TemporaryItemStack
import org.bukkit.Material

enum class ButtonType {
    PLAYER_X_BUTTON,
    PLAYER_V_BUTTON,
    PARTNER_X_BUTTON,
    PARTNER_V_BUTTON,
    MONEY_1_BUTTON,
    MONEY_10_BUTTON,
    MONEY_100_BUTTON;

    companion object {
        fun getKey(key: String): ButtonType? {
            return values().find { it.name == key.replace("-", "_").uppercase() }
        }
    }

    fun newInstance(
        material: Material,
        displayName: String?,
        lore: List<String>?,
        customModelData: Int
    ) = TemporaryItemStack(material, displayName, lore, customModelData)
}