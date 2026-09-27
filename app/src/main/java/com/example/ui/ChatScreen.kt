package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppDrawer
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.CodeStudioSheet
import com.example.ui.components.HomeEmptyState
import com.example.ui.components.InputBar
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.components.PersonaSelectorSheet
import com.example.ui.components.PromptPresetsSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBar
import com.example.ui.components.VoiceModal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentConversationId by viewModel.currentConversationId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val pinnedConversations by viewModel.pinnedConversations.collectAsStateWithLifecycle()
    val recentConversations by viewModel.recentConversations.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentPersona by viewModel.currentPersona.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    val novaApiKey by viewModel.novaApiKey.collectAsStateWithLifecycle()
    val thenuxApiKey by viewModel.thenuxApiKey.collectAsStateWithLifecycle()
    val customSystemPrompt by viewModel.customSystemPrompt.collectAsStateWithLifecycle()

    val isVoiceModalOpen by viewModel.isVoiceModalOpen.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val recognizedSpeech by viewModel.recognizedSpeech.collectAsStateWithLifecycle()

    // Sheet states
    var showModelSelectorSheet by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }
    var showPersonaSheet by remember { mutableStateOf(false) }
    var showCodeStudioSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val modelSelectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presetsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val personaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val codeStudioSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Black,
                drawerContentColor = Color.White
            ) {
                AppDrawer(
                    pinnedConversations = pinnedConversations,
                    recentConversations = recentConversations,
                    currentModel = currentModel,
                    selectedConversationId = currentConversationId,
                    onSelectConversation = { id ->
                        viewModel.selectConversation(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChatClick = {
                        viewModel.startNewChat()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onTogglePin = { id, pinned ->
                        viewModel.togglePin(id, pinned)
                    },
                    onDeleteConversation = { id ->
                        viewModel.deleteConversation(id)
                    },
                    onOpenModelSelector = {
                        coroutineScope.launch { drawerState.close() }
                        showModelSelectorSheet = true
                    },
                    onOpenSettings = {
                        coroutineScope.launch { drawerState.close() }
                        showSettingsDialog = true
                    },
                    onOpenPersonas = {
                        coroutineScope.launch { drawerState.close() }
                        showPersonaSheet = true
                    },
                    onOpenCodeLibrary = {
                        coroutineScope.launch { drawerState.close() }
                        showCodeStudioSheet = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            containerColor = Color.Black,
            topBar = {
                TopBar(
                    currentModel = currentModel,
                    currentPersona = currentPersona,
                    onMenuClick = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onModelClick = {
                        showModelSelectorSheet = true
                    },
                    onNewChatClick = {
                        viewModel.startNewChat()
                    }
                )
            },
            bottomBar = {
                Box(modifier = Modifier.imePadding()) {
                    InputBar(
                        text = inputText,
                        onTextChange = { viewModel.onInputTextChanged(it) },
                        onSend = { viewModel.sendMessage() },
                        onPlusClick = { showPresetsSheet = true },
                        onMicClick = {
                            viewModel.isVoiceModalOpen.value = true
                            viewModel.startVoiceRecognition()
                        },
                        onVoiceWaveClick = {
                            viewModel.isVoiceModalOpen.value = true
                            viewModel.startVoiceRecognition()
                        },
                        isLoading = isLoading
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (messages.isEmpty()) {
                    HomeEmptyState(
                        onPromptSelected = { selectedPrompt ->
                            viewModel.sendMessage(selectedPrompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                currentPersona = currentPersona,
                                onSpeakText = { viewModel.speakText(it) },
                                onRetry = {
                                    val lastUserMsg = messages.findLast { it.role == "user" }
                                    if (lastUserMsg != null) {
                                        viewModel.sendMessage(lastUserMsg.content)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Model Selector Bottom Sheet
    if (showModelSelectorSheet) {
        ModelSelectorSheet(
            selectedModel = currentModel,
            onSelectModel = { model ->
                viewModel.setModel(model)
            },
            sheetState = modelSelectorSheetState,
            onDismiss = { showModelSelectorSheet = false }
        )
    }

    // Presets Bottom Sheet
    if (showPresetsSheet) {
        PromptPresetsSheet(
            sheetState = presetsSheetState,
            onSelectPreset = { preset ->
                viewModel.onInputTextChanged(preset)
            },
            onDismiss = { showPresetsSheet = false }
        )
    }

    // Persona Selector Sheet
    if (showPersonaSheet) {
        PersonaSelectorSheet(
            selectedPersona = currentPersona,
            onSelect = { persona ->
                viewModel.setPersona(persona)
            },
            sheetState = personaSheetState,
            onDismiss = { showPersonaSheet = false }
        )
    }

    // Code Studio Sheet
    if (showCodeStudioSheet) {
        CodeStudioSheet(
            sheetState = codeStudioSheetState,
            onSelectSnippet = { prompt ->
                viewModel.sendMessage(prompt)
            },
            onDismiss = { showCodeStudioSheet = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentNovaApiKey = novaApiKey,
            currentThenuxApiKey = thenuxApiKey,
            customSystemPrompt = customSystemPrompt,
            onSave = { novaKey, thenuxKey, prompt ->
                viewModel.saveSettings(novaKey, thenuxKey, prompt)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Voice Modal
    if (isVoiceModalOpen) {
        VoiceModal(
            isListening = isListening,
            recognizedText = recognizedSpeech,
            onStartListening = { viewModel.startVoiceRecognition() },
            onStopListening = { viewModel.stopVoiceRecognition() },
            onSubmitVoice = { viewModel.submitVoiceQuery() },
            onDismiss = {
                viewModel.stopVoiceRecognition()
                viewModel.isVoiceModalOpen.value = false
            }
        )
    }
}
