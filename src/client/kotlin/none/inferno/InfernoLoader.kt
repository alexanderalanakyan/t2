package none.inferno

import net.fabricmc.api.ClientModInitializer
import none.inferno.events.EventBus
import none.inferno.events.EventDispatcher


class InfernoLoader : ClientModInitializer{
    override fun onInitializeClient() {
        EventBus.subscribe(EventDispatcher)
    }
}