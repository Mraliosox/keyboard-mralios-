package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ColorLens
import com.example.ime.theme.KeyboardTheme
import android.graphics.Color as AndroidColor
import androidx.compose.material3.AlertDialog
import com.example.data.ShortcutEntity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.PreferencesManager
import com.example.engine.SoundManager
import com.example.ui.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    KeyboardSettingsScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardSettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkKeyboardStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isEnabled by viewModel.isKeyboardEnabled.collectAsState()
    val isSelected by viewModel.isKeyboardSelected.collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()
    val clipboardItems by viewModel.clipboardItems.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var testInputText by remember { mutableStateOf("") }
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var editingShortcut by remember { mutableStateOf<ShortcutEntity?>(null) }
    var showThemeStudioDialog by remember { mutableStateOf(false) }

    val prefs = viewModel.prefs
    var themePref by remember { mutableStateOf(prefs.theme) }
    var heightPref by remember { mutableStateOf(prefs.keyboardHeight) }
    var showNumberRow by remember { mutableStateOf(prefs.showNumberRow) }
    var showKeyPopup by remember { mutableStateOf(prefs.showKeyPopup) }
    var showSuggestions by remember { mutableStateOf(prefs.showSuggestions) }
    var enableNextWord by remember { mutableStateOf(prefs.enableNextWordPrediction) }
    var enableSwipe by remember { mutableStateOf(prefs.enableSwipeTyping) }
    var doubleSpacePeriod by remember { mutableStateOf(prefs.doubleSpacePeriod) }
    var autoCap by remember { mutableStateOf(prefs.autoCapitalization) }
    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var soundProfile by remember { mutableStateOf(prefs.soundProfile) }
    var soundVolume by remember { mutableFloatStateOf(prefs.soundVolume.toFloat()) }
    var customSoundUri by remember { mutableStateOf(prefs.customSoundUri) }
    var vibrationEnabled by remember { mutableStateOf(prefs.vibrationEnabled) }
    var vibrationStrength by remember { mutableFloatStateOf(prefs.vibrationStrength.toFloat()) }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriStr = uri.toString()
            customSoundUri = uriStr
            prefs.customSoundUri = uriStr
            soundProfile = SoundManager.PROFILE_CUSTOM
            prefs.soundProfile = SoundManager.PROFILE_CUSTOM
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mralios Keyboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "v1.2 • مزيج تصميم iPhone ووظائف Samsung",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = { showAddShortcutDialog = true },
                    modifier = Modifier.testTag("fab_add_shortcut"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة اختصار جديد")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Setup & Activation Header Card
            ActivationWizardCard(
                isEnabled = isEnabled,
                isSelected = isSelected,
                onEnableClick = {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                },
                onSelectClick = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            )

            // Live Interactive Test Area (with auto keyboard pop-up)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "جرب لوحة المفاتيح مباشرة هنا:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = testInputText,
                        onValueChange = { testInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("test_input_field"),
                        placeholder = { Text("اضغط هنا لتجربة الكتابة، السحب، وفقاعات الحروف...") },
                        trailingIcon = {
                            if (testInputText.isNotEmpty()) {
                                IconButton(onClick = { testInputText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح النص")
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }

            // Categories Tabs (Fixed RTL layout, concise labels & edgePadding=8dp to prevent any clipping)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("المظهر", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الصوت والاهتزاز", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("الذكاء والكتابة", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("الاختصارات (${shortcuts.size})", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("الحافظة (${clipboardItems.size})", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Custom Theme Studio Card Banner
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showThemeStudioDialog = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🎨 صانع الثيمات المخصصة (Theme Studio)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "صمم كيبورد خاص بك واضبط ألوان الأزرار والخلفية مع معاينة حية فورية!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { showThemeStudioDialog = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("تصميم ✦", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Appearance Settings
                        item {
                            SettingCard(title = "سمة لوحة المفاتيح (Theme)") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThemeOptionItem("تلقائي (يتبع وضع النظام الداكن/الفاتح)", PreferencesManager.THEME_AUTO, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("🎨 ثيمي المخصص (Custom Created Theme)", PreferencesManager.THEME_CUSTOM, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("Cyberpunk نيون (Cyan & Pink)", PreferencesManager.THEME_CYBERPUNK, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("Emerald فخم (زمردي وذهبي)", PreferencesManager.THEME_EMERALD, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("Sunset غروب بنفسجي (Violet & Coral)", PreferencesManager.THEME_SUNSET, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("OLED أسود نقي (Pure AMOLED)", PreferencesManager.THEME_OLED, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("iPhone الفاتح (iOS Light)", PreferencesManager.THEME_IOS_LIGHT, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("iPhone الداكن (iOS Dark)", PreferencesManager.THEME_IOS_DARK, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("Samsung الفاتح (Samsung Light)", PreferencesManager.THEME_SAMSUNG_LIGHT, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                    ThemeOptionItem("Samsung الداكن (Samsung Dark)", PreferencesManager.THEME_SAMSUNG_DARK, themePref) {
                                        themePref = it
                                        prefs.theme = it
                                    }
                                }
                            }
                        }

                        // APK Package Info Card
                        item {
                            SettingCard(title = "حزمة التطبيق (APK File & Installation)") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "✓ تم بناء حزمة الـ APK بنجاح وبأحدث إصدار v1.2:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "app/build/outputs/apk/debug/app-debug.apk",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                    Text(
                                        text = "يمكنك تحميل وتصدير ملف الـ APK ومجلد المشروع كاملاً مباشرة من قائمة خيارات AI Studio في الأعلى عبر (Download APK أو Export Project).",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            SettingCard(title = "ارتفاع لوحة المفاتيح التكيفي") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HeightOptionButton("منخفض", PreferencesManager.HEIGHT_COMPACT, heightPref, Modifier.weight(1f)) {
                                        heightPref = it
                                        prefs.keyboardHeight = it
                                    }
                                    HeightOptionButton("عادي (متوازن)", PreferencesManager.HEIGHT_NORMAL, heightPref, Modifier.weight(1f)) {
                                        heightPref = it
                                        prefs.keyboardHeight = it
                                    }
                                    HeightOptionButton("مرتفع", PreferencesManager.HEIGHT_TALL, heightPref, Modifier.weight(1f)) {
                                        heightPref = it
                                        prefs.keyboardHeight = it
                                    }
                                }
                            }
                        }

                        item {
                            SettingCard(title = "تخصيص المفاتيح") {
                                SettingSwitchItem(
                                    title = "صف الأرقام العلوي الدائم (Samsung)",
                                    subtitle = "إبقاء أرقام 0-9 معروضة دائماً فوق الحروف",
                                    checked = showNumberRow
                                ) {
                                    showNumberRow = it
                                    prefs.showNumberRow = it
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                SettingSwitchItem(
                                    title = "ظهور فقاعة الحرف عند الضغط (iPhone Pop-up)",
                                    subtitle = "ظهور الحرف في فقاعة عائمة أنيقة فوق المفتاح أثناء اللمس",
                                    checked = showKeyPopup
                                ) {
                                    showKeyPopup = it
                                    prefs.showKeyPopup = it
                                }
                            }
                        }
                    }

                    1 -> {
                        // Advanced Sound & Vibration Customization
                        item {
                            SettingCard(title = "تخصيص أصوات النقر (SoundPool بدون تأخير)") {
                                SettingSwitchItem(
                                    title = "تشغيل صوت الضغط",
                                    subtitle = "سماع أصوات لطيفة عند النقر على الأزرار",
                                    checked = soundEnabled
                                ) {
                                    soundEnabled = it
                                    prefs.soundEnabled = it
                                }

                                if (soundEnabled) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "اختر نوع الصوت المفضل لديك:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val soundOptions = listOf(
                                        "iPhone Pop (فقاعة ناعمة)" to SoundManager.PROFILE_POP,
                                        "Click ناعم (Soft Click)" to SoundManager.PROFILE_CLICK,
                                        "آلة كاتبة كلاسيكية (Typewriter)" to SoundManager.PROFILE_TYPEWRITER,
                                        "فقاعات ماء (Bubble)" to SoundManager.PROFILE_BUBBLE,
                                        "نقر سامسونج المائي (Samsung Style)" to SoundManager.PROFILE_SAMSUNG,
                                        "صوت النظام الافتراضي (System Default)" to SoundManager.PROFILE_SYSTEM,
                                        "صوت مخصص من ذاكرة الهاتف (Custom Audio)" to SoundManager.PROFILE_CUSTOM
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        soundOptions.forEach { (label, profileKey) ->
                                            val isSelected = soundProfile == profileKey
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                        else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        soundProfile = profileKey
                                                        prefs.soundProfile = profileKey
                                                        viewModel.previewKeySound(profileKey, SoundManager.SoundEvent.CHARACTER, soundVolume.toInt(), customSoundUri)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (soundProfile == SoundManager.PROFILE_CUSTOM) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { audioFilePicker.launch("audio/*") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (customSoundUri != null) "تم اختيار ملف مخصص (اضغط للتغيير)" else "اختيار ملف صوتي من الهاتف")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "مستوى الصوت: ${soundVolume.toInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Slider(
                                        value = soundVolume,
                                        onValueChange = {
                                            soundVolume = it
                                            prefs.soundVolume = it.toInt()
                                        },
                                        valueRange = 10f..100f,
                                        modifier = Modifier.testTag("slider_sound_volume")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "معاينة وتجربة أصوات المفاتيح المختلفة:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.previewKeySound(soundProfile, SoundManager.SoundEvent.CHARACTER, soundVolume.toInt(), customSoundUri) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("حرف", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.previewKeySound(soundProfile, SoundManager.SoundEvent.SPACE, soundVolume.toInt(), customSoundUri) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("مسافة", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.previewKeySound(soundProfile, SoundManager.SoundEvent.DELETE, soundVolume.toInt(), customSoundUri) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("حذف", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.previewKeySound(soundProfile, SoundManager.SoundEvent.ENTER, soundVolume.toInt(), customSoundUri) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Enter", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            SettingCard(title = "الاهتزاز واللمس (Haptic Feedback)") {
                                SettingSwitchItem(
                                    title = "اهتزاز خفيف عند الضغط",
                                    subtitle = "استجابة لمسية فيزيائية فورية مع كل نقرة",
                                    checked = vibrationEnabled
                                ) {
                                    vibrationEnabled = it
                                    prefs.vibrationEnabled = it
                                }

                                if (vibrationEnabled) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "قوة الاهتزاز: ${vibrationStrength.toInt()} مللي ثانية",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Slider(
                                        value = vibrationStrength,
                                        onValueChange = {
                                            vibrationStrength = it
                                            prefs.vibrationStrength = it.toInt()
                                        },
                                        valueRange = 10f..70f,
                                        steps = 6,
                                        modifier = Modifier.testTag("slider_vibration")
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Intelligence & Typing
                        item {
                            SettingCard(title = "ميزات الذكاء والتصحيح") {
                                SettingSwitchItem(
                                    title = "شريط الاقتراحات والتصحيح التلقائي",
                                    subtitle = "3 خانات متساوية تدعم العربية بدقة وبدون قص للنصوص",
                                    checked = showSuggestions
                                ) {
                                    showSuggestions = it
                                    prefs.showSuggestions = it
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                SettingSwitchItem(
                                    title = "توقع الكلمة التالية",
                                    subtitle = "توقع ذكي للكلمات بناءً على أسلوبك وسياق الحديث",
                                    checked = enableNextWord
                                ) {
                                    enableNextWord = it
                                    prefs.enableNextWordPrediction = it
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                SettingSwitchItem(
                                    title = "الكتابة بالسحب (Swipe typing)",
                                    subtitle = "التمرير المستمر بين الحروف لتشكيل الكلمات بسرعة",
                                    checked = enableSwipe
                                ) {
                                    enableSwipe = it
                                    prefs.enableSwipeTyping = it
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                SettingSwitchItem(
                                    title = "مسافتين تضيف نقطة تلقائياً",
                                    subtitle = "الضغط المزدوج على زر المسافة يضع نقطة ومسافة جديدة",
                                    checked = doubleSpacePeriod
                                ) {
                                    doubleSpacePeriod = it
                                    prefs.doubleSpacePeriod = it
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                SettingSwitchItem(
                                    title = "كتابة حرف كبير بعد النقطة",
                                    subtitle = "تكبير الحرف الأول بعد علامات الترقيم في الإنجليزية",
                                    checked = autoCap
                                ) {
                                    autoCap = it
                                    prefs.autoCapitalization = it
                                }
                            }
                        }
                    }

                    3 -> {
                        // Shortcuts
                        item {
                            Text(
                                text = "اكتب كلمة الاختصار في أي تطبيق، وسيتم استبدالها تلقائياً بالنص الكامل!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (shortcuts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("لا توجد اختصارات بعد. اضغط + لإضافة أول اختصار!")
                                }
                            }
                        } else {
                            items(shortcuts) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { editingShortcut = item },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.triggerText,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "←",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = item.expandedText,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { editingShortcut = item },
                                                modifier = Modifier.testTag("btn_edit_shortcut_${item.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "تعديل الاختصار",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteShortcut(item) },
                                                modifier = Modifier.testTag("btn_delete_shortcut_${item.id}")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "حذف الاختصار",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Clipboard
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "يتم حفظ آخر 10 نصوص قمت بنسخها تلقائياً:",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (clipboardItems.isNotEmpty()) {
                                    TextButton(onClick = { viewModel.clearClipboard() }) {
                                        Text("مسح الكل", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        if (clipboardItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("سجل الحافظة فارغ حالياً.")
                                }
                            }
                        } else {
                            items(clipboardItems) { clip ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = clip.text,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddShortcutDialog) {
        var trigger by remember { mutableStateOf("") }
        var expansion by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddShortcutDialog = false },
            title = { Text("إضافة اختصار جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = trigger,
                        onValueChange = { trigger = it },
                        label = { Text("كلمة الاختصار (مثال: ايميلي)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shortcut_trigger"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expansion,
                        onValueChange = { expansion = it },
                        label = { Text("النص الكامل البديل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shortcut_expansion")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trigger.isNotBlank() && expansion.isNotBlank()) {
                            viewModel.addShortcut(trigger, expansion)
                            showAddShortcutDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_shortcut")
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddShortcutDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    editingShortcut?.let { target ->
        var editTrigger by remember(target) { mutableStateOf(target.triggerText) }
        var editExpansion by remember(target) { mutableStateOf(target.expandedText) }

        AlertDialog(
            onDismissRequest = { editingShortcut = null },
            title = { Text("تعديل الاختصار") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTrigger,
                        onValueChange = { editTrigger = it },
                        label = { Text("كلمة الاختصار (مثال: ايميلي)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_shortcut_trigger"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editExpansion,
                        onValueChange = { editExpansion = it },
                        label = { Text("النص الكامل البديل") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_edit_shortcut_expansion")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTrigger.isNotBlank() && editExpansion.isNotBlank()) {
                            viewModel.updateShortcut(target, editTrigger, editExpansion)
                            editingShortcut = null
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_edit_shortcut")
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingShortcut = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showThemeStudioDialog) {
        CustomThemeStudioDialog(
            prefs = prefs,
            onDismiss = { showThemeStudioDialog = false },
            onApply = {
                themePref = PreferencesManager.THEME_CUSTOM
                showThemeStudioDialog = false
            }
        )
    }
}

@Composable
fun CustomThemeStudioDialog(
    prefs: PreferencesManager,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = remember { KeyboardTheme.getCustomTheme(context) }

    var themeName by remember { mutableStateOf(currentTheme.name) }
    var kbBgInt by remember { mutableIntStateOf(currentTheme.keyboardBackground) }
    var keyBgInt by remember { mutableIntStateOf(currentTheme.keyBackground) }
    var actionKeyBgInt by remember { mutableIntStateOf(currentTheme.actionKeyBackground) }
    var textColorInt by remember { mutableIntStateOf(currentTheme.primaryTextColor) }
    var accentColorInt by remember { mutableIntStateOf(currentTheme.accentColor) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "صانع الكيبورد والثيم المخصص",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "صمم ألوان الكيبورد بحرية مع معاينة حية فورية",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Interactive Keyboard Preview Box
                Text(
                    text = "المعاينة الحية الفورية للكيبورد:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(kbBgInt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(accentColorInt), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 1. Suggestion Strip Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(actionKeyBgInt).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السلام",
                                color = Color(textColorInt).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color(keyBgInt), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "عليكم",
                                color = Color(accentColorInt),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color(keyBgInt), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(accentColorInt), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "ورحمة",
                                color = Color(textColorInt).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color(keyBgInt), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // 2. Sample Character Keys Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val sampleLetters = listOf("ض", "ص", "ث", "ق", "ف", "غ")
                            sampleLetters.forEach { letter ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(30.dp)
                                        .background(Color(keyBgInt), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter,
                                        color = Color(textColorInt),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // 3. Sample Action Keys Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(28.dp)
                                    .background(Color(actionKeyBgInt), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("١٢٣", color = Color(textColorInt), fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(3.4f)
                                    .height(28.dp)
                                    .background(Color(keyBgInt), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("مسافة (Space)", color = Color(textColorInt).copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(28.dp)
                                    .background(Color(accentColorInt), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("↵", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Quick Presets Selector
                Text(
                    text = "قوالب ألوان جاهزة للبدء السريع:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "نيون سايبر" to KeyboardTheme.CYBERPUNK,
                        "زمردي وذهبي" to KeyboardTheme.EMERALD_GOLD,
                        "غروب بنفسجي" to KeyboardTheme.SUNSET_PURPLE,
                        "أوليد أسود" to KeyboardTheme.OLED_BLACK,
                        "آيفون داكن" to KeyboardTheme.IOS_DARK,
                        "آيفون فاتح" to KeyboardTheme.IOS_LIGHT
                    )
                    presets.forEach { (label, themeObj) ->
                        OutlinedButton(
                            onClick = {
                                themeName = label
                                kbBgInt = themeObj.keyboardBackground
                                keyBgInt = themeObj.keyBackground
                                actionKeyBgInt = themeObj.actionKeyBackground
                                textColorInt = themeObj.primaryTextColor
                                accentColorInt = themeObj.accentColor
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
                    }
                }

                // Color Customization Pickers
                ColorPickerRow("خلفية الكيبورد الكاملة", kbBgInt) { kbBgInt = it }
                ColorPickerRow("خلفية أزرار الحروف", keyBgInt) { keyBgInt = it }
                ColorPickerRow("أزرار الوظائف (مسافة، إنتر، حذف)", actionKeyBgInt) { actionKeyBgInt = it }
                ColorPickerRow("لون نصوص الحروف", textColorInt) { textColorInt = it }
                ColorPickerRow("اللون التمييزي والتركيز (Accent)", accentColorInt) { accentColorInt = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customTheme = KeyboardTheme(
                        id = PreferencesManager.THEME_CUSTOM,
                        name = themeName.ifBlank { "ثيمي المخصص" },
                        isDark = true,
                        keyboardBackground = kbBgInt,
                        keyBackground = keyBgInt,
                        keyBackgroundPressed = adjustColorBrightness(keyBgInt, 1.2f),
                        actionKeyBackground = actionKeyBgInt,
                        actionKeyBackgroundPressed = adjustColorBrightness(actionKeyBgInt, 1.25f),
                        accentColor = accentColorInt,
                        primaryTextColor = textColorInt,
                        secondaryTextColor = adjustColorBrightness(textColorInt, 0.7f),
                        keyShadowColor = adjustColorBrightness(kbBgInt, 0.5f),
                        suggestionStripBackground = actionKeyBgInt,
                        suggestionTextColor = textColorInt,
                        suggestionHighlightColor = accentColorInt,
                        popupBackgroundColor = keyBgInt,
                        popupTextColor = textColorInt,
                        gestureTrailColor = accentColorInt
                    )
                    prefs.customThemeJson = customTheme.toJson()
                    prefs.theme = PreferencesManager.THEME_CUSTOM
                    onApply()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ وتطبيق الثيم فوراً ✓")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ColorPickerRow(
    title: String,
    selectedColorInt: Int,
    onColorSelected: (Int) -> Unit
) {
    val swatches = listOf(
        AndroidColor.parseColor("#0D0B18"), // Cyber Dark
        AndroidColor.parseColor("#1C1C1E"), // Apple Slate
        AndroidColor.parseColor("#000000"), // Pitch Black
        AndroidColor.parseColor("#061A14"), // Emerald Deep
        AndroidColor.parseColor("#1A0E2E"), // Sunset Indigo
        AndroidColor.parseColor("#2C2C2E"), // iOS Dark Key
        AndroidColor.parseColor("#3A3A3C"), // Soft Charcoal
        AndroidColor.parseColor("#FFFFFF"), // Pure White
        AndroidColor.parseColor("#D1D5DB"), // Light Gray
        AndroidColor.parseColor("#00F0FF"), // Neon Cyan
        AndroidColor.parseColor("#FF007F"), // Neon Pink
        AndroidColor.parseColor("#D4AF37"), // Rich Gold
        AndroidColor.parseColor("#FF6B6B"), // Sunset Coral
        AndroidColor.parseColor("#0A84FF"), // iOS Blue
        AndroidColor.parseColor("#10B981"), // Emerald Green
        AndroidColor.parseColor("#8B5CF6")  // Vivid Purple
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Surface(
                shape = CircleShape,
                color = Color(selectedColorInt),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(18.dp)
            ) {}
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            swatches.forEach { colorVal ->
                val isSelected = colorVal == selectedColorInt
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(colorVal), CircleShape)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorVal) }
                )
            }
        }
    }
}

fun adjustColorBrightness(color: Int, factor: Float): Int {
    val a = AndroidColor.alpha(color)
    val r = (AndroidColor.red(color) * factor).toInt().coerceIn(0, 255)
    val g = (AndroidColor.green(color) * factor).toInt().coerceIn(0, 255)
    val b = (AndroidColor.blue(color) * factor).toInt().coerceIn(0, 255)
    return AndroidColor.argb(a, r, g, b)
}

@Composable
fun ActivationWizardCard(
    isEnabled: Boolean,
    isSelected: Boolean,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled && isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حالة تفعيل Mralios Keyboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1
            WizardStepRow(
                stepNumber = "1",
                title = "تفعيل اللوحة في النظام",
                isDone = isEnabled,
                buttonLabel = if (isEnabled) "✓ تم التفعيل" else "تفعيل الآن",
                onAction = onEnableClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step 2
            WizardStepRow(
                stepNumber = "2",
                title = "تعيين كلوحة افتراضية",
                isDone = isSelected,
                buttonLabel = if (isSelected) "✓ افتراضي" else "اختيار الآن",
                onAction = onSelectClick
            )
        }
    }
}

@Composable
fun WizardStepRow(
    stepNumber: String,
    title: String,
    isDone: Boolean,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                shape = CircleShape,
                color = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDone) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onAction,
            enabled = !isDone,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isDone) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonLabel, fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ThemeOptionItem(
    label: String,
    themeKey: String,
    currentTheme: String,
    onSelect: (String) -> Unit
) {
    val isSelected = currentTheme == themeKey
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect(themeKey) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun HeightOptionButton(
    label: String,
    heightKey: String,
    currentHeight: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    val isSelected = currentHeight == heightKey
    Button(
        onClick = { onSelect(heightKey) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, fontSize = 13.sp)
    }
}
