package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
data class NeighbourDistance(val deviceId: String, val distance: Double)

class DeviceActuator(
    private val flow: MutableSharedFlow<List<NeighbourDistance>>
) : Actuator<List<NeighbourDistance>> {
    override suspend fun actuate(payload: List<NeighbourDistance>) = flow.emit(payload)
}

class DeviceActuatorContainer(
    private val flow: MutableSharedFlow<List<NeighbourDistance>>
) : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val actuator = DeviceActuator(flow).apply { initialize() }
        this += actuator
    }
}

suspend fun deviceActuatorLogic(
    actuator: ActuatorsContainer,
    behaviourRef: BehaviourRef<List<NeighbourDistance>>
) {
    actuator.get<DeviceActuator> {
        behaviourRef.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
