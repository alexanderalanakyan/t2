package none.inferno

import net.fabricmc.api.ClientModInitializer
import none.inferno.events.EventBus
import none.inferno.events.EventDispatcher
import none.inferno.features.ModuleManager
import none.inferno.features.misc.TestModule
import none.inferno.utils.HypixelUtils


class InfernoLoader : ClientModInitializer{
    override fun onInitializeClient() {
        listOf(EventDispatcher, ModuleManager, HypixelUtils, TestModule ).forEach { EventBus.subscribe(it); println("Loaded ${it.javaClass.simpleName}") }
    }
}