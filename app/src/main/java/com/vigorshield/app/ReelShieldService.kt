package com.vigorshield.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ReelShieldService : AccessibilityService() {

    private val adultKeywords = listOf(
        "nsfw", "18+", "nude", "bravulg", "bikini", "sensual", "lingerie", "onlyfans", "hot girl"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // 1. Anti-Tamper: Block user from disabling app in Settings without PIN
        if (packageName.contains("com.android.settings")) {
            val root = rootInActiveWindow
            if (root != null && scanTextForMatch(root, listOf("Vigor Shield", "Force stop", "Uninstall"))) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                Toast.makeText(this, "Action locked! Guardian authorization required.", Toast.LENGTH_SHORT).show()
                val lockIntent = Intent(this, OnboardingActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(lockIntent)
                return
            }
        }

        // 2. Real-Time Auto-Skip: Trigger inside Instagram or YouTube Shorts
        if (packageName.contains("com.instagram.android") || packageName.contains("com.google.android.youtube")) {
            val root = rootInActiveWindow ?: return
            if (scanTextForMatch(root, adultKeywords)) {
                executeSwipeUpGesture()
                incrementSkippedCount()
            }
        }
    }

    private fun scanTextForMatch(node: AccessibilityNodeInfo, targetWords: List<String>): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

        for (word in targetWords) {
            if (text.contains(word) || contentDesc.contains(word)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && scanTextForMatch(child, targetWords)) {
                return true
            }
        }
        return false
    }

    private fun executeSwipeUpGesture() {
        val displayMetrics = resources.displayMetrics
        val middleX = (displayMetrics.widthPixels / 2).toFloat()
        val startY = (displayMetrics.heightPixels * 0.80).toFloat()
        val endY = (displayMetrics.heightPixels * 0.15).toFloat()

        val swipePath = Path().apply {
            moveTo(middleX, startY)
            lineTo(middleX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 150))
            .build()

        dispatchGesture(gesture, null, null)
    }

    private fun incrementSkippedCount() {
        val prefs = getSharedPreferences("VigorPrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("reels_skipped", 0)
        prefs.edit().putInt("reels_skipped", current + 1).apply()
    }

    override fun onInterrupt() {}
}
