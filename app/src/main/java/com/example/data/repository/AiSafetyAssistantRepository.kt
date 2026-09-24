package com.example.data.repository

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, ASSISTANT
}

class AiSafetyAssistantRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Predefined safe, comprehensive academic answers for offline & guaranteed reliability
    private val predefinedKnowledge = mapOf(
        "what is driver fatigue" to """Driver fatigue (also known as driver drowsiness or sleepy driving) is a dangerous state of reduced mental and physical alertness caused by sleep deprivation, extended driving durations, circadian rhythm dips, or physical exhaustion.

In driving conditions, fatigue impairs reaction time, decreases cognitive awareness, causes lane drifting, and can lead to involuntary micro-sleep episodes (brief periods of sleep lasting 1 to 5 seconds) which frequently cause fatal highway collisions.""",

        "symptoms of fatigue" to """Key symptoms of driver fatigue include:
1. Difficulty keeping eyes open and heavy eyelids.
2. Frequent or prolonged blinking and slow eyelid reopening.
3. Involuntary, repetitive yawning.
4. Difficulty focusing, day-dreaming, or missing road exits.
5. Inability to remember the last few kilometers traveled.
6. Head nodding or drooping posture.
7. Lane drifting or erratic steering corrections.""",

        "why do closed eyes indicate fatigue" to """Closed eyes are the most direct biological indicator of sleep onset and reduced vigilance.

In computer vision systems:
• A normal voluntary blink lasts only 100 to 400 milliseconds (0.1 to 0.4 seconds).
• In contrast, when a driver is fatigued, involuntary eye closure lasts significantly longer (PERCLOS - Percentage of Eye Closure over time).
• If the Eye Aspect Ratio (EAR) remains below threshold for more than 1.0 to 1.5 seconds, the system classifies it as a micro-sleep event requiring an immediate emergency audible alert.""",

        "what is yawning detection" to """Yawning detection monitors mouth geometry to recognize drowsiness before micro-sleep occurs.

Our system computes the Mouth Aspect Ratio (MAR):
• MAR is computed by taking vertical lip landmarks divided by horizontal mouth width.
• During normal driving and regular speaking, MAR typically remains between 0.30 and 0.45.
• When yawning occurs, the jaw drops wide and MAR exceeds 0.65 for a prolonged duration (usually 2 to 4 seconds).
• Combining MAR with EAR creates a multi-modal fatigue detection pipeline that catches early exhaustion.""",

        "how does opencv help this project" to """OpenCV (Open Source Computer Vision) forms the foundational image processing pipeline of this project:
1. Video Capture: Streams live frames from the vehicle's cabin camera at 30 FPS.
2. Frame Preprocessing: Converts frames to grayscale, applies histogram equalization to normalize low-light cabin environments, and resizes frames.
3. Face & Eye ROI Detection: Employs Haar Feature-based Cascade Classifiers or DNN face detectors to isolate Region of Interest (ROI) bounding boxes for the driver's face, left eye, right eye, and mouth.
4. Landmark Extraction: Coordinates landmark geometry calculation for EAR and MAR feature extraction.""",

        "what is keras" to """Keras is a high-level deep learning API written in Python, running on top of TensorFlow.

In this driver fatigue project:
• Model Architecture: We designed a Convolutional Neural Network (CNN) based on MobileNetV2 architecture.
• High Efficiency: Optimized for low-latency real-time inference on edge and mobile hardware.
• Classification Task: The trained Keras model classifies cropped eye and mouth regions into distinct states (Eyes Open, Eyes Closed, Yawning) with high precision even with spectacles and varying illumination.""",

        "how does the fatigue detection model work" to """The Fatigue Detection Model operates in a 5-stage real-time pipeline:
1. Image Acquisition: Real-time front-facing camera frames are read sequentially.
2. Face & Landmark Localization: Face ROI and 68-point facial landmark coordinates (or 6-point eye vectors) are identified using OpenCV.
3. Feature Computation:
   • Eye Aspect Ratio (EAR) = (||p2-p6|| + ||p3-p5||) / (2 * ||p1-p4||)
   • Mouth Aspect Ratio (MAR) = (||m2-m8|| + ||m3-m7|| + ||m4-m6||) / (2 * ||m1-m5||)
4. Deep CNN Classification: Cropped feature matrices are passed through our trained TensorFlow/Keras neural network for confidence scoring.
5. Temporal Threshold Logic: If EAR stays below 0.22 for consecutive frames exceeding 1.5 seconds, or MAR exceeds 0.65 repeatedly, the system triggers the high-priority alarm.""",

        "what should a driver do when fatigue is detected" to """When a Fatigue Alert is triggered, the driver should follow these safety protocols:
1. Safely Pull Over: Immediately find a safe rest area, service station, or designated parking zone. Never try to 'push through' fatigue.
2. Take a Rest Break: Take a 15 to 20 minute power nap. Research proves short naps effectively restore alertness.
3. Hydrate & Oxygenate: Drink water, consume light refreshments, or have a cup of coffee (note that caffeine takes 20-30 minutes to absorb).
4. Physical Movement: Walk briskly for 5 minutes and stretch to stimulate circulation before driving again.
5. Switch Drivers: If traveling with passengers, let an alert, rested passenger take the wheel."""
    )

    suspend fun getResponse(userPrompt: String): String = withContext(Dispatchers.IO) {
        val trimmed = userPrompt.trim()
        val lower = trimmed.lowercase()

        // 1. Domain Relevance Guardrail
        val isRelevant = isDriverSafetyOrProjectRelated(lower)
        if (!isRelevant) {
            return@withContext "I am the Driver Safety AI Assistant, designed specifically to answer questions about driver fatigue, computer vision (OpenCV), deep learning (Keras/TensorFlow), and road safety telemetry for this capstone project. Please ask a question related to driver fatigue detection, symptoms, or system architecture!"
        }

        // 2. Try online Gemini API if configured
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResponse = callGeminiApi(apiKey, trimmed)
                if (apiResponse.isNotBlank()) {
                    return@withContext apiResponse
                }
            } catch (_: Exception) {
                // Graceful fallback to rich offline knowledge base
            }
        }

        // 3. Match against rich predefined educational knowledge base
        for ((key, answer) in predefinedKnowledge) {
            if (lower.contains(key) || key.split(" ").all { lower.contains(it) }) {
                return@withContext answer
            }
        }

        // Fallback for related queries with helpful technical summary
        return@withContext """Based on the Driver Fatigue Detection Project design:
Our system uses real-time computer vision (OpenCV) and deep convolutional neural networks (Keras/TensorFlow) to track driver vigilance. 

Key metrics tracked include:
• Eye Aspect Ratio (EAR) for micro-sleep & prolonged closure detection.
• Mouth Aspect Ratio (MAR) for automated yawn detection.
• Blink Frequency Analysis to recognize declining driver cognitive response.

When fatigue thresholds are breached, the system initiates an immediate multi-modal alert (visual HUD warning + emergency audio tone)."""
    }

    private fun isDriverSafetyOrProjectRelated(query: String): Boolean {
        val keywords = listOf(
            "fatigue", "drowsy", "drowsiness", "sleep", "eye", "mouth", "yawn", "alert",
            "opencv", "keras", "tensorflow", "model", "cnn", "ear", "mar", "blink",
            "driver", "safety", "camera", "accident", "perclos", "break", "rest", "project",
            "detection", "deep learning", "micro", "face", "vision", "threshold", "what is",
            "how", "symptoms", "ai", "hardware", "architecture", "college"
        )
        return keywords.any { query.contains(it) }
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply {
                    put("text", prompt)
                })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            val systemInstructionObj = JSONObject().apply {
                val parts = JSONArray().put(JSONObject().apply {
                    put("text", "You are an educational AI assistant for a College Final-Year Project titled 'Real-Time Driver Fatigue Detection'. Provide clear, technically accurate, concise answers on driver fatigue, OpenCV computer vision, Keras/TensorFlow deep learning models, EAR/MAR metric calculation, and road safety. If the user asks something unrelated to driver fatigue, traffic safety, or this computer vision project, politely explain that you are specialized strictly in driver fatigue detection and this project.")
                })
                put("parts", parts)
            }
            put("systemInstruction", systemInstructionObj)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            val raw = response.body?.string() ?: return ""
            val root = JSONObject(raw)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            return parts.getJSONObject(0).optString("text", "")
        }
    }
}
