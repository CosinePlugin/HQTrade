package kr.hqservice.trade.inventory

import kr.hqservice.trade.HQTrade.Companion.plugin
import kr.hqservice.trade.enums.TradeSlotType
import kr.hqservice.trade.extension.later
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
    private val headCustomModelData: Int,
    override val playerTrade: Trade,
    override val partnerTrade: Trade,
    private val tradeButtonHolder: TradeButtonHolder,
    private val tradeProcessService: TradeProcessService
) : TradeContainer(54, title, false) {

    private companion object {
        val preventClickTypes = arrayOf(ClickType.NUMBER_KEY, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT)
        val preventActionType = InventoryAction.COLLECT_TO_CURSOR
        val clickRegex = Regex("PLACE|PICKUP")
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
                if (headCustomModelData != 0) {
                    setCustomModelData(headCustomModelData)
                }
                setDisplayName("§f${player.name}")
                owningPlayer = player
                lore = listOf("§7금액: §e0원")
            }
        }
    }

    override fun onClick(event: InventoryClickEvent) {
        val slot = event.rawSlot
        val click = event.click
        val action = event.action
        if (action.name.contains(clickRegex)) {
            syncInventory()
        }
        if (playerTrade.isFirstReady() || preventClickTypes.contains(click) || preventActionType == action) {
            event.isCancelled = true
        }
        if (slot < 54 && slot !in TradeSlotType.PLAYER.slots) {
            event.isCancelled = true
        }
    }

    override fun onDrag(event: InventoryDragEvent) {
        val slots = event.rawSlots
        if (TradeSlotType.PARTNER.isIntersect(slots) || TradeSlotType.BACKGROUND.isIntersect(slots)) {
            event.isCancelled = true
        } else {
            syncInventory()
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
        plugin.later {
            val playerTradingInventory = playerTrade.getTradingInventory() ?: return@later
            val playerInventory = playerTradingInventory.inventory

            val partnerTradingInventory = partnerTrade.getTradingInventory() ?: return@later
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
}