package none.inferno.events

import net.minecraft.network.protocol.common.ClientboundPingPacket

abstract class TickEvent : Event() {
    class Start : TickEvent()
    class End : TickEvent()
    class Server : TickEvent()
}
