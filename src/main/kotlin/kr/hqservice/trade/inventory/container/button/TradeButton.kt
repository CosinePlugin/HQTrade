package kr.hqservice.trade.inventory.container.button

import kr.hqservice.trade.inventory.container.button.event.ButtonClickEvent
import kr.hqservice.trade.inventory.container.TradeContainer
import org.bukkit.inventory.ItemStack

class TradeButton(
    private val item: ItemStack,
    private val clickFunction: (ButtonClickEvent) -> Unit = {},
) {

    fun getItemStack() = item

    fun click(event: ButtonClickEvent) {
        clickFunction(event)
    }

    fun setSlot(container: TradeContainer, vararg slots: Int) {
        slots.forEach { container.registerButton(it, this) }
    }

    fun setSlot(container: TradeContainer, vararg ranges: IntProgression) {
        ranges.forEach { range ->
            range.forEach {
                container.registerButton(it, this)
            }
        }
    }
}