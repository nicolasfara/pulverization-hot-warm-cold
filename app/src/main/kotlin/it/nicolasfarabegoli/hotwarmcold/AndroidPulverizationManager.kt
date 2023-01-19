package it.nicolasfarabegoli.hotwarmcold

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import it.nicolasfarabegoli.hotwarmcold.components.smartphone.*
import it.nicolasfarabegoli.hotwarmcold.config.config
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.actuatorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.sensorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.joinAll

class AndroidPulverizationManager(
    private val lifecycle: Lifecycle,
    private val lifeCycleScope: LifecycleCoroutineScope,
    private val deviceId: String,
    private val rssiFlow: Flow<Int>
) : DefaultLifecycleObserver {
    val neighboursRssi = MutableSharedFlow<List<NeighbourDistance>>()
    private var canRunThePlatform = false
    private lateinit var platformJobRef: Job

    companion object {
        private val TAG = AndroidPulverizationManager::class.simpleName
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i(TAG, "I don't know")
        if (canRunThePlatform) {
            // Start the platform
            Log.i(TAG, "I don't know")
            platformJobRef = lifeCycleScope.launch(Dispatchers.IO) {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lifeCycleScope.launch(Dispatchers.IO) {
            if (::platformJobRef.isInitialized) {
                platformJobRef.cancelAndJoin()
            }
        }
    }

    fun runPlatform() {
        Log.i(TAG, "Setting up the platform")
        canRunThePlatform = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // Start the platform if not started
            platformJobRef = lifeCycleScope.launch(Dispatchers.IO) {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    private suspend fun initPulverization() {
        val platform = pulverizationPlatform(
            config.getDeviceConfiguration("smartphone")!!
        ) {
            sensorsLogic(SmartphoneSensorContainer(rssiFlow), ::deviceSensorLogic)
            actuatorsLogic(DeviceActuatorContainer(neighboursRssi), ::deviceActuatorLogic)
            withPlatform { RabbitmqCommunicator(hostname = "10.0.1.0") }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext {
                deviceID(deviceId)
            }
        }
        platform.start().joinAll()
        platform.stop()
    }
}
