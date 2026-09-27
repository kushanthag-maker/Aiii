package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.NovaRepository
import com.example.data.model.AiModelInfo
import com.example.data.model.AiModels
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.persona.AgentPersona
import com.example.data.persona.Personas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = NovaRepository(application)
    private val prefs = application.getSharedPreferences("nova_ai_prefs", Context.MODE_PRIVATE)

    // TTS
    private var textToSpeech: TextToSpeech? = TextToSpeech(application, this)
    private var speechRecognizer: SpeechRecognizer? = null

    // State
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPersona = MutableStateFlow(Personas.Coder)
    val currentPersona: StateFlow<AgentPersona> = _currentPersona.asStateFlow()

    // Active AI Model (Nova AI or Thenux AI)
    private val _currentModel = MutableStateFlow(
        AiModels.getById(prefs.getString("selected_model_id", "nova") ?: "nova")
    )
    val currentModel: StateFlow<AiModelInfo> = _currentModel.asStateFlow()

    private val _novaApiKey = MutableStateFlow(
        prefs.getString("nova_api_key", NovaRepository.DEFAULT_NOVA_KEY) ?: NovaRepository.DEFAULT_NOVA_KEY
    )
    val novaApiKey: StateFlow<String> = _novaApiKey.asStateFlow()

    private val _thenuxApiKey = MutableStateFlow(
        prefs.getString("thenux_api_key", NovaRepository.DEFAULT_THENUX_KEY) ?: NovaRepository.DEFAULT_THENUX_KEY
    )
    val thenuxApiKey: StateFlow<String> = _thenuxApiKey.asStateFlow()

    private val _customSystemPrompt = MutableStateFlow(prefs.getString("system_prompt", "") ?: "")
    val customSystemPrompt: StateFlow<String> = _customSystemPrompt.asStateFlow()

    // Voice Modal State
    val isVoiceModalOpen = MutableStateFlow(false)
    val isListening = MutableStateFlow(false)
    val recognizedSpeech = MutableStateFlow("")

    val pinnedConversations: StateFlow<List<Conversation>> = repository.pinnedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentConversations: StateFlow<List<Conversation>> = repository.recentConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _currentConversationId
        .flatMapLatest { convId ->
            if (convId != null) repository.getMessages(convId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            startNewChat()
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun setPersona(persona: AgentPersona) {
        _currentPersona.value = persona
    }

    fun setModel(model: AiModelInfo) {
        _currentModel.value = model
        prefs.edit().putString("selected_model_id", model.id).apply()
        Toast.makeText(getApplication(), "Switched to ${model.displayName} (${model.versionTag})", Toast.LENGTH_SHORT).show()
    }

    fun startNewChat(
        persona: AgentPersona = _currentPersona.value,
        model: AiModelInfo = _currentModel.value
    ) {
        viewModelScope.launch {
            val conv = repository.createConversation(
                personaId = persona.id,
                selectedModelId = model.id
            )
            _currentConversationId.value = conv.id
            _inputText.value = ""
        }
    }

    fun selectConversation(conversationId: String) {
        _currentConversationId.value = conversationId
    }

    fun togglePin(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(conversationId, isPinned)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (_currentConversationId.value == conversationId) {
                startNewChat()
            }
        }
    }

    fun sendMessage(promptOverride: String? = null) {
        val messageToSend = (promptOverride ?: _inputText.value).trim()
        if (messageToSend.isBlank() || _isLoading.value) return

        val conversationId = _currentConversationId.value ?: return

        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val history = messages.value
            repository.sendMessage(
                conversationId = conversationId,
                userText = messageToSend,
                modelId = _currentModel.value.id,
                novaApiKey = _novaApiKey.value,
                thenuxApiKey = _thenuxApiKey.value,
                customSystemPrompt = _customSystemPrompt.value,
                personaId = _currentPersona.value.id,
                conversationHistory = history
            )
            _isLoading.value = false
        }
    }

    fun saveSettings(newNovaApiKey: String, newThenuxApiKey: String, newSystemPrompt: String) {
        _novaApiKey.value = newNovaApiKey
        _thenuxApiKey.value = newThenuxApiKey
        _customSystemPrompt.value = newSystemPrompt
        prefs.edit()
            .putString("nova_api_key", newNovaApiKey)
            .putString("thenux_api_key", newThenuxApiKey)
            .putString("system_prompt", newSystemPrompt)
            .apply()
        Toast.makeText(getApplication(), "Settings saved successfully", Toast.LENGTH_SHORT).show()
    }

    // TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        }
    }

    fun speakText(text: String) {
        val plainText = text.replace(Regex("```[\\s\\S]*?```"), "Code snippet omitted.")
            .replace("#", "")
            .replace("*", "")
            .replace("`", "")
        textToSpeech?.speak(plainText, TextToSpeech.QUEUE_FLUSH, null, "nova_tts")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    // Voice recognition
    fun startVoiceRecognition() {
        val context = getApplication<Application>()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition is not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening.value = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening.value = false
                }
                override fun onError(error: Int) {
                    isListening.value = false
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        recognizedSpeech.value = spokenText
                    }
                    isListening.value = false
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedSpeech.value = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening.value = true
        } catch (e: Exception) {
            isListening.value = false
        }
    }

    fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        isListening.value = false
    }

    fun submitVoiceQuery() {
        val query = recognizedSpeech.value
        if (query.isNotBlank()) {
            isVoiceModalOpen.value = false
            recognizedSpeech.value = ""
            sendMessage(query)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}
