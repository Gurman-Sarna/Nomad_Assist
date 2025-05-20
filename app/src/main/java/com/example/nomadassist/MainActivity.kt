package com.example.nomadassist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var feedbackTextView: TextView
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        micButton = findViewById(R.id.micButton)
        resultTextView = findViewById(R.id.resultTextView)
        feedbackTextView = findViewById(R.id.feedbackTextView)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                feedbackTextView.text = "Listening..."
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
                resultTextView.text = "You said: \"$spokenText\""
                handleCommand(spokenText)
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                feedbackTextView.text = "Error occurred: $error"
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        micButton.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        speechRecognizer.startListening(intent)
    }

    private fun handleCommand(input: String) {
        val lowerCmd = input.lowercase()

        val commandMap = mapOf(
            "turn on wifi" to ::enableWifi,
            "wifi on" to ::enableWifi,
            "enable wifi" to ::enableWifi,
            "turn off wifi" to ::disableWifi,
            "disable wifi" to ::disableWifi,
            "turn on bluetooth" to ::enableBluetooth,
            "bluetooth on" to ::enableBluetooth,
            "turn off bluetooth" to ::disableBluetooth,
            "bluetooth off" to ::disableBluetooth,
            "turn on airplane mode" to ::enableAirplaneMode,
            "airplane mode on" to ::enableAirplaneMode,
            "turn off airplane mode" to ::disableAirplaneMode,
            "airplane mode off" to ::disableAirplaneMode
        )

        for ((key, action) in commandMap) {
            if (lowerCmd.contains(key)) {
                action()
                feedbackTextView.text = "Executing: $key"
                return
            }
        }

        // 🎯 Additional app-launching and system intents
        when {
            "open youtube" in lowerCmd -> launchApp("com.google.android.youtube")
            "open whatsapp" in lowerCmd -> launchApp("com.whatsapp")
            "open telegram" in lowerCmd -> launchApp("org.telegram.messenger")
            "open camera" in lowerCmd -> openCamera()
            "open browser" in lowerCmd -> openBrowser()
            else -> updateFeedback("Command not recognized.")
        }
    }


    private fun enableWifi() {
        val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }

    private fun disableWifi() {
        val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }

    private fun enableBluetooth() {
        val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
    }

    private fun disableBluetooth() {
        val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent)
    }

    private fun enableAirplaneMode() {
        val intent = Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        startActivity(intent)
    }

    private fun disableAirplaneMode() {
        val intent = Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        startActivity(intent)
    }

    private fun launchApp(packageName: String) {
        val pm = packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
            updateFeedback("Launching ${getAppName(packageName)}")
        } else {
            updateFeedback("App not found.")
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "App"
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            updateFeedback("Camera app not found.")
        }
    }

    private fun openBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
        startActivity(intent)
    }

    private fun updateFeedback(message: String){
        feedbackTextView.text = message
    }


    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
