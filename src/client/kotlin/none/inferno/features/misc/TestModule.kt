package none.inferno.features.misc

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import none.inferno.config.gui.CustomScreen
import none.inferno.events.TickEvent
import none.inferno.events.on
import none.inferno.features.Module
import none.inferno.references.mc

object TestModule : Module(
    name = "TestModule",
    key = 0,
    description = "Test module",
    toggled = true
)
{

        init {
            on<TickEvent.Start>
            {
                val currentScreen: Screen? = mc.screen
                if (currentScreen is CustomScreen) return@on
                mc.setScreen(
                    CustomScreen(Component.empty(), currentScreen)
                )
            }
        }
}
