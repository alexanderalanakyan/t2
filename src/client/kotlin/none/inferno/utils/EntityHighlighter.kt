package none.inferno.utils

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import none.inferno.references.Functions.removeIfKey
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object EntityHighlighter {

    private val entityColorMap = mutableMapOf<LivingEntity, Color>()
    private val entityColorCondition = ConcurrentHashMap<LivingEntity, () -> Boolean>()

    private val entityNoHurtTimeCondition = mutableMapOf<LivingEntity, () -> Boolean>()

    fun onTick() {
        entityColorMap.removeIfKey { it.isDeadOrDying }
        entityColorCondition.removeIfKey { it.isDeadOrDying }
        entityNoHurtTimeCondition.removeIfKey { it.isDeadOrDying }
    }

    fun onWolrdUpdate() {
        entityColorMap.clear()

        entityColorCondition.clear()
        entityNoHurtTimeCondition.clear()
    }

    public fun getEntity(Entity : Entity) : Boolean {
        return entityColorMap.containsKey(Entity.asLivingEntity())
    }
    public fun getColor(Entity : Entity) : Color {
        return Color.RED
    }
    public fun addEntity(entity : Entity) {

    }
}