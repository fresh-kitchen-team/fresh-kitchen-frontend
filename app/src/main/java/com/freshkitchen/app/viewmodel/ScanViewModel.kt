package com.freshkitchen.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freshkitchen.app.data.scan.ScanRepository
import com.freshkitchen.app.data.scan.ScanResultUiModel
import com.freshkitchen.app.data.scan.simulatedFridgeUiModel
import com.freshkitchen.app.data.scan.simulatedIngredientUiModel
import com.freshkitchen.app.data.scan.simulatedReceiptUiModel
import com.freshkitchen.app.logging.ApiLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanOperationState {
    data object Idle : ScanOperationState

    data object Loading : ScanOperationState

    data class Success(val result: ScanResultUiModel) : ScanOperationState

    data class Error(val message: String) : ScanOperationState
}

class ScanViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = ScanRepository(application)

    private val _operationState = MutableStateFlow<ScanOperationState>(ScanOperationState.Idle)
    val operationState: StateFlow<ScanOperationState> = _operationState.asStateFlow()

    fun resetOperation() {
        _operationState.value = ScanOperationState.Idle
    }

    /**
     * 식재료 탭 — [POST /api/v1/scan/ingredient-image](...ingredient-image).
     * API 미설정 시 로컬 시뮬레이션.
     */
    fun requestIngredientScan(imageUri: Uri, localPreviewUriString: String?) {
        runScan(
            logTag = "ingredient",
            failureMessage = "식재료 스캔에 실패했습니다.",
            simulate = { simulatedIngredientUiModel(localPreviewUriString) },
            apiCall = { repository.scanIngredientImage(imageUri, localPreviewUriString) },
        )
    }

    /**
     * 냉장고 탭 — POST /api/v1/scan/fridge-image
     */
    fun requestFridgeScan(imageUri: Uri, localPreviewUriString: String?) {
        runScan(
            logTag = "fridge",
            failureMessage = "냉장고 스캔에 실패했습니다.",
            simulate = { simulatedFridgeUiModel(localPreviewUriString) },
            apiCall = { repository.scanFridgeImage(imageUri, localPreviewUriString) },
        )
    }

    /**
     * 영수증 탭 — [POST /api/v1/scan/receipt-image](...receipt-image).
     */
    fun requestReceiptScan(imageUri: Uri, localPreviewUriString: String?) {
        runScan(
            logTag = "receipt",
            failureMessage = "영수증 스캔에 실패했습니다.",
            simulate = { simulatedReceiptUiModel(localPreviewUriString) },
            apiCall = { repository.scanReceiptImage(imageUri, localPreviewUriString) },
        )
    }

    /**
     * 스캔 공통 흐름: 로딩 표시 → API 미설정 시 로컬 시뮬 → 실제 호출 → 성공/실패 상태 반영.
     * 세 탭(식재료/냉장고/영수증)의 중복 try/로딩/에러 처리를 한 곳으로 모읍니다.
     */
    private fun runScan(
        logTag: String,
        failureMessage: String,
        simulate: () -> ScanResultUiModel,
        apiCall: suspend () -> Result<ScanResultUiModel>,
    ) {
        viewModelScope.launch {
            _operationState.value = ScanOperationState.Loading
            if (!ScanRepository.isApiConfigured()) {
                ApiLog.w("Scan", "$logTag: API 미설정 → 로컬 시뮬만 수행")
                _operationState.value = ScanOperationState.Success(simulate())
                return@launch
            }
            ApiLog.i("Scan", "$logTag: 실제 스캔 API 호출")
            apiCall().fold(
                onSuccess = { model ->
                    ApiLog.i("Scan", "$logTag: Success items=${model.items.size}")
                    _operationState.value = ScanOperationState.Success(model)
                },
                onFailure = { e ->
                    ApiLog.e("Scan", "$logTag: Failure ${e.message}", e)
                    _operationState.value = ScanOperationState.Error(e.message ?: failureMessage)
                },
            )
        }
    }

    /** [ScanOperationState.Success] 소비 후 [Idle]로 되돌립니다 (네비게이션 직후 호출). */
    fun acknowledgeSuccess() {
        if (_operationState.value is ScanOperationState.Success) {
            _operationState.value = ScanOperationState.Idle
        }
    }

    fun acknowledgeError() {
        if (_operationState.value is ScanOperationState.Error) {
            _operationState.value = ScanOperationState.Idle
        }
    }
}
