package it.nicolasfarabegoli.hotwarmcold

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var pulverizationManager: AndroidPulverizationManager
    private lateinit var btManager: BluetoothManager
    private val rssiTextView by lazy {
        findViewById<TextView>(R.id.rssiLabel)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = BluetoothManager(this).apply { start() }
        lifecycleScope.launch(Dispatchers.Main) {
            btManager.rssiFlow().collect {
                rssiTextView.text = "RSSI: $it"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::btManager.isInitialized) {
            lifecycleScope.launch { btManager.stop() }
        }
    }

    private fun startPlatform() {
        pulverizationManager =
            AndroidPulverizationManager(this, lifecycle, lifecycleScope)
        lifecycle.addObserver(pulverizationManager)
        pulverizationManager.runPlatform()
    }
}
