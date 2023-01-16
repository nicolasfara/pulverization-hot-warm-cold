package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
data class NeighbourRssi(val deviceId: String, val rssi: Double)

class DeviceActuator(
    private val flow: MutableSharedFlow<List<NeighbourRssi>>
) : Actuator<List<NeighbourRssi>> {
    override suspend fun actuate(payload: List<NeighbourRssi>) = flow.emit(payload)
}

class DeviceActuatorContainer(
    private val flow: MutableSharedFlow<List<NeighbourRssi>>
) : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val actuator = DeviceActuator(flow).apply { initialize() }
        this += actuator
    }
}

suspend fun deviceActuatorLogic(
    actuator: ActuatorsContainer,
    behaviourRef: BehaviourRef<List<NeighbourRssi>>
) {
    actuator.get<DeviceActuator> {
        behaviourRef.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
