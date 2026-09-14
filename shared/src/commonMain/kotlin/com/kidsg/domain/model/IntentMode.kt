package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class IntentModeType {
    SCHOOL,
    EXAM,
    CREATE,
    NEW_TERM,
    OOPS
}

@Serializable
data class IntentModeInfo(
    val type: IntentModeType,
    val title: String,
    val subtitle: String,
    val badge: String,
    val accentColorHex: String,
    val description: String,
    val promptQuestion: String
)

@Serializable
data class OopsEmergencyItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetQuery: String,
    val emoji: String,
    val estimatedMinutes: Int = 15
)
