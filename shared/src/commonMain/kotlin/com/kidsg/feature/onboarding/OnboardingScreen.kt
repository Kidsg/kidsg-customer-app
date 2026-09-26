package com.kidsg.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGResourceImage
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.core.storage.SessionStorage
import com.kidsg.feature.onboarding.components.CharacterSelectionCard
import com.kidsg.feature.onboarding.components.FloatingStationeryBackground
import com.kidsg.feature.onboarding.components.KidsGCharacter
import com.kidsg.feature.onboarding.components.PencilTransitionOverlay
import com.kidsg.feature.onboarding.components.StationeryTrolley
import androidx.compose.ui.layout.ContentScale

/**
 * KidsG Interactive Character-Selection Onboarding Screen
 *
 * Uses the four user-supplied trimmed MP4 video animation assets:
 * - kidsg_boy_enter.mp4 & kidsg_girl_enter.mp4 for initial entrance
 * - kidsg_boy_trolley.mp4 & kidsg_girl_trolley.mp4 for selection sequence
 *
 * Enhancements:
 * - Girl video horizontally mirrored so she faces and looks at the boy!
 * - Organic breathing/idling motion so characters are alive and not frozen after entrance.
 * - Selection controls placed cleanly below the characters for optimal visual balance.
 * - Welcome screen proudly displays the selected character with the stationery trolley.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onExploreGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val stateHolder = remember(coroutineScope) {
        OnboardingStateHolder(
            scope = coroutineScope,
            onCompleteCallback = { onGetStarted() }
        )
    }

    val state by stateHolder.uiState.collectAsState()

    // Save character selection into session storage whenever it changes
    LaunchedEffect(state.selectedCharacter) {
        state.selectedCharacter?.let {
            SessionStorage.saveSelectedCharacter(it.name)
        }
    }

    // KidsG signature warm cream/off-white background blending
    val creamBackground = Color(0xFFFFFBF5)

    // Idle organic breathing motion for characters after arrival
    val idleTransition = rememberInfiniteTransition(label = "idleMotion")
    val idleFloatY by idleTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleFloatY"
    )
    val idleScale by idleTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(creamBackground)
    ) {
        // Subtle ambient floating stationery in the background
        FloatingStationeryBackground(modifier = Modifier.fillMaxSize())

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP HEADER: KidsG Brand Pill & Skip Action
                TopOnboardingHeader(
                    onSkip = { stateHolder.onSkipClicked() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // HEADLINE & SUBTITLE (Animated Text Transitions)
                AnimatedHeadlineArea(
                    stage = state.stage,
                    headlineText = state.headlineText,
                    subtitleText = state.subtitleText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // CHARACTER VIDEO STAGE AREA (Responsive middle section)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 4.dp),
                    contentAlignment = if (state.stage == OnboardingStage.WELCOME || state.stage == OnboardingStage.COMPLETE)
                        Alignment.Center
                    else
                        Alignment.BottomCenter
                ) {
                    when (state.stage) {
                        OnboardingStage.CHARACTER_ENTRY,
                        OnboardingStage.CHARACTER_SELECTION -> {
                            // Initial State: Both Boy and Girl enter and hold standing position
                            // Both characters have subtle breathing animation so they remain alive!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Boy enter video (Left side)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .offset(y = if (state.stage == OnboardingStage.CHARACTER_SELECTION) idleFloatY.dp else 0.dp)
                                        .scale(if (state.stage == OnboardingStage.CHARACTER_SELECTION) idleScale else 1.0f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    KidsGVideoPlayer(
                                        videoResName = "kidsg_boy_enter",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(9f / 16f),
                                        playWhenReady = true,
                                        isLooping = false,
                                        scaleMode = VideoScaleMode.FIT,
                                        onCompleted = { stateHolder.onBoyEntryCompleted() }
                                    )
                                }

                                // Girl enter video (Right side, mirrored horizontally to look at the boy!)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .offset(y = if (state.stage == OnboardingStage.CHARACTER_SELECTION) (-idleFloatY).dp else 0.dp)
                                        .scale(if (state.stage == OnboardingStage.CHARACTER_SELECTION) idleScale else 1.0f),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    KidsGVideoPlayer(
                                        videoResName = "kidsg_girl_enter",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(9f / 16f)
                                            .scale(scaleX = -1f, scaleY = 1f), // Horizontally flipped to face boy!
                                        playWhenReady = true,
                                        isLooping = false,
                                        scaleMode = VideoScaleMode.FIT,
                                        onCompleted = { stateHolder.onGirlEntryCompleted() }
                                    )
                                }
                            }
                        }

                        OnboardingStage.BOY_TROLLEY -> {
                            // Boys selected: Boy trolley animation plays across screen to right
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                KidsGVideoPlayer(
                                    videoResName = "kidsg_boy_trolley",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(9f / 16f),
                                    playWhenReady = true,
                                    isLooping = false,
                                    scaleMode = VideoScaleMode.FIT,
                                    onCompleted = { stateHolder.onTrolleyCompleted() }
                                )
                            }
                        }

                        OnboardingStage.GIRL_TROLLEY -> {
                            // Girls selected: Girl trolley animation plays across screen to right
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                KidsGVideoPlayer(
                                    videoResName = "kidsg_girl_trolley",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(9f / 16f),
                                    playWhenReady = true,
                                    isLooping = false,
                                    scaleMode = VideoScaleMode.FIT,
                                    onCompleted = { stateHolder.onTrolleyCompleted() }
                                )
                            }
                        }

                        OnboardingStage.WELCOME,
                        OnboardingStage.COMPLETE -> {
                            // Celebratory Welcome State with SELECTED BOY OR GIRL AND TROLLEY
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(Color(0xFFFFF7ED))
                                        .border(1.5.dp, Color(0xFFFFD8A8), RoundedCornerShape(28.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isGirl = state.selectedCharacter == CharacterType.GIRL
                                    val resName = if (isGirl) "kidsg_girl_welcome" else "kidsg_boy_welcome"

                                    KidsGResourceImage(
                                        resName = resName,
                                        contentDescription = if (isGirl) "KidsG Girl Student with Stationery Trolley" else "KidsG Boy Student with Stationery Trolley",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = if (state.selectedCharacter == CharacterType.GIRL) "Ready to explore, Star! ✨" else "Ready to explore, Champ! 🚀",
                                    style = KidsGTypography.TitleMedium.copy(
                                        color = KidsGColors.OrangePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1000+ school stationery products at your fingertips",
                                    style = KidsGTypography.BodyMedium.copy(
                                        color = KidsGColors.TextSecondary,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }

                        else -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                // SELECTION CONTROLS: [ BOYS ] & [ GIRLS ]
                // Positioned below characters for optimal visual balance and beauty
                AnimatedVisibility(
                    visible = state.stage == OnboardingStage.CHARACTER_ENTRY ||
                            state.stage == OnboardingStage.CHARACTER_SELECTION ||
                            state.stage == OnboardingStage.BOY_TROLLEY ||
                            state.stage == OnboardingStage.GIRL_TROLLEY,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CharacterSelectionCard(
                            characterType = CharacterType.BOY,
                            isSelected = state.selectedCharacter == CharacterType.BOY,
                            isDeemphasized = state.selectedCharacter == CharacterType.GIRL,
                            onClick = {
                                SessionStorage.saveSelectedCharacter(CharacterType.BOY.name)
                                stateHolder.onSelectCharacter(CharacterType.BOY)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        CharacterSelectionCard(
                            characterType = CharacterType.GIRL,
                            isSelected = state.selectedCharacter == CharacterType.GIRL,
                            isDeemphasized = state.selectedCharacter == CharacterType.BOY,
                            onClick = {
                                SessionStorage.saveSelectedCharacter(CharacterType.GIRL.name)
                                stateHolder.onSelectCharacter(CharacterType.GIRL)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // BOTTOM ACTION AREA (Welcome / Progress indicator)
                BottomActionArea(
                    stage = state.stage,
                    onStartShoppingClicked = { stateHolder.onStartShoppingClicked() }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // KIDSG BRANDED PENCIL / LOGO TRANSITION OVERLAY (300-600ms)
            AnimatedVisibility(
                visible = state.stage == OnboardingStage.TRANSITION,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(creamBackground.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    PencilTransitionOverlay(
                        modifier = Modifier.fillMaxSize(),
                        progress = 1.0f
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Kids",
                                style = KidsGTypography.DisplayLarge.copy(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KidsGColors.BlackText
                                )
                            )
                            Text(
                                text = "G",
                                style = KidsGTypography.DisplayLarge.copy(
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KidsGColors.OrangePrimary
                                )
                            )
                        }
                        Text(
                            text = "Small Supplies. Big Futures.",
                            style = KidsGTypography.TitleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = KidsGColors.TextSecondary,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopOnboardingHeader(onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // KidsG Pill Badge (Clean crisp white, no grey shadow)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(KidsGColors.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kids",
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = "G",
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = KidsGColors.OrangePrimary,
                        fontSize = 15.sp
                    )
                )
            }
        }

        // Skip Button (Clean crisp white, no grey shadow)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(KidsGColors.White)
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                .clickable { onSkip() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Skip ➔",
                style = KidsGTypography.BodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KidsGColors.TextSecondary,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedHeadlineArea(
    stage: OnboardingStage,
    headlineText: String,
    subtitleText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        AnimatedContent(
            targetState = headlineText,
            transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
            label = "headlineText"
        ) { targetHeadline ->
            Text(
                text = targetHeadline,
                style = KidsGTypography.TitleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = KidsGColors.BlackText,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = subtitleText,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "subtitleText"
        ) { targetSubtitle ->
            Text(
                text = targetSubtitle,
                style = KidsGTypography.BodyMedium.copy(
                    color = KidsGColors.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun BottomActionArea(
    stage: OnboardingStage,
    onStartShoppingClicked: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    when (stage) {
        OnboardingStage.WELCOME -> {
            Button(
                onClick = onStartShoppingClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .scale(scale),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
            ) {
                Text(
                    text = "Start Shopping →",
                    style = KidsGTypography.TitleMedium.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }
        }

        else -> {
            // Subtle progress indicator during selection & video playback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (stage == OnboardingStage.BOY_TROLLEY || stage == OnboardingStage.GIRL_TROLLEY)
                                KidsGColors.OrangePrimary
                            else
                                KidsGColors.BorderStrong
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.BorderStrong)
                )
            }
        }
    }
}
