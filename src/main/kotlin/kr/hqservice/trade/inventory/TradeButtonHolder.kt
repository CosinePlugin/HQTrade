package kr.hqservice.trade.inventory

import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import kr.hqservice.trade.enums.ButtonType
import kr.hqservice.trade.inventory.container.button.TradeButton
import kr.hqservice.trade.inventory.container.button.event.ButtonClickEvent
import kr.hqservice.trade.registry.InventoryInfoRegistry
import net.milkbowl.vault.economy.Economy
import org.bukkit.Instrument
import org.bukkit.Note
import org.bukkit.Sound
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat

class TradeButtonHolder(
    private val inventoryInfoRegistry: InventoryInfoRegistry,
    private val economy: Economy
) {

    private val decimalFormat = DecimalFormat("#,##0.###")

    private var playerXButton: TradeButton? = null
    private var playerVButton: TradeButton? = null

    private var partnerXButton: TradeButton? = null
    private var partnerVButton: TradeButton? = null

    private var money1Button: TradeButton? = null
    private var money10Button: TradeButton? = null
    private var money100Button: TradeButton? = null

    private fun getTradeButtonBuilder(buttonType: ButtonType) = inventoryInfoRegistry.getButton(buttonType)?.getTradeButtonBuilder()

    init {
        setPlayerXButton()
        setPlayerVButton()

        setPartnerXButton()
        setPartnerVButton()

        setMoney1Button()
        setMoney10Button()
        setMoney100Button()
    }

    fun getPlayerXButton(): TradeButton? = playerXButton

    private fun setPlayerXButton() {
        val readyFunction = getReadyClickFunction()
        playerXButton = getTradeButtonBuilder(ButtonType.PLAYER_X_BUTTON)?.setClickFunction(readyFunction)?.build()
    }

    fun getPlayerVButton(): TradeButton? = playerVButton

    private fun setPlayerVButton() {
        val readyFunction = getReadyClickFunction()
        playerVButton = getTradeButtonBuilder(ButtonType.PLAYER_V_BUTTON)?.setClickFunction(readyFunction)?.build()
    }

    private fun getReadyClickFunction(): (ButtonClickEvent) -> Unit {
        return ButtonClickEvent@ {
            val playerContainer = it.container

            val playerTrade = playerContainer.playerTrade
            val partnerTrade = playerContainer.partnerTrade

            val partnerContainer = partnerTrade.getTradingInventory() ?: return@ButtonClickEvent

            val player = it.player
            val partner = partnerTrade.getPlayer()

            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)

            when (val slot = it.rawSlot) {
                18 -> {
                    // 1차 레디 했으면 풀기 (만약 상대 2차 레디 중이면 모두 풀어야 됨)
                    if (playerTrade.isFirstReady()) {
                        if (playerTrade.isSecondReady()) return@ButtonClickEvent

                        if (partnerTrade.isSecondReady()) {
                            partnerTrade.setFirstReady(false)
                            getPlayerXButton()?.setSlot(partnerContainer, 18)
                            getPartnerXButton()?.setSlot(playerContainer, 26)
                        }

                        playerTrade.setFirstReady(false)
                        partnerTrade.setSecondReady(false)

                        getPlayerXButton()?.setSlot(playerContainer, slot)

                        getPartnerXButton()?.setSlot(partnerContainer, 26)
                        getPartnerXButton()?.setSlot(playerContainer, 35)

                        getPlayerXButton()?.setSlot(partnerContainer, 27)

                        player.playNote(player.location, Instrument.BELL, Note.natural(0, Note.Tone.F))
                    } else { // 1차 레디 안했으면 하기
                        playerTrade.setFirstReady(true)

                        getPlayerVButton()?.setSlot(playerContainer, slot)
                        getPartnerVButton()?.setSlot(partnerContainer, 26)

                        player.playNote(player.location, Instrument.BELL, Note.natural(0, Note.Tone.D))
                    }
                }
                27 -> {
                    // 2차 레디 했으면 풀기
                    if (playerTrade.isSecondReady()) {
                        playerTrade.setSecondReady(false)

                        getPlayerXButton()?.setSlot(playerContainer, slot)
                        getPartnerXButton()?.setSlot(partnerContainer, 35)

                        player.playNote(player.location, Instrument.BELL, Note.natural(1, Note.Tone.F))
                    } else { // 2차 레디 안했으면 하기
                        if (!playerTrade.isFirstReady() || !partnerTrade.isFirstReady()) return@ButtonClickEvent
                        playerTrade.setSecondReady(true)

                        getPlayerVButton()?.setSlot(playerContainer, slot)
                        getPartnerVButton()?.setSlot(partnerContainer, 35)

                        player.playNote(player.location, Instrument.BELL, Note.natural(1, Note.Tone.D))

                        // 거래 성공
                        if (partnerTrade.isSecondReady()) {
                            player.closeInventory()

                            playerTrade.setTrading(true)
                            partnerTrade.setTrading(true)

                            partner.closeInventory()
                        }
                    }
                }
            }
        }
    }

    fun getPartnerXButton(): TradeButton? = partnerXButton

    private fun setPartnerXButton() {
        partnerXButton = getTradeButtonBuilder(ButtonType.PARTNER_X_BUTTON)?.build()
    }

    fun getPartnerVButton(): TradeButton? = partnerVButton

    private fun setPartnerVButton() {
        partnerVButton = getTradeButtonBuilder(ButtonType.PARTNER_V_BUTTON)?.build()
    }

    fun getMoney1Button(): TradeButton? = money1Button

    private fun setMoney1Button() {
        val moneyClickFunction = getMoneyClickFunction(10000)
        money1Button = getTradeButtonBuilder(ButtonType.MONEY_1_BUTTON)?.setClickFunction(moneyClickFunction)?.build()
    }

    fun getMoney10Button(): TradeButton? = money10Button

    private fun setMoney10Button() {
        val moneyClickFunction = getMoneyClickFunction(100000)
        money10Button = getTradeButtonBuilder(ButtonType.MONEY_10_BUTTON)?.setClickFunction(moneyClickFunction)?.build()
    }

    fun getMoney100Button(): TradeButton? = money100Button

    private fun setMoney100Button() {
        val moneyClickFunction = getMoneyClickFunction(1000000)
        money100Button = getTradeButtonBuilder(ButtonType.MONEY_100_BUTTON)?.setClickFunction(moneyClickFunction)?.build()
    }

    private fun getMoneyClickFunction(money: Long): (ButtonClickEvent) -> Unit {
        return ButtonClickEvent@ {
            val clickType = it.clickType

            val playerContainer = it.container
            val playerTradingInventory = playerContainer.inventory

            val playerTrade = playerContainer.playerTrade

            if (playerTrade.isFirstReady()) return@ButtonClickEvent

            val partnerTrade = playerContainer.partnerTrade

            val partnerContainer = partnerTrade.getTradingInventory() ?: return@ButtonClickEvent
            val partnerTradingInventory = partnerContainer.inventory

            val player = it.player
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)

            val playerMoney = economy.getBalance(player).toLong()
            val playerTradeMoney = playerTrade.getMoney()

            var newMoney = playerTradeMoney
            if (clickType == ClickType.LEFT) {
                newMoney += money
                if (playerMoney < newMoney) {
                    player.sendMessage("$prefix 더 이상 금액을 추가할 수 없습니다.")
                    return@ButtonClickEvent
                }
            }
            if (clickType == ClickType.RIGHT) {
                newMoney -= money
                if (playerTradeMoney - money < 0) {
                    player.sendMessage("$prefix 더 이상 금액을 차감할 수 없습니다.")
                    return@ButtonClickEvent
                }
            }
            playerTrade.setMoney(newMoney)
            playerTradingInventory.getItem(2)?.editAmount(newMoney)
            partnerTradingInventory.getItem(6)?.editAmount(newMoney)
        }
    }

    private fun ItemStack.editAmount(amount: Long) {
        itemMeta = itemMeta?.apply {
            lore = listOf("§7금액: §e${amount.format()}원")
        }
    }

    private fun Long.format(): String = decimalFormat.format(this)
}