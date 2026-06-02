package com.freshkitchen.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.data.ChatRoomSectionMapper
import com.freshkitchen.app.data.SessionTokenProvider
import com.freshkitchen.app.data.auth.AuthTokenStore
import com.freshkitchen.app.data.auth.TokenDataStore
import com.freshkitchen.app.data.remote.ChatRetrofitProvider
import com.google.gson.Gson
import com.freshkitchen.app.data.remote.dto.ChatRoomSectionsDto
import com.freshkitchen.app.data.remote.dto.ChatRoomSummaryDto
import com.freshkitchen.app.BuildConfig
import com.freshkitchen.app.data.remote.dto.AiSettingDto
import com.freshkitchen.app.data.remote.dto.SendMessageRequest
import com.freshkitchen.app.data.remote.dto.SendMessageResponseDto
import com.freshkitchen.app.data.repository.ChatRepository
import com.freshkitchen.app.data.repository.RecipeConsumeResolver
import com.freshkitchen.app.data.repository.toChatMessage
import com.freshkitchen.app.logging.ApiLog
import com.freshkitchen.app.network.InquiryApiType
import com.freshkitchen.app.network.InquiryRepository
import com.freshkitchen.app.network.IngredientRepository
import com.freshkitchen.app.ui.screens.chat.ChatMessage
import com.freshkitchen.app.ui.screens.chat.RecipeMatchedItemUi
import com.freshkitchen.app.ui.screens.chat.Sender
import com.freshkitchen.app.ui.screens.chat.SideMenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

private const val LOG_TAG = "FreshKitchenChat"

private const val AI_LOADING_MESSAGE_ID = "ai-loading-pending"
private const val AI_LOADING_TEXT = "답변 생성 중..."
private const val SEND_AI_ERROR_MESSAGE = "AI 응답 생성에 실패했습니다. 다시 시도해 주세요."

data class AiChatUiState(
    val sideMenuItems: List<SideMenuItem> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentRoomId: Long? = null,
    val topBarTitle: String = "AI 주방 비서",
    val isLoadingRooms: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val isSubmittingSupport: Boolean = false,
    val supportError: String? = null,
    /** 문의/신고 접수 완료 문구 (폼에 표시 후 잠시 뒤 닫힘). */
    val supportSuccessMessage: String? = null,
    /** Increments after success message — UI closes form overlay. */
    val supportSubmitSuccessToken: Long = 0L,
    val aiSetting: AiSettingDto? = null,
    val isLoadingAiSetting: Boolean = false,
    val isSavingAiSetting: Boolean = false,
)

class AiChatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = ChatRepository(
        ChatRetrofitProvider.chatApi(SessionTokenProvider),
    )

    private val inquiryRepository = InquiryRepository()

    private val ingredientRepository = IngredientRepository()


    private val debugGson: Gson = ChatRetrofitProvider.gson()

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    /** Last successful GET `/ai/v1/chat/room` buckets (for sidebar + title lookup). */
    private var cachedSections: ChatRoomSectionsDto = ChatRoomSectionsDto()

    init {
        refreshRooms(selectFirstAfterLoad = true)
    }

    /**
     * AI 채팅 탭 재진입 시 호출.
     * Activity-scoped VM이라 예전 401 메시지가 남거나, [AuthTokenStore]만 비어 있는 경우가 있어
     * DataStore에서 토큰을 다시 올린 뒤 필요 시 방 목록을 재요청합니다.
     * 열려 있는 채팅방이 있으면 GET `/ai/v1/chat/room/{roomId}` 로 메시지를 다시 받아
     * 재고 변경(missingIngredients 등)이 반영되게 합니다.
     */
    fun onAiChatScreenVisible() {
        viewModelScope.launch {
            hydrateTokenFromStore()
            val s = _uiState.value
            if (s.error != null || s.sideMenuItems.isEmpty()) {
                refreshRooms(selectFirstAfterLoad = s.currentRoomId == null && s.sideMenuItems.isEmpty())
            }
            s.currentRoomId?.let { roomId ->
                fetchAndApplyRoomDetail(roomId, showLoadingIndicator = false)
            }
        }
    }

    private suspend fun hydrateTokenFromStore() {
        val stored = TokenDataStore.getAccessToken(getApplication()).first()
        if (!stored.isNullOrBlank()) {
            AuthTokenStore.setAccessToken(stored)
            Log.i(LOG_TAG, "[hydrateToken] loaded access token from DataStore")
        } else {
            Log.w(LOG_TAG, "[hydrateToken] no token in DataStore")
        }
        logTokenPresence("onAiChatScreenVisible")
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissSupportError() {
        _uiState.update { it.copy(supportError = null) }
    }

    /** Swagger `POST /api/v1/inquiries` — `type=INQUIRY` */
    fun submitInquiry(categoryLabel: String, content: String, imageUri: String? = null) {
        submitSupport(InquiryApiType.INQUIRY, categoryLabel, content, imageUri)
    }

    /** Swagger `POST /api/v1/inquiries` — `type=REPORT` */
    fun submitReport(categoryLabel: String, content: String, imageUri: String? = null) {
        submitSupport(InquiryApiType.REPORT, categoryLabel, content, imageUri)
    }

    private fun submitSupport(
        apiType: InquiryApiType,
        categoryLabel: String,
        content: String,
        imageUri: String?,
    ) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(supportError = "내용을 입력해 주세요.") }
            return
        }
        if (_uiState.value.isSubmittingSupport) return

        val category = inquiryRepository.categoryFromUiLabel(categoryLabel)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingSupport = true,
                    supportError = null,
                    supportSuccessMessage = null,
                )
            }
            ApiLog.i(
                "Inquiry:ViewModel",
                "submit ${apiType.apiValue} category=${category.apiValue} len=${trimmed.length} " +
                    "hasImage=${!imageUri.isNullOrBlank()}",
            )
            inquiryRepository.send(
                context = getApplication(),
                type = apiType,
                category = category,
                content = trimmed,
                imageUri = imageUri,
            )
                .onSuccess { displayMessage ->
                    ApiLog.i("Inquiry:ViewModel", "submit success: $displayMessage")
                    _uiState.update {
                        it.copy(
                            isSubmittingSupport = false,
                            supportSuccessMessage = displayMessage,
                        )
                    }
                    delay(2_000)
                    _uiState.update { s ->
                        s.copy(
                            supportSuccessMessage = null,
                            supportSubmitSuccessToken = s.supportSubmitSuccessToken + 1,
                        )
                    }
                }
                .onFailure { e ->
                    ApiLog.e("Inquiry:ViewModel", "submit failed: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isSubmittingSupport = false,
                            supportError = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    fun refreshRooms(selectFirstAfterLoad: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRooms = true, error = null) }
            logTokenPresence("getChatRooms")
            repository.getChatRooms()
                .onSuccess { sections ->
                    cachedSections = sections
                    val items = buildSideMenuItems(_uiState.value.currentRoomId)
                    _uiState.update {
                        it.copy(
                            isLoadingRooms = false,
                            sideMenuItems = items,
                        )
                    }
                    if (selectFirstAfterLoad && _uiState.value.currentRoomId == null) {
                        val firstId = items.firstOrNull()?.threadId?.toLongOrNull()
                        if (firstId != null) selectRoom(firstId)
                    }
                }
                .onFailure { e ->
                    logFailure("getChatRooms", e)
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
        val state = _uiState.value
        if (state.isSending && state.currentRoomId == roomId) {
            _uiState.update {
                it.copy(
                    currentRoomId = roomId,
                    sideMenuItems = markSelected(buildSideMenuItems(roomId), roomId),
                    topBarTitle = roomTitleFromCache(roomId) ?: it.topBarTitle,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentRoomId = roomId,
                    error = null,
                    sideMenuItems = markSelected(buildSideMenuItems(roomId), roomId),
                    topBarTitle = roomTitleFromCache(roomId) ?: it.topBarTitle,
                )
            }
            fetchAndApplyRoomDetail(roomId, showLoadingIndicator = true)
        }
    }

    /**
     * GET `/ai/v1/chat/room/{roomId}` — 서버가 ACTIVE 재고 기준으로 aiPayload(missingIngredients 등)를 다시 계산.
     */
    private suspend fun fetchAndApplyRoomDetail(
        roomId: Long,
        showLoadingIndicator: Boolean,
    ) {
        if (_uiState.value.isSending && _uiState.value.currentRoomId == roomId) return

        if (showLoadingIndicator) {
            _uiState.update { it.copy(isLoadingMessages = true, error = null) }
        }
        logTokenPresence("getChatRoomDetail")
        repository.getChatRoomDetail(roomId)
            .onSuccess { detail ->
                val mapped = detail.messages.orEmpty().map { it.toChatMessage() }
                _uiState.update { current ->
                    if (current.currentRoomId != roomId) return@update current
                    val resolvedTitle = detail.title?.takeIf { t -> t.isNotBlank() }
                        ?: roomTitleFromCache(roomId)
                        ?: current.topBarTitle
                    if (current.isSending) {
                        current.copy(
                            isLoadingMessages = false,
                            topBarTitle = resolvedTitle,
                        )
                    } else {
                        current.copy(
                            isLoadingMessages = false,
                            messages = mapped,
                            topBarTitle = resolvedTitle,
                        )
                    }
                }
            }
            .onFailure { e ->
                logFailure("getChatRoomDetail", e)
                _uiState.update { current ->
                    if (current.currentRoomId != roomId) {
                        current
                    } else {
                        current.copy(
                            isLoadingMessages = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
            }
    }

    /**
     * Creates an empty room (POST body 없음). Swagger: `POST /ai/v1/chat/room`.
     *
     */
    fun createRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRooms = true, error = null) }
            logTokenPresence("createChatRoom")
            repository.createChatRoom()
                .onSuccess { created ->
                    val summary = ChatRoomSummaryDto(
                        roomId = created.roomId,
                        title = created.title,
                        updatedAt = created.createdAt,
                        sender = null,
                        content = null,
                    )
                    cachedSections = cachedSections.withRoomPrependedToday(summary)
                    val newId = created.roomId
                    _uiState.update { s ->
                        s.copy(
                            currentRoomId = newId,
                            isLoadingRooms = false,
                            isLoadingMessages = true,
                            sideMenuItems = markSelected(buildSideMenuItems(newId), newId),
                            topBarTitle = created.title?.takeIf { it.isNotBlank() }
                                ?: s.topBarTitle,
                        )
                    }
                    logTokenPresence("getChatRoomDetail(afterCreate)")
                    repository.getChatRoomDetail(newId)
                        .onSuccess { detail ->
                            _uiState.update {
                                it.copy(
                                    isLoadingMessages = false,
                                    messages = detail.messages.orEmpty().map { dto -> dto.toChatMessage() },
                                    topBarTitle = detail.title?.takeIf { t -> t.isNotBlank() }
                                        ?: created.title
                                        ?: it.topBarTitle,
                                )
                            }
                        }
                        .onFailure { e ->
                            logFailure("getChatRoomDetail(afterCreate)", e)
                            _uiState.update {
                                it.copy(
                                    isLoadingMessages = false,
                                    error = e.toUserMessage(),
                                )
                            }
                        }
                }
                .onFailure { e ->
                    logFailure("createChatRoom", e)
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
     * Swagger `POST /ai/v1/chat/room/{roomId}` — sends `message` only.
     * AI settings are updated separately via [updateAiSetting].
     */
    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }

            var roomId = _uiState.value.currentRoomId
            if (roomId == null) {
                logTokenPresence("createChatRoom(beforeSend)")
                repository.createChatRoom()
                    .onSuccess { created ->
                        roomId = created.roomId
                        val summary = ChatRoomSummaryDto(
                            roomId = created.roomId,
                            title = created.title,
                            updatedAt = created.createdAt,
                            sender = null,
                            content = null,
                        )
                        cachedSections = cachedSections.withRoomPrependedToday(summary)
                        _uiState.update { s ->
                            s.copy(
                                currentRoomId = created.roomId,
                                sideMenuItems = markSelected(buildSideMenuItems(created.roomId), created.roomId),
                                topBarTitle = created.title?.takeIf { it.isNotBlank() } ?: s.topBarTitle,
                            )
                        }
                    }
                    .onFailure { e ->
                        logFailure("createChatRoom(beforeSend)", e)
                        _uiState.update {
                            it.copy(isSending = false, error = e.toUserMessage())
                        }
                        return@launch
                    }
            }

            val effectiveRoomId = roomId ?: run {
                _uiState.update { it.copy(isSending = false, error = "채팅방을 만들 수 없습니다.") }
                return@launch
            }

            val optimisticId = "local-${UUID.randomUUID()}"
            val userBubble = ChatMessage(
                id = optimisticId,
                sender = Sender.User,
                text = trimmed,
            )
            _uiState.update { s ->
                s.copy(
                    messages = s.messages.withoutAiLoadingPlaceholder() + userBubble + aiLoadingPlaceholder(),
                )
            }

            val request = SendMessageRequest(message = trimmed)

            logTokenPresence("sendMessage")
            repository.sendMessage(effectiveRoomId, request)
                .onSuccess { resp ->
                    logSendMessageResponse(resp)
                    val ai = resp.aiMessage.toChatMessage()
                    _uiState.update { s ->
                        s.copy(
                            isSending = false,
                            messages = s.messages.withoutAiLoadingPlaceholder() + ai,
                            topBarTitle = resp.title?.takeIf { it.isNotBlank() } ?: s.topBarTitle,
                        )
                    }
                    resp.title?.let { t ->
                        if (t.isNotBlank()) {
                            cachedSections = cachedSections.withRoomTitle(effectiveRoomId, t)
                            _uiState.update { s ->
                                s.copy(sideMenuItems = markSelected(buildSideMenuItems(s.currentRoomId), s.currentRoomId))
                            }
                        }
                    }
                }
                .onFailure { e ->
                    logFailure("sendMessage", e)
                    _uiState.update { s ->
                        s.copy(
                            isSending = false,
                            messages = s.messages.withoutAiLoadingPlaceholder(),
                            error = SEND_AI_ERROR_MESSAGE,
                        )
                    }
                }
        }
    }

    /** Refresh inventory and map aiPayload matched rows to consumable UI rows. */
    suspend fun enrichRecipeMatchedItems(items: List<RecipeMatchedItemUi>): List<RecipeMatchedItemUi> {
        if (items.isEmpty()) return emptyList()
        val inventory = ingredientRepository.getIngredients()
        return RecipeConsumeResolver.enrichMatchedItems(items, inventory)
    }

    /**
     * Consumes selected recipe rows.
     * Re-fetches inventory and picks item id by name (earliest expiry, then lowest id).
     */
    suspend fun consumeRecipeMatchedItems(
        selectedRows: List<RecipeMatchedItemUi>,
    ): Result<Int> {
        if (selectedRows.isEmpty()) {
            return Result.failure(IllegalArgumentException("선택된 재료가 없습니다."))
        }
        val inventory = ingredientRepository.getIngredients()
        val ids = RecipeConsumeResolver.resolveConsumeIdsForRows(selectedRows, inventory)
        if (ids.isEmpty()) {
            return Result.failure(IllegalStateException("저장소에서 선택한 재료를 찾을 수 없습니다."))
        }
        var successCount = 0
        for (id in ids) {
            if (ingredientRepository.consumeItem(id)) {
                successCount++
            }
        }
        return if (successCount > 0) {
            _uiState.value.currentRoomId?.let { roomId ->
                fetchAndApplyRoomDetail(roomId, showLoadingIndicator = false)
            }
            Result.success(successCount)
        } else {
            Result.failure(IOException("재료 소비 처리에 실패했습니다."))
        }
    }

    fun loadAiSettings() {
        if (_uiState.value.isLoadingAiSetting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAiSetting = true) }
            logTokenPresence("getAiSetting")
            repository.getAiSetting()
                .onSuccess { setting ->
                    Log.i(LOG_TAG, "[getAiSetting] success")
                    _uiState.update {
                        it.copy(isLoadingAiSetting = false, aiSetting = setting)
                    }
                }
                .onFailure { e ->
                    logFailure("getAiSetting", e)
                    _uiState.update {
                        it.copy(isLoadingAiSetting = false, error = e.toUserMessage())
                    }
                }
        }
    }

    fun updateAiSetting(aiSetting: AiSettingDto, onComplete: (Boolean) -> Unit = {}) {
        if (_uiState.value.isSavingAiSetting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAiSetting = true) }
            logTokenPresence("updateAiSetting")
            repository.updateAiSetting(aiSetting)
                .onSuccess { saved ->
                    Log.i(LOG_TAG, "[updateAiSetting] success")
                    _uiState.update {
                        it.copy(isSavingAiSetting = false, aiSetting = saved)
                    }
                    onComplete(true)
                }
                .onFailure { e ->
                    logFailure("updateAiSetting", e)
                    _uiState.update {
                        it.copy(isSavingAiSetting = false, error = e.toUserMessage())
                    }
                    onComplete(false)
                }
        }
    }

    /** DELETE `/ai/v1/chat/delete/room/{roomId}` — 목록에서 제거, 현재 방이면 채팅 화면 초기화. */
    fun deleteRoom(roomId: Long) {
        viewModelScope.launch {
            logTokenPresence("deleteChatRoom")
            repository.deleteChatRoom(roomId)
                .onSuccess {
                    cachedSections = cachedSections.withoutRoom(roomId)
                    val wasCurrent = _uiState.value.currentRoomId == roomId

                    // Keep chat UI "in place" even if the current room was deleted.
                    // If there are remaining rooms, jump to one of them; otherwise create a new room.
                    val fallbackRoomId = cachedSections.allSummaries().firstOrNull()?.roomId

                    _uiState.update { s ->
                        val targetId = if (wasCurrent) (fallbackRoomId ?: s.currentRoomId) else s.currentRoomId
                        s.copy(
                            sideMenuItems = markSelected(buildSideMenuItems(targetId), targetId),
                        )
                    }

                    if (wasCurrent) {
                        when {
                            fallbackRoomId != null -> selectRoom(fallbackRoomId)
                            else -> createRoom()
                        }
                    }
                }
                .onFailure { e ->
                    logFailure("deleteChatRoom", e)
                    _uiState.update { it.copy(error = e.toUserMessage()) }
                }
        }
    }

    /** PATCH `/ai/v1/chat/room/{roomId}` — 응답 `data.title`로 로컬 갱신. */
    fun updateRoomTitle(roomId: Long, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            logTokenPresence("updateRoomTitle")
            repository.updateRoomTitle(roomId, trimmed)
                .onSuccess { body ->
                    val resolvedTitle = body.title?.takeIf { it.isNotBlank() } ?: trimmed
                    cachedSections = cachedSections.withRoomTitle(roomId, resolvedTitle)
                    _uiState.update { s ->
                        s.copy(
                            sideMenuItems = markSelected(buildSideMenuItems(s.currentRoomId), s.currentRoomId),
                            topBarTitle = if (s.currentRoomId == roomId) resolvedTitle else s.topBarTitle,
                        )
                    }
                }
                .onFailure { e ->
                    logFailure("updateRoomTitle", e)
                    _uiState.update { it.copy(error = e.toUserMessage()) }
                }
        }
    }

    private fun buildSideMenuItems(selectedId: Long?): List<SideMenuItem> {
        val sel = selectedId?.toString()
        val out = mutableListOf<SideMenuItem>()
        fun appendSection(label: String, rooms: List<ChatRoomSummaryDto>?) {
            rooms?.sortedWith(ChatRoomSectionMapper::compareRooms)?.forEach { room ->
                val title = room.title?.trim().orEmpty().ifBlank { "채팅" }
                out += SideMenuItem(
                    threadId = room.roomId.toString(),
                    title = title,
                    section = label,
                    selected = room.roomId.toString() == sel,
                )
            }
        }
        appendSection(ChatRoomSectionMapper.SECTION_TODAY, cachedSections.today)
        appendSection(ChatRoomSectionMapper.SECTION_LAST_7, cachedSections.last7Days)
        appendSection(ChatRoomSectionMapper.SECTION_LAST_30, cachedSections.last30Days)
        return out
    }

    private fun markSelected(items: List<SideMenuItem>, currentId: Long?): List<SideMenuItem> {
        val idStr = currentId?.toString()
        return items.map { it.copy(selected = it.threadId == idStr) }
    }

    private fun roomTitleFromCache(roomId: Long): String? =
        cachedSections.allSummaries().firstOrNull { it.roomId == roomId }?.title?.trim()?.takeIf { it.isNotEmpty() }

    private fun ChatRoomSectionsDto.allSummaries(): List<ChatRoomSummaryDto> =
        (today.orEmpty() + last7Days.orEmpty() + last30Days.orEmpty())

    private fun ChatRoomSectionsDto.withRoomTitle(roomId: Long, newTitle: String): ChatRoomSectionsDto {
        fun mapList(list: List<ChatRoomSummaryDto>?) =
            list?.map { if (it.roomId == roomId) it.copy(title = newTitle) else it }
        return copy(
            today = mapList(today),
            last7Days = mapList(last7Days),
            last30Days = mapList(last30Days),
        )
    }

    private fun ChatRoomSectionsDto.withRoomPrependedToday(room: ChatRoomSummaryDto): ChatRoomSectionsDto {
        val rest = today.orEmpty().filter { it.roomId != room.roomId }
        return copy(today = listOf(room) + rest)
    }

    private fun ChatRoomSectionsDto.withoutRoom(roomId: Long): ChatRoomSectionsDto {
        fun filterList(list: List<ChatRoomSummaryDto>?) =
            list?.filter { it.roomId != roomId }
        return copy(
            today = filterList(today),
            last7Days = filterList(last7Days),
            last30Days = filterList(last30Days),
        )
    }

    private fun aiLoadingPlaceholder(): ChatMessage = ChatMessage(
        id = AI_LOADING_MESSAGE_ID,
        sender = Sender.Ai,
        text = AI_LOADING_TEXT,
        isLoading = true,
    )

    private fun List<ChatMessage>.withoutAiLoadingPlaceholder(): List<ChatMessage> =
        filterNot { it.id == AI_LOADING_MESSAGE_ID || it.isLoading }

    /** DEBUG: 파싱된 응답 요약 + raw JSON은 OkHttp BODY 로그(`FreshKitchenChat`)에서 확인. */
    private fun logSendMessageResponse(resp: SendMessageResponseDto) {
        val msg = resp.aiMessage
        val recipeCount = msg.aiPayload?.recipes?.size ?: 0
        Log.i(
            LOG_TAG,
            "[sendMessage] parsed: title=${resp.title}, uiType=${msg.uiType}, " +
                "text=${msg.text.take(80)}, recipeCount=$recipeCount",
        )
        if (BuildConfig.DEBUG) {
            try {
                Log.d(LOG_TAG, "[sendMessage] data JSON: ${debugGson.toJson(resp)}")
            } catch (e: Exception) {
                Log.w(LOG_TAG, "[sendMessage] could not serialize response: ${e.message}")
            }
        }
    }

    private fun logTokenPresence(apiName: String) {
        val has = !AuthTokenStore.getAccessToken().isNullOrBlank()
        Log.i(LOG_TAG, "[$apiName] Authorization token present=$has (value not logged)")
    }

    private fun logFailure(apiName: String, e: Throwable) {
        when (e) {
            is HttpException -> Log.e(
                LOG_TAG,
                "[$apiName] failed HTTP ${e.code()} ${e.message()}",
                e,
            )
            is IOException -> Log.e(LOG_TAG, "[$apiName] network: ${e.message}", e)
            else -> Log.e(LOG_TAG, "[$apiName] ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is HttpException -> when (code()) {
            401 -> "로그인이 필요합니다. (401)"
            403 -> "권한이 없습니다. (403)"
            404 -> "요청한 채팅을 찾을 수 없습니다. (404)"
            400 -> "요청 형식이 서버와 맞지 않습니다. (400)"
            in 500..599 -> "서버 오류가 발생했습니다. (HTTP ${code()})"
            else -> message() ?: "요청에 실패했습니다. (${code()})"
        }
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> localizedMessage ?: "오류가 발생했습니다."
    }
}
