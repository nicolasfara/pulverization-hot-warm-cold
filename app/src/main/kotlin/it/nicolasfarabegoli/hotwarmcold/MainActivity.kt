package it.nicolasfarabegoli.hotwarmcold

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {
    private lateinit var pulverizationManager: AndroidPulverizationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pulverizationManager = AndroidPulverizationManager(this, lifecycle, lifecycleScope)
        lifecycle.addObserver(pulverizationManager)

        // After the user fill the text box with DEVICE_ID and press the start button, then start the platform
        pulverizationManager.runPlatform()
    }
}
