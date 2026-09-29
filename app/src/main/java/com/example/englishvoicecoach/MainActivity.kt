package com.example.englishvoicecoach

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishVoiceCoachApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishVoiceCoachApp() {
    val context = LocalContext.current
    var currentPrompt by remember {
        mutableStateOf(
            "Today we will practice a short English sentence. Please repeat: 'I am learning English every day.'"
        )
    }
    var spokenAnswer by remember { mutableStateOf("Tap the microphone to speak.") }
    var isListening by remember { mutableStateOf(false) }

    val tts = remember(context) { TextToSpeech(context) {} }

    LaunchedEffect(Unit) {
        tts.language = Locale.US
    }

    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenAnswer = matches?.firstOrNull() ?: "No speech detected."
            currentPrompt = "Nice try! Try saying: 'I am learning English every day.'"
        }
        isListening = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("English Voice Coach") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = currentPrompt,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    tts.speak(
                        currentPrompt,
                        TextToSpeech.QUEUE_FLUSH,
                        Bundle(),
                        "englishCoachPrompt"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play sentence")
            }

            Button(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the sentence aloud")
                    }

                    try {
                        speechRecognizerLauncher.launch(intent)
                        isListening = true
                    } catch (e: ActivityNotFoundException) {
                        spokenAnswer = "Speech recognition is not available on this device."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isListening) "Listening..." else "Speak now")
            }

            Text(
                text = "Your answer",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = spokenAnswer,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
