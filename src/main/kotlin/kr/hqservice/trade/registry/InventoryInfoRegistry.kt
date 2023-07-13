package kr.hqservice.trade.registry

import kr.hqservice.trade.data.TemporaryItemStack
import kr.hqservice.trade.enums.ButtonType

class InventoryInfoRegistry {

    private var title = "%partner%님과의 거래"
    private var buttons = mutableMapOf<ButtonType, TemporaryItemStack>()

    fun getTitle() = title

    fun setTitle(title: String) {
        this.title = title
    }

    fun getButton(buttonType: ButtonType) = buttons[buttonType]

    fun setButton(buttonType: ButtonType, temporaryItemStack: TemporaryItemStack) {
        buttons[buttonType] = temporaryItemStack
    }

    fun clear() {
        buttons.clear()
    }
}