package kr.hqservice.trade.enums

import kr.hqservice.trade.config.TradeConfig.Companion.prefix
import org.bukkit.entity.Player

enum class FailReason(private val message: String) {
    TARGET_IS_OFFLINE("해당 유저가 오프라인입니다."),
    IS_SELF("자신에게 거래를 신청할 수 없습니다."),
    HAS_TRADE_APPLICATION("이미 거래 신청을 보낸 상태입니다."),
    TARGET_HAS_TRADE_APPLICATION("해당 유저가 이미 다른 거래 신청을 보유 중입니다."),
    TARGET_ALREADY_TRADING("이미 거래를 진행 중인 유저입니다."),
    HAS_NOT_TRADE_APPLICATION("보유 중인 거래 신청이 없습니다.");
    
    fun sendMessage(player: Player) {
        player.sendMessage("$prefix $message")
    }
}