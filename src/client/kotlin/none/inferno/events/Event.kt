package none.inferno.events



abstract class Event {

    open fun postAndCatch(): Boolean {
        runCatching {
            EventBus.post(this)
        }.onFailure {
            println("${it}, ${this}")

        }
        return false
    }
}