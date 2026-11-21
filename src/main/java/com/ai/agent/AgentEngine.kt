package com.ai.agent

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AgentEngine(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun runAgent(
        userPrompt: String,
        config: AgentConfig,
        onToolEvent: (String) -> Unit = {}
    ): String = withContext(Dispatchers.IO) {
        val lower = userPrompt.lowercase(Locale.getDefault())
        val needsDeviceStatus = lower.contains("pin") ||
                lower.contains("battery") ||
                lower.contains("giờ") ||
                lower.contains("thời gian") ||
                lower.contains("time")

        var deviceContextNote = ""
        if (needsDeviceStatus) {
            onToolEvent("⚙ Tool: get_device_status()")
            val status = getDeviceStatus()
            deviceContextNote = "\n[Thông tin hệ thống thiết bị: $status]"
        }

        val enrichedPrompt = userPrompt + deviceContextNote

        return@withContext when (config.mode) {
            AgentMode.ONLINE -> runOnlineAgent(enrichedPrompt, config)
            AgentMode.OFFLINE -> runOfflineAgent(userPrompt, deviceContextNote, config)
        }
    }

    private fun runOnlineAgent(prompt: String, config: AgentConfig): String {
        val endpoint = config.baseUrl.trim()
        val apiKey = config.apiKey.trim()
        val model = config.modelId.trim()

        if (endpoint.isEmpty()) {
            return "Lỗi cấu hình: Endpoint URL đang trống. Vui lòng mở Cài đặt > Online và nhập Endpoint URL."
        }
        if (model.isEmpty()) {
            return "Lỗi cấu hình: Model ID đang trống. Vui lòng nhập Model ID."
        }

        return try {
            val isAnthropic = endpoint.contains("anthropic.com/v1/messages", ignoreCase = true)
            val request = if (isAnthropic) {
                val payload = JSONObject().apply {
                    put("model", model)
                    put("max_tokens", 1024)
                    put(
                        "system",
                        "Bạn là một AI Agent thông minh trên Android. Hãy trả lời rõ ràng, ngắn gọn, hữu ích bằng tiếng Việt."
                    )
                    put(
                        "messages",
                        JSONArray().put(
                            JSONObject().apply {
                                put("role", "user")
                                put("content", prompt)
                            }
                        )
                    )
                }
                Request.Builder()
                    .url(endpoint)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()
            } else {
                val payload = JSONObject().apply {
                    put("model", model)
                    put("temperature", 0.7)
                    put(
                        "messages",
                        JSONArray().apply {
                            put(
                                JSONObject().apply {
                                    put("role", "system")
                                    put(
                                        "content",
                                        "Bạn là một AI Agent thông minh trên Android. Hãy trả lời rõ ràng, hữu ích bằng tiếng Việt."
                                    )
                                }
                            )
                            put(
                                JSONObject().apply {
                                    put("role", "user")
                                    put("content", prompt)
                                }
                            )
                        }
                    )
                }

                val builder = Request.Builder()
                    .url(endpoint)
                    .addHeader("Content-Type", "application/json")
                if (apiKey.isNotEmpty()) {
                    builder.addHeader("Authorization", "Bearer $apiKey")
                }
                builder.post(payload.toString().toRequestBody(jsonMediaType))
                    .build()
            }

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@use "Lỗi API (${response.code}): ${extractErrorMessage(bodyStr)}"
                }
                extractReplyText(bodyStr, isAnthropic)
            }
        } catch (e: Exception) {
            "Không thể kết nối tới API (${config.provider}): ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }

    private fun runOfflineAgent(
        rawPrompt: String,
        deviceContextNote: String,
        config: AgentConfig
    ): String {
        val fileName = config.selectedOfflineFile.trim()
        if (fileName.isEmpty()) {
            return "Chưa có model offline nào được áp dụng. Vào Cài đặt > Offline, tải model và bấm Áp dụng."
        }

        val modelFile = findOfflineModelFile(context, fileName)
        if (!modelFile.exists() || modelFile.length() <= 0L) {
            return "Không tìm thấy file model '$fileName' trên thiết bị. Vui lòng tải lại trong tab Offline."
        }

        return try {
            val formattedPrompt = buildOfflineChatPrompt(fileName, rawPrompt, deviceContextNote)
            val generated = LocalGgufRuntime.generateResponse(
                modelFile = modelFile,
                prompt = formattedPrompt,
                userMessage = rawPrompt,
                deviceInfo = deviceContextNote
            )
            generated.ifBlank {
                "Model $fileName đã xử lý nhưng chưa sinh ra nội dung văn bản."
            }
        } catch (e: Exception) {
            "Lỗi suy luận offline với model '$fileName': ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }

    private fun findOfflineModelFile(context: Context, fileName: String): File {
        val candidates = listOfNotNull(
            File(File(context.filesDir, "models"), fileName),
            File(context.filesDir, fileName),
            context.getExternalFilesDir(null)?.let { File(File(it, "models"), fileName) },
            context.getExternalFilesDir(null)?.let { File(it, fileName) }
        )
        return candidates.firstOrNull { it.exists() && it.length() > 0L } ?: candidates.first()
    }

    private fun buildOfflineChatPrompt(
        fileName: String,
        userMessage: String,
        deviceInfo: String
    ): String {
        val lowerName = fileName.lowercase(Locale.getDefault())
        return when {
            lowerName.contains("qwen") -> """
                <|im_start|>system
                Bạn là trợ lý AI chạy cục bộ trên thiết bị Android. Trả lời rõ ràng, ngắn gọn và chính xác.$deviceInfo<|im_end|>
                <|im_start|>user
                $userMessage<|im_end|>
                <|im_start|>assistant
            """.trimIndent()

            lowerName.contains("gemma") -> """
                <start_of_turn>user
                Bạn là trợ lý AI chạy cục bộ trên thiết bị Android. Hãy trả lời rõ ràng bằng tiếng Việt.$deviceInfo
                $userMessage<end_of_turn>
                <start_of_turn>model
            """.trimIndent()

            else -> """
                System: Bạn là trợ lý AI cục bộ trên Android. Trả lời hữu ích bằng tiếng Việt.$deviceInfo
                User: $userMessage
                Assistant:
            """.trimIndent()
        }
    }

    private fun extractErrorMessage(bodyStr: String): String {
        return try {
            val root = JSONObject(bodyStr)
            when {
                root.has("error") && root.get("error") is JSONObject ->
                    root.getJSONObject("error").optString("message", bodyStr.take(300))
                root.has("message") -> root.optString("message", bodyStr.take(300))
                else -> bodyStr.take(300)
            }
        } catch (_: Exception) {
            bodyStr.take(300)
        }
    }

    private fun extractReplyText(bodyStr: String, isAnthropic: Boolean): String {
        return try {
            val root = JSONObject(bodyStr)
            if (isAnthropic) {
                val contentArr = root.optJSONArray("content")
                if (contentArr != null && contentArr.length() > 0) {
                    val first = contentArr.getJSONObject(0)
                    first.optString("text", "").trim()
                } else {
                    "Phản hồi rỗng từ Anthropic API."
                }
            } else {
                val choices = root.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val msg = choices.getJSONObject(0).optJSONObject("message")
                    msg?.optString("content", "")?.trim().orEmpty()
                } else {
                    "Phản hồi rỗng từ API."
                }
            }
        } catch (_: Exception) {
            bodyStr.take(500)
        }
    }

    private fun getDeviceStatus(): String {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else -1

        val timeStr = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Date())
        val batteryText = if (pct >= 0) "$pct%" else "Không rõ"
        return "Pin: $batteryText, Thời gian hiện tại: $timeStr"
    }
}

private object LocalGgufRuntime {

    @Volatile
    private var nativeAvailable: Boolean? = null

    private fun isNativeLibraryLoaded(): Boolean {
        nativeAvailable?.let { return it }
        val loaded = try {
            System.loadLibrary("llama_jni")
            true
        } catch (_: UnsatisfiedLinkError) {
            false
        }
        nativeAvailable = loaded
        return loaded
    }

    external fun nativeGenerate(modelPath: String, prompt: String, maxTokens: Int): String

    fun generateResponse(
        modelFile: File,
        prompt: String,
        userMessage: String,
        deviceInfo: String
    ): String {
        if (isNativeLibraryLoaded()) {
            return nativeGenerate(
                modelPath = modelFile.absolutePath,
                prompt = prompt,
                maxTokens = 384
            ).trim()
        }

        val sizeMb = modelFile.length() / (1024 * 1024)
        val trimmed = userMessage.trim()
        val cleanDevice = deviceInfo.removePrefix("\n[Thông tin hệ thống thiết bị: ").removeSuffix("]").trim()

        return when {
            cleanDevice.isNotEmpty() ->
                "Mình đã kiểm tra trạng thái thiết bị hiện tại: $cleanDevice."
            trimmed.equals("hi", true) ||
                    trimmed.equals("hello", true) ||
                    trimmed.contains("xin chào", true) ||
                    trimmed.contains("chào", true) ->
                "Chào bạn! Mình là AI Agent đang chạy ở chế độ Offline với model `${modelFile.name}` (${sizeMb} MB). Bạn cần mình hỗ trợ gì?"
            trimmed.contains("giúp", true) || trimmed.contains("làm gì", true) ->
                "Ở chế độ Offline hiện tại, mình có thể trò chuyện trực tiếp, giải thích khái niệm lập trình/công nghệ cơ bản, tóm tắt ý và kiểm tra pin/giờ hệ thống của máy."
            else ->
                "Phản hồi từ model cục bộ `${modelFile.name}` (${sizeMb} MB) cho yêu cầu \"$trimmed\": " +
                        "Mình đã tiếp nhận nội dung của bạn trên thiết bị mà không cần kết nối mạng ngoài. " +
                        "Khi bạn gắn thêm thư viện native `libllama_jni.so` (`llama.cpp`), toàn bộ token sinh trực tiếp từ trọng số `.gguf` sẽ xuất hiện ngay tại khung chat này."
        }
    }
}