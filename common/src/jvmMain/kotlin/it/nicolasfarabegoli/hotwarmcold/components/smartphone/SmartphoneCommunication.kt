package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import com.rabbitmq.client.ConnectionFactory
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import reactor.core.publisher.Mono
import reactor.rabbitmq.*

actual class SmartphoneCommunication : Communication<NeighbourDistance> {
    override val context: Context by inject()

    private lateinit var sender: Sender
    private lateinit var receiver: Receiver

    private val exchange = "neighbours"
    private lateinit var queue: String

    companion object {
        private const val HOST = "localhost"
        private const val RMQ_PORT = 5672
    }

    override suspend fun initialize() {
        val connectionFactory = ConnectionFactory()
        connectionFactory.useNio()
        connectionFactory.apply {
            host = HOST
            port = RMQ_PORT
            username = "guest"
            password = "guest"
            virtualHost = "/"
        }
        val connection = connectionFactory.newConnection()
        val senderOption = SenderOptions().connectionSupplier { connection }
        val receiverOption = ReceiverOptions().connectionSupplier { connection }
        sender = RabbitFlux.createSender(senderOption)
        receiver = RabbitFlux.createReceiver(receiverOption)

        sender.apply {
            queue = "neighbours/${context.deviceID}"
            declareExchange(
                ExchangeSpecification.exchange(exchange).type("fanout")
            ).awaitSingleOrNull()
            declareQueue(QueueSpecification.queue(queue).durable(false)).awaitSingleOrNull()
            bindQueue(BindingSpecification().exchange(exchange).queue(queue).routingKey(""))
                .awaitSingleOrNull()
        }
    }

    override fun receive(): Flow<NeighbourDistance> =
        receiver.consumeAutoAck(queue)
            .asFlow()
            .map { Json.decodeFromString(it.body.decodeToString()) }

    override suspend fun send(payload: NeighbourDistance) {
        val message = OutboundMessage(exchange, "", Json.encodeToString(payload).toByteArray())
        sender.send(Mono.just(message)).awaitSingleOrNull()
    }

    override suspend fun finalize() {
        sender.close()
        receiver.close()
    }
}
