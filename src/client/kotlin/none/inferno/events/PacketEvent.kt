package none.inferno.events

import net.minecraft.network.protocol.Packet

abstract class PacketEvent(val packet: Packet<*>) : CancellableEvent() {

    class Receive(packet: Packet<*>) : PacketEvent(packet)

    class Send(packet: Packet<*>) : PacketEvent(packet)
}