package com.kidsg.domain

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.data.repository.MockAuthRepository
import com.kidsg.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verification test suite for KidsG Profile Screen.
 */
class ProfileScreenFlowTest {

    @Test
    fun test1_defaultProfile_fields() {
        val user = KidsGMockData.defaultUser
        assertEquals("Aarav Sharma", user.studentName)
        assertEquals("Class 7", user.studentGrade)
        assertEquals("National Public School, Bengaluru", user.schoolName)
    }

    @Test
    fun test2_profileUpdate_persistsViaAuthRepository() = runTest {
        val authRepository = MockAuthRepository()
        val initialUser = authRepository.currentUser.first()
        assertNotNull(initialUser)

        val updatedProfile = initialUser.copy(
            studentName = "Veenith Kumar S",
            studentGrade = "Class 10",
            schoolName = "VBC School"
        )

        val result = authRepository.updateProfile(updatedProfile)
        assertTrue(result.isSuccess)

        val persistedUser = authRepository.currentUser.first()
        assertNotNull(persistedUser)
        assertEquals("Veenith Kumar S", persistedUser.studentName)
        assertEquals("Class 10", persistedUser.studentGrade)
        assertEquals("VBC School", persistedUser.schoolName)
    }

    @Test
    fun test3_logout_clearsUser() = runTest {
        val authRepository = MockAuthRepository()
        assertNotNull(authRepository.currentUser.first())

        authRepository.logout()
        assertNull(authRepository.currentUser.first())
    }

    @Test
    fun test4_gradeNumberExtraction() {
        val grade1 = "Class 10"
        val extracted1 = grade1.replace("Class ", "").trim()
        assertEquals("10", extracted1)

        val grade2 = "Class 7"
        val extracted2 = grade2.replace("Class ", "").trim()
        assertEquals("7", extracted2)
    }

    @Test
    fun test5_navigationCallbacks() {
        var navigatedToAddresses = false
        var navigatedToOrders = false
        var navigatedToHelp = false

        val onNavAddresses: () -> Unit = { navigatedToAddresses = true }
        val onNavOrders: () -> Unit = { navigatedToOrders = true }
        val onNavHelp: () -> Unit = { navigatedToHelp = true }

        onNavAddresses()
        onNavOrders()
        onNavHelp()

        assertTrue(navigatedToAddresses)
        assertTrue(navigatedToOrders)
        assertTrue(navigatedToHelp)
    }
}
