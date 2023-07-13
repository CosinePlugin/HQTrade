package kr.hqservice.trade.command

import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import kr.hqservice.trade.extension.sendMessages
import kr.hqservice.trade.service.TradeApplicationService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class TradeUserCommand(
    private val tradeApplicationService: TradeApplicationService
) : CommandExecutor, TabExecutor {

    companion object {
        private val commandTabList = listOf("신청", "수락", "거절")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.size <= 1) {
            return commandTabList
        }
        if (args.size == 2 && args[0] == "신청") {
            return sender.server.onlinePlayers.map(Player::getName)
        }
        return emptyList()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$prefix 콘솔에서 사용할 수 없는 명령어입니다.")
            return true
        }
        if (args.isEmpty()) {
            printHelp(sender)
            return true
        }
        if (TradeAdminCommand.isTradeLocked) {
            sender.sendMessage("$prefix 거래 시스템이 비활성화 된 상태입니다.")
            return true
        }
        checker(args, sender)
        return true
    }

    private fun printHelp(player: Player) {
        player.sendMessages(
            "$prefix 거래 명령어 도움말",
            "",
            "$prefix /거래 신청 [닉네임]",
            "$prefix /거래 수락",
            "$prefix /거래 거절"
        )
    }

    private fun checker(args: Array<out String>, player: Player) {
        when (args[0]) {
            "신청" -> applicationTrade(args, player)

            "수락" -> acceptTrade(player)

            "거절" -> refuseTrade(player)

            else -> printHelp(player)
        }
    }

    private fun applicationTrade(args: Array<out String>, player: Player) {
        if (args.size == 1) {
            player.sendMessage("$prefix 닉네임을 입력해주세요.")
            return
        }
        val failReason = tradeApplicationService.application(player, args[1])
        failReason?.sendMessage(player)
    }

    private fun acceptTrade(player: Player) {
        when (val failReason = tradeApplicationService.accept(player)) {
            null -> player.sendMessage("$prefix 거래를 수락하였습니다.")
            else -> failReason.sendMessage(player)
        }
    }

    private fun refuseTrade(player: Player) {
        when (val failReason = tradeApplicationService.refuse(player)) {
            null -> player.sendMessage("$prefix 거래를 거절하였습니다.")
            else -> failReason.sendMessage(player)
        }
    }
}