package com.freshkitchen.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AnalyticsTipsUiState(val headline: String = "분석 / 팁")

class AnalyticsTipsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsTipsUiState())
    val uiState: StateFlow<AnalyticsTipsUiState> = _uiState.asStateFlow()
}
