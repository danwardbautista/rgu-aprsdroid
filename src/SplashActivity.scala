package org.aprsdroid.app

import _root_.android.app.Activity
import _root_.android.content.Intent
import _root_.android.os.Bundle
import _root_.android.os.Handler
import _root_.android.os.Looper

class SplashActivity extends Activity {

	private val SPLASH_DELAY: Long = 2000 // 2 seconds

	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.splash_screen)

		// Use Handler to delay transition to main activity
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable {
			def run(): Unit = {
				// Start the main APRSdroid activity
				val mainIntent = new Intent(SplashActivity.this, classOf[APRSdroid])
				startActivity(mainIntent)
				finish() // Close splash activity so user can't navigate back to it

				// Add a smooth transition
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
			}
		}, SPLASH_DELAY)
	}

	override def onBackPressed() {
		// Prevent user from going back from splash screen
		// Optional: you could allow it if you want users to exit during splash
		// super.onBackPressed()
	}
}