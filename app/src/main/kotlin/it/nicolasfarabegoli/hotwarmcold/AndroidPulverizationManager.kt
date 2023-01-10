package it.nicolasfarabegoli.hotwarmcold

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import it.nicolasfarabegoli.hotwarmcold.components.DeviceActuatorContainer
import it.nicolasfarabegoli.hotwarmcold.components.SmartphoneSensorContainer
import it.nicolasfarabegoli.hotwarmcold.components.deviceActuatorLogic
import it.nicolasfarabegoli.hotwarmcold.components.deviceSensorLogic
import it.nicolasfarabegoli.hotwarmcold.config.config
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AndroidPulverizationManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val lifeCycleScope: LifecycleCoroutineScope
) : DefaultLifecycleObserver {
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
            platformJobRef = lifeCycleScope.launch {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lifeCycleScope.launch {
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
            platformJobRef = lifeCycleScope.launch {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    private suspend fun initPulverization() {
        val platform = pulverizationPlatform<Any, Any, Double, List<Double>, Unit>(
            config.getDeviceConfiguration("smartphone")!!
        ) {
            sensorsLogic(SmartphoneSensorContainer(), ::deviceSensorLogic)
            actuatorsLogic(DeviceActuatorContainer(), ::deviceActuatorLogic)
        }
        platform.start().joinAll()
        platform.stop()
    }
}
