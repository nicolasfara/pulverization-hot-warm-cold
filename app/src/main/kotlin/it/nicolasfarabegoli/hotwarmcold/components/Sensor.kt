package it.nicolasfarabegoli.hotwarmcold.components

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.delay
import org.koin.core.component.inject
import quevedo.soares.leandro.blemadeeasy.BluetoothConnection

class BluetoothSensor(private val ble: BluetoothConnection) : Sensor<Double> {

    override suspend fun initialize() {
    }

    override suspend fun sense(): Double {
        TODO("Not yet implemented")
    }
}

class SmartphoneSensorContainer(private val ble: BluetoothConnection) : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val btSensor = BluetoothSensor(ble).apply { initialize() }
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
