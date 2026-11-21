package com.ai.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.ai.agent.ui.theme.AndroidAIAgentTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val isSystemTool: Boolean = false,
    val errorCard: ChatErrorCardInfo? = null
)

data class ChatSessionItem(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>
)

data class TopAlertBanner(
    val title: String,
    val message: String,
    val isError: Boolean = true,
    val showSettingsButton: Boolean = false
)

private data class HubLangEntry(
    val code: String,
    val name: String,
    val nativeName: String
)

private data class HubUiStrings(
    val appTitle: String,
    val newChat: String,
    val searchChats: String,
    val tokensUsed: String,
    val tokensUsedToggleTitle: String,
    val tokensUsedToggleOnDesc: String,
    val tokensUsedToggleOffDesc: String,
    val costLabel: String,
    val settings: String,
    val close: String,
    val save: String,
    val placeholder: String,
    val thinking: String,
    val stopAi: String,
    val send: String,
    val tabAI: String,
    val tabLanguage: String,
    val tabAppearance: String,
    val tabUsage: String,
    val tabData: String,
    val tabAbout: String,
    val welcomeMessage: String,
    val resetMessage: String,
    val menuAttach: String,
    val menuScreenshot: String,
    val menuProject: String,
    val menuGithub: String,
    val menuWebSearch: String,
    val retryLabel: String,
    val switchModelLabel: String,
    val syncOnTitle: String,
    val syncOnBody: String,
    val syncOffTitle: String,
    val syncOffBody: String,
    val syncedTitle: String,
    val syncedBody: String,
    val missingConfigTitle: String,
    val missingConfigBody: String,
    val stoppedText: String,
    val attachedTitle: String,
    val attachedBody: String,
    val permDeniedTitle: String,
    val permDeniedBody: String
)

private val HUB_48_LANGUAGES: List<HubLangEntry> = listOf(
    HubLangEntry("en", "English", "English"),
    HubLangEntry("vi", "Vietnamese", "Tiếng Việt"),
    HubLangEntry("ja", "Japanese", "日本語"),
    HubLangEntry("ko", "Korean", "한국어"),
    HubLangEntry("zh", "Chinese (Simplified)", "简体中文"),
    HubLangEntry("zt", "Chinese (Traditional)", "繁體中文"),
    HubLangEntry("es", "Spanish", "Español"),
    HubLangEntry("fr", "French", "Français"),
    HubLangEntry("de", "German", "Deutsch"),
    HubLangEntry("it", "Italian", "Italiano"),
    HubLangEntry("pt", "Portuguese", "Português"),
    HubLangEntry("ru", "Russian", "Русский"),
    HubLangEntry("ar", "Arabic", "العربية"),
    HubLangEntry("hi", "Hindi", "हिन्दी"),
    HubLangEntry("th", "Thai", "ไทย"),
    HubLangEntry("id", "Indonesian", "Bahasa Indonesia"),
    HubLangEntry("ms", "Malay", "Bahasa Melayu"),
    HubLangEntry("tl", "Filipino / Tagalog", "Tagalog"),
    HubLangEntry("tr", "Turkish", "Türkçe"),
    HubLangEntry("pl", "Polish", "Polski"),
    HubLangEntry("nl", "Dutch", "Nederlands"),
    HubLangEntry("sv", "Swedish", "Svenska"),
    HubLangEntry("no", "Norwegian", "Norsk"),
    HubLangEntry("da", "Danish", "Dansk"),
    HubLangEntry("fi", "Finnish", "Suomi"),
    HubLangEntry("cs", "Czech", "Čeština"),
    HubLangEntry("sk", "Slovak", "Slovenčina"),
    HubLangEntry("hu", "Hungarian", "Magyar"),
    HubLangEntry("ro", "Romanian", "Română"),
    HubLangEntry("bg", "Bulgarian", "Български"),
    HubLangEntry("el", "Greek", "Ελληνικά"),
    HubLangEntry("uk", "Ukrainian", "Українська"),
    HubLangEntry("he", "Hebrew", "עברית"),
    HubLangEntry("fa", "Persian", "فارسی"),
    HubLangEntry("bn", "Bengali", "বাংলা"),
    HubLangEntry("ta", "Tamil", "தமிழ்"),
    HubLangEntry("te", "Telugu", "తెలుగు"),
    HubLangEntry("mr", "Marathi", "मराठी"),
    HubLangEntry("ur", "Urdu", "اردو"),
    HubLangEntry("sw", "Swahili", "Kiswahili"),
    HubLangEntry("af", "Afrikaans", "Afrikaans"),
    HubLangEntry("ca", "Catalan", "Català"),
    HubLangEntry("hr", "Croatian", "Hrvatski"),
    HubLangEntry("sr", "Serbian", "Српски"),
    HubLangEntry("sl", "Slovenian", "Slovenščina"),
    HubLangEntry("et", "Estonian", "Eesti"),
    HubLangEntry("lv", "Latvian", "Latviešu"),
    HubLangEntry("lt", "Lithuanian", "Lietuvių")
)

private fun normalizeHubLang(raw: String?): String {
    val clean = raw?.trim()?.lowercase(Locale.ROOT).orEmpty()
    if (clean.isEmpty()) return "en"
    val base = clean.substringBefore('-').substringBefore('_')
    return if (HUB_48_LANGUAGES.any { it.code == base }) base else "en"
}

private fun lookupHubLang(code: String?): HubLangEntry {
    val norm = normalizeHubLang(code)
    return HUB_48_LANGUAGES.firstOrNull { it.code == norm } ?: HUB_48_LANGUAGES.first()
}

private fun buildHubStrings(langCode: String?): HubUiStrings {
    val norm = normalizeHubLang(langCode)
    val entry = lookupHubLang(norm)
    val label = "${entry.name} (${entry.code.uppercase(Locale.ROOT)})"
    val en = HubUiStrings(
        appTitle = "Local GPT Hub",
        newChat = "New chat",
        searchChats = "Search chats",
        tokensUsed = "Tokens used",
        tokensUsedToggleTitle = "Tokens used widget",
        tokensUsedToggleOnDesc = "Showing token counter, input/output bar, and Cost.",
        tokensUsedToggleOffDesc = "Hidden in sidebar and header.",
        costLabel = "Cost",
        settings = "Settings",
        close = "Close",
        save = "Save",
        placeholder = "Type a message...",
        thinking = "AI Thinking",
        stopAi = "Stop AI",
        send = "Send",
        tabAI = "AI",
        tabLanguage = "Language",
        tabAppearance = "Appearance",
        tabUsage = "Usage",
        tabData = "Data",
        tabAbout = "About",
        welcomeMessage = "How can I help you today? Tap Settings to configure AI providers, 48 languages, themes, usage limits, data, or origin/license/credit.",
        resetMessage = "All settings and chats have been reset.",
        menuAttach = "Add files or photos",
        menuScreenshot = "Take a screenshot",
        menuProject = "Add to project",
        menuGithub = "Add from GitHub",
        menuWebSearch = "Web search",
        retryLabel = "Retry",
        switchModelLabel = "Model / Provider",
        syncOnTitle = "AutoSync Lang: ON",
        syncOnBody = "Alerts, AI, and settings automatically sync to the active language.",
        syncOffTitle = "AutoSync Lang: OFF",
        syncOffBody = "Keeping selected language without auto-switching.",
        syncedTitle = "Language synced",
        syncedBody = "Alerts, AI, and settings synced to $label.",
        missingConfigTitle = "Missing AI config",
        missingConfigBody = "Please configure in Settings:",
        stoppedText = "Stopped AI response.",
        attachedTitle = "File attached",
        attachedBody = "Added file reference:",
        permDeniedTitle = "Allow File & Picture",
        permDeniedBody = "Enable File/Photo permission in App Settings to attach files or images."
    )

    return when (norm) {
        "en" -> en
        "vi" -> en.copy(
            newChat = "Đoạn chat mới",
            searchChats = "Tìm đoạn chat",
            tokensUsed = "Tokens used",
            tokensUsedToggleTitle = "Hiển thị thẻ Tokens used",
            tokensUsedToggleOnDesc = "Đang hiện bộ đếm token, thanh input/output và Cost.",
            tokensUsedToggleOffDesc = "Đã ẩn thẻ token ở thanh bên và đầu trang.",
            costLabel = "Cost",
            settings = "Cài đặt",
            close = "Đóng",
            save = "Lưu",
            placeholder = "Nhập tin nhắn của bạn...",
            thinking = "AI đang suy nghĩ",
            stopAi = "Dừng AI",
            send = "Gửi",
            tabAI = "AI",
            tabLanguage = "Ngôn ngữ",
            tabAppearance = "Giao diện",
            tabUsage = "Sử dụng",
            tabData = "Dữ liệu",
            tabAbout = "Giới thiệu",
            welcomeMessage = "Tôi có thể giúp gì cho bạn hôm nay? Nhấn Cài đặt để cấu hình AI, 48 ngôn ngữ, giao diện, giới hạn dùng, dữ liệu hoặc giới thiệu.",
            resetMessage = "Đã đặt lại toàn bộ cài đặt và đoạn chat.",
            menuAttach = "Thêm tệp hoặc ảnh",
            menuScreenshot = "Chụp màn hình",
            menuProject = "Thêm vào dự án",
            menuGithub = "Thêm từ GitHub",
            menuWebSearch = "Tìm kiếm web",
            retryLabel = "Thử lại",
            switchModelLabel = "Đổi model / nhà cung cấp",
            syncOnTitle = "AutoSync Lang: BẬT",
            syncOnBody = "Thông báo, AI và cài đặt tự động đồng bộ theo ngôn ngữ đang dùng.",
            syncOffTitle = "AutoSync Lang: TẮT",
            syncOffBody = "Giữ nguyên ngôn ngữ đã chọn, không tự đổi.",
            syncedTitle = "Đã đồng bộ ngôn ngữ",
            syncedBody = "Thông báo, AI và cài đặt đã đồng bộ theo $label.",
            missingConfigTitle = "Thiếu cấu hình AI",
            missingConfigBody = "Vui lòng cấu hình trong Cài đặt:",
            stoppedText = "Đã dừng phản hồi của AI.",
            attachedTitle = "Đã đính kèm tệp",
            attachedBody = "Đã thêm tệp:",
            permDeniedTitle = "Allow File & Picture",
            permDeniedBody = "Hãy cấp quyền File/Ảnh trong Cài đặt ứng dụng để đính kèm tệp hoặc ảnh."
        )
        "ja" -> en.copy(
            newChat = "新規チャット",
            searchChats = "チャットを検索",
            tokensUsed = "Tokens used",
            tokensUsedToggleTitle = "Tokens used ウィジェット",
            tokensUsedToggleOnDesc = "トークン数・入出力バー・Costを表示中。",
            tokensUsedToggleOffDesc = "サイドバーとヘッダーで非表示。",
            costLabel = "Cost",
            settings = "設定",
            close = "閉じる",
            save = "保存",
            placeholder = "メッセージを入力...",
            thinking = "AI思考中",
            stopAi = "停止",
            send = "送信",
            tabAI = "AI",
            tabLanguage = "言語",
            tabAppearance = "外観",
            tabUsage = "使用量",
            tabData = "データ",
            tabAbout = "情報",
            welcomeMessage = "今日はどのようなお手伝いができますか？設定からAI・48言語・テーマ・使用量・データ・情報を設定できます。",
            resetMessage = "すべての設定とチャットをリセットしました。",
            menuAttach = "ファイルや写真を追加",
            menuScreenshot = "スクリーンショット",
            menuProject = "プロジェクトに追加",
            menuGithub = "GitHubから追加",
            menuWebSearch = "ウェブ検索",
            retryLabel = "再試行",
            switchModelLabel = "モデル / プロバイダー変更",
            syncOnTitle = "AutoSync Lang: オン",
            syncOnBody = "通知・AI・設定をアクティブ言語に自動同期します。",
            syncOffTitle = "AutoSync Lang: オフ",
            syncOffBody = "選択中の言語を維持します。",
            syncedTitle = "言語を同期しました",
            syncedBody = "通知・AI・設定を $label に同期しました。",
            missingConfigTitle = "AI設定が不足しています",
            missingConfigBody = "設定で確認してください:",
            stoppedText = "AIの応答を停止しました。",
            attachedTitle = "ファイルを追加しました",
            attachedBody = "追加したファイル:",
            permDeniedTitle = "Allow File & Picture",
            permDeniedBody = "アプリ設定でファイル/写真へのアクセスを許可してください。"
        )
        "ko" -> en.copy(
            newChat = "새 채팅",
            searchChats = "채팅 검색",
            tokensUsed = "Tokens used",
            tokensUsedToggleTitle = "Tokens used 위젯",
            tokensUsedToggleOnDesc = "토큰 카운터, 입출력 막대 및 Cost 표시 중.",
            tokensUsedToggleOffDesc = "사이드바 및 상단에서 숨김.",
            costLabel = "Cost",
            settings = "설정",
            close = "닫기",
            save = "저장",
            placeholder = "메시지를 입력하세요...",
            thinking = "AI 생각 중",
            stopAi = "중지",
            send = "전송",
            tabAI = "AI",
            tabLanguage = "언어",
            tabAppearance = "화면",
            tabUsage = "사용량",
            tabData = "데이터",
            tabAbout = "정보",
            welcomeMessage = "오늘 어떤 도움이 필요하신가요? 설정에서 AI, 48개 언어, 테마, 사용량, 데이터, 정보를 설정할 수 있습니다.",
            resetMessage = "모든 설정과 대화가 초기화되었습니다.",
            menuAttach = "파일 또는 사진 추가",
            menuScreenshot = "스크린샷",
            menuProject = "프로젝트에 추가",
            menuGithub = "GitHub에서 추가",
            menuWebSearch = "웹 검색",
            retryLabel = "다시 시도",
            switchModelLabel = "모델 / 제공업체 변경",
            syncOnTitle = "AutoSync Lang: 켜짐",
            syncOnBody = "알림, AI, 설정이 활성 언어로 자동 동기화됩니다.",
            syncOffTitle = "AutoSync Lang: 꺼짐",
            syncOffBody = "선택한 언어를 유지합니다.",
            syncedTitle = "언어 동기화됨",
            syncedBody = "알림, AI 및 설정이 $label 으로 동기화되었습니다.",
            missingConfigTitle = "AI 설정 누락",
            missingConfigBody = "설정에서 확인하세요:",
            stoppedText = "AI 응답을 중지했습니다.",
            attachedTitle = "파일 첨부됨",
            attachedBody = "추가된 파일:",
            permDeniedTitle = "Allow File & Picture",
            permDeniedBody = "앱 설정에서 파일/사진 권한을 허용하세요."
        )
        "zh", "zt" -> en.copy(
            newChat = "新对话",
            searchChats = "搜索对话",
            tokensUsed = "Tokens used",
            tokensUsedToggleTitle = "Tokens used 组件",
            tokensUsedToggleOnDesc = "正在显示 Token 计数、输入/输出条与 Cost。",
            tokensUsedToggleOffDesc = "已在侧边栏和顶栏隐藏。",
            costLabel = "Cost",
            settings = "设置",
            close = "关闭",
            save = "保存",
            placeholder = "输入消息...",
            thinking = "AI 思考中",
            stopAi = "停止",
            send = "发送",
            tabAI = "AI",
            tabLanguage = "语言",
            tabAppearance = "外观",
            tabUsage = "用量",
            tabData = "数据",
            tabAbout = "关于",
            welcomeMessage = "今天有什么我可以帮您的？点击“设置”即可配置 AI、48 种语言、主题、用量、数据或关于信息。",
            resetMessage = "所有设置和聊天记录已重置。",
            menuAttach = "添加文件或图片",
            menuScreenshot = "屏幕截图",
            menuProject = "添加到项目",
            menuGithub = "从 GitHub 添加",
            menuWebSearch = "网页搜索",
            retryLabel = "重试",
            switchModelLabel = "切换模型 / 服务商",
            syncOnTitle = "AutoSync Lang：已开启",
            syncOnBody = "通知、AI 与设置将自动同步至当前语言。",
            syncOffTitle = "AutoSync Lang：已关闭",
            syncOffBody = "保持当前选定语言不变。",
            syncedTitle = "语言已同步",
            syncedBody = "通知、AI 与设置已同步为 $label。",
            missingConfigTitle = "缺少 AI 配置",
            missingConfigBody = "请在设置中填写：",
            stoppedText = "已停止 AI 回复。",
            attachedTitle = "已添加附件",
            attachedBody = "已添加文件：",
            permDeniedTitle = "Allow File & Picture",
            permDeniedBody = "请在应用设置中授予文件/图片权限。"
        )
        else -> en.copy(
            syncedBody = "Alerts, AI, and settings synced to $label."
        )
    }
}

private fun detectInputLang(text: String, fallbackLang: String): String {
    val clean = text.trim()
    val safeFallback = normalizeHubLang(fallbackLang)
    if (clean.isEmpty()) return safeFallback

    val hasVietnamese = Regex(
        "[àáảãạăằắẳẵặâầấẩẫậđèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵ]",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(clean)
    if (hasVietnamese) return "vi"

    if (Regex("[\\u3040-\\u309F\\u30A0-\\u30FF]").containsMatchIn(clean)) return "ja"
    if (Regex("[\\uAC00-\\uD7AF]").containsMatchIn(clean)) return "ko"
    if (Regex("[\\u4E00-\\u9FFF]").containsMatchIn(clean)) return "zh"
    if (Regex("[\\u0400-\\u04FF]").containsMatchIn(clean)) return "ru"
    if (Regex("[\\u0600-\\u06FF]").containsMatchIn(clean)) return "ar"

    val lowerWords = clean.lowercase(Locale.ROOT)
        .split(Regex("[^a-zA-Z]+"))
        .filter { it.isNotBlank() }
    val enHints = setOf(
        "hi", "hello", "hey", "how", "are", "you", "today", "what", "why", "when",
        "where", "who", "yes", "no", "ok", "okay", "thanks", "thank", "please",
        "help", "can", "could", "would", "should", "is", "am", "im", "i'm", "good",
        "fine", "translate", "explain", "code", "write", "fix", "make", "tell"
    )
    if (lowerWords.any { it in enHints } || Regex("^[a-zA-Z0-9\\s.,?!'\"()-]+$").matches(clean)) {
        return "en"
    }

    return safeFallback
}

private fun buildSyncedPrompt(
    userPrompt: String,
    systemPrompt: String,
    targetLang: String
): String {
    val code = normalizeHubLang(targetLang)
    val langName = lookupHubLang(code).name
    val rule = """
        [AutoSync Lang System Rule]
        - Active language: $langName ("$code").
        - Always respond naturally in $langName ("$code").
        - Example: "Hi how are you today?" -> "Yes, I'm good!".
    """.trimIndent()

    return if (systemPrompt.isNotBlank()) {
        "[System Instruction: ${systemPrompt.trim()}]\n$rule\n\nUser message:\n$userPrompt"
    } else {
        "$rule\n\nUser message:\n$userPrompt"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            enableEdgeToEdge()
        }
        setContent {
            AndroidAIAgentTheme(darkTheme = true, dynamicColor = false) {
                AgentScreen()
            }
        }
    }
}

@Composable
fun rememberSmoothPalette(target: HubThemePalette): HubThemePalette {
    val spec = tween<Color>(durationMillis = 380, easing = FastOutSlowInEasing)
    val bg by animateColorAsState(target.bg, spec, label = "bg")
    val side by animateColorAsState(target.side, spec, label = "side")
    val card by animateColorAsState(target.card, spec, label = "card")
    val text by animateColorAsState(target.text, spec, label = "text")
    val muted by animateColorAsState(target.muted, spec, label = "muted")
    val border by animateColorAsState(target.border, spec, label = "border")
    val accent by animateColorAsState(target.accent, spec, label = "accent")
    val userBubble by animateColorAsState(target.userBubble, spec, label = "userBubble")
    return HubThemePalette(
        id = target.id,
        label = target.label,
        bg = bg,
        side = side,
        card = card,
        text = text,
        muted = muted,
        border = border,
        accent = accent,
        userBubble = userBubble
    )
}

@Composable
fun LocalGptLoadingOverlay(
    visible: Boolean,
    palette: HubThemePalette,
    animationLevel: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)),
        exit = fadeOut(tween(320))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "localGptLogoLoader")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
        val ringRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ringRotation"
        )

        val effectiveScale = if (animationLevel == "off") 1f else pulseScale
        val effectiveRot = if (animationLevel == "full") ringRotation else 0f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (animationLevel != "off") {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .graphicsLayer { rotationZ = effectiveRot }
                                .border(
                                    width = 2.5.dp,
                                    color = palette.accent.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(effectiveScale)
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = "Local GPT",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Text(
                    text = "Local GPT Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )
                Text(
                    text = "Loading workspace…",
                    fontSize = 12.sp,
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
private fun AiThinkingBubble(
    palette: HubThemePalette,
    animationLevel: String,
    thinkingLabel: String,
    syncLangEnabled: Boolean,
    activeLangCode: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aiThinkingDots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val bubbleShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    val alphas = when (animationLevel) {
        "off" -> listOf(1f, 1f, 1f)
        "lite" -> listOf(0.8f, 0.8f, 0.8f)
        else -> listOf(dot1, dot2, dot3)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = bubbleShape,
            color = palette.card,
            border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.45f)),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (syncLangEnabled) "$thinkingLabel · Sync (${activeLangCode.uppercase(Locale.ROOT)})" else thinkingLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.text
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    alphas.forEach { a ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = a))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoStyleSidebar(
    palette: HubThemePalette,
    strings: HubUiStrings,
    sessions: List<ChatSessionItem>,
    activeSessionId: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    usageStats: HubUsageStats,
    showTokensUsedCard: Boolean,
    onOpenSettings: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US) }
    var costCollapsed by remember { mutableStateOf(false) }

    val auroraBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF432A24),
            Color(0xFF2B2220),
            palette.side
        )
    )

    val filteredSessions = remember(sessions, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) sessions else sessions.filter { it.title.lowercase().contains(q) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(auroraBrush)
            .border(BorderStroke(1.dp, palette.border.copy(alpha = 0.6f)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE07A5F)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = strings.appTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onCloseSidebar, modifier = Modifier.size(30.dp)) {
                Icon(
                    Icons.Rounded.MenuOpen,
                    contentDescription = strings.close,
                    tint = palette.muted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = palette.card.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onNewChat)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.text, modifier = Modifier.size(18.dp))
                Text(strings.newChat, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = palette.text)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            singleLine = true,
            placeholder = { Text(strings.searchChats, fontSize = 12.sp, color = palette.muted) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.muted, modifier = Modifier.size(18.dp))
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.border,
                focusedContainerColor = palette.card.copy(alpha = 0.7f),
                unfocusedContainerColor = palette.card.copy(alpha = 0.7f),
                focusedTextColor = palette.text,
                unfocusedTextColor = palette.text
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSessions, key = { it.id }) { item ->
                val selected = item.id == activeSessionId
                val itemBg by animateColorAsState(
                    targetValue = if (selected) palette.card.copy(alpha = 0.9f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "sessionBg"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(itemBg)
                        .clickable { onSelectSession(item.id) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) palette.text else palette.muted,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    if (sessions.size > 1) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = palette.muted.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDeleteSession(item.id) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showTokensUsedCard,
            enter = fadeIn(tween(220)) + expandVertically(),
            exit = fadeOut(tween(180)) + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF281A18).copy(alpha = 0.92f),
                border = BorderStroke(1.dp, Color(0xFFE07A5F).copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.tokensUsed, fontSize = 11.sp, color = palette.muted)
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = palette.muted, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = numFmt.format(usageStats.totalTokens),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.text
                    )
                    Text(
                        text = "tokens",
                        fontSize = 11.sp,
                        color = palette.muted
                    )

                    val totalTok = usageStats.totalTokens.coerceAtLeast(1L)
                    val inRatio = (usageStats.inputTokens.toFloat() / totalTok.toFloat()).coerceIn(0.15f, 0.85f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(palette.border),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(inRatio)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - inRatio).coerceAtLeast(0.15f))
                                .clip(CircleShape)
                                .background(Color(0xFFE07A5F))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "↑ ${numFmt.format(usageStats.inputTokens)} in",
                            fontSize = 10.sp,
                            color = Color(0xFF4ADE80)
                        )
                        Text(
                            text = "↓ ${numFmt.format(usageStats.outputTokens)} out",
                            fontSize = 10.sp,
                            color = Color(0xFFE07A5F)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.card.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { costCollapsed = !costCollapsed }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF5B93A).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF5B93A))
                                }
                                Text(strings.costLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = palette.text)
                            }
                            Text(
                                text = if (costCollapsed) "+" else "−",
                                fontSize = 14.sp,
                                color = palette.muted
                            )
                        }
                    }

                    AnimatedVisibility(visible = !costCollapsed) {
                        Text(
                            text = String.format(Locale.US, "$%.4f USD", usageStats.estimatedCostUsd),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF5B93A),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = palette.card.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, palette.border),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenSettings)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = palette.text, modifier = Modifier.size(18.dp))
                Text(strings.settings, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = palette.text)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen() {
    val context = LocalContext.current
    val storage = remember { ConfigStorage(context) }
    val hubStorage = remember { HubSettingsStorage(context) }
    val engine = remember { AgentEngine(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var config by remember { mutableStateOf(storage.load()) }
    var clientSettings by remember {
        val loaded = hubStorage.loadClient()
        mutableStateOf(
            if (loaded.language.isBlank()) loaded.copy(language = "en")
            else loaded.copy(language = normalizeHubLang(loaded.language))
        )
    }
    var usageStats by remember { mutableStateOf(hubStorage.loadUsage()) }
    var showTokensUsedCard by remember { mutableStateOf(true) }
    var autoSyncLang by remember { mutableStateOf(true) }
    var sidebarOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var activeGenerationJob by remember { mutableStateOf<Job?>(null) }

    var showStartupSplash by remember { mutableStateOf(clientSettings.showStartupAnimation) }
    LaunchedEffect(Unit) {
        if (showStartupSplash) {
            delay(900)
            showStartupSplash = false
        }
    }

    var alertBanner by remember { mutableStateOf<TopAlertBanner?>(null) }
    var missingConfigHighlight by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeTrigger % 2 == 1) 10f else 0f,
        animationSpec = spring(dampingRatio = 0.25f, stiffness = Spring.StiffnessHigh),
        label = "inputShake"
    )

    LaunchedEffect(alertBanner) {
        if (alertBanner != null) {
            delay(4500)
            alertBanner = null
        }
    }

    val activeLang = normalizeHubLang(clientSettings.language)
    val strings = remember(activeLang) { buildHubStrings(activeLang) }

    fun syncLanguageEverywhere(newLang: String, notifyBanner: Boolean = false) {
        val normalized = normalizeHubLang(newLang)
        if (normalized != normalizeHubLang(clientSettings.language)) {
            val updated = clientSettings.copy(language = normalized)
            hubStorage.saveClient(updated)
            clientSettings = updated
            if (notifyBanner) {
                val s = buildHubStrings(normalized)
                alertBanner = TopAlertBanner(
                    title = "${s.syncedTitle} (${normalized.uppercase(Locale.ROOT)})",
                    message = s.syncedBody,
                    isError = false
                )
            }
        }
    }

    var showPlusMenu by remember { mutableStateOf(false) }
    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
            val snippet = "[Attached file/image: $name]"
            input = if (input.isBlank()) snippet else "$input\n$snippet"
            val s = buildHubStrings(activeLang)
            alertBanner = TopAlertBanner(
                title = s.attachedTitle,
                message = "${s.attachedBody} $name",
                isError = false
            )
        }
    }

    val requiredMediaPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var pendingActionAfterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants.values.any { it }
        val nextAction = pendingActionAfterPermission
        pendingActionAfterPermission = null
        if (!granted) {
            val s = buildHubStrings(activeLang)
            alertBanner = TopAlertBanner(
                title = s.permDeniedTitle,
                message = s.permDeniedBody,
                isError = true,
                showSettingsButton = false
            )
        }
        nextAction?.invoke()
    }

    fun runWithMediaPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            action()
            return
        }
        val alreadyGranted = requiredMediaPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (alreadyGranted) {
            action()
        } else {
            pendingActionAfterPermission = action
            mediaPermissionLauncher.launch(requiredMediaPermissions)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val needAsk = requiredMediaPermissions.any { perm ->
                ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
            }
            if (needAsk) {
                mediaPermissionLauncher.launch(requiredMediaPermissions)
            }
        }
    }

    val rawPalette = remember(clientSettings.themeId) { getPaletteById(clientSettings.themeId) }
    val palette = rememberSmoothPalette(rawPalette)

    val sessions = remember {
        mutableStateListOf(
            ChatSessionItem(
                id = "chat_1",
                title = "New chat",
                messages = listOf(
                    ChatMessage(
                        text = buildHubStrings("en").welcomeMessage,
                        isUser = false
                    )
                )
            )
        )
    }
    var activeSessionId by remember { mutableStateOf("chat_1") }
    val activeSessionIndex = sessions.indexOfFirst { it.id == activeSessionId }.coerceAtLeast(0)
    val activeSession = sessions[activeSessionIndex]
    val messages = activeSession.messages

    fun updateActiveSessionMessages(mutator: (MutableList<ChatMessage>) -> Unit) {
        val idx = sessions.indexOfFirst { it.id == activeSessionId }.coerceAtLeast(0)
        val current = sessions[idx]
        val copyList = current.messages.toMutableList()
        mutator(copyList)
        val firstUserPrompt = copyList.firstOrNull { it.isUser }?.text?.trim()
        val updatedTitle = if (!firstUserPrompt.isNullOrBlank()) {
            firstUserPrompt.take(36)
        } else {
            current.title
        }
        sessions[idx] = current.copy(title = updatedTitle, messages = copyList)
    }

    fun createNewChatSession() {
        val nextId = "chat_${System.nanoTime()}"
        val s = buildHubStrings(activeLang)
        sessions.add(
            0,
            ChatSessionItem(
                id = nextId,
                title = s.newChat,
                messages = listOf(ChatMessage(text = s.welcomeMessage, isUser = false))
            )
        )
        activeSessionId = nextId
        sidebarOpen = false
    }

    fun triggerPromptExecution(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() || isThinking) return

        val targetLang = if (autoSyncLang) {
            val detected = detectInputLang(cleanPrompt, clientSettings.language)
            syncLanguageEverywhere(detected, notifyBanner = false)
            detected
        } else {
            normalizeHubLang(clientSettings.language)
        }
        val activeStrings = buildHubStrings(targetLang)

        val missingFields = mutableListOf<String>()
        val isLocalEndpoint = config.baseUrl.contains("127.0.0.1") ||
                config.baseUrl.contains("localhost") ||
                config.provider.contains("Ollama", true) ||
                config.provider.contains("LM Studio", true)
        if (config.apiKey.isBlank() && !isLocalEndpoint) {
            missingFields.add("API Key")
        }
        if (config.modelId.isBlank()) {
            missingFields.add("Model ID")
        }

        if (missingFields.isNotEmpty()) {
            missingConfigHighlight = true
            shakeTrigger += 1
            alertBanner = TopAlertBanner(
                title = activeStrings.missingConfigTitle,
                message = "${activeStrings.missingConfigBody} ${missingFields.joinToString(", ")} (${config.provider})",
                isError = true,
                showSettingsButton = true
            )
            return
        }

        missingConfigHighlight = false
        alertBanner = null
        updateActiveSessionMessages { it.add(ChatMessage(text = cleanPrompt, isUser = true)) }
        isThinking = true

        val job = scope.launch {
            try {
                listState.animateScrollToItem(sessions.firstOrNull { it.id == activeSessionId }?.messages?.size ?: 0)
                val effectivePrompt = if (autoSyncLang) {
                    buildSyncedPrompt(cleanPrompt, clientSettings.systemPrompt, targetLang)
                } else if (clientSettings.systemPrompt.isNotBlank()) {
                    "[System Instruction: ${clientSettings.systemPrompt.trim()}]\n\n$cleanPrompt"
                } else {
                    cleanPrompt
                }

                val reply = engine.runAgent(effectivePrompt, config) { toolLog ->
                    updateActiveSessionMessages { it.add(ChatMessage(text = toolLog, isUser = false, isSystemTool = true)) }
                }

                val looksLikeError = reply.startsWith("Error", ignoreCase = true) ||
                        reply.startsWith("Lỗi", ignoreCase = true) ||
                        Regex("""\b(400|401|402|403|404|408|413|422|429|500|502|503|504|529)\b""").containsMatchIn(reply)

                if (looksLikeError) {
                    val errInfo = buildErrorCardFromMessage(reply, retryPrompt = cleanPrompt)
                    updateActiveSessionMessages {
                        it.add(ChatMessage(text = reply, isUser = false, errorCard = errInfo))
                    }
                    alertBanner = TopAlertBanner(
                        title = "${errInfo.codeLabel}: ${errInfo.title}",
                        message = errInfo.tip,
                        isError = true,
                        showSettingsButton = errInfo.actions.contains(ErrorActionType.OPEN_SETTINGS)
                    )
                } else {
                    updateActiveSessionMessages { it.add(ChatMessage(text = reply, isUser = false)) }
                    hubStorage.recordTurn(effectivePrompt, reply)
                    usageStats = hubStorage.loadUsage()
                }
            } catch (_: CancellationException) {
                updateActiveSessionMessages {
                    it.add(
                        ChatMessage(
                            text = buildHubStrings(normalizeHubLang(clientSettings.language)).stoppedText,
                            isUser = false,
                            isSystemTool = true
                        )
                    )
                }
            } catch (e: Exception) {
                val errInfo = buildErrorCardFromMessage(e.localizedMessage ?: "Unknown error", retryPrompt = cleanPrompt)
                updateActiveSessionMessages {
                    it.add(ChatMessage(text = e.localizedMessage ?: "Unknown error", isUser = false, errorCard = errInfo))
                }
                alertBanner = TopAlertBanner(
                    title = "${errInfo.codeLabel}: ${errInfo.title}",
                    message = errInfo.tip,
                    isError = true,
                    showSettingsButton = errInfo.actions.contains(ErrorActionType.OPEN_SETTINGS)
                )
            } finally {
                isThinking = false
                activeGenerationJob = null
                val count = sessions.firstOrNull { it.id == activeSessionId }?.messages?.size ?: 1
                listState.animateScrollToItem((count - 1).coerceAtLeast(0))
            }
        }
        activeGenerationJob = job
    }

    val drawerWidth by animateDpAsState(
        targetValue = if (sidebarOpen) 286.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow),
        label = "drawerWidth"
    )
    val mainContentShift by animateDpAsState(
        targetValue = if (sidebarOpen) 286.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow),
        label = "mainContentShift"
    )

    Box(modifier = Modifier.fillMaxSize().background(palette.bg)) {
        if (drawerWidth > 0.dp) {
            VideoStyleSidebar(
                palette = palette,
                strings = strings,
                sessions = sessions,
                activeSessionId = activeSessionId,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onNewChat = { createNewChatSession() },
                onSelectSession = { id ->
                    activeSessionId = id
                    sidebarOpen = false
                },
                onDeleteSession = { id ->
                    if (sessions.size > 1) {
                        sessions.removeAll { it.id == id }
                        if (activeSessionId == id) {
                            activeSessionId = sessions.first().id
                        }
                    }
                },
                usageStats = usageStats,
                showTokensUsedCard = showTokensUsedCard,
                onOpenSettings = {
                    sidebarOpen = false
                    showSettings = true
                },
                onCloseSidebar = { sidebarOpen = false },
                modifier = Modifier.width(drawerWidth)
            )
        }

        Scaffold(
            containerColor = palette.bg,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = mainContentShift),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.side),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { sidebarOpen = !sidebarOpen },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Menu,
                                    contentDescription = strings.newChat,
                                    tint = palette.text,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (activeSession.title == strings.newChat) strings.appTitle else activeSession.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = palette.text
                                )
                                Text(
                                    text = if (isThinking) "${strings.thinking}..." else "${config.provider} · ${config.modelId.ifBlank { "—" }}",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isThinking) palette.accent else palette.muted
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (autoSyncLang) palette.accent.copy(alpha = 0.18f) else palette.card,
                            border = BorderStroke(1.dp, if (autoSyncLang) palette.accent else palette.border),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    autoSyncLang = !autoSyncLang
                                    if (autoSyncLang && input.isNotBlank()) {
                                        val synced = detectInputLang(input, clientSettings.language)
                                        syncLanguageEverywhere(synced, notifyBanner = false)
                                    }
                                    val s = buildHubStrings(normalizeHubLang(clientSettings.language))
                                    alertBanner = if (autoSyncLang) {
                                        TopAlertBanner(
                                            title = "${s.syncOnTitle} · ${activeLang.uppercase(Locale.ROOT)}",
                                            message = s.syncOnBody,
                                            isError = false
                                        )
                                    } else {
                                        TopAlertBanner(
                                            title = s.syncOffTitle,
                                            message = s.syncOffBody,
                                            isError = false
                                        )
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Sync,
                                    contentDescription = "AutoSync Lang",
                                    tint = if (autoSyncLang) palette.accent else palette.muted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (autoSyncLang) "Sync · ${activeLang.uppercase(Locale.ROOT)}" else "Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (autoSyncLang) palette.accent else palette.muted
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showTokensUsedCard && usageStats.totalTokens > 0L,
                            enter = fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = palette.card,
                                border = BorderStroke(1.dp, palette.border),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.US).format(usageStats.totalTokens)} tok",
                                    fontSize = 11.sp,
                                    color = palette.muted,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = strings.settings,
                                tint = palette.text
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.side)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    AnimatedVisibility(
                        visible = showPlusMenu,
                        enter = fadeIn(tween(160)) + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut(tween(140)) + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = palette.card,
                            border = BorderStroke(1.dp, palette.border),
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .fillMaxWidth(0.86f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                PlusMenuRow(
                                    icon = Icons.Rounded.AttachFile,
                                    label = strings.menuAttach,
                                    trailing = "Ctrl+U",
                                    palette = palette,
                                    onClick = {
                                        showPlusMenu = false
                                        runWithMediaPermission {
                                            attachmentLauncher.launch(arrayOf("*/*"))
                                        }
                                    }
                                )
                                PlusMenuRow(
                                    icon = Icons.Rounded.PhotoCamera,
                                    label = strings.menuScreenshot,
                                    palette = palette,
                                    onClick = {
                                        showPlusMenu = false
                                        runWithMediaPermission {
                                            val note = "[Screenshot requested by user]"
                                            input = if (input.isBlank()) note else "$input\n$note"
                                        }
                                    }
                                )
                                PlusMenuRow(
                                    icon = Icons.Rounded.Inventory2,
                                    label = strings.menuProject,
                                    palette = palette,
                                    onClick = {
                                        showPlusMenu = false
                                        val note = "[Context: Local GPT Hub Project]"
                                        input = if (input.isBlank()) note else "$input\n$note"
                                    }
                                )
                                PlusMenuRow(
                                    icon = Icons.Rounded.Code,
                                    label = strings.menuGithub,
                                    palette = palette,
                                    onClick = {
                                        showPlusMenu = false
                                        val ghRef = "[GitHub: https://github.com/theleduylamofficialvngpro]"
                                        input = if (input.isBlank()) ghRef else "$input\n$ghRef"
                                    }
                                )
                                HorizontalDivider(color = palette.border.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 4.dp))
                                PlusMenuRow(
                                    icon = Icons.Rounded.Public,
                                    label = strings.menuWebSearch,
                                    palette = palette,
                                    onClick = {
                                        showPlusMenu = false
                                        val prefix = if (input.isBlank()) "[Web search]: " else "[Web search] $input"
                                        input = prefix
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationX = shakeOffset }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val plusBtnColor = if (showPlusMenu) palette.accent else palette.card
                        FilledIconButton(
                            onClick = { showPlusMenu = !showPlusMenu },
                            modifier = Modifier
                                .size(44.dp)
                                .border(1.dp, if (showPlusMenu) palette.accent else palette.border, CircleShape),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = plusBtnColor,
                                contentColor = if (showPlusMenu) Color.White else palette.text
                            )
                        ) {
                            Icon(
                                imageVector = if (showPlusMenu) Icons.Rounded.Close else Icons.Rounded.Add,
                                contentDescription = strings.menuAttach,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            value = input,
                            onValueChange = {
                                input = it
                                if (missingConfigHighlight) missingConfigHighlight = false
                                if (autoSyncLang && it.isNotBlank()) {
                                    val synced = detectInputLang(it, clientSettings.language)
                                    syncLanguageEverywhere(synced, notifyBanner = false)
                                }
                            },
                            placeholder = { Text(strings.placeholder, color = palette.muted) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (missingConfigHighlight) Color(0xFFEF4444) else palette.accent,
                                unfocusedBorderColor = if (missingConfigHighlight) Color(0xFFEF4444) else palette.border,
                                focusedContainerColor = palette.card,
                                unfocusedContainerColor = palette.card,
                                focusedTextColor = palette.text,
                                unfocusedTextColor = palette.text
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        val actionInteraction = remember { MutableInteractionSource() }
                        val actionPressed by actionInteraction.collectIsPressedAsState()
                        val actionScale by animateFloatAsState(
                            targetValue = if (actionPressed) 0.88f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                            label = "actionScale"
                        )

                        if (isThinking) {
                            FilledIconButton(
                                onClick = {
                                    activeGenerationJob?.cancel()
                                    isThinking = false
                                },
                                interactionSource = actionInteraction,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(actionScale),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Icon(
                                    Icons.Rounded.Stop,
                                    contentDescription = strings.stopAi,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            FilledIconButton(
                                onClick = {
                                    val draft = input
                                    val canSend = draft.trim().isNotEmpty()
                                    triggerPromptExecution(draft)
                                    if (canSend && alertBanner == null) {
                                        input = ""
                                    }
                                },
                                interactionSource = actionInteraction,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(actionScale),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = palette.accent)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = strings.send, tint = Color.White)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AnimatedMessageBubble(
                        msg = msg,
                        palette = palette,
                        strings = strings,
                        onOpenSettings = { showSettings = true },
                        onSwitchModel = { showSettings = true },
                        onRetry = { prompt ->
                            input = ""
                            triggerPromptExecution(prompt)
                        }
                    )
                }

                if (isThinking) {
                    item(key = "ai_thinking_indicator") {
                        AiThinkingBubble(
                            palette = palette,
                            animationLevel = clientSettings.animationLevel,
                            thinkingLabel = strings.thinking,
                            syncLangEnabled = autoSyncLang,
                            activeLangCode = activeLang
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = alertBanner != null,
            enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(tween(180)) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 62.dp, start = 14.dp, end = 14.dp)
        ) {
            alertBanner?.let { banner ->
                val bgCol = if (banner.isError) Color(0xFF3A1618) else palette.card
                val borderCol = if (banner.isError) Color(0xFFEF4444) else palette.accent
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = bgCol,
                    border = BorderStroke(1.dp, borderCol),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (banner.isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (banner.isError) Color(0xFFF87171) else palette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                banner.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                banner.message,
                                fontSize = 12.sp,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                        if (banner.showSettingsButton) {
                            TextButton(
                                onClick = {
                                    alertBanner = null
                                    showSettings = true
                                }
                            ) {
                                Text(strings.settings, color = Color(0xFFFCA5A5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                        IconButton(
                            onClick = { alertBanner = null },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        LocalGptLoadingOverlay(
            visible = showStartupSplash,
            palette = palette,
            animationLevel = clientSettings.animationLevel
        )
    }

    if (showSettings) {
        HubSettingsDialog(
            aiConfig = config,
            clientSettings = clientSettings,
            usageStats = usageStats,
            showTokensUsedCard = showTokensUsedCard,
            onToggleTokensUsedCard = { showTokensUsedCard = it },
            messages = messages,
            hubStorage = hubStorage,
            onDismiss = { showSettings = false },
            onSaveAll = { newAi, newClient, newUsage ->
                val normalizedLang = normalizeHubLang(newClient.language)
                val syncedClient = newClient.copy(language = normalizedLang)
                storage.save(newAi)
                hubStorage.saveClient(syncedClient)
                hubStorage.saveUsage(newUsage)
                config = newAi
                clientSettings = syncedClient
                usageStats = newUsage
                showSettings = false
                if (autoSyncLang) {
                    val s = buildHubStrings(normalizedLang)
                    alertBanner = TopAlertBanner(
                        title = "${s.syncedTitle} (${normalizedLang.uppercase(Locale.ROOT)})",
                        message = s.syncedBody,
                        isError = false
                    )
                }
            },
            onResetAll = {
                hubStorage.resetAll()
                val defCfg = AgentConfig(
                    mode = AgentMode.ONLINE,
                    provider = "OpenAI",
                    baseUrl = "https://api.openai.com/v1/chat/completions",
                    apiKey = "",
                    modelId = "gpt-4o-mini",
                    selectedOfflineFile = ""
                )
                storage.save(defCfg)
                config = defCfg
                val defClient = HubClientSettings(language = "en")
                hubStorage.saveClient(defClient)
                clientSettings = defClient
                usageStats = HubUsageStats()
                showTokensUsedCard = true
                sessions.clear()
                sessions.add(
                    ChatSessionItem(
                        id = "chat_1",
                        title = "New chat",
                        messages = listOf(ChatMessage(text = buildHubStrings("en").resetMessage, isUser = false))
                    )
                )
                activeSessionId = "chat_1"
                showSettings = false
            }
        )
    }
}

@Composable
private fun PlusMenuRow(
    icon: ImageVector,
    label: String,
    palette: HubThemePalette,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = palette.muted, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = palette.text)
        }
        if (trailing != null) {
            Text(trailing, fontSize = 11.sp, color = palette.muted)
        }
    }
}

@Composable
private fun AnimatedMessageBubble(
    msg: ChatMessage,
    palette: HubThemePalette,
    strings: HubUiStrings,
    onOpenSettings: () -> Unit,
    onSwitchModel: () -> Unit,
    onRetry: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { 34 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.94f,
                    transformOrigin = if (msg.isUser) TransformOrigin(1f, 1f) else TransformOrigin(0f, 1f),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                )
    ) {
        val alignment = when {
            msg.isSystemTool -> Alignment.CenterHorizontally
            msg.isUser -> Alignment.End
            else -> Alignment.Start
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
            if (msg.errorCard != null) {
                val err = msg.errorCard
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2B1214),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444)
                            ) {
                                Text(
                                    err.codeLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                err.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            err.tip,
                            color = Color(0xFFE5E7EB),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        if (err.actions.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                err.actions.forEach { act ->
                                    when (act) {
                                        ErrorActionType.OPEN_SETTINGS -> {
                                            OutlinedButton(
                                                onClick = onOpenSettings,
                                                border = BorderStroke(1.dp, Color(0xFFF87171)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.settings, color = Color(0xFFFCA5A5), fontSize = 12.sp)
                                            }
                                        }
                                        ErrorActionType.SWITCH_MODEL -> {
                                            OutlinedButton(
                                                onClick = onSwitchModel,
                                                border = BorderStroke(1.dp, palette.accent),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.switchModelLabel, color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                        ErrorActionType.RETRY -> {
                                            val p = err.retryPrompt
                                            if (p != null) {
                                                Button(
                                                    onClick = { onRetry(p) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                ) {
                                                    Text(strings.retryLabel, color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (msg.isSystemTool) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.card,
                    border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = msg.text,
                        color = palette.accent,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                val bubbleShape = if (msg.isUser) {
                    RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                } else {
                    RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                }

                Box(
                    modifier = Modifier
                        .widthIn(max = 315.dp)
                        .clip(bubbleShape)
                        .background(if (msg.isUser) palette.userBubble else palette.card)
                        .border(1.dp, palette.border, bubbleShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg.text,
                        color = palette.text,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubSettingsDialog(
    aiConfig: AgentConfig,
    clientSettings: HubClientSettings,
    usageStats: HubUsageStats,
    showTokensUsedCard: Boolean,
    onToggleTokensUsedCard: (Boolean) -> Unit,
    messages: List<ChatMessage>,
    hubStorage: HubSettingsStorage,
    onDismiss: () -> Unit,
    onSaveAll: (AgentConfig, HubClientSettings, HubUsageStats) -> Unit,
    onResetAll: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 6 })
    val tabRowState = rememberLazyListState()

    LaunchedEffect(pagerState.currentPage) {
        tabRowState.animateScrollToItem(
            index = pagerState.currentPage,
            scrollOffset = -80
        )
    }

    var dialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { dialogVisible = true }

    fun dismissSmoothly() {
        scope.launch {
            dialogVisible = false
            delay(180)
            onDismiss()
        }
    }

    var provider by remember { mutableStateOf(aiConfig.provider) }
    var endpoint by remember { mutableStateOf(aiConfig.baseUrl) }
    var modelId by remember { mutableStateOf(aiConfig.modelId) }
    var apiKey by remember { mutableStateOf(aiConfig.apiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var systemPrompt by remember { mutableStateOf(clientSettings.systemPrompt) }

    var selectedLang by remember { mutableStateOf(normalizeHubLang(clientSettings.language)) }
    var langQuery by remember { mutableStateOf("") }

    var selectedThemeId by remember { mutableStateOf(clientSettings.themeId) }
    var animationLevel by remember { mutableStateOf(clientSettings.animationLevel) }
    var showStartupAnim by remember { mutableStateOf(clientSettings.showStartupAnimation) }
    val previewPalette = rememberSmoothPalette(getPaletteById(selectedThemeId))

    var sessionLimitStr by remember { mutableStateOf(usageStats.sessionTokensLimit.toString()) }
    var sessionHoursStr by remember { mutableStateOf(usageStats.sessionHours.toString()) }
    var weeklyLimitStr by remember { mutableStateOf(usageStats.weeklyTokensLimit.toString()) }
    var autoReset by remember { mutableStateOf(usageStats.autoResetOnChange) }
    var localUsage by remember { mutableStateOf(usageStats) }

    var includeKeysInExport by remember { mutableStateOf(false) }
    var dataStatus by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val draftAi = aiConfig.copy(
                    provider = provider.trim(),
                    baseUrl = endpoint.trim(),
                    modelId = modelId.trim(),
                    apiKey = apiKey.trim()
                )
                val json = hubStorage.exportJson(draftAi, includeKeysInExport, messages)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                dataStatus = "Exported successfully."
            } catch (e: Exception) {
                dataStatus = "Export failed: ${e.localizedMessage}"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                val (impAi, impCl, ok) = hubStorage.importJson(raw, aiConfig)
                if (ok) {
                    provider = impAi.provider
                    endpoint = impAi.baseUrl
                    modelId = impAi.modelId
                    apiKey = impAi.apiKey
                    selectedLang = normalizeHubLang(impCl.language)
                    selectedThemeId = impCl.themeId
                    animationLevel = impCl.animationLevel
                    showStartupAnim = impCl.showStartupAnimation
                    systemPrompt = impCl.systemPrompt
                    dataStatus = "Imported settings successfully."
                } else {
                    dataStatus = "Import failed: invalid JSON format."
                }
            } catch (e: Exception) {
                dataStatus = "Import failed: ${e.localizedMessage}"
            }
        }
    }

    val strings = remember(selectedLang) { buildHubStrings(selectedLang) }
    val numFmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    Dialog(
        onDismissRequest = { dismissSmoothly() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = dialogVisible,
                enter = fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = 0.88f,
                            transformOrigin = TransformOrigin(0.92f, 0.06f),
                            animationSpec = spring(
                                dampingRatio = 0.78f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) +
                        slideInVertically(
                            initialOffsetY = { -28 },
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                        ),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.92f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.88f),
                    shape = RoundedCornerShape(22.dp),
                    color = previewPalette.bg,
                    border = BorderStroke(1.dp, previewPalette.border),
                    shadowElevation = 24.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    strings.settings,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = previewPalette.text
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = previewPalette.card,
                                    border = BorderStroke(1.dp, previewPalette.border)
                                ) {
                                    Text(
                                        "${pagerState.currentPage + 1}/6",
                                        fontSize = 11.sp,
                                        color = previewPalette.muted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { dismissSmoothly() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = previewPalette.muted)
                            }
                        }
                        HorizontalDivider(color = previewPalette.border)

                        val navItems: List<Triple<String, ImageVector, Int>> = listOf(
                            Triple(strings.tabAI, Icons.Rounded.SmartToy, 0),
                            Triple(strings.tabLanguage, Icons.Rounded.Language, 1),
                            Triple(strings.tabAppearance, Icons.Rounded.Palette, 2),
                            Triple(strings.tabUsage, Icons.Rounded.BarChart, 3),
                            Triple(strings.tabData, Icons.Rounded.Storage, 4),
                            Triple(strings.tabAbout, Icons.Rounded.VerifiedUser, 5)
                        )

                        LazyRow(
                            state = tabRowState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(previewPalette.side),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(navItems) { _, (label, icon, idx) ->
                                val selected = pagerState.currentPage == idx
                                val pillBg by animateColorAsState(
                                    targetValue = if (selected) previewPalette.accent.copy(alpha = 0.20f) else Color.Transparent,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "pillBg"
                                )
                                val pillBorder by animateColorAsState(
                                    targetValue = if (selected) previewPalette.accent.copy(alpha = 0.5f) else Color.Transparent,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "pillBorder"
                                )
                                val pillScale by animateFloatAsState(
                                    targetValue = if (selected) 1.03f else 0.98f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                                    label = "pillScale"
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = pillBg,
                                    border = BorderStroke(1.dp, pillBorder),
                                    modifier = Modifier
                                        .scale(pillScale)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    page = idx,
                                                    animationSpec = spring(
                                                        dampingRatio = 0.82f,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = if (selected) previewPalette.accent else previewPalette.muted,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            label,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (selected) previewPalette.text else previewPalette.muted
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = previewPalette.border)

                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            val scaleEffect = 1f - (pageOffset.coerceIn(0f, 1f) * 0.045f)
                            val alphaEffect = 1f - (pageOffset.coerceIn(0f, 1f) * 0.28f)

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scaleEffect
                                        scaleY = scaleEffect
                                        alpha = alphaEffect
                                    }
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                when (page) {
                                    0 -> {
                                        Text("Provider", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        ExposedDropdownMenuBox(
                                            expanded = providerMenuExpanded,
                                            onExpandedChange = { providerMenuExpanded = it }
                                        ) {
                                            OutlinedTextField(
                                                value = provider,
                                                onValueChange = { provider = it },
                                                singleLine = true,
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                                                colors = hubFieldColors(previewPalette)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = providerMenuExpanded,
                                                onDismissRequest = { providerMenuExpanded = false }
                                            ) {
                                                PROVIDER_PRESETS.forEach { preset ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Column {
                                                                Text(preset.name, fontWeight = FontWeight.Medium)
                                                                if (preset.defaultModel.isNotBlank()) {
                                                                    Text(preset.defaultModel, fontSize = 11.sp, color = previewPalette.muted)
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            provider = preset.name
                                                            if (preset.defaultEndpoint.isNotBlank()) endpoint = preset.defaultEndpoint
                                                            if (preset.defaultModel.isNotBlank()) modelId = preset.defaultModel
                                                            providerMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Text("API Key", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        OutlinedTextField(
                                            value = apiKey,
                                            onValueChange = { apiKey = it },
                                            singleLine = true,
                                            placeholder = { Text("sk-...", color = previewPalette.muted) },
                                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                                    Icon(
                                                        if (showApiKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                        contentDescription = "Toggle key",
                                                        tint = previewPalette.muted
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )

                                        Text("Endpoint URL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        OutlinedTextField(
                                            value = endpoint,
                                            onValueChange = { endpoint = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )

                                        Text("Model ID", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        OutlinedTextField(
                                            value = modelId,
                                            onValueChange = { modelId = it },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )

                                        Text("System prompt", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        OutlinedTextField(
                                            value = systemPrompt,
                                            onValueChange = { systemPrompt = it },
                                            minLines = 3,
                                            maxLines = 5,
                                            placeholder = { Text("Optional instructions for the AI...", color = previewPalette.muted) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )
                                    }

                                    1 -> {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(strings.tabLanguage, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = previewPalette.accent.copy(alpha = 0.16f)
                                            ) {
                                                Text(
                                                    "${HUB_48_LANGUAGES.size} languages",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = previewPalette.accent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = langQuery,
                                            onValueChange = { langQuery = it },
                                            singleLine = true,
                                            placeholder = { Text("Search language...", color = previewPalette.muted) },
                                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = previewPalette.muted) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )

                                        val filteredLangs = remember(langQuery) {
                                            val q = langQuery.trim().lowercase()
                                            HUB_48_LANGUAGES.filter {
                                                q.isEmpty() ||
                                                        it.name.lowercase().contains(q) ||
                                                        it.nativeName.lowercase().contains(q) ||
                                                        it.code.lowercase().contains(q)
                                            }
                                        }

                                        filteredLangs.chunked(2).forEach { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                pair.forEach { lang ->
                                                    val isSel = normalizeHubLang(selectedLang) == lang.code
                                                    val cardScale by animateFloatAsState(
                                                        targetValue = if (isSel) 1.02f else 1f,
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                                        label = "langScale"
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = if (isSel) previewPalette.accent.copy(alpha = 0.16f) else previewPalette.card,
                                                        border = BorderStroke(
                                                            width = if (isSel) 1.5.dp else 1.dp,
                                                            color = if (isSel) previewPalette.accent else previewPalette.border
                                                        ),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .scale(cardScale)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .clickable { selectedLang = lang.code }
                                                    ) {
                                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                                                            Text(
                                                                lang.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = previewPalette.text,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                "${lang.nativeName} · ${lang.code}",
                                                                fontSize = 11.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                color = previewPalette.muted
                                                            )
                                                        }
                                                    }
                                                }
                                                if (pair.size == 1) {
                                                    Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }

                                    2 -> {
                                        Text(strings.tabAppearance, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        HUB_THEMES.chunked(2).forEach { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                pair.forEach { th ->
                                                    val isSel = selectedThemeId == th.id
                                                    val themeScale by animateFloatAsState(
                                                        targetValue = if (isSel) 1.03f else 1f,
                                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                                        label = "themeScale"
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(14.dp),
                                                        color = previewPalette.card,
                                                        border = BorderStroke(
                                                            width = if (isSel) 2.dp else 1.dp,
                                                            color = if (isSel) previewPalette.accent else previewPalette.border
                                                        ),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .scale(themeScale)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .clickable { selectedThemeId = th.id }
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(48.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(th.bg)
                                                                    .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxHeight()
                                                                        .fillMaxWidth(0.28f)
                                                                        .background(th.side)
                                                                )
                                                                Column(
                                                                    modifier = Modifier
                                                                        .fillMaxSize()
                                                                        .padding(horizontal = 8.dp),
                                                                    verticalArrangement = Arrangement.Center
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth(0.75f)
                                                                            .height(5.dp)
                                                                            .clip(CircleShape)
                                                                            .background(th.text.copy(alpha = 0.75f))
                                                                    )
                                                                    Spacer(Modifier.height(5.dp))
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth(0.45f)
                                                                            .height(5.dp)
                                                                            .clip(CircleShape)
                                                                            .background(th.accent)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.height(6.dp))
                                                            Text(
                                                                th.label,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = previewPalette.text
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(4.dp))
                                        Text("Animation level", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("full" to "Full", "lite" to "Lite", "off" to "Off").forEach { (key, label) ->
                                                val isSel = animationLevel == key
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSel) previewPalette.accent else previewPalette.card,
                                                    border = BorderStroke(1.dp, if (isSel) previewPalette.accent else previewPalette.border),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .clickable { animationLevel = key }
                                                ) {
                                                    Text(
                                                        label,
                                                        color = if (isSel) Color.White else previewPalette.text,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            "Full: aurora, particles, glow and parallax. Lite: simple transitions only. Off: no animation at all.",
                                            fontSize = 12.sp,
                                            color = previewPalette.muted
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = showStartupAnim,
                                                onCheckedChange = { showStartupAnim = it },
                                                colors = CheckboxDefaults.colors(checkedColor = previewPalette.accent)
                                            )
                                            Text(
                                                "Show startup animation",
                                                fontSize = 14.sp,
                                                color = previewPalette.text,
                                                modifier = Modifier.clickable { showStartupAnim = !showStartupAnim }
                                            )
                                        }
                                    }

                                    3 -> {
                                        Text(strings.tabUsage, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = previewPalette.text)

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = strings.tokensUsedToggleTitle,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = previewPalette.text
                                                    )
                                                    Text(
                                                        text = if (showTokensUsedCard) {
                                                            strings.tokensUsedToggleOnDesc
                                                        } else {
                                                            strings.tokensUsedToggleOffDesc
                                                        },
                                                        fontSize = 12.sp,
                                                        color = previewPalette.muted
                                                    )
                                                }
                                                Switch(
                                                    checked = showTokensUsedCard,
                                                    onCheckedChange = onToggleTokensUsedCard,
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = previewPalette.accent
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            "Set optional token budgets and see when they reset. If the AI reports a rate limit or asks for an upgrade, sending is paused with a countdown until you can try again.",
                                            fontSize = 12.sp,
                                            color = previewPalette.muted
                                        )

                                        val sesLimit = sessionLimitStr.toLongOrNull() ?: 0L
                                        val targetSesPct = if (sesLimit > 0L) (localUsage.currentSessionTokens.toFloat() / sesLimit).coerceIn(0f, 1f) else 0f
                                        val animatedSesPct by animateFloatAsState(
                                            targetValue = targetSesPct,
                                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                                            label = "sesPct"
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Current usage", fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                    Text("${numFmt.format(localUsage.currentSessionTokens)} tokens", fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                }
                                                LinearProgressIndicator(
                                                    progress = { animatedSesPct },
                                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                                    color = previewPalette.accent,
                                                    trackColor = previewPalette.border
                                                )
                                                Text(
                                                    if (localUsage.currentSessionTokens == 0L) "No usage yet" else "Session budget: ${if (sesLimit > 0L) numFmt.format(sesLimit) else "Unlimited"}",
                                                    fontSize = 12.sp,
                                                    color = previewPalette.muted
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Weekly limit", fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                    Text("${numFmt.format(localUsage.weeklyTokensUsed)} tokens", fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                }
                                                Text(
                                                    if (localUsage.weeklyTokensUsed == 0L) "No usage yet" else "Weekly cap: ${weeklyLimitStr.toLongOrNull()?.takeIf { it > 0L }?.let { numFmt.format(it) } ?: "Unlimited"}",
                                                    fontSize = 12.sp,
                                                    color = previewPalette.muted
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.accent.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                    Text("Need a higher limit?", fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                    Text("Top up or upgrade your account with this provider to get higher limits.", fontSize = 12.sp, color = previewPalette.muted)
                                                }
                                                Button(
                                                    onClick = {
                                                        val url = when {
                                                            provider.contains("OpenAI", true) -> "https://platform.openai.com/settings/organization/billing"
                                                            provider.contains("Anthropic", true) || provider.contains("Claude", true) -> "https://console.anthropic.com/settings/billing"
                                                            provider.contains("Gemini", true) -> "https://aistudio.google.com/"
                                                            provider.contains("Groq", true) -> "https://console.groq.com/settings/billing"
                                                            else -> "https://openrouter.ai/settings/credits"
                                                        }
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = previewPalette.accent)
                                                ) {
                                                    Text("Upgrade", color = Color.White)
                                                }
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = sessionLimitStr,
                                                onValueChange = { sessionLimitStr = it.filter(Char::isDigit) },
                                                label = { Text("Session limit (0 = off)", fontSize = 11.sp) },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = hubFieldColors(previewPalette)
                                            )
                                            OutlinedTextField(
                                                value = sessionHoursStr,
                                                onValueChange = { sessionHoursStr = it.filter(Char::isDigit) },
                                                label = { Text("Session length (hours)", fontSize = 11.sp) },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = hubFieldColors(previewPalette)
                                            )
                                        }

                                        OutlinedTextField(
                                            value = weeklyLimitStr,
                                            onValueChange = { weeklyLimitStr = it.filter(Char::isDigit) },
                                            label = { Text("Weekly limit (tokens, 0 = off)", fontSize = 12.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = hubFieldColors(previewPalette)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = autoReset,
                                                onCheckedChange = { autoReset = it },
                                                colors = CheckboxDefaults.colors(checkedColor = previewPalette.accent)
                                            )
                                            Text(
                                                "Auto-reset counters when the model or provider changes",
                                                fontSize = 13.sp,
                                                color = previewPalette.text,
                                                modifier = Modifier.clickable { autoReset = !autoReset }
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                hubStorage.resetCounters()
                                                localUsage = hubStorage.loadUsage()
                                            },
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Text("Reset counters now", color = previewPalette.text)
                                        }

                                        HorizontalDivider(color = previewPalette.border)
                                        Text("Total tokens", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = previewPalette.text)

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            StatBox(
                                                title = "Total tokens",
                                                value = numFmt.format(localUsage.totalTokens),
                                                highlight = true,
                                                palette = previewPalette,
                                                modifier = Modifier.weight(1f)
                                            )
                                            StatBox(
                                                title = "Input",
                                                value = numFmt.format(localUsage.inputTokens),
                                                highlight = false,
                                                palette = previewPalette,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            StatBox(
                                                title = "Output",
                                                value = numFmt.format(localUsage.outputTokens),
                                                highlight = false,
                                                palette = previewPalette,
                                                modifier = Modifier.weight(1f)
                                            )
                                            StatBox(
                                                title = "Replies",
                                                value = numFmt.format(localUsage.repliesCount),
                                                highlight = false,
                                                palette = previewPalette,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        val totalTok = localUsage.totalTokens.coerceAtLeast(1L)
                                        val inRatio = localUsage.inputTokens.toFloat() / totalTok.toFloat()
                                        val inPct = if (localUsage.totalTokens > 0L) (inRatio * 100).toInt() else 0
                                        val outPct = if (localUsage.totalTokens > 0L) 100 - inPct else 0

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(previewPalette.border)
                                        ) {
                                            if (inPct > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .weight(inPct.toFloat())
                                                        .background(previewPalette.accent.copy(alpha = 0.45f))
                                                )
                                            }
                                            if (outPct > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .weight(outPct.toFloat())
                                                        .background(previewPalette.accent)
                                                )
                                            }
                                        }
                                        Text(
                                            "Input $inPct%   •   Output $outPct%",
                                            fontSize = 12.sp,
                                            color = previewPalette.muted
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, Color(0xFFF5B93A).copy(alpha = 0.55f))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Estimated cost", fontSize = 12.sp, color = previewPalette.muted)
                                                    Text(
                                                        String.format(Locale.US, "$%.4f", localUsage.estimatedCostUsd),
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = previewPalette.text
                                                    )
                                                }
                                                Text("USD", fontWeight = FontWeight.SemiBold, color = Color(0xFFF5B93A))
                                            }
                                        }
                                    }

                                    4 -> {
                                        Text(
                                            "Stored in this device (SharedPreferences)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = previewPalette.text
                                        )

                                        val rows = listOf(
                                            Triple("lgh.ai.v1", "AI settings: provider, key, host, model", "${(provider.length + endpoint.length + modelId.length + apiKey.length + 48)} B"),
                                            Triple("lgh.client.v1", "Client settings: theme, language", "${(selectedLang.length + selectedThemeId.length + systemPrompt.length + 64)} B"),
                                            Triple("lgh.chats.v1", "Local data: chats (${messages.size})", "${messages.sumOf { it.text.length }} B"),
                                            Triple("lgh.projects.v1", "Projects (0)", "0 B")
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                rows.forEach { (key, desc, size) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                            Text(key, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = previewPalette.text)
                                                            Text(desc, fontSize = 12.sp, color = previewPalette.muted)
                                                        }
                                                        Text(size, fontSize = 12.sp, color = previewPalette.muted)
                                                    }
                                                    HorizontalDivider(color = previewPalette.border.copy(alpha = 0.5f))
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = previewPalette.accent.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, previewPalette.accent.copy(alpha = 0.45f))
                                        ) {
                                            Text(
                                                "API keys are saved locally on this device. Don't share exported files containing API keys.",
                                                fontSize = 12.sp,
                                                color = previewPalette.text,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = includeKeysInExport,
                                                onCheckedChange = { includeKeysInExport = it },
                                                colors = CheckboxDefaults.colors(checkedColor = previewPalette.accent)
                                            )
                                            Text(
                                                "Include API keys in export",
                                                fontSize = 13.sp,
                                                color = previewPalette.text,
                                                modifier = Modifier.clickable { includeKeysInExport = !includeKeysInExport }
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { exportLauncher.launch("local-gpt-hub-backup.json") },
                                                border = BorderStroke(1.dp, previewPalette.border)
                                            ) {
                                                Text("Export data", color = previewPalette.text, fontSize = 12.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                                                border = BorderStroke(1.dp, previewPalette.border)
                                            ) {
                                                Text("Import data", color = previewPalette.text, fontSize = 12.sp)
                                            }
                                            Spacer(Modifier.weight(1f))
                                            OutlinedButton(
                                                onClick = onResetAll,
                                                border = BorderStroke(1.dp, Color(0xFFF97066))
                                            ) {
                                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFF97066), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Reset all", color = Color(0xFFF97066), fontSize = 12.sp)
                                            }
                                        }

                                        if (dataStatus.isNotBlank()) {
                                            Text(dataStatus, fontSize = 12.sp, color = previewPalette.accent)
                                        }
                                    }

                                    5 -> {
                                        val about = remember { HubAboutInfo() }

                                        Text("Source & Origin", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = previewPalette.text)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    "${about.appName} · v${about.versionName}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = previewPalette.text
                                                )
                                                Text(
                                                    "Source: ${about.sourceType}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = previewPalette.accent
                                                )
                                                Text(
                                                    about.originInfo,
                                                    fontSize = 12.sp,
                                                    color = previewPalette.text
                                                )
                                                Text(
                                                    about.originNote,
                                                    fontSize = 12.sp,
                                                    color = previewPalette.muted
                                                )
                                            }
                                        }

                                        Text("License", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = previewPalette.text)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    about.licenseName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = previewPalette.accent
                                                )
                                                Text(
                                                    about.licenseText,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = previewPalette.muted
                                                )
                                            }
                                        }

                                        Text("Credits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = previewPalette.text)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = previewPalette.card,
                                            border = BorderStroke(1.dp, previewPalette.border)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                about.credits.forEachIndexed { idx, (role, owner) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            role,
                                                            fontSize = 12.sp,
                                                            color = previewPalette.muted,
                                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                                        )
                                                        Text(
                                                            owner,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = previewPalette.text
                                                        )
                                                    }
                                                    if (idx < about.credits.lastIndex) {
                                                        HorizontalDivider(color = previewPalette.border.copy(alpha = 0.5f))
                                                    }
                                                }

                                                HorizontalDivider(color = previewPalette.border.copy(alpha = 0.5f))

                                                Button(
                                                    onClick = {
                                                        context.startActivity(
                                                            Intent(Intent.ACTION_VIEW, Uri.parse(about.githubUrl))
                                                        )
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = previewPalette.accent)
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.Code,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        "GitHub · @theleduylamofficialvngpro",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = previewPalette.border)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { dismissSmoothly() }) {
                                Text(strings.close, color = previewPalette.muted)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val updatedAi = aiConfig.copy(
                                        mode = AgentMode.ONLINE,
                                        provider = provider.trim(),
                                        baseUrl = endpoint.trim(),
                                        apiKey = apiKey.trim(),
                                        modelId = modelId.trim()
                                    )
                                    val updatedClient = HubClientSettings(
                                        language = normalizeHubLang(selectedLang),
                                        themeId = selectedThemeId,
                                        animationLevel = animationLevel,
                                        showStartupAnimation = showStartupAnim,
                                        systemPrompt = systemPrompt.trim()
                                    )
                                    val providerChanged = updatedAi.provider != aiConfig.provider || updatedAi.modelId != aiConfig.modelId
                                    val baseUsage = if (autoReset && providerChanged) {
                                        localUsage.copy(
                                            currentSessionTokens = 0L,
                                            weeklyTokensUsed = 0L,
                                            inputTokens = 0L,
                                            outputTokens = 0L,
                                            repliesCount = 0
                                        )
                                    } else localUsage

                                    val updatedUsage = baseUsage.copy(
                                        sessionTokensLimit = sessionLimitStr.toLongOrNull() ?: 0L,
                                        sessionHours = (sessionHoursStr.toIntOrNull() ?: 5).coerceIn(1, 168),
                                        weeklyTokensLimit = weeklyLimitStr.toLongOrNull() ?: 0L,
                                        autoResetOnChange = autoReset
                                    )
                                    scope.launch {
                                        dialogVisible = false
                                        delay(160)
                                        onSaveAll(updatedAi, updatedClient, updatedUsage)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = previewPalette.accent)
                            ) {
                                Text(strings.save, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    highlight: Boolean,
    palette: HubThemePalette,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) palette.accent.copy(alpha = 0.14f) else palette.card,
        border = BorderStroke(1.dp, if (highlight) palette.accent.copy(alpha = 0.45f) else palette.border)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, color = palette.muted)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) palette.accent else palette.text
            )
        }
    }
}

@Composable
private fun hubFieldColors(palette: HubThemePalette) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = palette.accent,
    unfocusedBorderColor = palette.border,
    focusedContainerColor = palette.card,
    unfocusedContainerColor = palette.card,
    focusedTextColor = palette.text,
    unfocusedTextColor = palette.text
)