package none.inferno.utils

import none.inferno.events.ServerEvent
import none.inferno.events.on
object HypixelUtils {
var inHypixel : Boolean = false
    init {
        on<ServerEvent.Connect> {
            if(serverAddress.contains("hypixel", true)) {
                inHypixel = true
            }
            else {
                inHypixel = false
            }
            return@on
        }
        on<ServerEvent.Disconnect> {
            if(inHypixel) {
                inHypixel = false
            }
            return@on
        }
    }
}