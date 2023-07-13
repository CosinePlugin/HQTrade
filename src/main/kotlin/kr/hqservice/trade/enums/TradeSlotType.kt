package kr.hqservice.trade.enums

enum class TradeSlotType(val slots: Set<Int>) {
    PLAYER(setOf(10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39)),
    PARTNER(setOf(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43))
}