package com.grvig.financetracker

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Outlives any single screen, for writes that must survive the caller closing. */
object AppScope {
    val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

class FinanceTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SessionManager.restore(this)
        NotificationPoster.ensureChannel(this)
    }
}
