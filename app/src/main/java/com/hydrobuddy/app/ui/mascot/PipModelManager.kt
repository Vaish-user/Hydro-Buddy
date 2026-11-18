package com.hydrobuddy.app.ui.mascot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe model selection manager allowing runtime switching between
 * the production asset (models/pip.glb) and the V2 upgrade asset (models/pip_v2.glb).
 * Defaults safely to the production asset.
 */
object PipModelManager {
    const val MODEL_PATH_PRODUCTION = "models/pip.glb"
    const val MODEL_PATH_V2 = "models/pip_v2.glb"

    enum class ModelVersion(val displayName: String, val assetPath: String) {
        PRODUCTION("Production Pip (v1)", MODEL_PATH_PRODUCTION),
        V2("Pip V2 (Expressive & Modular)", MODEL_PATH_V2)
    }

    private val _activeModelVersion = MutableStateFlow(ModelVersion.PRODUCTION)
    val activeModelVersion: StateFlow<ModelVersion> = _activeModelVersion.asStateFlow()

    val activeModelPath: String
        get() = _activeModelVersion.value.assetPath

    fun setModelVersion(version: ModelVersion) {
        _activeModelVersion.value = version
    }

    fun isV2Active(): Boolean = _activeModelVersion.value == ModelVersion.V2
}
