package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.network.InquiryRepository
import com.freshkitchen.app.ui.screens.help.InquiryListItemUi
import com.freshkitchen.app.ui.screens.help.InquiryListMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class InquiryListUiState(
    val items: List<InquiryListItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class InquiryListViewModel(
    private val repository: InquiryRepository = InquiryRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(InquiryListUiState())
    val uiState: StateFlow<InquiryListUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getInquiries()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = list.map(InquiryListMapper::toUi),
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is HttpException -> when (code()) {
            401 -> "로그인이 필요합니다."
            else -> message() ?: "목록을 불러오지 못했습니다."
        }
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> localizedMessage ?: "목록을 불러오지 못했습니다."
    }
}
