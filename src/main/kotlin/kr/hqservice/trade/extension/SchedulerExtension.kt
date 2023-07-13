package kr.hqservice.trade.extension

import org.bukkit.plugin.Plugin

fun Plugin.later(delay: Long = 1L, runnable: Runnable) {
    server.scheduler.runTaskLater(this, runnable, delay)
}