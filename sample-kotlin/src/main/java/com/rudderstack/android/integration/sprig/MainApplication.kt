package com.rudderstack.android.integration.sprig

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication: Application() {
    companion object {
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            BuildConfig.WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(BuildConfig.DATA_PLANE_URL)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .withFactory(SprigIntegrationFactory.FACTORY)
                .withTrackLifecycleEvents(false)
                .withRecordScreenViews(false)
                .withSleepCount(3)
                .build()
        )
    }
}
