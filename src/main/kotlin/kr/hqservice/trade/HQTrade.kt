package kr.hqservice.trade

import kr.hqservice.trade.command.TradeAdminCommand
import kr.hqservice.trade.command.TradeUserCommand
import kr.hqservice.trade.config.TradeConfig
import kr.hqservice.trade.inventory.TradeButtonHolder
import kr.hqservice.trade.inventory.container.handler.TradeContainerHandler
import kr.hqservice.trade.registry.InventoryInfoRegistry
import kr.hqservice.trade.service.TradeApplicationService
import kr.hqservice.trade.service.TradeProcessService
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin


class HQTrade : JavaPlugin() {

    companion object {
        internal lateinit var plugin: Plugin
            private set
    }

    private lateinit var economy: Economy

    override fun onLoad() {
        plugin = this
    }

    override fun onEnable() {
        if (!isEconomyExist()) {
            logger.warning("Vault를 찾을 수 없어, 플러그인이 종료됩니다.")
            server.pluginManager.disablePlugin(this)
            return
        }

        val inventoryInfoRegistry = InventoryInfoRegistry()
        val tradeConfig = TradeConfig(this, inventoryInfoRegistry)
        tradeConfig.load()

        val tradeButtonHolder = TradeButtonHolder(inventoryInfoRegistry, economy)
        val tradeProcessService = TradeProcessService(plugin, inventoryInfoRegistry, tradeButtonHolder, economy)
        val tradeApplicationService = TradeApplicationService(tradeProcessService)

        server.pluginManager.registerEvents(TradeContainerHandler(), this)

        getCommand("거래")?.setExecutor(TradeUserCommand(tradeApplicationService))
        getCommand("거래관리")?.setExecutor(TradeAdminCommand(tradeConfig))
    }

    override fun onDisable() {

    }

    private fun isEconomyExist(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) return false
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }
}