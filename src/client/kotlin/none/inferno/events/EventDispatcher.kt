package none.inferno.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.network.protocol.common.ClientboundPingPacket
import none.inferno.references.mc

object EventDispatcher {

    init {
        ClientPlayConnectionEvents.JOIN.register { handler, _, _ ->
            WorldLoadEvent().postAndCatch()
            ServerEvent.Connect(handler.serverData?.ip ?: "Singleplayer").postAndCatch()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ServerEvent.Disconnect(handler.serverData?.ip ?: "Singleplayer").postAndCatch()
        }
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            mc.level?.let { TickEvent.Start().postAndCatch() }
        }

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            mc.level?.let { TickEvent.End().postAndCatch() }
        }

        onReceive<ClientboundPingPacket> {
            if (id != 0) TickEvent.Server().postAndCatch()
        }

        // onReceive<ClientboundSoundPacket> {}
    }

}