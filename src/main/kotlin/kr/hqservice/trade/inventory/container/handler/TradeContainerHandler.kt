package kr.hqservice.trade.inventory.container.handler

import kr.hqservice.trade.inventory.container.button.event.ButtonClickEvent
import kr.hqservice.trade.inventory.container.TradeContainer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.InventoryView

class TradeContainerHandler : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        getContainer(event.view)?.apply {
            event.isCancelled = isCancelled

            getButton(event.rawSlot)?.also { button ->
                event.isCancelled = true
                button.click(ButtonClickEvent(event, this, button))
            } ?: onClick(event)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryDrag(event: InventoryDragEvent) {
        getContainer(event.view)?.apply {
            onDrag(event)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        getContainer(event.view)?.apply {
            onClose(event)
        }
    }

    private fun getContainer(view: InventoryView): TradeContainer? {
        val holder = view.topInventory.holder ?: return null
        return if (holder is TradeContainer) holder else null
    }
}