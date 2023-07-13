package kr.hqservice.trade.trade

import kr.hqservice.trade.inventory.TradingInventory
import org.bukkit.entity.Player

class Trade(
    private val player: Player
) {

    private var isFirstReady = false
    private var isSecondReady = false

    private var money = 0L

    private var tradingInventory: TradingInventory? = null

    private var isTrading = false

    fun getPlayer(): Player = player

    fun isReady(): Boolean = isFirstReady() && isSecondReady()

    fun isFirstReady(): Boolean = isFirstReady

    fun isSecondReady(): Boolean = isSecondReady

    fun setFirstReady(value: Boolean) {
        isFirstReady = value
    }

    fun setSecondReady(value: Boolean) {
        isSecondReady = value
    }

    fun getMoney(): Long = money

    fun setMoney(amount: Long) {
        money = amount
    }

    fun getTradingInventory(): TradingInventory? = tradingInventory

    fun setTradingInventory(tradingInventory: TradingInventory) {
        this.tradingInventory = tradingInventory
    }

    fun isTrading(): Boolean = isTrading

    fun setTrading(value: Boolean) {
        isTrading = value
    }
}