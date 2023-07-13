package kr.hqservice.trade.inventory.container.button

import kr.hqservice.trade.inventory.container.button.event.ButtonClickEvent
import kr.hqservice.trade.extension.applyColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class TradeButtonBuilder(
    originalItemStack: ItemStack
) {

    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))

    @Deprecated(message = "because ItemStack's durability constructor is deprecated")
    constructor(material: Material, amount: Int, data: Short) : this(ItemStack(material, amount, data))

    private val itemStack = originalItemStack.clone()
    private val itemMeta = itemStack.itemMeta

    private var displayName = itemMeta?.displayName ?: ""
    private var lore = itemMeta?.lore ?: mutableListOf()
    private var itemFlags = itemMeta?.itemFlags ?: mutableSetOf()
    private var customModelData = if (itemMeta?.hasCustomModelData() == true) itemMeta.customModelData else 0

    private var clickFunction: (ButtonClickEvent) -> Unit = {}

    fun setDisplayName(displayName: String?): TradeButtonBuilder {
        if (displayName != null) {
            this.displayName = displayName
        }
        return this
    }

    fun setLore(lore: List<String>?): TradeButtonBuilder {
        if (lore != null) {
            this.lore = lore
        }
        return this
    }

    fun setCustomModelData(customModelData: Int): TradeButtonBuilder {
        this.customModelData = customModelData
        return this
    }

    fun addItemFlags(vararg itemFlag: ItemFlag): TradeButtonBuilder {
        itemFlags.addAll(itemFlag)
        return this
    }

    fun setClickFunction(clickFunction: (ButtonClickEvent) -> Unit): TradeButtonBuilder {
        this.clickFunction = clickFunction
        return this
    }

    fun build(): TradeButton {
        val applyItemMeta: (ItemMeta) -> Unit = { meta ->
            meta.setDisplayName(displayName.applyColor())
            if (lore.isNotEmpty()) {
                meta.lore = lore.applyColor()
            }
            meta.addItemFlags(*itemFlags.toTypedArray())
            meta.setCustomModelData(customModelData)
        }
        itemStack.itemMeta = itemStack.itemMeta?.also(applyItemMeta)
        return TradeButton(itemStack, clickFunction)
    }
}