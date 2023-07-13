package kr.hqservice.trade.command

import kr.hqservice.trade.config.TradeConfig
import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import kr.hqservice.trade.extension.sendMessages
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class TradeAdminCommand(
    private val tradeConfig: TradeConfig
) : CommandExecutor, TabExecutor {

    companion object {
        private val commandTabList = listOf("금지", "리로드")

        var isTradeLocked = false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.size <= 1) {
            return commandTabList
        }
        return emptyList()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$prefix 콘솔에서 사용할 수 없는 명령어입니다.")
            return true
        }
        if (!sender.isOp) {
            sender.sendMessage("$prefix 해당 명령어를 사용할 권한이 없습니다.")
            return true
        }
        if (args.isEmpty()) {
            printHelp(sender)
            return true
        }
        checker(args, sender)
        return true
    }

    private fun printHelp(player: Player) {
        player.sendMessages(
            "$prefix 거래관리 명령어 도움말",
            "",
            "$prefix /거래관리 금지",
            "$prefix /거래관리 리로드"
        )
    }

    private fun checker(args: Array<out String>, player: Player) {
        when (args[0]) {
            "금지" -> lockTrade(player)

            "리로드" -> reload(player)

            else -> printHelp(player)
        }
    }

    private fun lockTrade(player: Player) {
        if (isTradeLocked) {
            isTradeLocked = false
            player.sendMessage("$prefix 거래 시스템이 활성화되었습니다.")
        } else {
            isTradeLocked = true
            player.sendMessage("$prefix 거래 시스템이 비활성화되었습니다.")
        }
    }

    private fun reload(player: Player) {
        tradeConfig.reload()
        player.sendMessage("$prefix config.yml을 리로드하였습니다.")
    }
}