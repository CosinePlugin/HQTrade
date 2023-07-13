package kr.hqservice.trade.extension

import org.bukkit.ChatColor

fun String.applyColor() = ChatColor.translateAlternateColorCodes('&', this)

fun List<String>.applyColor() = map { it.applyColor() }