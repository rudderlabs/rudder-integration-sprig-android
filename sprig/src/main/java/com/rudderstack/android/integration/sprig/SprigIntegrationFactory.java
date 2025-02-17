package com.rudderstack.android.integration.sprig;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.userleap.EventPayload;
import com.userleap.Sprig;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SprigIntegrationFactory extends RudderIntegration<Sprig> {

    // String constants
    private static final String SPRIG_KEY = "Sprig";
    private static final String ENVIRONMENT_ID = "environmentId";
    private static final String EMAIL_KEY = "email";

    @Nullable
    private Sprig sprig;
    @Nullable
    private static FragmentActivity currentActivity;

    @Override
    public void reset() {
        if (this.sprig != null) {
            this.sprig.logout();
        }
    }

    @Override
    public void dump(RudderMessage rudderMessage) {
        if (sprig != null) {
            if (rudderMessage.getType() != null) {
                switch (rudderMessage.getType()) {
                    case MessageType.TRACK:
                        processTrackEvent(rudderMessage);
                        break;
                    case MessageType.IDENTIFY:
                        processIdentifyEvent(rudderMessage);
                        break;
                    default:
                        RudderLogger.logWarn("SprigIntegrationFactory: MessageType is not valid");
                        break;
                }
            }
        } else {
            RudderLogger.logWarn("SprigIntegrationFactory: Sprig is not initialized");
        }
    }

    @Override
    public Sprig getUnderlyingInstance() {
        return sprig;
    }

    public interface SprigFactory extends Factory {
        void setFragmentActivity(@Nullable FragmentActivity activity);
    }

    public static final SprigFactory FACTORY = new SprigFactory() {
        @Override
        public RudderIntegration<?> create(Object config, RudderClient rudderClient, RudderConfig rudderConfig) {
            return new SprigIntegrationFactory(config);
        }

        @Override
        public String key() {
            return SPRIG_KEY;
        }

        @Override
        public void setFragmentActivity(@Nullable FragmentActivity activity) {
            currentActivity = activity;
        }
    };

    private SprigIntegrationFactory(Object config) {
        String environmentId = "";
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig == null) {
            RudderLogger.logError("Invalid configuration. Aborting Sprig initialization.");
        } else if (RudderClient.getApplication() == null) {
            RudderLogger.logError("RudderClient is not initialized correctly. Application is null. Aborting Sprig initialization.");
        } else {
            if (destinationConfig.containsKey(ENVIRONMENT_ID)) {
                environmentId = (String) destinationConfig.get(ENVIRONMENT_ID);
            }
            if (TextUtils.isEmpty(environmentId)) {
                RudderLogger.logError("Invalid api key. Aborting Sprig initialization.");
                return;
            }

            this.sprig = Sprig.INSTANCE;
            this.sprig.configure(RudderClient.getApplication().getApplicationContext(), environmentId);

            RudderClient.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

                @Override
                public void onActivityCreated(@NonNull Activity activity, @androidx.annotation.Nullable Bundle bundle) {
                    // NO-OP
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    // NO-OP
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    // NO-OP
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                    // NO-OP
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    // NO-OP
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
                    // NO-OP
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    // clearing the activity instance on onDestroy
                    if (activity == currentActivity) {
                        currentActivity = null;
                    }
                }
            });
        }
    }

    private void processTrackEvent(RudderMessage message) {
        if (this.sprig == null) {
            return;
        }
        String eventName = message.getEventName();
        if (eventName == null) {
            return;
        }
        Map<String, Object> properties = message.getProperties();

        EventPayload payload = new EventPayload(eventName, null, null, properties, null, null);

        if (currentActivity == null) {
            this.sprig.track(payload);
        } else {
            this.sprig.trackAndPresent(payload, currentActivity);
        }
    }

    private void processIdentifyEvent(RudderMessage message) {
        if (this.sprig == null) {
            return;
        }
        String userId = message.getUserId();
        if (userId != null) {
            this.sprig.setUserIdentifier(userId);
        }

        Map<String, Object> traits = message.getTraits();

        processSprigAttributes(traits);
    }

    private void processSprigAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            if (attributes.containsKey(EMAIL_KEY)) {
                String email = (String) attributes.get(EMAIL_KEY);
                if (email != null) {
                    this.sprig.setEmailAddress(email);
                }
            }
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                if (Objects.equals(entry.getKey(), EMAIL_KEY)) {
                    continue;
                }
                if (entry.getKey().length() < 256 && !entry.getKey().startsWith("!")) {
                    if (entry.getValue() instanceof String) {
                        this.sprig.setVisitorAttribute(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Number) {
                        this.sprig.setVisitorAttribute(entry.getKey(), getInt(entry.getValue()));
                    } else if (entry.getValue() instanceof Boolean) {
                        this.sprig.setVisitorAttribute(entry.getKey(), (Boolean) entry.getValue());
                    } else {
                        RudderLogger.logWarn(String.format(Locale.US, "%s is not a valid property value. Only String, Bool, Double and Int are accepted as valid attributes. Ignoring property.", entry.getValue()));
                    }
                } else {
                    RudderLogger.logWarn(String.format(Locale.US, "%s is not a valid property name. Property names must be less than 256 characters and cannot start with a '!'. Ignoring property.", entry.getKey()));
                }
            }
        }
    }

    private Integer getInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}
