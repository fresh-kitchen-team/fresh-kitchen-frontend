package com.example.myfrigelocal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.data.ChatRoomSectionMapper
import com.example.myfrigelocal.data.SessionTokenProvider
import com.example.myfrigelocal.data.remote.ChatRetrofitProvider
import com.example.myfrigelocal.data.remote.dto.ChatRoomDto
import com.example.myfrigelocal.data.repository.ChatRepository
import com.example.myfrigelocal.data.repository.toChatMessage
import com.example.myfrigelocal.ui.screens.chat.ChatMessage
import com.example.myfrigelocal.ui.screens.chat.SideMenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Swagger requires a non-empty title on room create; original product idea was `{}` and server-set title.
 * Using a neutral default until backend supports optional title — confirm with backend team.
 *
 * TODO: Replace with server-driven default or first-message title once API supports it.
 */
const val SWAGGER_DEFAULT_NEW_CHAT_TITLE = "새 채팅"

data class AiChatUiState(
    val sideMenuItems: List<SideMenuItem> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentRoomId: Long? = null,
    val topBarTitle: String = "AI 주방 비서",
    val isLoadingRooms: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
)

class AiChatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = ChatRepository(
        ChatRetrofitProvider.chatApi(SessionTokenProvider),
    )

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var cachedRooms: List<ChatRoomDto> = emptyList()

    init {
        refreshRooms(selectFirstAfterLoad = true)
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshRooms(selectFirstAfterLoad: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRooms = true, error = null) }
            repository.listRooms()
                .onSuccess { rooms ->
                    cachedRooms = rooms.sortedWith(::compareRoomsForSidebar)
                    val items = buildSideMenuItems(_uiState.value.currentRoomId)
                    _uiState.update {
                        it.copy(
                            isLoadingRooms = false,
                            sideMenuItems = items,
                        )
                    }
                    if (selectFirstAfterLoad && cachedRooms.isNotEmpty() && _uiState.value.currentRoomId == null) {
                        selectRoom(cachedRooms.first().id)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingRooms = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    fun selectRoom(roomId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentRoomId = roomId,
                    isLoadingMessages = true,
                    error = null,
                    sideMenuItems = markSelected(buildSideMenuItems(roomId), roomId),
                    topBarTitle = cachedRooms.find { r -> r.id == roomId }?.title ?: it.topBarTitle,
                )
            }
            repository.getMessages(roomId)
                .onSuccess { list ->
                    val mapped = list.map { it.toChatMessage() }
                    _uiState.update {
                        it.copy(
                            isLoadingMessages = false,
                            messages = mapped,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingMessages = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    /**
     * TODO: Swagger has no delete room endpoint in the provided spec — add when available.
     */
    fun createRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRooms = true, error = null) }
            repository.createRoom(SWAGGER_DEFAULT_NEW_CHAT_TITLE)
                .onSuccess { room ->
                    cachedRooms = (listOf(room) + cachedRooms.filter { it.id != room.id })
                        .sortedWith(::compareRoomsForSidebar)
                    val newId = room.id
                    _uiState.update { s ->
                        s.copy(
                            currentRoomId = newId,
                            isLoadingRooms = false,
                            isLoadingMessages = true,
                            sideMenuItems = markSelected(buildSideMenuItems(newId), newId),
                            topBarTitle = room.title,
                        )
                    }
                    repository.getMessages(newId)
                        .onSuccess { list ->
                            _uiState.update {
                                it.copy(
                                    isLoadingMessages = false,
                                    messages = list.map { dto -> dto.toChatMessage() },
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isLoadingMessages = false,
                                    error = e.toUserMessage(),
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingRooms = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    /**
     * Sends only [com.example.myfrigelocal.data.remote.dto.SendMessageRequest.message] per Swagger.
     *
     * TODO: Original design included `type`, `ingredients`, `userPreferences`, etc. — not in Swagger;
     * fridge/preference-aware recommendations may be limited until backend exposes those fields.
     */
    fun sendMessage(text: String) {
        val roomId = _uiState.value.currentRoomId ?: run {
            _uiState.update { it.copy(error = "채팅방을 먼저 선택하거나 새 채팅을 만들어 주세요.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            repository.sendMessage(roomId, text)
                .onSuccess {
                    /**
                     * Swagger example shows POST may return a single [com.example.myfrigelocal.data.remote.dto.ChatMessageDto].
                     * Reload history so an AI follow-up message (if persisted separately) appears without guessing payload shape.
                     *
                     * TODO: If backend returns the full thread (or user+AI) in one response later, avoid the extra GET.
                     */
                    repository.getMessages(roomId)
                        .onSuccess { list ->
                            _uiState.update { s ->
                                s.copy(
                                    isSending = false,
                                    messages = list.map { dto -> dto.toChatMessage() },
                                )
                            }
                        }
                        .onFailure { e ->
                            _uiState.update { s ->
                                s.copy(
                                    isSending = false,
                                    error = e.toUserMessage(),
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    /**
     * Local-only title change (no PATCH). Call [updateRoomTitle] when backend sync is required.
     */
    fun renameRoomLocal(threadId: String, newTitle: String) {
        val roomId = threadId.toLongOrNull() ?: return
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        cachedRooms = cachedRooms.map { r ->
            if (r.id == roomId) r.copy(title = trimmed) else r
        }
        _uiState.update { s ->
            s.copy(
                sideMenuItems = markSelected(buildSideMenuItems(s.currentRoomId), s.currentRoomId),
                topBarTitle = if (s.currentRoomId == roomId) trimmed else s.topBarTitle,
            )
        }
    }

    /**
     * Swagger PATCH returns `data: {}` — no room payload; we update local title after success.
     *
     * TODO: Wire from UI when room rename UX exists (sidebar long-press, etc.).
     */
    fun updateRoomTitle(roomId: Long, title: String) {
        viewModelScope.launch {
            repository.updateRoomTitle(roomId, title)
                .onSuccess {
                    cachedRooms = cachedRooms.map { r ->
                        if (r.id == roomId) r.copy(title = title) else r
                    }
                    _uiState.update { s ->
                        s.copy(
                            sideMenuItems = markSelected(buildSideMenuItems(s.currentRoomId), s.currentRoomId),
                            topBarTitle = if (s.currentRoomId == roomId) title else s.topBarTitle,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.toUserMessage()) }
                }
        }
    }

    private fun buildSideMenuItems(selectedId: Long?): List<SideMenuItem> {
        val sel = selectedId?.toString()
        return cachedRooms.map { room ->
            SideMenuItem(
                threadId = room.id.toString(),
                title = room.title,
                section = ChatRoomSectionMapper.sectionFor(room),
                selected = room.id.toString() == sel,
            )
        }
    }

    private fun markSelected(items: List<SideMenuItem>, currentId: Long?): List<SideMenuItem> {
        val idStr = currentId?.toString()
        return items.map { it.copy(selected = it.threadId == idStr) }
    }

    private fun compareRoomsForSidebar(a: ChatRoomDto, b: ChatRoomDto): Int {
        val sa = ChatRoomSectionMapper.sectionFor(a)
        val sb = ChatRoomSectionMapper.sectionFor(b)
        val order = compareValuesBy(sa, sb, { sectionSortKey(it) })
        if (order != 0) return order
        return ChatRoomSectionMapper.compareRooms(a, b)
    }

    private fun sectionSortKey(section: String): Int = when (section) {
        ChatRoomSectionMapper.SECTION_TODAY -> 0
        ChatRoomSectionMapper.SECTION_LAST_7 -> 1
        else -> 2
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is HttpException -> when (code()) {
            401, 403 -> "로그인이 필요하거나 권한이 없습니다."
            404 -> "요청한 채팅을 찾을 수 없습니다."
            in 500..599 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
            else -> message() ?: "요청에 실패했습니다. (${code()})"
        }
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> localizedMessage ?: "오류가 발생했습니다."
    }
}
