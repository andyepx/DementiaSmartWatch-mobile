package com.agileninjas.dementiasmartwatch;

import com.agileninjas.dementiasmartwatch.util.SystemUiHider;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Main extends Activity {

    public static final String EXTRA_MESSAGE = "DementiaSmartWatch";
    public static final String PROPERTY_REG_ID = "DSW_QUT_000001";
    private static final String PROPERTY_APP_VERSION = "1.0";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "676125543177";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "DSWDemo";

    private GoogleCloudMessaging gcm;
    private String regid;

	
	static Main mainContext;
	private EmailPost ep = new EmailPost();
	private SendErrorCode sendErrorCode = new SendErrorCode(); 
	private AlertDialog lastDialog;
	
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(Main.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */

    private class AsyncCaller extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(mainContext);
                }
                regid = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + regid;

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                sendRegistrationIdToBackend(msg);

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the regID - no need to register again.

                final SharedPreferences prefs = getGCMPreferences(mainContext);
                int appVersion = getAppVersion(mainContext);
                Log.i(TAG, "Saving regId on app version " + appVersion);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PROPERTY_REG_ID, regid);
                editor.putInt(PROPERTY_APP_VERSION, appVersion);
                editor.commit();

            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            //Log.i(msg);
            return null;
            //return msg;
        }

        //@Override
        //protected void onPostExecute(String msg) {
            //Log.d(msg);
        //}
    }

    private void sendRegistrationIdToBackend(String msg) {

        Log.i(TAG, msg);

    }

    private void registerInBackground() {

        new AsyncCaller().execute();

    }

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Setting static context
		mainContext = this;

        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(mainContext);

        Log.i(TAG, regid);

        if (regid.isEmpty()) {
            registerInBackground();
        }
		
		//Battery check
		BatteryCheck bc = new BatteryCheck();
		bc.getBatterLevel(this);
		
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		//final View controlsView2 = findViewById(R.id.fullscreen_content_controls2);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight()*2;
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
							//controlsView2
							//		.animate()
							//		.translationY(visible ? 0 : mControlsHeight)
							//		.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
							//controlsView2.setVisibility(visible ? View.VISIBLE
							//		: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
					
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.fullscreen_content_controls).setOnTouchListener(
				mDelayHideTouchListener);
		//findViewById(R.id.fullscreen_content_controls2).setOnTouchListener(
		//		mDelayHideTouchListener2);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			sendErrorCode.sendErrorCode(Main.this, 3);
			Toast.makeText(Main.this.getApplicationContext(), getResources().getString(R.string.panic_button_send), Toast.LENGTH_SHORT).show();

			ep.postEmail(getResources().getString(R.string.email_panic_subject), getResources().getString(R.string.email_panic_content));
			//Create alert dialog with OK button only
		    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
		    builder.setMessage(getResources().getString(R.string.patient_name) + "Johnny Bravo\n" + getResources().getString(R.string.relative_name) + "Bunny Runner\n" + getResources().getString(R.string.emergency_contact_number)+ "0412-345-678")
		    		.setCancelable(false)
		    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int id) {
		    				//To-Do
		    			}
		    		});
		    AlertDialog alert = builder.create();
		    lastDialog = alert;
		    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			    alert.show();
		    }
			return false;
		}
	};
	
	View.OnTouchListener mDelayHideTouchListener2 = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
		    AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
		    builder.setMessage(getResources().getString(R.string.patient_name) + "Johnny Bravo\n" + getResources().getString(R.string.relative_name) + "Bunny Runner\n" + getResources().getString(R.string.emergency_contact_number)+ "0412-345-678")
		    		.setCancelable(false)
		    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int id) {
		    				//To-Do
		    			}
		    		});
		    AlertDialog alert = builder.create();
		    lastDialog = alert;
		    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			    alert.show();
		    }
			return false;
		}
	};
	
	Handler mHideHandler = new Handler();
	Handler mHideHandler2 = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	Runnable mHideRunnable2 = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	public AlertDialog getLastDialog()
	{
		return lastDialog;
	}
	
	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
		mHideHandler2.removeCallbacks(mHideRunnable2);
		mHideHandler.postDelayed(mHideRunnable2, delayMillis);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GPSLocation gpsLocation = new GPSLocation();
		if (gpsLocation.checkSignal(this) == true) {
			gpsLocation.start(this);
		} else {
			Log.e("No signal", "No GPS signal");
		}
		sendErrorCode.sendErrorCode(this, 4);
		ep.postEmail(getResources().getString(R.string.email_app_start_subject), getResources().getString(R.string.email_app_start_content));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		sendErrorCode.sendErrorCode(this, 5);
		ep.postEmail(getResources().getString(R.string.email_app_stop_subject), getResources().getString(R.string.email_app_stop_content));
	}
}
