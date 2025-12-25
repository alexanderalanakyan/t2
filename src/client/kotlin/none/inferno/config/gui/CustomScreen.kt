package none.inferno.config.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component


class CustomScreen(title: Component?, val parent: Screen?) : Screen(title) {
    override fun init() {
        val buttonWidget: Button? = Button.builder(Component.nullToEmpty("Hello World"), { btn ->
            // When the button is clicked, we can display a toast to the screen.
            this.minecraft!!.getToastManager().addToast(
                SystemToast.multiline(
                    this.minecraft,
                    SystemToast.SystemToastId.NARRATOR_TOGGLE,
                    Component.nullToEmpty("Hello World!"),
                    Component.nullToEmpty("This is a toast.")
                )
            )
        }).bounds(40, 40, 120, 20).build()

        // x, y, width, height
        // It's recommended to use the fixed height of 20 to prevent rendering issues with the button
        // textures.

        // Register the button widget.
        this.addRenderableWidget(buttonWidget)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        // Minecraft doesn't have a "label" widget, so we'll have to draw our own text.
        // We'll subtract the font height from the Y position to make the text appear above the button.
        // Subtracting an extra 10 pixels will give the text some padding.
        // textRenderer, text, x, y, color, hasShadow
        context.drawString(this.font, "Special Button", 40, 40 - this.font.lineHeight - 10, -0x1, true)
    }

    override fun onClose() {
        this.minecraft?.setScreen(parent)
    }
}