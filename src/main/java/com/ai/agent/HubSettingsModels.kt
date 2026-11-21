package com.ai.agent

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

data class LangOption(val code: String, val name: String)

val HUB_LANGUAGES = listOf(
    LangOption("en", "English"),
    LangOption("vi", "Tiếng Việt"),
    LangOption("zh", "简体中文"),
    LangOption("zh-TW", "繁體中文"),
    LangOption("ja", "日本語"),
    LangOption("ko", "한국어"),
    LangOption("es", "Español"),
    LangOption("fr", "Français"),
    LangOption("de", "Deutsch"),
    LangOption("it", "Italiano"),
    LangOption("pt", "Português"),
    LangOption("ru", "Русский"),
    LangOption("uk", "Українська"),
    LangOption("pl", "Polski"),
    LangOption("nl", "Nederlands"),
    LangOption("sv", "Svenska"),
    LangOption("no", "Norsk"),
    LangOption("da", "Dansk"),
    LangOption("fi", "Suomi"),
    LangOption("cs", "Čeština"),
    LangOption("sk", "Slovenčina"),
    LangOption("hu", "Magyar"),
    LangOption("ro", "Română"),
    LangOption("bg", "Български"),
    LangOption("el", "Ελληνικά"),
    LangOption("tr", "Türkçe"),
    LangOption("ar", "العربية"),
    LangOption("he", "עברית"),
    LangOption("fa", "فارسی"),
    LangOption("ur", "اردو"),
    LangOption("hi", "हिन्दी"),
    LangOption("bn", "বাংলা"),
    LangOption("ta", "தமிழ்"),
    LangOption("te", "తెలుగు"),
    LangOption("th", "ไทย"),
    LangOption("id", "Bahasa Indonesia"),
    LangOption("ms", "Bahasa Melayu"),
    LangOption("fil", "Filipino"),
    LangOption("hr", "Hrvatski"),
    LangOption("sr", "Српски"),
    LangOption("sl", "Slovenščina"),
    LangOption("lt", "Lietuvių"),
    LangOption("lv", "Latviešu"),
    LangOption("et", "Eesti"),
    LangOption("ca", "Català"),
    LangOption("sw", "Kiswahili"),
    LangOption("af", "Afrikaans"),
    LangOption("mr", "मराठी")
)

data class HubThemePalette(
    val id: String,
    val label: String,
    val bg: Color,
    val side: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val userBubble: Color
)

val HUB_THEMES = listOf(
    HubThemePalette(
        id = "auto", label = "Auto",
        bg = Color(0xFF262624), side = Color(0xFF1F1E1D), card = Color(0xFF30302E),
        text = Color(0xFFEFECE3), muted = Color(0xFFA3A198), border = Color(0xFF3D3D3A),
        accent = Color(0xFFD97757), userBubble = Color(0xFF3A3936)
    ),
    HubThemePalette(
        id = "claude", label = "Claude",
        bg = Color(0xFFFAF9F5), side = Color(0xFFF0EEE6), card = Color(0xFFFFFFFF),
        text = Color(0xFF1F1E1D), muted = Color(0xFF73726C), border = Color(0xFFE4E1D6),
        accent = Color(0xFFC96442), userBubble = Color(0xFFEEE9DC)
    ),
    HubThemePalette(
        id = "light", label = "Light",
        bg = Color(0xFFFFFFFF), side = Color(0xFFF6F7F8), card = Color(0xFFFFFFFF),
        text = Color(0xFF0F172A), muted = Color(0xFF64748B), border = Color(0xFFE2E8F0),
        accent = Color(0xFF0F766E), userBubble = Color(0xFFF1F5F9)
    ),
    HubThemePalette(
        id = "dark", label = "Dark",
        bg = Color(0xFF262624), side = Color(0xFF1F1E1D), card = Color(0xFF30302E),
        text = Color(0xFFEFECE3), muted = Color(0xFFA3A198), border = Color(0xFF3D3D3A),
        accent = Color(0xFFD97757), userBubble = Color(0xFF3A3936)
    ),
    HubThemePalette(
        id = "midnight", label = "Midnight",
        bg = Color(0xFF0B1020), side = Color(0xFF080C18), card = Color(0xFF131A2E),
        text = Color(0xFFE6EBF7), muted = Color(0xFF8B97B5), border = Color(0xFF1F2A48),
        accent = Color(0xFF7F9BFF), userBubble = Color(0xFF18223D)
    ),
    HubThemePalette(
        id = "forest", label = "Forest",
        bg = Color(0xFF101A15), side = Color(0xFF0C1410), card = Color(0xFF17251D),
        text = Color(0xFFE4F0E8), muted = Color(0xFF8AA596), border = Color(0xFF223A2C),
        accent = Color(0xFF5FD68A), userBubble = Color(0xFF1C2F24)
    ),
    HubThemePalette(
        id = "ocean", label = "Ocean",
        bg = Color(0xFFF2F8FC), side = Color(0xFFE3EFF8), card = Color(0xFFFFFFFF),
        text = Color(0xFF0F2A3D), muted = Color(0xFF5B7A91), border = Color(0xFFCFE1EE),
        accent = Color(0xFF0284C7), userBubble = Color(0xFFDCECF7)
    ),
    HubThemePalette(
        id = "rose", label = "Rose",
        bg = Color(0xFFFFF8F8), side = Color(0xFFFDEEEE), card = Color(0xFFFFFFFF),
        text = Color(0xFF2B1618), muted = Color(0xFF8A6A6D), border = Color(0xFFF3D9DB),
        accent = Color(0xFFD61F4F), userBubble = Color(0xFFFDE4E6)
    )
)

fun getPaletteById(id: String): HubThemePalette =
    HUB_THEMES.firstOrNull { it.id == id } ?: HUB_THEMES.first()

data class HubClientSettings(
    val language: String = "en",
    val themeId: String = "auto",
    val animationLevel: String = "full",
    val showStartupAnimation: Boolean = true,
    val systemPrompt: String = ""
)

data class HubUsageStats(
    val sessionTokensLimit: Long = 0L,
    val sessionHours: Int = 5,
    val weeklyTokensLimit: Long = 0L,
    val autoResetOnChange: Boolean = true,
    val currentSessionTokens: Long = 0L,
    val weeklyTokensUsed: Long = 0L,
    val inputTokens: Long = 0L,
    val outputTokens: Long = 0L,
    val repliesCount: Int = 0
) {
    val totalTokens: Long get() = inputTokens + outputTokens
    val estimatedCostUsd: Double get() = (inputTokens * 0.15 + outputTokens * 0.60) / 1_000_000.0
}

data class HubAboutInfo(
    val appName: String = "Local GPT Hub",
    val versionName: String = "1.0.0",
    val sourceType: String = "Self-built Local & Cloud Client",
    val originInfo: String = "Built directly from local source code · Settings & chats stored locally on device",
    val originNote: String = "Android does not auto-delete an installed app just because it was installed outside Play Store; if you see warnings or permission resets, check Google Play Protect or 'Unused apps' settings.",
    val licenseName: String = "MIT License",
    val licenseText: String = """
        Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.
    """.trimIndent(),
    val githubUrl: String = "https://github.com/theleduylamofficialvngpro",
    val credits: List<Pair<String, String>> = listOf(
        "Creator & Developer" to "TheLeduylamOfficial-VNG",
        "UI & Motion" to "Jetpack Compose & Material 3",
        "AI Integrations" to "OpenAI, Anthropic, Gemini, Groq, OpenRouter, Ollama, LM Studio"
    )
)

enum class ErrorActionType {
    OPEN_SETTINGS,
    SWITCH_MODEL,
    RETRY
}

data class ChatErrorCardInfo(
    val codeLabel: String,
    val title: String,
    val tip: String,
    val actions: List<ErrorActionType>,
    val retryPrompt: String? = null
)

fun buildErrorCardFromMessage(rawError: String, retryPrompt: String? = null): ChatErrorCardInfo {
    val codeMatch = Regex("""\b(400|401|402|403|404|408|413|422|429|500|502|503|504|529)\b""").find(rawError)
    val code = codeMatch?.value
    return when (code) {
        "400" -> ChatErrorCardInfo(
            codeLabel = "400",
            title = "Yêu cầu không hợp lệ",
            tip = "Kiểm tra lại định dạng tin nhắn, tham số model hoặc Endpoint URL trong Cài đặt.",
            actions = listOf(ErrorActionType.OPEN_SETTINGS),
            retryPrompt = retryPrompt
        )
        "401" -> ChatErrorCardInfo(
            codeLabel = "401",
            title = "API key sai hoặc thiếu",
            tip = "Nhập đúng API Key của provider hiện tại rồi lưu lại trong Cài đặt.",
            actions = listOf(ErrorActionType.OPEN_SETTINGS),
            retryPrompt = retryPrompt
        )
        "402" -> ChatErrorCardInfo(
            codeLabel = "402",
            title = "Hết số dư, cần nạp tiền",
            tip = "Tài khoản provider đã hết credit/quota. Hãy nạp thêm hoặc đổi sang model/provider miễn phí.",
            actions = listOf(ErrorActionType.SWITCH_MODEL),
            retryPrompt = retryPrompt
        )
        "403" -> ChatErrorCardInfo(
            codeLabel = "403",
            title = "Bị từ chối truy cập",
            tip = "API Key không có quyền truy cập model này hoặc bị hạn chế vùng/quyền.",
            actions = listOf(ErrorActionType.OPEN_SETTINGS),
            retryPrompt = retryPrompt
        )
        "404" -> ChatErrorCardInfo(
            codeLabel = "404",
            title = "Không tìm thấy model hoặc endpoint",
            tip = "Kiểm tra lại Model ID và Endpoint URL xem có gõ sai hoặc model đã ngừng hỗ trợ không.",
            actions = listOf(ErrorActionType.OPEN_SETTINGS),
            retryPrompt = retryPrompt
        )
        "408" -> ChatErrorCardInfo(
            codeLabel = "408",
            title = "Quá thời gian",
            tip = "Kết nối tới máy chủ phản hồi quá lâu. Kiểm tra mạng và bấm Thử lại.",
            actions = listOf(ErrorActionType.RETRY),
            retryPrompt = retryPrompt
        )
        "413" -> ChatErrorCardInfo(
            codeLabel = "413",
            title = "Nội dung quá lớn",
            tip = "Tin nhắn hoặc ngữ cảnh vượt giới hạn kích thước của model. Hãy rút gọn nội dung gửi.",
            actions = emptyList(),
            retryPrompt = retryPrompt
        )
        "422" -> ChatErrorCardInfo(
            codeLabel = "422",
            title = "Dữ liệu không được hỗ trợ",
            tip = "Model hiện tại không hỗ trợ kiểu dữ liệu hoặc tham số này. Thử đổi model hoặc provider khác.",
            actions = listOf(ErrorActionType.SWITCH_MODEL),
            retryPrompt = retryPrompt
        )
        "429" -> ChatErrorCardInfo(
            codeLabel = "429",
            title = "Chạm giới hạn tốc độ",
            tip = "Bạn gửi quá nhanh hoặc vượt hạn mức RPM/TPM. Đợi một lát để thử lại hoặc đổi model/provider.",
            actions = listOf(ErrorActionType.RETRY, ErrorActionType.SWITCH_MODEL),
            retryPrompt = retryPrompt
        )
        "500" -> ChatErrorCardInfo(
            codeLabel = "500",
            title = "Lỗi máy chủ provider",
            tip = "Máy chủ phía nhà cung cấp gặp sự cố tạm thời. Hãy thử lại sau ít giây.",
            actions = listOf(ErrorActionType.RETRY),
            retryPrompt = retryPrompt
        )
        "502", "503", "504", "529" -> ChatErrorCardInfo(
            codeLabel = code,
            title = "Dịch vụ quá tải hoặc bảo trì",
            tip = "Máy chủ đang bận hoặc bảo trì tạm thời. Có thể bấm Thử lại hoặc đổi model/provider khác.",
            actions = listOf(ErrorActionType.RETRY, ErrorActionType.SWITCH_MODEL),
            retryPrompt = retryPrompt
        )
        else -> ChatErrorCardInfo(
            codeLabel = "ERR",
            title = "Lỗi khi AI phản hồi",
            tip = rawError.take(180).ifBlank { "Không thể kết nối hoặc nhận phản hồi từ AI." },
            actions = listOf(ErrorActionType.RETRY, ErrorActionType.OPEN_SETTINGS),
            retryPrompt = retryPrompt
        )
    }
}

class HubSettingsStorage(context: Context) {
    private val prefs = context.getSharedPreferences("lgh_hub_prefs", Context.MODE_PRIVATE)

    fun loadClient(): HubClientSettings = HubClientSettings(
        language = prefs.getString("lang", "en") ?: "en",
        themeId = prefs.getString("theme", "auto") ?: "auto",
        animationLevel = prefs.getString("fx", "full") ?: "full",
        showStartupAnimation = prefs.getBoolean("splash", true),
        systemPrompt = prefs.getString("system_prompt", "") ?: ""
    )

    fun saveClient(s: HubClientSettings) {
        prefs.edit()
            .putString("lang", s.language)
            .putString("theme", s.themeId)
            .putString("fx", s.animationLevel)
            .putBoolean("splash", s.showStartupAnimation)
            .putString("system_prompt", s.systemPrompt)
            .apply()
    }

    fun loadUsage(): HubUsageStats = HubUsageStats(
        sessionTokensLimit = prefs.getLong("lim_session", 0L),
        sessionHours = prefs.getInt("lim_hours", 5),
        weeklyTokensLimit = prefs.getLong("lim_weekly", 0L),
        autoResetOnChange = prefs.getBoolean("lim_auto", true),
        currentSessionTokens = prefs.getLong("cur_session", 0L),
        weeklyTokensUsed = prefs.getLong("cur_weekly", 0L),
        inputTokens = prefs.getLong("tok_in", 0L),
        outputTokens = prefs.getLong("tok_out", 0L),
        repliesCount = prefs.getInt("replies", 0)
    )

    fun saveUsage(u: HubUsageStats) {
        prefs.edit()
            .putLong("lim_session", u.sessionTokensLimit)
            .putInt("lim_hours", u.sessionHours)
            .putLong("lim_weekly", u.weeklyTokensLimit)
            .putBoolean("lim_auto", u.autoResetOnChange)
            .putLong("cur_session", u.currentSessionTokens)
            .putLong("cur_weekly", u.weeklyTokensUsed)
            .putLong("tok_in", u.inputTokens)
            .putLong("tok_out", u.outputTokens)
            .putInt("replies", u.repliesCount)
            .apply()
    }

    fun recordTurn(promptText: String, replyText: String) {
        val u = loadUsage()
        val inTok = (promptText.length / 4L).coerceAtLeast(1L)
        val outTok = (replyText.length / 4L).coerceAtLeast(1L)
        saveUsage(
            u.copy(
                currentSessionTokens = u.currentSessionTokens + inTok + outTok,
                weeklyTokensUsed = u.weeklyTokensUsed + inTok + outTok,
                inputTokens = u.inputTokens + inTok,
                outputTokens = u.outputTokens + outTok,
                repliesCount = u.repliesCount + 1
            )
        )
    }

    fun resetCounters() {
        val u = loadUsage()
        saveUsage(
            u.copy(
                currentSessionTokens = 0L,
                weeklyTokensUsed = 0L,
                inputTokens = 0L,
                outputTokens = 0L,
                repliesCount = 0
            )
        )
    }

    fun exportJson(aiConfig: AgentConfig, includeApiKeys: Boolean, messages: List<ChatMessage>): String {
        val client = loadClient()
        val usage = loadUsage()
        val root = JSONObject().apply {
            put("app", "local-gpt-hub")
            put("version", 1)
            put("lgh.ai.v1", JSONObject().apply {
                put("provider", aiConfig.provider)
                put("baseUrl", aiConfig.baseUrl)
                put("modelId", aiConfig.modelId)
                if (includeApiKeys) put("apiKey", aiConfig.apiKey)
            })
            put("lgh.client.v1", JSONObject().apply {
                put("language", client.language)
                put("theme", client.themeId)
                put("fx", client.animationLevel)
                put("splash", client.showStartupAnimation)
                put("systemPrompt", client.systemPrompt)
            })
            put("lgh.usage.v1", JSONObject().apply {
                put("inputTokens", usage.inputTokens)
                put("outputTokens", usage.outputTokens)
                put("replies", usage.repliesCount)
            })
            put("lgh.chats.v1", JSONArray().apply {
                messages.forEach { m ->
                    put(JSONObject().apply {
                        put("text", m.text)
                        put("isUser", m.isUser)
                    })
                }
            })
        }
        return root.toString(2)
    }

    fun importJson(rawJson: String, currentAi: AgentConfig): Triple<AgentConfig, HubClientSettings, Boolean> {
        return try {
            val root = JSONObject(rawJson)
            val aiObj = root.optJSONObject("lgh.ai.v1")
            val nextAi = if (aiObj != null) {
                currentAi.copy(
                    provider = aiObj.optString("provider", currentAi.provider),
                    baseUrl = aiObj.optString("baseUrl", currentAi.baseUrl),
                    modelId = aiObj.optString("modelId", currentAi.modelId),
                    apiKey = aiObj.optString("apiKey", currentAi.apiKey)
                )
            } else currentAi

            val clObj = root.optJSONObject("lgh.client.v1")
            val curCl = loadClient()
            val nextCl = if (clObj != null) {
                curCl.copy(
                    language = clObj.optString("language", "en"),
                    themeId = clObj.optString("theme", "auto"),
                    animationLevel = clObj.optString("fx", "full"),
                    showStartupAnimation = clObj.optBoolean("splash", true),
                    systemPrompt = clObj.optString("systemPrompt", "")
                )
            } else curCl

            saveClient(nextCl)
            Triple(nextAi, nextCl, true)
        } catch (_: Exception) {
            Triple(currentAi, loadClient(), false)
        }
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}

data class HubStrings(
    val newChat: String,
    val placeholder: String,
    val settings: String,
    val tabAI: String,
    val tabLanguage: String,
    val tabAppearance: String,
    val tabUsage: String,
    val tabData: String,
    val tabAbout: String,
    val save: String,
    val close: String
)

fun hubStrings(lang: String): HubStrings {
    return if (lang == "vi") {
        HubStrings(
            newChat = "Cuộc trò chuyện mới",
            placeholder = "Nhập tin nhắn…",
            settings = "Cài đặt",
            tabAI = "AI",
            tabLanguage = "Ngôn ngữ",
            tabAppearance = "Giao diện",
            tabUsage = "Sử dụng",
            tabData = "Dữ liệu",
            tabAbout = "Nguồn gốc, Giấy phép & Credit",
            save = "Lưu",
            close = "Đóng"
        )
    } else {
        HubStrings(
            newChat = "New chat",
            placeholder = "Type a message…",
            settings = "Settings",
            tabAI = "AI",
            tabLanguage = "Language",
            tabAppearance = "Appearance",
            tabUsage = "Usage",
            tabData = "Data",
            tabAbout = "Origin, License & Credit",
            save = "Save",
            close = "Close"
        )
    }
}