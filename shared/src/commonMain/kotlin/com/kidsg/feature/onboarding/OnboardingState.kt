package com.kidsg.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Onboarding Stage Progression in the Story Sequence:
 * INTRO → CHARACTER_ENTRY → CHARACTER_SELECTION → CHARACTER_SELECTED →
 * OTHER_CHARACTER_EXIT → TROLLEY_ENTRY → TROLLEY_PICKUP → WALKING → LOGO_TRANSITION → WELCOME → COMPLETE
 */
enum class OnboardingStage {
    INTRO,
    CHARACTER_ENTRY,
    CHARACTER_SELECTION,
    CHARACTER_SELECTED,
    OTHER_CHARACTER_EXIT,
    TROLLEY_ENTRY,
    TROLLEY_PICKUP,
    WALKING,
    LOGO_TRANSITION,
    WELCOME,
    COMPLETE
}

enum class CharacterType {
    BOY,
    GIRL
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
    val stage: OnboardingStage = OnboardingStage.INTRO,
    val selectedCharacter: CharacterType? = null,
    val isTransitioning: Boolean = false,
    val boyPositionX: Float = -0.2f, // Fraction of width (-0.2 = off-screen left)
    val girlPositionX: Float = 1.2f,  // Fraction of width (1.2 = off-screen right)
    val trolleyPositionX: Float = 1.3f,
    val boyPose: CharacterPose = CharacterPose(isFacingRight = true),
    val girlPose: CharacterPose = CharacterPose(isFacingRight = false),
    val headlineText: String = "Small Supplies. Big Futures.",
    val subtitleText: String = "Your neighborhood stationery quick-commerce app"
)

/**
 * Centralized State Machine for Interactive Animated Storyboarding
 */
class OnboardingStateHolder(
    private val scope: CoroutineScope,
    private val onCompleteCallback: () -> Unit
) {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var animJob: Job? = null

    init {
        // Start from Intro stage
        runIntroSequence()
    }

    fun startFromIntro() {
        animJob?.cancel()
        _uiState.update { OnboardingUiState(stage = OnboardingStage.INTRO) }
        runIntroSequence()
    }

    private fun runIntroSequence() {
        animJob?.cancel()
        animJob = scope.launch {
            // Screen 1: INTRO setup
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.INTRO,
                    headlineText = "Small Supplies. Big Futures.",
                    subtitleText = "Let's get you started!",
                    boyPositionX = -0.3f,
                    girlPositionX = 1.3f
                )
            }
            delay(2800)
            // Auto advance or wait for user to click "Let's get started"
            transitionToCharacterEntry()
        }
    }

    fun onGetStartedClicked() {
        if (_uiState.value.stage == OnboardingStage.INTRO) {
            transitionToCharacterEntry()
        }
    }

    private fun transitionToCharacterEntry() {
        animJob?.cancel()
        animJob = scope.launch {
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.CHARACTER_ENTRY,
                    headlineText = "Who's shopping today?",
                    subtitleText = "Find the perfect stationery for your world",
                    boyPositionX = -0.2f,
                    girlPositionX = 1.2f,
                    boyPose = CharacterPose(type = PoseType.WALK, isFacingRight = true),
                    girlPose = CharacterPose(type = PoseType.WALK, isFacingRight = false)
                )
            }

            // Animate Boy from -0.2f to 0.28f, Girl from 1.2f to 0.72f over 1200ms
            val steps = 24
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                val boyX = -0.2f + progress * (0.28f - (-0.2f))
                val girlX = 1.2f - progress * (1.2f - 0.72f)
                val walkFrame = (i % 4)

                _uiState.update {
                    it.copy(
                        boyPositionX = boyX,
                        girlPositionX = girlX,
                        boyPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = true),
                        girlPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = false)
                    )
                }
                delay(40)
            }

            // Both reach center positions and stand idle
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.CHARACTER_SELECTION,
                    boyPositionX = 0.28f,
                    girlPositionX = 0.72f,
                    boyPose = CharacterPose(type = PoseType.IDLE, isFacingRight = true),
                    girlPose = CharacterPose(type = PoseType.IDLE, isFacingRight = false)
                )
            }
        }
    }

    fun onSelectCharacter(character: CharacterType) {
        val currentState = _uiState.value
        if (currentState.stage != OnboardingStage.CHARACTER_SELECTION &&
            currentState.stage != OnboardingStage.CHARACTER_ENTRY
        ) {
            return
        }

        animJob?.cancel()
        animJob = scope.launch {
            // 1. CHARACTER_SELECTED: Selected character reacts in EXCITED pose
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.CHARACTER_SELECTED,
                    selectedCharacter = character,
                    headlineText = "Who's shopping today?",
                    boyPose = if (character == CharacterType.BOY) CharacterPose(type = PoseType.EXCITED, isFacingRight = true) else CharacterPose(type = PoseType.IDLE, isFacingRight = true),
                    girlPose = if (character == CharacterType.GIRL) CharacterPose(type = PoseType.EXCITED, isFacingRight = false) else CharacterPose(type = PoseType.IDLE, isFacingRight = false)
                )
            }
            delay(600)

            // 2. OTHER_CHARACTER_EXIT: Non-selected character walks off screen
            _uiState.update { it.copy(stage = OnboardingStage.OTHER_CHARACTER_EXIT) }
            val exitSteps = 16
            val startGirlX = _uiState.value.girlPositionX
            val startBoyX = _uiState.value.boyPositionX

            for (i in 1..exitSteps) {
                val progress = i.toFloat() / exitSteps
                val walkFrame = (i % 4)

                _uiState.update { state ->
                    if (character == CharacterType.BOY) {
                        // Girl turns around and walks right off screen (to 1.3f)
                        val newGirlX = startGirlX + progress * (1.3f - startGirlX)
                        state.copy(
                            girlPositionX = newGirlX,
                            girlPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = true)
                        )
                    } else {
                        // Boy turns around and walks left off screen (to -0.3f)
                        val newBoyX = startBoyX - progress * (startBoyX - (-0.3f))
                        state.copy(
                            boyPositionX = newBoyX,
                            boyPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = false)
                        )
                    }
                }
                delay(35)
            }

            // 3. TROLLEY_ENTRY: Trolley enters with stationery
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.TROLLEY_ENTRY,
                    headlineText = "Great choice!",
                    subtitleText = "All the essentials for brighter tomorrows",
                    trolleyPositionX = if (character == CharacterType.BOY) 1.2f else -0.3f
                )
            }

            // Move chosen character to center & trolley towards character
            val targetCharX = 0.38f
            val targetTrolleyX = 0.58f

            val trolleySteps = 20
            val initialCharX = if (character == CharacterType.BOY) _uiState.value.boyPositionX else _uiState.value.girlPositionX
            val initialTrolleyX = if (character == CharacterType.BOY) 1.2f else -0.3f

            for (i in 1..trolleySteps) {
                val progress = i.toFloat() / trolleySteps
                val walkFrame = (i % 4)
                val newCharX = initialCharX + progress * (targetCharX - initialCharX)
                val newTrolleyX = initialTrolleyX + progress * (targetTrolleyX - initialTrolleyX)

                _uiState.update { state ->
                    if (character == CharacterType.BOY) {
                        state.copy(
                            boyPositionX = newCharX,
                            trolleyPositionX = newTrolleyX,
                            boyPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = true)
                        )
                    } else {
                        state.copy(
                            girlPositionX = newCharX,
                            trolleyPositionX = newTrolleyX,
                            girlPose = CharacterPose(type = PoseType.WALK, walkFrame = walkFrame, isFacingRight = true)
                        )
                    }
                }
                delay(40)
            }

            // 4. TROLLEY_PICKUP: Character reaches handle and grabs trolley
            _uiState.update { state ->
                state.copy(
                    stage = OnboardingStage.TROLLEY_PICKUP,
                    boyPose = if (character == CharacterType.BOY) CharacterPose(type = PoseType.PUSH_TROLLEY, isFacingRight = true) else state.boyPose,
                    girlPose = if (character == CharacterType.GIRL) CharacterPose(type = PoseType.PUSH_TROLLEY, isFacingRight = true) else state.girlPose
                )
            }
            delay(700)

            // 5. WALKING: Character + Trolley walk together off screen to the RIGHT
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.WALKING,
                    headlineText = "On to a brighter day!",
                    subtitleText = "Fast 15-min delivery to your doorstep"
                )
            }

            val walkOffSteps = 28
            val startWalkCharX = 0.38f
            val startWalkTrolleyX = 0.58f

            for (i in 1..walkOffSteps) {
                val progress = i.toFloat() / walkOffSteps
                val walkFrame = (i % 4)
                val charX = startWalkCharX + progress * (1.3f - startWalkCharX)
                val trolleyX = startWalkTrolleyX + progress * (1.5f - startWalkTrolleyX)

                _uiState.update { state ->
                    if (character == CharacterType.BOY) {
                        state.copy(
                            boyPositionX = charX,
                            trolleyPositionX = trolleyX,
                            boyPose = CharacterPose(type = PoseType.PUSH_TROLLEY, walkFrame = walkFrame, isFacingRight = true)
                        )
                    } else {
                        state.copy(
                            girlPositionX = charX,
                            trolleyPositionX = trolleyX,
                            girlPose = CharacterPose(type = PoseType.PUSH_TROLLEY, walkFrame = walkFrame, isFacingRight = true)
                        )
                    }
                }
                delay(35)
            }

            // 6. LOGO_TRANSITION: Pencil draws line & KidsG logo reveals
            _uiState.update {
                it.copy(
                    stage = OnboardingStage.LOGO_TRANSITION,
                    headlineText = "KidsG",
                    subtitleText = "Small Supplies. Big Futures."
                )
            }
            delay(1800)

            // 7. WELCOME: Welcome screen with celebratory character
            _uiState.update { state ->
                state.copy(
                    stage = OnboardingStage.WELCOME,
                    headlineText = "Welcome to KidsG!",
                    subtitleText = "Let's make learning more fun ✨",
                    boyPositionX = if (character == CharacterType.BOY) 0.5f else -0.3f,
                    girlPositionX = if (character == CharacterType.GIRL) 0.5f else 1.3f,
                    trolleyPositionX = 0.5f,
                    boyPose = CharacterPose(type = PoseType.EXCITED, isFacingRight = true),
                    girlPose = CharacterPose(type = PoseType.EXCITED, isFacingRight = false)
                )
            }
        }
    }

    fun onStartShoppingClicked() {
        animJob?.cancel()
        animJob = scope.launch {
            _uiState.update { it.copy(stage = OnboardingStage.COMPLETE) }
            delay(300)
            onCompleteCallback()
        }
    }

    fun onSkipClicked() {
        animJob?.cancel()
        animJob = scope.launch {
            _uiState.update { it.copy(stage = OnboardingStage.COMPLETE) }
            delay(150)
            onCompleteCallback()
        }
    }
}
