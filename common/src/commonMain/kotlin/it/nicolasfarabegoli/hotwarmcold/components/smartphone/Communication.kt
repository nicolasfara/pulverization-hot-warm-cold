package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class SmartphoneCommunication : Communication<NeighbourRssi> {
    override val context: Context by inject()

    override fun receive(): Flow<NeighbourRssi> {
        TODO("Not yet implemented")
    }

    override suspend fun send(payload: NeighbourRssi) {
        TODO("Not yet implemented")
    }
}

suspend fun smartphoneCommunicationLogic(
    communication: Communication<NeighbourRssi>,
    behaviourRef: BehaviourRef<NeighbourRssi>
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
