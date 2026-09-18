package com.nailbitebreaker.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.nailbitebreaker.MainActivity
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_displaysTitle() {
        composeTestRule.onNodeWithText("NailBiteBreaker").assertExists()
    }

    @Test
    fun homeScreen_clickingUrgeButton_navigatesToCoach() {
        // Tapping the "I Feel the Urge!" button should navigate to Coaching screen
        composeTestRule.onNodeWithText("I Feel\nthe Urge!").performClick()
        
        // Check if we are on the coaching session screen
        composeTestRule.onNodeWithText("Coaching Session").assertExists()
    }
}
