package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import org.koin.core.component.inject

class SmartphoneBehaviour : Behaviour<Unit, NeighbourRssi, Double, List<NeighbourRssi>, Unit> {
    override val context: Context by inject()

    override fun invoke(
        state: Unit,
        export: List<NeighbourRssi>,
        sensedValues: Double
    ): BehaviourOutput<Unit, NeighbourRssi, List<NeighbourRssi>, Unit> {
        return BehaviourOutput(
            Unit,
            NeighbourRssi(context.deviceID, sensedValues),
            emptyList(),
            Unit
        )
    }
}

suspend fun smartphoneBehaviourLogic(
    behaviour: Behaviour<Unit, NeighbourRssi, Double, List<NeighbourRssi>, Unit>,
    stateRef: StateRef<Unit>,
    commRef: CommunicationRef<NeighbourRssi>,
    sensorsRef: SensorsRef<Double>,
    actuatorsRef: ActuatorsRef<List<NeighbourRssi>>
) {
    sensorsRef.receiveFromComponent().collect { sensedValue ->
        val (_, _, actions, _) = behaviour(Unit, emptyList(), sensedValue)
        actuatorsRef.sendToComponent(actions)
    }
}
