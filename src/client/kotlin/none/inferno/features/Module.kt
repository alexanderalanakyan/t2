package none.inferno.features

import none.inferno.references.Category

abstract class Module(
    val name: String,
    var description: String,
    toggled: Boolean = false,
){
    var enabled: Boolean = toggled
        private set
    init {}

    open fun onEnable() {

    }
    open fun onDisable() {

    }
    val category = getCategory(this::class.java) ?: Category.MISC

    fun toggle() {
        enabled = !enabled
        if(enabled) onEnable()
        else onDisable()
    }
    companion object {
        private fun getCategory(clazz: Class<out Module>): Category? =
            Category.entries.find {clazz.`package`.name.contains(it.name, true)}
    }
}