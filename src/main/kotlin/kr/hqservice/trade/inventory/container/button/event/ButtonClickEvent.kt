package kr.hqservice.trade.inventory.container.button.event

import kr.hqservice.trade.inventory.container.TradeContainer
import kr.hqservice.trade.inventory.container.button.TradeButton
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ButtonClickEvent(
    private val event: InventoryClickEvent,
    val container: TradeContainer,
    val button: TradeButton
) {

    val slot get() = event.slot

    val rawSlot get() = event.rawSlot

    val clickType get() = event.click

    val player get() = event.whoClicked as Player

    val isShiftClick get() = event.isShiftClick
}