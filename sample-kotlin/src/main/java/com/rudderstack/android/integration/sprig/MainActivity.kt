package com.rudderstack.android.integration.sprig;

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SprigIntegrationFactory.FACTORY.setCurrentFragmentActivity(this@MainActivity)

        findViewById<Button>(R.id.identify_button).setOnClickListener {
            MainApplication.rudderClient.identify(
                "test_user_id",
                RudderTraits()
                    .putEmail("test@gmail.com")
                    .put("v1", 1)
                    .put("v2", "2"),
                null
            )
        }

        findViewById<Button>(R.id.track_button).setOnClickListener {
            MainApplication.rudderClient.track(
                "test_event"
            )
        }

        findViewById<Button>(R.id.track_properties_button).setOnClickListener {
            MainApplication.rudderClient.track(
                "test_event_with_properties",
                RudderProperty()
                    .putValue("key_1", "value_1")
                    .putValue("key_2", "value_2")
            )
        }

        findViewById<Button>(R.id.logout_button).setOnClickListener {
            MainApplication.rudderClient.reset(false)
        }
    }
}
