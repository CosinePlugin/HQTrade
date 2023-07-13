package kr.hqservice.trade.extension

import org.bukkit.command.CommandSender

fun CommandSender.sendMessages(vararg message: String) {
    message.forEach(::sendMessage)
}