package kr.hqservice.trade.service

import kr.hqservice.trade.HQTrade.Companion.plugin
import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import kr.hqservice.trade.enums.Application
import kr.hqservice.trade.enums.FailReason
import kr.hqservice.trade.runnable.TradeApplicationRunnable
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.UUID

class TradeApplicationService(
    private val tradeProcessService: TradeProcessService
) {

    private val applications = mutableMapOf<UUID, Application>()

    fun application(player: Player, targetName: String): FailReason? {
        val server = player.server
        val target = server.getPlayerExact(targetName) ?: return FailReason.TARGET_IS_OFFLINE

        val playerUUID = player.uniqueId
        val targetUUID = target.uniqueId

        if (playerUUID == targetUUID) {
            return FailReason.IS_SELF
        }
        if (applications.containsKey(playerUUID)) {
            return FailReason.HAS_TRADE_APPLICATION
        }
        if (applications.containsKey(targetUUID)) {
            return FailReason.TARGET_HAS_TRADE_APPLICATION
        }
        if (tradeProcessService.isTrading(targetUUID)) {
            return FailReason.TARGET_ALREADY_TRADING
        }

        applications[playerUUID] = Application.WAIT
        applications[targetUUID] = Application.NONE

        player.sendMessage("$prefix ${target.name}님에게 거래 신청을 보냈습니다.")
        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        target.sendMessage("$prefix ${player.name}님이 거래 신청을 보냈습니다.")
        target.sendClickableTradeMessage()
        target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)

        TradeApplicationRunnable(applications, targetUUID) { application ->
            if (application == Application.ACCEPT) {
                player.sendMessage("$prefix ${target.name}님이 거래를 수락하였습니다.")
                tradeProcessService.startTrade(player, target)
            }
            if (application == Application.REFUSE || application == Application.TIME_OUT) {
                player.sendMessage("$prefix ${target.name}님이 거래를 거절하였습니다.")
            }
            applications.remove(playerUUID)
            applications.remove(targetUUID)
        }.runTaskTimer(plugin, 0, 20)

        return null
    }

    private fun Player.sendClickableTradeMessage() {
        val editorComponent = TextComponent("§7└ 수락하시려면 ").apply {
            val buttonBuilder: (String, String, String) -> Unit = { title, command, description ->
                val editorButton = TextComponent(title)
                editorButton.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
                editorButton.hoverEvent =
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(description)))
                addExtra(editorButton)
            }
            buttonBuilder("§a/거래 수락[클릭]", "/거래 수락", "§f클릭 시 거래 신청을 수락합니다.")
            addExtra("§7, 거절하시려면 ")
            buttonBuilder("§c/거래 거절[클릭]", "/거래 거절", "§f클릭 시 거래 신청을 거절합니다.")
        }
        spigot().sendMessage(editorComponent)
    }

    fun accept(player: Player): FailReason? {
        val uuid = player.uniqueId
        if (applications[uuid] != Application.NONE) {
            return FailReason.HAS_NOT_TRADE_APPLICATION
        }
        applications[uuid] = Application.ACCEPT
        return null
    }

    fun refuse(player: Player): FailReason? {
        val uuid = player.uniqueId
        if (applications[uuid] != Application.NONE) {
            return FailReason.HAS_NOT_TRADE_APPLICATION
        }
        applications[uuid] = Application.REFUSE
        return null
    }
}