package kr.hqservice.trade.runnable

import kr.hqservice.trade.enums.Application
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class TradeApplicationRunnable(
    private val applications: Map<UUID, Application>,
    private val targetUUID: UUID,
    private val applicationFunction: (Application) -> Unit
) : BukkitRunnable() {

    private var time = 30

    override fun run() {
        val application = applications[targetUUID]
        if (application == Application.ACCEPT || application == Application.REFUSE) {
            cancel()
            applicationFunction(application)
            return
        }
        if (time == 0) {
            cancel()
            applicationFunction(Application.TIME_OUT)
            return
        }
        time--
    }
}