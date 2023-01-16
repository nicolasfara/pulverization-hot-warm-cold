package it.nicolasfarabegoli.hotwarmcold.components

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import org.koin.core.component.inject

class DeviceActuator : Actuator<List<Double>> {
    override suspend fun actuate(payload: List<Double>) {
        TODO("Not yet implemented")
    }
}

class DeviceActuatorContainer : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val actuator = DeviceActuator().apply { initialize() }
        this += actuator
    }
}

suspend fun deviceActuatorLogic(
    actuator: ActuatorsContainer,
    behaviourRef: BehaviourRef<List<Double>>
) {
    actuator.get<DeviceActuator> {
        behaviourRef.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
