package com.remotejobs.ui

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.remotejobs.ApplicationTest
import com.remotejobs.io.app.R
import com.remotejobs.io.app.jobs.view.home.HomeFragment
import com.remotejobs.io.app.view.MainActivity
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Rule
    @JvmField
    var serverRule = InstrumentedTestRequestMatcherRule()

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    val app = InstrumentationRegistry.getTargetContext().applicationContext as ApplicationTest

    @Before
    fun setUp() {
        activityRule.activity.supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment(), "home")
    }

    @Test
    fun sholdListJobs() {
        serverRule.addFixture(200, "remote-jobs.json")
                .ifRequestMatches()
                .methodIs(HttpMethod.GET)
                .pathMatches(CoreMatchers.endsWith("/fetchJobs"))

        activityRule.launchActivity(null)


        onView(withText("Dev")).check(matches(isDisplayed()))
    }
}