package com.example.kalkapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScientificButtonsTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSinButton() {
        onView(withId(R.id.btn_nine)).perform(click())
        onView(withId(R.id.btn_zero)).perform(click())
        onView(withId(R.id.btn_sin)).perform(click())
        onView(withId(R.id.btn_equals)).perform(click())
        onView(withId(R.id.display)).check(matches(withText("0.893996663601")))
    }

    @Test
    fun testCosButton() {
        onView(withId(R.id.btn_four)).perform(click())
        onView(withId(R.id.btn_five)).perform(click())
        onView(withId(R.id.btn_cos)).perform(click())
        onView(withId(R.id.btn_equals)).perform(click())
        onView(withId(R.id.display)).check(matches(withText("0.525321988818")))
    }

    @Test
    fun testTanButton() {
        onView(withId(R.id.btn_six)).perform(click())
        onView(withId(R.id.btn_zero)).perform(click())
        onView(withId(R.id.btn_tan)).perform(click())
        onView(withId(R.id.btn_equals)).perform(click())
        onView(withId(R.id.display)).check(matches(withText("0.32004038938")))
    }

    @Test
    fun testSqrtButton() {
        onView(withId(R.id.btn_nine)).perform(click())
        onView(withId(R.id.btn_sqrt)).perform(click())
        onView(withId(R.id.btn_equals)).perform(click())
        onView(withId(R.id.display)).check(matches(withText("3")))
    }
}
