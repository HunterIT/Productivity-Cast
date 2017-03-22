package com.kennyhunter.tvtrack;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CastContext mCastContext;
    private CastSession mCastSession;
    private HelloWorldChannel mHelloWorldChannel;
    private SharedPreferences sharedPreferences;
    private TextView target_view, count1_view, count2_view;

    private SessionManagerListener<CastSession> mSessionManagerListener
            = new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarting(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
            Log.d(TAG, "Session started");
            mCastSession = castSession;
            invalidateOptionsMenu();
            startCustomMessageChannel();
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int error) {
            // ignore
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
            // ignore
        }

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
            Log.d(TAG, "Session ended");
            if (mCastSession == castSession) {
                cleanupSession();
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int reason) {
            // ignore
        }

        @Override
        public void onSessionResuming(CastSession castSession, String sessionId) {
            // ignore
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean wasSuspended) {
            Log.d(TAG, "Session resumed");
            mCastSession = castSession;
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int error) {
            // ignore
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        target_view = (TextView) findViewById(R.id.target_input_text);
        count1_view = (TextView) findViewById(R.id.count1_input_text);
        count2_view = (TextView) findViewById(R.id.count2_input_text);

        updateFields();

        // When the user clicks on the button, use Android voice recognition to get text
        Button config = (Button) findViewById(R.id.config_button);
        config.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, Update.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // When the user clicks on the button, use Android voice recognition to get text
        Button count1 = (Button) findViewById(R.id.count1_button);
        count1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setcount1(1);
                updateFields();
            }
        });

        Button voiceButton = (Button) findViewById(R.id.count2_button);
        voiceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setcount2(1);
                updateFields();
            };
        });

        // Setup the CastContext
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);

    }

    public void updateFields() {

        int value = getWeeklycount1() +  getWeeklycount2();
        target_view.setText(String.valueOf(getTarget()  + " / "  + String.valueOf(value)));
        count1_view.setText(getWeeklycount1() + " / "  + getDailycount1());
        count2_view.setText(getWeeklycount2() + " / "  + getDailycount2());

        updateDisplay();

    }

    public int getTarget() {
        int target = sharedPreferences.getInt("target", 0);
        return target;
    }

    public int getDailycount1() {
        int count1 = sharedPreferences.getInt("count1_daily_achieved", 0);
        return count1;
    }

    public int getWeeklycount1() {
        int count1 = sharedPreferences.getInt("count1_total_achieved", 0);
        return count1;
    }

    public int getDailycount2() {
        int count2 = sharedPreferences.getInt("count2_daily_achieved", 0);
        return count2;
    }

    public int getWeeklycount2() {
        int count2 = sharedPreferences.getInt("count2_total_achieved", 0);
        return count2;
    }

    public void setcount1(int value) {
        int weekly = getWeeklycount1();
        int daily = getDailycount1();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("count1_daily_achieved", daily + value);
        editor.putInt("count1_total_achieved", weekly + value);
        editor.apply();
    }

    public void setcount2(int value) {
        int weekly = getWeeklycount2();
        int daily = getDailycount2();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("count2_daily_achieved", daily + value);
        editor.putInt("count2_total_achieved", weekly + value);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        // Setup the menu item for connecting to cast devices
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register cast session listener
        mCastContext.getSessionManager().addSessionManagerListener(mSessionManagerListener,
                CastSession.class);
        if (mCastSession == null) {
            // Get the current session if there is one
            mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister cast session listener
        mCastContext.getSessionManager().removeSessionManagerListener(mSessionManagerListener,
                CastSession.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupSession();
    }

    private void cleanupSession() {
        closeCustomMessageChannel();
        mCastSession = null;
    }

    private void startCustomMessageChannel() {
        if (mCastSession != null && mHelloWorldChannel == null) {
            mHelloWorldChannel = new HelloWorldChannel(getString(R.string.cast_namespace));
            try {
                mCastSession.setMessageReceivedCallbacks(mHelloWorldChannel.getNamespace(),
                        mHelloWorldChannel);
                Log.d(TAG, "Message channel started");
            } catch (IOException e) {
                Log.d(TAG, "Error starting message channel", e);
                mHelloWorldChannel = null;
            }
        }
    }

    private void closeCustomMessageChannel() {
        if (mCastSession != null && mHelloWorldChannel != null) {
            try {
                mCastSession.removeMessageReceivedCallbacks(mHelloWorldChannel.getNamespace());
                Log.d(TAG, "Message channel closed");
            } catch (IOException e) {
                Log.d(TAG, "Error closing message channel", e);
            } finally {
                mHelloWorldChannel = null;
            }
        }
    }
    /*
     * Update the Chromecast display
     */
    private void updateDisplay () {

        int target = getTarget();
        int count1_daily = getDailycount1();
        int count1_weekly = getWeeklycount1();
        int count2_daily = getDailycount2();
        int count2_weekly = getWeeklycount2();

        int target_left = target - count1_weekly - count2_weekly;

        String output_message = "" + target_left + "," + count1_daily + "," + count1_weekly + "," + count2_daily +
                "," + count2_weekly;


        if (mHelloWorldChannel != null) {

            mCastSession.sendMessage(mHelloWorldChannel.getNamespace(), output_message);
        } else {
            Toast.makeText(this, "Not connect to Chromecast", Toast.LENGTH_SHORT).show();
        }
    }

    static class HelloWorldChannel implements MessageReceivedCallback {

        private final String mNamespace;

        /**
         * @param namespace the namespace used for sending messages
         */
        HelloWorldChannel(String namespace) {
            mNamespace = namespace;
        }

        /**
         * @return the namespace used for sending messages
         */
        public String getNamespace() {
            return mNamespace;
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }

    }

}
