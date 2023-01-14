package it.nicolasfarabegoli.hotwarmcold.components

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.*
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

class BluetoothSensor : Sensor<Double> {

    private var distance = 0.0
    private lateinit var job: Job

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun initialize() {
        job = GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                delay(500.milliseconds)
            }
        }
    }

    override suspend fun finalize() {
        job.cancelAndJoin()
    }

    override suspend fun sense(): Double = distance
}

class SmartphoneSensorContainer : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val btSensor = BluetoothSensor().apply { initialize() }
        this += btSensor
    }
}

suspend fun deviceSensorLogic(sensor: SensorsContainer, behaviourRef: BehaviourRef<Double>) {
    sensor.get<BluetoothSensor> {
        while (true) {
            behaviourRef.sendToComponent(sense())
            delay(1_000)
        }
    }
}
