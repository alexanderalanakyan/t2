package none.inferno.utils

import net.minecraft.world.entity.Entity

/**
 * Simple helper — currently returns the same color for every entity.
 * You can extend this to return different colors per-entity or add toggles.
 */
object GlowHelper {
    // RGB color: 0xRRGGBB (magenta)
    const val GLOW_COLOR_RGB: Int = 0xFF00FF

    fun getColorForEntity(entity: Entity?): Int {
        // Keep it simple: ignore entity, return constant color
        return GLOW_COLOR_RGB
    }
}