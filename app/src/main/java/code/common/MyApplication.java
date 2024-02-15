package code.common;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.hathme.merchat.android.BuildConfig;
import com.hathme.merchat.android.R;
import com.sendbird.android.LogLevel;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.params.InitParams;
import com.sendbird.calls.AuthenticateParams;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.handler.CompletionHandler;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.handler.SendBirdCallListener;

import java.util.UUID;

import code.call.CallService;
import code.utils.AppSettings;
import code.utils.BroadcastUtils;

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();

    private static MyApplication mainApplication;

    public static final String APP_ID = "07D50B3C-A234-4916-9808-62A9B1EAFECA";

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
//        Intent serviceIntent = new Intent(this, CallService.class);
//        startService(serviceIntent);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initSendBirdCall();

        sendbirdChatInit();

        createAlertNotificationChannel();

    }

    private void createAlertNotificationChannel() {

        Uri NOTIFICATION_SOUND_URI = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.tone);
        NotificationChannel androidChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            androidChannel = new NotificationChannel("Alert", getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.RED);

            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            androidChannel.setSound(NOTIFICATION_SOUND_URI, attributes);
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mManager.createNotificationChannel(androidChannel);

        }

    }

    public boolean initSendBirdCall() {
        Log.i(MyApplication.TAG, "[BaseApplication] initSendBirdCall(appId: " + APP_ID + ")");
        Context context = getApplicationContext();


        if (SendBirdCall.init(context, APP_ID)) {
            SendBirdCall.removeAllListeners();
            SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
                @Override
                public void onRinging(DirectCall call) {
                    int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                    Log.i(TAG, "[BaseApplication] onRinging() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                    if (ongoingCallCount >= 2) {
                        call.end();
                        return;
                    }

                    call.setListener(new DirectCallListener() {
                        @Override
                        public void onConnected(DirectCall call) {
                        }

                        @Override
                        public void onEnded(DirectCall call) {

                            int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                            Log.i(TAG, "[BaseApplication] onEnded() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                            BroadcastUtils.sendCallLogBroadcast(context, call.getCallLog());

                            if (ongoingCallCount == 0) {
                                CallService.stopService(context);
                            }
                        }
                    });

                    CallService.onRinging(context, call);
                }
            });

            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.DIALING, R.raw.dialing);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RINGING, R.raw.ringing);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTING, R.raw.reconnecting);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTED, R.raw.reconnected);
            return true;
        }
        return false;
    }

    public void sendbirdChatInit() {
        InitParams initParams = new InitParams(APP_ID, this, false);
        initParams.setLogLevel(LogLevel.ERROR);

        SendbirdChat.init(initParams, new InitResultHandler() {
            @Override
            public void onMigrationStarted() {

            }

            @Override
            public void onInitFailed(@NonNull SendbirdException e) {
            }

            @Override
            public void onInitSucceed() {

            }
        });


    }

    public void setUpVoip() {


        AuthenticateParams params = new AuthenticateParams(AppSettings.getString(AppSettings.userId)).setAccessToken(AppSettings.getString(AppSettings.fcmToken));


        SendBirdCall.authenticate(params, (user, e) -> {

            if (e == null) {
                // The user has been authenticated successfully and is connected to Sendbird server.
                SendBirdCall.registerPushToken(AppSettings.getString(AppSettings.fcmToken), false, new CompletionHandler() {
                    @Override
                    public void onResult(@Nullable SendBirdException e) {

                        if (e == null) {


                        }
                    }
                });
            }
        });
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

    }

    public static synchronized MyApplication getInstance() {
        return mainApplication;
    }
}
