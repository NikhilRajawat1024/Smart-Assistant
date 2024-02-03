package com.example.smartassistant

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.ClipboardManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import io.grpc.Context
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class startactivity : AppCompatActivity() {
    private lateinit var textView:TextView
    private lateinit var tts:TextToSpeech
    private lateinit var button: Button
    private lateinit var hirobotGif: GifImageView
    private lateinit var spokenTextParts: Array<String>
    private var currentTextPartIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startactivity)
        textView = findViewById(R.id.textview)

        button = findViewById(R.id.getstarted)
        hirobotGif = findViewById(R.id.hirobot)

        val gifDrawable = hirobotGif.drawable as? GifDrawable

        gifDrawable?.addAnimationListener {
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 2000)
        }
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.setSpeechRate(1.2f)
                spokenTextParts = arrayOf(
                    "Hello,",
                    "I'm Autumn,",
                    "Your personal virtual assistant",
                    "I am here to make your life easier and more organized.",
                    "Let's get Started"
                )
                currentTextPartIndex = 0

                // Set a listener to detect when speech is completed
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        if (utteranceId == "part$currentTextPartIndex") {
                            runOnUiThread {
                                currentTextPartIndex++
                                speakNextTextPart()


                            }
                        }

                    }

                    override fun onError(utteranceId: String) {
                        // Handle errors if needed
                    }

                    override fun onStart(utteranceId: String) {


                    }
                })

                // Start speaking the first part
                speakNextTextPart()
            }
        }

        button.setOnClickListener{
            tts.stop()
            val intent = Intent(this@startactivity, MainActivity::class.java)
            startActivity(intent)

        }


    }


    private fun speakNextTextPart() {
        if (spokenTextParts.isNotEmpty() && currentTextPartIndex < spokenTextParts.size) {
            val currentPart = spokenTextParts[currentTextPartIndex]
            textView.text = currentPart
            textView.visibility = View.VISIBLE

            val utteranceId = "part$currentTextPartIndex"
            tts.speak(currentPart, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        }
    }

    private fun speak(response: String) {

        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}


