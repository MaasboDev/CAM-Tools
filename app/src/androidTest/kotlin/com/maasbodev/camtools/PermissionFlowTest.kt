package com.maasbodev.camtools

import android.content.Context
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionFlowTest {

    private val uiAutomatorTimeout = 5000L

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext
        revokePermissions()
    }

    @Test
    fun whenPermissionsGrantedFirstTime_cameraScreenIsShown() {
        grantPermissionWhenAsked() // For Camera
        grantPermissionWhenAsked() // For Audio

        composeTestRule.onNodeWithTag("CameraPreview").assertIsDisplayed()
    }

    @Test
    fun whenCameraPermissionDenied_thenGranted_cameraScreenIsShown() {
        denyPermissionWhenAsked()

        val cameraRationaleText = context.getString(R.string.camera_permission_description)
        val okButtonText = context.getString(android.R.string.ok)

        composeTestRule.onNodeWithText(cameraRationaleText).assertIsDisplayed()

        composeTestRule.onNodeWithText(okButtonText).performClick()
        grantPermissionWhenAsked() // Grant Camera
        grantPermissionWhenAsked() // Grant Audio

        composeTestRule.onNodeWithTag("CameraPreview").assertIsDisplayed()
    }

    @Test
    fun whenPermissionsPermanentlyDenied_allOrNothingDialogShowsOpenSettings() {
        val dialogTitle = context.getString(R.string.permission_dialog_title)
        val openSettingsButtonText = context.getString(R.string.all_or_nothing_dialog_settings_button)
        val okButtonText = context.getString(android.R.string.ok)

        denyPermissionWhenAsked()
        composeTestRule.onNodeWithText(okButtonText).performClick()
        denyPermissionWhenAsked()

        denyPermissionWhenAsked()
        composeTestRule.onNodeWithText(okButtonText).performClick()
        denyPermissionWhenAsked()

        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(openSettingsButtonText).assertIsDisplayed()
    }

    @Test
    fun whenAppStartsWithoutPermissions_allOrNothingDialogIsShown() {
        val dialogTitle = context.getString(R.string.permission_dialog_title)
        val grantButtonText = context.getString(R.string.all_or_nothing_dialog_grant_button)

        denyPermissionWhenAsked() // Camera
        denyPermissionWhenAsked() // Audio

        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(grantButtonText).assertIsDisplayed()
    }

    private fun grantPermissionWhenAsked() {
        val permissionButtonText = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> "Allow"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> "While using the app"
            else -> "Allow"
        }
        val button = device.findObject(UiSelector().text(permissionButtonText))
        if (button.waitForExists(uiAutomatorTimeout)) {
            button.click()
        }
    }

    private fun denyPermissionWhenAsked() {
        val button = device.findObject(UiSelector().text("Deny"))
        if (button.waitForExists(uiAutomatorTimeout)) {
            button.click()
        }
    }

    private fun revokePermissions() {
        val packageName = context.packageName
        device.executeShellCommand("pm revoke $packageName android.permission.CAMERA")
        device.executeShellCommand("pm revoke $packageName android.permission.RECORD_AUDIO")
    }
}