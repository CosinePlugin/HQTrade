package kr.hqservice.trade.inventory

import kr.hqservice.trade.enums.TradeSlotType
import kr.hqservice.trade.inventory.container.TradeContainer
import kr.hqservice.trade.service.TradeProcessService
import kr.hqservice.trade.trade.Trade
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class TradingInventory(
    title: String,
    override val playerTrade: Trade,
    override val partnerTrade: Trade,
    private val tradeButtonHolder: TradeButtonHolder,
    private val tradeProcessService: TradeProcessService
) : TradeContainer(54, title, false) {

    companion object {
        private val preventClickTypes = arrayOf(ClickType.NUMBER_KEY, ClickType.SHIFT_LEFT)
        private val preventActionType = InventoryAction.COLLECT_TO_CURSOR
    }

    override fun init(inventory: Inventory) {
        tradeButtonHolder.getPlayerXButton()?.setSlot(this, 18, 27)
        tradeButtonHolder.getPartnerXButton()?.setSlot(this, 26, 35)
        tradeButtonHolder.getMoney1Button()?.setSlot(this, 46)
        tradeButtonHolder.getMoney10Button()?.setSlot(this, 47)
        tradeButtonHolder.getMoney100Button()?.setSlot(this, 48)

        val playerHead = createHead(playerTrade.getPlayer())
        val partnerHead = createHead(partnerTrade.getPlayer())

        inventory.setItem(2, playerHead)
        inventory.setItem(6, partnerHead)
    }

    private fun createHead(player: Player): ItemStack {
        return ItemStack(Material.PLAYER_HEAD).apply {
            itemMeta = (itemMeta as SkullMeta).apply {
                setCustomModelData(1)
                setDisplayName("§f${player.name}")
                owningPlayer = player
                lore = listOf("§7금액: §e0원")
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        syncInventory()

        val slot = event.rawSlot
        val click = event.click
        val action = event.action

        if (playerTrade.isFirstReady() || preventClickTypes.contains(click) || preventActionType == action) {
            event.isCancelled = true
        }
        if (slot < 54 && slot !in TradeSlotType.PLAYER.slots) {
            event.isCancelled = true
        }
    }

    override fun onDrag(event: InventoryDragEvent) {
        val slots = event.rawSlots
        if (slots.intersect(TradeSlotType.PARTNER.slots).isNotEmpty()) {
            event.isCancelled = true
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        val partner = partnerTrade.getPlayer()

        if (playerTrade.isTrading()) return

        if (playerTrade.isReady() && partnerTrade.isReady()) {
            tradeProcessService.successTrade(player, partner)
        } else {
            tradeProcessService.failTrade(player, partner)
        }
    }

    private fun syncInventory() {
        val playerTradingInventory = playerTrade.getTradingInventory() ?: return
        val playerInventory = playerTradingInventory.inventory

        val partnerTradingInventory = partnerTrade.getTradingInventory() ?: return
        val partnerInventory = partnerTradingInventory.inventory

        val slots = TradeSlotType.PLAYER.slots
        slots.forEach { slot ->
            val proposerItem = inventory.getItem(slot)
            val partnerSlot = slot + 4
            partnerInventory.setItem(partnerSlot, proposerItem)
        }
        slots.forEach { slot ->
            val partnerItem = partnerInventory.getItem(slot)
            val partnerSlot = slot + 4
            playerInventory.setItem(partnerSlot, partnerItem)
        }
    }
}