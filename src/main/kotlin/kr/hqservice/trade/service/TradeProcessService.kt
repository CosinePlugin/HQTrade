package kr.hqservice.trade.service

import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import kr.hqservice.trade.enums.FailReason
import kr.hqservice.trade.enums.TradeSlotType
import kr.hqservice.trade.extension.later
import kr.hqservice.trade.inventory.TradeButtonHolder
import kr.hqservice.trade.inventory.TradingInventory
import kr.hqservice.trade.registry.InventoryInfoRegistry
import kr.hqservice.trade.trade.Trade
import net.milkbowl.vault.economy.Economy
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.UUID

class TradeProcessService(
    private val plugin: Plugin,
    private val inventoryInfoRegistry: InventoryInfoRegistry,
    private val tradeButtonHolder: TradeButtonHolder,
    private val economy: Economy
) {

    private val trades = mutableMapOf<UUID, Trade>()

    fun isTrading(uuid: UUID): Boolean = trades.containsKey(uuid)

    fun startTrade(player: Player, target: Player) {
        val playerTrade = Trade(player)
        val targetTrade = Trade(target)

        trades[player.uniqueId] = playerTrade
        trades[target.uniqueId] = targetTrade

        val title = inventoryInfoRegistry.getTitle()
        val headCustomModelData = inventoryInfoRegistry.getHeadCustomModelData()
        val playerTradingInventory = TradingInventory(title, headCustomModelData, playerTrade, targetTrade, tradeButtonHolder, this)
        val targetTradingInventory = TradingInventory(title, headCustomModelData, targetTrade, playerTrade, tradeButtonHolder, this)

        playerTrade.setTradingInventory(playerTradingInventory)
        targetTrade.setTradingInventory(targetTradingInventory)

        playerTradingInventory.open(player)
        targetTradingInventory.open(target)
    }

    fun stopTrade(player: Player, partner: Player) {
        trades.remove(partner.uniqueId)
        trades.remove(player.uniqueId)
    }

    fun successTrade(player: Player, partner: Player) {
        val playerUUID = player.uniqueId
        val partnerUUID = partner.uniqueId

        val playerTrade = trades[playerUUID] ?: return
        val partnerTrade = trades[partnerUUID] ?: return

        val playerMoney = playerTrade.getMoney().toDouble()
        val partnerMoney = partnerTrade.getMoney().toDouble()

        economy.withdrawPlayer(player, playerMoney)
        economy.withdrawPlayer(partner, partnerMoney)

        economy.depositPlayer(player, partnerMoney)
        economy.depositPlayer(partner, playerMoney)

        giveTradingItem(player, partner, TradeSlotType.PLAYER.slots)
        giveTradingItem(partner, player, TradeSlotType.PLAYER.slots)

        player.sendMessage("$prefix ${partner.name}님과의 거래가 완료되었습니다.")
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f)

        partner.sendMessage("$prefix ${player.name}님과의 거래가 완료되었습니다.")
        partner.playSound(partner.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f)

        stopTrade(player, partner)
    }

    fun failTrade(player: Player, partner: Player) {
        val playerUUID = player.uniqueId
        val partnerUUID = partner.uniqueId

        val playerTrade = trades[playerUUID] ?: return
        val partnerTrade = trades[partnerUUID] ?: return

        playerTrade.setTrading(true)
        partnerTrade.setTrading(true)

        giveTradingItem(player, player, TradeSlotType.PLAYER.slots)
        giveTradingItem(partner, partner, TradeSlotType.PLAYER.slots)

        partner.closeInventory()

        player.sendMessage("$prefix 거래를 취소하였습니다.")
        partner.sendMessage("$prefix ${player.name}님이 거래를 취소하였습니다.")

        stopTrade(player, partner)
    }

    private fun giveTradingItem(player: Player, target: Player, slots: Set<Int>) {
        val playerTradingInventory = player.openInventory.topInventory
        val targetInventory = target.inventory
        slots.forEach { slot ->
            val item = playerTradingInventory.getItem(slot) ?: return@forEach
            targetInventory.addItem(item)
        }
    }
}