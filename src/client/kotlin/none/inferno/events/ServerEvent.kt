package none.inferno.events

abstract class ServerEvent(val serverAddress: String) : Event() {
    class Connect(serverAddress: String) : ServerEvent(serverAddress)

    class Disconnect(serverAddress: String) : ServerEvent(serverAddress)
}