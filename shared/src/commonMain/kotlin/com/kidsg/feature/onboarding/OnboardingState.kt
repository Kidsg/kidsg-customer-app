package com.kidsg.feature.onboarding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CharacterType {
    BOY,
    GIRL
}

enum class OnboardingStage {
    CHARACTER_ENTRY,
    CHARACTER_SELECTION,
    BOY_TROLLEY,
    GIRL_TROLLEY,
    TRANSITION,
    WELCOME,
    COMPLETE
}

enum class PoseType {
    IDLE,
    WALK,
    EXCITED,
    PUSH_TROLLEY
}

data class CharacterPose(
    val type: PoseType = PoseType.IDLE,
    val walkFrame: Int = 0,
    val isFacingRight: Boolean = true
)

data class OnboardingUiState(
    val stage: OnboardingStage = OnboardingStage.CHARACTER_ENTRY,
    val selectedCharacter: CharacterType? = null,
    val isAnimationPlaying: Boolean = false,
    val headlineText: String = "Who's shopping today?",
    val subtitleText: String = "Find the perfect stationery for your world"
)

/**
 * Centralized State Machine for KidsG Character Selection Onboarding
 */
class OnboardingStateHolder(
    private val scope: CoroutineScope,
    private val onCompleteCallback: () -> Unit
) {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var transitionJob: Job? = null
    private var boyEntryCompleted = false
    private var girlEntryCompleted = false

    fun onBoyEntryCompleted() {
        boyEntryCompleted = true
        checkEntryCompleted()
    }

    fun onGirlEntryCompleted() {
        girlEntryCompleted = true
        checkEntryCompleted()
    }

    private fun checkEntryCompleted() {
        if (_uiState.value.stage == OnboardingStage.CHARACTER_ENTRY) {
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.CHARACTER_SELECTION,
                    isAnimationPlaying = false
                )
            }
        }
    }

    fun onSelectCharacter(character: CharacterType) {
        val currentState = _uiState.value
        // Guard against duplicate taps or selecting during trolley playback
        if (currentState.isAnimationPlaying &&
            (currentState.stage == OnboardingStage.BOY_TROLLEY ||
             currentState.stage == OnboardingStage.GIRL_TROLLEY ||
             currentState.stage == OnboardingStage.TRANSITION)
        ) {
            return
        }

        transitionJob?.cancel()

        if (character == CharacterType.BOY) {
            _uiState.update {
                it.copy(
                    selectedCharacter = CharacterType.BOY,
                    stage = OnboardingStage.BOY_TROLLEY,
                    isAnimationPlaying = true,
                    headlineText = "Great choice!",
                    subtitleText = "Rolling in with your school stationery essentials"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedCharacter = CharacterType.GIRL,
                    stage = OnboardingStage.GIRL_TROLLEY,
                    isAnimationPlaying = true,
                    headlineText = "Great choice!",
                    subtitleText = "Rolling in with your school stationery essentials"
                )
            }
        }
    }

    fun onTrolleyCompleted() {
        if (_uiState.value.stage != OnboardingStage.BOY_TROLLEY &&
            _uiState.value.stage != OnboardingStage.GIRL_TROLLEY
        ) {
            return
        }

        transitionJob?.cancel()
        transitionJob = scope.launch {
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.TRANSITION,
                    isAnimationPlaying = true,
                    headlineText = "KidsG",
                    subtitleText = "Small Supplies. Big Futures."
                )
            }
            // 450ms branded pencil stroke / logo transition
            delay(450)

            _uiState.update {
                it.copy(
                    stage = OnboardingStage.WELCOME,
                    isAnimationPlaying = false,
                    headlineText = "Welcome to KidsG!",
                    subtitleText = "Let's make learning more fun ✨"
                )
            }
        }
    }

    fun onStartShoppingClicked() {
        transitionJob?.cancel()
        _uiState.update { it.copy(stage = OnboardingStage.COMPLETE) }
        onCompleteCallback()
    }

    fun onSkipClicked() {
        transitionJob?.cancel()
        _uiState.update { it.copy(stage = OnboardingStage.COMPLETE) }
        onCompleteCallback()
    }
}
