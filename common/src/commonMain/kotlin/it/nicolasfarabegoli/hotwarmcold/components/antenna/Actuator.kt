package it.nicolasfarabegoli.hotwarmcold.components.antenna

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.*
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

class LedActuator : Actuator<Double> {
    private lateinit var job: Job
    private val scope = CoroutineScope(SupervisorJob())

    private var currentLedBrightness = 0.0

    companion object {
        private const val PORT = 8088
    }

    override suspend fun initialize() {
        val selectorManager = SelectorManager(scope.coroutineContext)
        val serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", PORT)
        job = scope.launch {
            while (true) {
                val socket = serverSocket.accept()
                launch {
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    while (true) {
                        sendChannel.writeStringUtf8(currentLedBrightness.toString() + "\n")
                        delay(500.milliseconds)
                    }
                }
            }
        }
    }

    override suspend fun finalize() {
        job.cancelAndJoin()
    }

    override suspend fun actuate(payload: Double) {
        currentLedBrightness = payload
    }
}

class LedActuatorContainer : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += LedActuator().apply { initialize() }
    }

    override suspend fun finalize() {
        this.get<LedActuator> { finalize() }
    }
}

suspend fun ledActuatorLogic(actuator: ActuatorsContainer, behaviourRef: BehaviourRef<Double>) {
    actuator.get<LedActuator> {
        behaviourRef.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
