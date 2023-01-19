package it.nicolasfarabegoli.hotwarmcold.components.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject

class BluetoothSensor(private val rssiFlow: Flow<Int>) : Sensor<Int> {

    private var currentRssi = 0
    private lateinit var job: Job
    private val scope = CoroutineScope(SupervisorJob())

    override suspend fun initialize() {
        job = scope.launch {
            rssiFlow.collect {
                currentRssi = it
            }
        }
    }

    override suspend fun finalize() {
        job.cancelAndJoin()
    }

    override suspend fun sense(): Int = currentRssi
}

class SmartphoneSensorContainer(private val rssiFlow: Flow<Int>) : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val btSensor = BluetoothSensor(rssiFlow).apply { initialize() }
        this += btSensor
    }
}

suspend fun deviceSensorLogic(sensor: SensorsContainer, behaviourRef: BehaviourRef<Int>) {
    sensor.get<BluetoothSensor> {
        while (true) {
            behaviourRef.sendToComponent(sense())
            delay(1_000)
        }
    }
}
