package com.ai.agent

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

enum class AgentMode { ONLINE, OFFLINE }

data class ProviderPreset(
    val name: String,
    val defaultEndpoint: String,
    val defaultModel: String
)

val PROVIDER_PRESETS = listOf(
    ProviderPreset("OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini"),
    ProviderPreset("Anthropic (Claude)", "https://api.anthropic.com/v1/messages", "claude-sonnet-4-5"),
    ProviderPreset("Google Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", "gemini-2.5-flash"),
    ProviderPreset("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "openai/gpt-4o-mini"),
    ProviderPreset("Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile"),
    ProviderPreset("DeepSeek", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat"),
    ProviderPreset("Mistral AI", "https://api.mistral.ai/v1/chat/completions", "mistral-small-latest"),
    ProviderPreset("xAI (Grok)", "https://api.x.ai/v1/chat/completions", "grok-2-latest"),
    ProviderPreset("Together AI", "https://api.together.xyz/v1/chat/completions", "meta-llama/Llama-3.3-70B-Instruct-Turbo"),
    ProviderPreset("Ollama (local)", "http://10.0.2.2:11434/v1/chat/completions", "llama3.2"),
    ProviderPreset("LM Studio (local)", "http://10.0.2.2:1234/v1/chat/completions", ""),
    ProviderPreset("Custom (OpenAI-compatible)", "", "")
)

data class OfflineModelItem(
    val id: String,
    val name: String,
    val sizeLabel: String,
    val fileName: String,
    val downloadUrl: String
)

val OFFLINE_MODEL_CATALOG = listOf(
    OfflineModelItem(
        id = "gemma-3-270m-q8",
        name = "Gemma 3 270M Instruct (Q8_0 - Siêu nhẹ)",
        sizeLabel = "~292 MB",
        fileName = "gemma-3-270m-it-Q8_0.gguf",
        downloadUrl = "https://huggingface.co/ggml-org/gemma-3-270m-it-GGUF/resolve/main/gemma-3-270m-it-Q8_0.gguf"
    ),
    OfflineModelItem(
        id = "gemma-3-1b-q4",
        name = "Gemma 3 1B Instruct (Q4_0 - Cân bằng)",
        sizeLabel = "~800 MB",
        fileName = "gemma-3-1b-it-Q4_0.gguf",
        downloadUrl = "https://huggingface.co/unsloth/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-Q4_0.gguf"
    ),
    OfflineModelItem(
        id = "qwen-2.5-0.5b-q4km",
        name = "Qwen 2.5 0.5B Instruct (Q4_K_M - Nhanh)",
        sizeLabel = "~400 MB",
        fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
    ),
    OfflineModelItem(
        id = "qwen-2.5-1.5b-q2k",
        name = "Qwen 2.5 1.5B Instruct (Q2_K - Gọn nhẹ)",
        sizeLabel = "~650 MB",
        fileName = "qwen2.5-1.5b-instruct-q2_k.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q2_k.gguf"
    ),
    OfflineModelItem(
        id = "gpt-oss-20b-mxfp4",
        name = "GPT-OSS 20B MoE (MXFP4 - Rất nặng)",
        sizeLabel = "~13.8 GB",
        fileName = "gpt-oss-20b-GGUF-MXFP4.gguf",
        downloadUrl = "https://huggingface.co/axteam/gpt-oss-20b-GGUF/resolve/main/gpt-oss-20b-GGUF-MXFP4.gguf"
    )
)

data class AgentConfig(
    val mode: AgentMode = AgentMode.ONLINE,
    val provider: String = "Groq",
    val baseUrl: String = "https://api.groq.com/openai/v1/chat/completions",
    val apiKey: String = "",
    val modelId: String = "openai/gpt-oss-120b",
    val selectedOfflineFile: String = ""
)

class ConfigStorage(context: Context) {
    private val prefs = context.getSharedPreferences("ai_agent_prefs", Context.MODE_PRIVATE)

    fun load(): AgentConfig {
        val modeName = prefs.getString("mode", AgentMode.ONLINE.name) ?: AgentMode.ONLINE.name
        val mode = runCatching { AgentMode.valueOf(modeName) }.getOrDefault(AgentMode.ONLINE)
        return AgentConfig(
            mode = mode,
            provider = prefs.getString("provider", "Groq") ?: "Groq",
            baseUrl = prefs.getString("base_url", "https://api.groq.com/openai/v1/chat/completions")
                ?: "https://api.groq.com/openai/v1/chat/completions",
            apiKey = prefs.getString("api_key", "") ?: "",
            modelId = prefs.getString("model_id", "openai/gpt-oss-120b") ?: "openai/gpt-oss-120b",
            selectedOfflineFile = prefs.getString("offline_file", "") ?: ""
        )
    }

    fun save(config: AgentConfig) {
        prefs.edit {
            putString("mode", config.mode.name)
            putString("provider", config.provider)
            putString("base_url", config.baseUrl)
            putString("api_key", config.apiKey)
            putString("model_id", config.modelId)
            putString("offline_file", config.selectedOfflineFile)
        }
    }
}

class ModelDownloader(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    fun getModelsDir(): File {
        val dir = File(context.filesDir, "ai_models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLocalModelFile(fileName: String): File = File(getModelsDir(), fileName)

    fun isDownloaded(fileName: String): Boolean {
        val f = getLocalModelFile(fileName)
        return f.exists() && f.length() > 0L
    }

    fun deleteModel(fileName: String): Boolean {
        val f = getLocalModelFile(fileName)
        return f.exists() && f.delete()
    }

    suspend fun downloadModel(
        item: OfflineModelItem,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        if (item.downloadUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Chưa có link tải cho ${item.name}"))
        }

        val targetFile = getLocalModelFile(item.fileName)
        val tempFile = File(getModelsDir(), "${item.fileName}.part")

        try {
            val request = Request.Builder().url(item.downloadUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(RuntimeException("Lỗi HTTP ${response.code}"))
                }
                val body = response.body
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            if (totalBytes > 0L) {
                                withContext(Dispatchers.Main) {
                                    onProgress((downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f))
                                }
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            withContext(Dispatchers.Main) { onProgress(1f) }
            Result.success(targetFile)
        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(e)
        }
    }
}