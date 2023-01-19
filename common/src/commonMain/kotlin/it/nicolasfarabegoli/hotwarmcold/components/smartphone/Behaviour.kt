package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.math.pow

class SmartphoneBehaviour : Behaviour<Unit, NeighbourDistance, Int, List<NeighbourDistance>, Unit> {
    override val context: Context by inject()

    override fun invoke(
        state: Unit,
        export: List<NeighbourDistance>,
        sensedValues: Int
    ): BehaviourOutput<Unit, NeighbourDistance, List<NeighbourDistance>, Unit> {
        // Convert the RSSI signal strength into a distance in meters
        val myDistance = 10.0.pow((-63 - sensedValues) / (10 * 2.4))
        return BehaviourOutput(
            Unit,
            NeighbourDistance(context.deviceID, myDistance),
            export, // The "prescriptive actions" are used to show the neighbour's distance to the UI
            Unit
        )
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun smartphoneBehaviourLogic(
    behaviour: Behaviour<Unit, NeighbourDistance, Int, List<NeighbourDistance>, Unit>,
    stateRef: StateRef<Unit>,
    commRef: CommunicationRef<NeighbourDistance>,
    sensorsRef: SensorsRef<Int>,
    actuatorsRef: ActuatorsRef<List<NeighbourDistance>>
) = coroutineScope {
    var neighboursComms = emptyList<NeighbourDistance>()

    val job = launch {
        commRef.receiveFromComponent().collect { c ->
            neighboursComms = neighboursComms.filter { it.deviceId != c.deviceId } + c
        }
    }

    sensorsRef.receiveFromComponent().collect { sensedValue ->
        val (_, myComm, actions, _) = behaviour(Unit, neighboursComms, sensedValue)
        actuatorsRef.sendToComponent(actions)
        commRef.sendToComponent(myComm)
    }

    job.join()
}
