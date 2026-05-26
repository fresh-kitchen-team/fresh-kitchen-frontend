package com.example.myfrigelocal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfrigelocal.network.InquiryRepository
import com.example.myfrigelocal.ui.screens.help.InquiryDetailUi
import com.example.myfrigelocal.ui.screens.help.InquiryListMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class InquiryDetailUiState(
    val detail: InquiryDetailUi? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class InquiryDetailViewModel(
    private val repository: InquiryRepository = InquiryRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(InquiryDetailUiState())
    val uiState: StateFlow<InquiryDetailUiState> = _uiState.asStateFlow()

    fun load(inquiryId: Long) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { InquiryDetailUiState(isLoading = true) }
            repository.getInquiryDetail(inquiryId)
                .onSuccess { dto ->
                    _uiState.update {
                        InquiryDetailUiState(
                            isLoading = false,
                            detail = InquiryListMapper.toDetailUi(dto),
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

    private fun Throwable.toUserMessage(): String = when (this) {
        is HttpException -> when (code()) {
            401 -> "로그인이 필요합니다."
            404 -> "문의를 찾을 수 없습니다."
            else -> message() ?: "상세를 불러오지 못했습니다."
        }
        is IOException -> "네트워크 연결을 확인해 주세요."
        else -> localizedMessage ?: "상세를 불러오지 못했습니다."
    }
}
