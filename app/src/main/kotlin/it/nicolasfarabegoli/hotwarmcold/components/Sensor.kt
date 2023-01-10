package it.nicolasfarabegoli.hotwarmcold.components

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.delay
import org.koin.core.component.inject

class BluetoothSensor : Sensor<Double> {
    override suspend fun sense(): Double {
        TODO("Not yet implemented")
    }
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
