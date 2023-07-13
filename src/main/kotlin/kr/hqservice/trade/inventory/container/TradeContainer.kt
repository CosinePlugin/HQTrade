package kr.hqservice.trade.inventory.container

import kr.hqservice.trade.HQTrade
import kr.hqservice.trade.extension.applyColor
import kr.hqservice.trade.inventory.container.button.TradeButton
import kr.hqservice.trade.trade.Trade
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class TradeContainer(
    private val size: Int,
    private val title: String,
    val isCancelled: Boolean = true
) : InventoryHolder {

    companion object {
        private val plugin by lazy { HQTrade.plugin }
    }

    private var baseInventory: Inventory? = null
    private val buttons = mutableMapOf<Int, TradeButton>()

    abstract val playerTrade: Trade
    abstract val partnerTrade: Trade

    protected open fun init(inventory: Inventory) {}

    open fun onClick(event: InventoryClickEvent) {}
    open fun onDrag(event: InventoryDragEvent) {}
    open fun onClose(event: InventoryCloseEvent) {}

    internal fun registerButton(slot: Int, button: TradeButton) {
        if (buttons[slot] == button) return
        if (slot >= size) throw IndexOutOfBoundsException("$slot")

        buttons[slot] = button
        val original = inventory.contents[slot]
        val buttonItemStack = button.getItemStack()

        if (original?.isMatchType(buttonItemStack) == true) {
            original.itemMeta = buttonItemStack.itemMeta
        } else {
            inventory.setItem(slot, buttonItemStack.clone())
        }
    }

    internal fun getButton(slot: Int): TradeButton? = buttons[slot]

    @Suppress("deprecation")
    private fun ItemStack.isMatchType(item: ItemStack): Boolean {
        return type == item.type && durability == item.durability
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    override fun getInventory(): Inventory {
        return baseInventory ?: plugin.server.createInventory(this, size, title.applyColor()).apply {
            baseInventory = this
            init(this)
        }
    }
}