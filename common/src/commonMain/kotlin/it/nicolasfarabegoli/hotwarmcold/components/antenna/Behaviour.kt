package it.nicolasfarabegoli.hotwarmcold.components.antenna

import it.nicolasfarabegoli.hotwarmcold.components.smartphone.NeighbourDistance
import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import org.koin.core.component.inject

class LedBehaviour : Behaviour<Unit, NeighbourDistance, Unit, Double, Unit> {
    override val context: Context by inject()

    companion object {
        private const val MIN_DISTANCE = 0.05
        private const val MAX_DISTANCE = 3.0
    }

    override fun invoke(
        state: Unit,
        export: List<NeighbourDistance>,
        sensedValues: Unit
    ): BehaviourOutput<Unit, NeighbourDistance, Double, Unit> {
        val meanDistance = export.sumOf { it.distance } / export.size
        val normalizedDistance = 1.0 - (meanDistance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)
        return BehaviourOutput(Unit, NeighbourDistance("", 0.0), normalizedDistance, Unit)
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun ledBehaviourLogic(
    behaviour: Behaviour<Unit, NeighbourDistance, Unit, Double, Unit>,
    state: StateRef<Unit>,
    comm: CommunicationRef<NeighbourDistance>,
    sensors: SensorsRef<Unit>,
    actuators: ActuatorsRef<Double>
) {
    var neighboursMessages = emptyList<NeighbourDistance>()
    comm.receiveFromComponent().collect {
        neighboursMessages = neighboursMessages.filter { e -> e.deviceId != it.deviceId } + it
        val (_, _, actuation, _) = behaviour(Unit, neighboursMessages, Unit)
        actuators.sendToComponent(actuation)
    }
}
