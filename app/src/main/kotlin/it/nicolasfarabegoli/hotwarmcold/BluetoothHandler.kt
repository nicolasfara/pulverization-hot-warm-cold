package it.nicolasfarabegoli.hotwarmcold

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheral
import com.welie.blessed.ConnectionFailedException
import com.welie.blessed.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.time.Duration.Companion.seconds

class BluetoothHandler private constructor(context: Context) {
    private val rssiChannel = MutableSharedFlow<Int>(1)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val btCentralManager by lazy { BluetoothCentralManager(context) }
    private lateinit var observerJob: Job

    companion object {
        private var instance: BluetoothHandler? = null
        private val TAG = BluetoothHandler::class.simpleName

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): BluetoothHandler {
            if (instance == null) {
                instance = BluetoothHandler(context)
            }
            return requireNotNull(instance)
        }
    }

    fun start() {
        btCentralManager.observeConnectionState { peripheral, state ->
            Log.i(TAG, "Peripheral $peripheral is $state")
            when (state) {
                ConnectionState.CONNECTED -> peripheralLogic(peripheral)
                ConnectionState.DISCONNECTED -> {}
                else -> {}
            }
        }
        btCentralManager.observeAdapterState {
            when (it) {
                BluetoothAdapter.STATE_ON -> startScanning()
            }
        }
        startScanning()
    }

    suspend fun stop() {
        observerJob.cancelAndJoin()
    }

    fun rssiFlow(): Flow<Int> = rssiChannel.asSharedFlow()

    private fun startScanning() {
        btCentralManager.scanForPeripherals(
            { bluetoothPeripheral, scanResult ->
                Log.i(TAG, "Found '${bluetoothPeripheral.name}' with RSSI ${scanResult.rssi}")
                if (bluetoothPeripheral.name == "ESP32") {
                    btCentralManager.stopScan()
                    connectPeripherals(bluetoothPeripheral)
                }
            },
            {
                Log.e(TAG, "Fail scan with reason $it")
            }
        )
    }

    private fun connectPeripherals(peripherals: BluetoothPeripheral) {
        peripherals.observeBondState { Log.i(TAG, "Bond state is $it") }
        scope.launch {
            try {
                btCentralManager.connectPeripheral(peripherals)
            } catch (ex: ConnectionFailedException) {
                Log.e(TAG, "Connection failed: ${ex.printStackTrace()}")
            }
        }
    }

    private fun peripheralLogic(peripherals: BluetoothPeripheral) {
        observerJob = scope.launch(Dispatchers.IO) {
            while (true) {
                val rssi = peripherals.readRemoteRssi()
                Log.i(TAG, "Device RSSI: $rssi")
                rssiChannel.emit(rssi)
                delay(1.seconds)
            }
        }
    }
}
