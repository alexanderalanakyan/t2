package none.inferno.mixin.client;



import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;



/**
 * Forces every entity to be considered glowing and returns a constant color for the outline.
 * Use this to verify the glow pass works on your client.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

	// Redirect the call to Minecraft.shouldEntityAppearGlowing(...) inside extractRenderState
	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"
			)
	)
	private boolean redirectShouldEntityAppearGlowing(Minecraft client, Entity entity) {

        return true;
	}

	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"
			)
	)
	private int redirectGetTeamColor(Entity entity) {

		return Color.BLACK.getRGB();
	}
}