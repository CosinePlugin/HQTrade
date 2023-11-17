package kr.hqservice.trade.enums

enum class TradeSlotType(val slots: Set<Int>) {
    BACKGROUND(
        setOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 13, 17,
            18, 22, 26,
            27, 31, 35,
            36, 40, 44,
            45, 49, 50, 51, 52, 53
        )
    ),
    PLAYER(setOf(10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39)),
    PARTNER(setOf(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43));

    fun isIntersect(slots: Set<Int>): Boolean {
        return this.slots.intersect(slots).isNotEmpty()
    }
}