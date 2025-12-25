package none.inferno.features

object ModuleManager {

    val modules: ArrayList<Module> = arrayListOf()

    init {
        for (module in modules) {
           if(!module.enabled) module.toggle()
            }
        }
}