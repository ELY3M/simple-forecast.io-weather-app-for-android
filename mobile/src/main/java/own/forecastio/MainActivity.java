package own.forecastio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.location.Location;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;


public class MainActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = "forecastio";

    private String url = "https://api.forecast.io/forecast/getyourownapikey!!!/";
    private String finalurl = "setup";
    private handlejson obj;

    private TextView mygps;
    private ImageView icon;
    private TextView text;
    private Button getweather;
    private double mylat = 0.0;
    private double mylon = 0.0;
    private String mytemp = "103";
    private String myicon = "sunny";
    private String finalicon = "sunny";
    private String myweather = "sunny";
    private int updatecount = 0;
    private GoogleApiClient GoogleApiClient;
    public static final long GPSUPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = GPSUPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGoogleAPI();
        mygps = (TextView) findViewById(R.id.mygps);
        icon = (ImageView) findViewById(R.id.icon);
        text = (TextView) findViewById(R.id.text);
        getweather = (Button) findViewById(R.id.getweather);


    }


    public void update() {
        ///get weather////
        text.setText("\nWeather\n");
        LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApiClient, mLocationRequest, this);
        mRequestingUpdates = true;
        if (isOnline(getApplicationContext())) {
            finalurl = url + mylat + "," + mylon;
            Log.i(TAG, "finalurl: " + finalurl);
            obj = new handlejson(finalurl);
            obj.fetchJSON();
            while (obj.parsingComplete) ;
            mytemp = obj.getTemp();
            myicon = obj.getIcon();
            myweather = obj.getWeather();
            finalicon = myicon.replace('-', '_');
            Log.i(TAG, myicon);
            Log.i(TAG, finalicon);
            Log.i(TAG, mytemp);
            Log.i(TAG, myweather);
            double finaltemp = Math.ceil(Double.valueOf(mytemp));
            mytemp = String.valueOf((int) finaltemp) + "°F";
            text.append(mytemp + "\n");
            text.append(myweather + "\n");
            int res = getResources().getIdentifier(finalicon, "drawable", getApplicationContext().getPackageName());
            Log.i(TAG, String.valueOf(icon));
            Log.i(TAG, String.valueOf(res));
            icon.setImageResource(res);
            text.append("\n");
            text.append(myicon + "\n");
            text.append(finalicon + "\n");



        SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
        Calendar c = Calendar.getInstance();
        String mytimestamp = timestamp.format(c.getTime());
        updatecount++;
        text.append("Last Update: " + mytimestamp + "\nUpdate Count: " + updatecount + "\n");
        ///Toast.makeText(getApplicationContext(), "Weather Update: " + mytimestamp + "  Update Count: " + updatecount, Toast.LENGTH_SHORT).show();

    } else { //internet check
        SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
        Calendar c = Calendar.getInstance();
        String mytimestamp = timestamp.format(c.getTime());
        text.append("Failed Update (Offline): " + mytimestamp + "\nUpdate Count: " + updatecount + "\n");
    }
}

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }


    public void getweather(View view) {
        update();

    }

    private void initializeGoogleAPI() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            GoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleApiClient.connect();
        Log.i(TAG, "Connected to GoogleApiClient in onStart");
    }



    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApiClient);
            mylat = mCurrentLocation.getLatitude();
            mylon = mCurrentLocation.getLongitude();
            mygps.setText("lat: " + mylat + " lon:" + mylon + " (lastloc)");
            Log.i(TAG, "onConnected lat: " + mylat + " lon: " + mylon);
            //make sure to setup latlon with weather url
            finalurl = url + mylat + "," + mylon;
        }
        startUpdates();
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mylat = mCurrentLocation.getLatitude();
        mylon = mCurrentLocation.getLongitude();
        mygps.setText("lat: " + mylat + " lon:" + mylon);
        Log.i(TAG, "onLocationChanged lat: " + mylat + " lon: " + mylon);
        //make sure to setup latlon with weather url
        finalurl = url + mylat + "," + mylon;

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        GoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPSUPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void startUpdates() {
        if (GoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApiClient, mLocationRequest, this);
            mRequestingUpdates = true;
            Log.i(TAG, "Updates started");
        }

    }

    private void stopUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(GoogleApiClient, this);
        mRequestingUpdates = false;
        Log.i(TAG, "Updates stopped");


    }



}
