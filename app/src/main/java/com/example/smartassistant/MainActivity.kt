package com.example.smartassistant

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.format.DateUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView

import java.io.IOException

import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val speechrecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(
            this
        )
    }

    private val client = OkHttpClient()
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var soundwave:GifImageView


    private val allowPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            it?.let {
                if (it) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    lateinit var listenergif: View
    lateinit var questiontxt: TextView
    lateinit var answertxt: TextView
    lateinit var resultbtn: Button


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivitylayout2)
        listenergif = findViewById(R.id.listener)
        questiontxt = findViewById(R.id.question)
        answertxt = findViewById(R.id.answer)
        soundwave = findViewById(R.id.soundwave)
        resultbtn = findViewById(R.id.button)
        listenergif.setBackgroundResource(R.drawable.blackholelistener)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.getDefault()
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }


        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Do nothing on start
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "response") {
                    // Update visibility to INVISIBLE when speech is done
                    runOnUiThread {
                        soundwave.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                // Handle error if needed
            }
        })




        Toast.makeText(this, "long press on the blue button to speak", Toast.LENGTH_SHORT).show()

        listenergif.visibility = View.INVISIBLE

        resultbtn.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    speechrecognizer.stopListening()
                    listenergif.visibility = View.INVISIBLE
                    soundwave.visibility = View.VISIBLE
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_DOWN -> {
                    listenergif.visibility = View.VISIBLE
                    soundwave.visibility = View.INVISIBLE


                    val userCommand = questiontxt.text.toString().lowercase(Locale.getDefault())

                    getPermissionOverO(this) {
                        startlisten()


                        Toast.makeText(this, "Listening", Toast.LENGTH_SHORT).show()
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener true
                }
            }
        }
    }


    private fun checkDarkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // Dark mode is enabled, set the app to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                // Dark mode is not enabled, you can keep the app in light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                // The night mode is not defined, you can handle it accordingly
                // For example, set it to light mode by default
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    fun startlisten() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechrecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {}

            override fun onBeginningOfSpeech() {
                questiontxt.text = "listening"
            }

            override fun onRmsChanged(p0: Float) {}

            override fun onBufferReceived(p0: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(p0: Int) {}

            override fun onResults(bundle: Bundle?) {
                bundle?.let {
                    val result = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    questiontxt.setText(result?.get(0))
                    val userQuestion = result?.get(0)?.lowercase(Locale.getDefault())
                    if (userQuestion?.contains("open spotify") == true) {
                        openSpotify()
                    } else if (userQuestion?.contains("open instagram") == true) {
                        openInstagram()
                    } else if (userQuestion?.contains("open youtube") == true) {
                        openyoutube()
                    } else if (userQuestion?.contains("open linkedin") == true) {
                        openLinkedin()
                    } else if (userQuestion?.contains("open whatsapp") == true) {
                        openWhatsApp()
                    }else if (userQuestion?.contains("open clock") == true || userQuestion?.contains("open alarm clock") == true){
                        openApp("com.android.deskclock")
                    } else if (userQuestion?.contains("your name") == true) {
                        speakResponse("My name is autumn")
                        answertxt.text = "My name is autumn"
                    } else if (userQuestion?.contains("my name") == true) {
                        speakResponse("Your name is Nikhil")
                        answertxt.text = "your name is nikhil"
                    } else if (userQuestion?.contains("open snapchat") == true || userQuestion?.contains(
                            "open snap"
                        ) == true
                    ) {
                        opensnapchat()
                    } else if (userQuestion?.contains("what is the time right now") == true || userQuestion?.contains(
                            "what is the time "
                        ) == true || userQuestion?.contains("tell me the time") == true
                    ) {
                        val time = DateUtils.formatDateTime(
                            this@MainActivity,
                            System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME

                        )
                        answertxt.text = "the time now is $time"
                        speakResponse("the time now is $time")
                    } else if (userQuestion?.contains("open camera") == true) {
                        openApp("com.android.camera")

                    } else if (userQuestion?.startsWith("open notes") == true) {
                        openApp("com.miui.notes")
                    } else if (userQuestion != null)
                        getResponse(userQuestion) { response ->
                            runOnUiThread {
                                answertxt.text = response
                            }
                        }
                }
            }


            private fun openLinkedin() {
                val uri = Uri.parse("https://in.linkedin.com/")
                val intent = Intent(Intent.ACTION_VIEW, uri)

                intent.setPackage("com.linkedin.android")
                answertxt.text = "opening linkedin"
                speakResponse("opening linkedin")
            }

            private fun openInstagram() {

                val uri = Uri.parse("https://instagram.com/")
                val intent = Intent(Intent.ACTION_VIEW, uri)

                // Optionally specify a package name for better targeting:
                intent.setPackage("com.instagram.android")
                answertxt.text = "opening instagram"
                speakResponse("opening instagram")

                startActivity(intent)
            }

            private fun openSpotify() {
                val uri = Uri.parse("https://open.spotify.com/")
                val intent = Intent(Intent.ACTION_VIEW, uri)

                // Optionally specify a package name for better targeting:
                intent.setPackage("com.spotify.music")
                answertxt.text = "opening spotify"
                speakResponse("opening spotify")

                startActivity(intent)
            }

            private fun openyoutube() {
                val uri = Uri.parse("https://www.youtube.com")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                // Optionally specify a package name for better targeting:
                intent.setPackage("com.google.android.youtube")
                answertxt.text = "opening youtube"
                speakResponse("opening youtube")
                startActivity(intent)
            }

            private fun openApp(packageName: String) {
                val i: Intent? = packageManager.getLaunchIntentForPackage(packageName)
                if (i != null) {
                    try {
                        startActivity(i)
                    } catch (e: ActivityNotFoundException) {
                        speakResponse("The app could not be opened. Make sure it's installed on your device.")
                    }
                } else {
                    speakResponse("The app is not installed.")
                }
            }

            @SuppressLint("SetTextI18n")
            private fun opensnapchat() {
                val uri = Uri.parse("https://www.snapchat.com/")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                // Optionally specify a package name for better targeting:
                intent.setPackage("com.snapchat.android")
                answertxt.text = "opening youtube"
                speakResponse("opening snapchat")
                startActivity(intent)
            }


            @SuppressLint("SetTextI18n")
            private fun openWhatsApp() {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.`package` = "com.whatsapp"

                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // WhatsApp not installed or no activity can handle the send action
                    answertxt.text = "WhatsApp is not installed on your device."
                    speakResponse("WhatsApp is not installed on your device.")
                }
            }


            override fun onPartialResults(p0: Bundle?) {}

            override fun onEvent(p0: Int, p1: Bundle?) {}
        })
        speechrecognizer.startListening(intent)
    }


    private fun getResponse(question: String, callback: (String) -> Unit) {
        val url = "https://api.openai.com/v1/chat/completions"
        val apiKey = "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"

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", getMessages(question))
            put("max_tokens", 2000)
            put("temperature", 0)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()


        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("error", "API failed", e)
                    callback("Error processing response")
                }


                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()

                    if (body != null) {
                        Log.v("data", body)

                        try {
                            val jsonObject = JSONObject(body)
                            if (jsonObject.has("choices")) {
                                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                                val textResult = jsonArray.getJSONObject(0).getJSONObject("message")
                                    .getString("content")
                                speakResponse(textResult)
                                callback(textResult)
                                runOnUiThread {
                                    soundwave.visibility = View.VISIBLE

                                }
                            } else {
                                Log.e("error", "No 'choices' field in the JSON response")
                                callback("Error processing response")
                            }
                        } catch (e: JSONException) {
                            Log.e("error", "JSON parsing error", e)
                            callback("Error processing response")
                        }
                    } else {
                        Log.v("data", "empty")
                        callback("Error processing response")
                    }
                }
            })
        }
    }


    private fun speakResponse(response: String) {
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "response"

        textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, params)
    }



    private fun getMessages(userInput: String): JSONArray {
        val messages = JSONArray()

        // Add new system message
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", "You are a helpful assistant.")
        })

        // Add new user message
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userInput)
        })

        return messages
    }

    fun getPermissionOverO(context: Context, call: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                call.invoke()
            } else {
                allowPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}


