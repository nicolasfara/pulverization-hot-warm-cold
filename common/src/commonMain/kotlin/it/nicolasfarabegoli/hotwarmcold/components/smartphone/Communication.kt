package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

expect class SmartphoneCommunication : Communication<NeighbourDistance>

suspend fun smartphoneCommunicationLogic(
    communication: Communication<NeighbourDistance>,
    behaviourRef: BehaviourRef<NeighbourDistance>
) = coroutineScope {
    val j1 = launch {
        behaviourRef.receiveFromComponent().collect {
            communication.send(it)
        }
    }
    val j2 = launch {
        communication.receive().collect {
            behaviourRef.sendToComponent(it)
        }
    }
    listOf(j1, j2).joinAll()
}
