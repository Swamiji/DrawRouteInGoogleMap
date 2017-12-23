package com.example.amit.drawroutegooglemap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.fragment;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //private GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private LatLng delhi = new LatLng(28.6139, 77.2090);
    private LatLng chandigarh = new LatLng(30.7333, 76.7794);
    private LatLng kolkata = new LatLng(22.572646, 88.363895);
    EditText Search_Destination;
    Button Btn_Search;
    List<Address> addresslist = null;
    double latitude, longitude;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Spinner mSprPlaceType;
    String[] mPlaceType = null;
    String[] mPlaceTypeName = null;
    double mLatitude = 0;
    double mLongitude = 0;
    PlaceAutocompleteFragment placeAutoComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available.");
        }
        // Array of place types
        mPlaceType = getResources().getStringArray(R.array.place_type);

        // Array of place type names
        mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);

        // Creating an array adapter with an array of Place types
        // to populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);

        // Getting reference to the Spinner
        mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);

        // Setting adapter on Spinner to set place types
        mSprPlaceType.setAdapter(adapter);

        Button btnFind;

        // Getting reference to Find Button
        btnFind = (Button) findViewById(R.id.btn_find);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Getting Google Map
        //mMap.setMyLocationEnabled(true);
        //locationManager.requestLocationUpdates(Provider,20000,0, (android.location.LocationListener) getApplicationContext());

        // Setting click event lister for the find button
        btnFind.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int selectedPosition = mSprPlaceType.getSelectedItemPosition();
                String type = mPlaceType[selectedPosition];

                StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                sb.append("location=" + mLatitude + "," + mLongitude);
                sb.append("&radius=5000");
                sb.append("&types=" + type);
                sb.append("&sensor=true");
                sb.append("&key=AIzaSyA4Svqj1FK3IteT8TOz0nbnPzV3JhXJMpQ");

                // Creating a new non-ui thread task to download json data
                PlacesTask placesTask = new PlacesTask();

                // Invokes the "doInBackground()" method of the class PlaceTask
                placesTask.execute(sb.toString());

            }
        });


        // Google Play Services are available
    }

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) MapsActivity.this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();*/
    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onLocationChanged(Location location) {
        Search_Destination = (EditText) findViewById(R.id.search_destination);
        Btn_Search = (Button) findViewById(R.id.btnSearch);
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String Provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(Provider);
        //locationManager.requestLocationUpdates(Provider,20000,0, this);
        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        final LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");
        Btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Edit Text Search for destination

                String Search_des=Search_Destination.getText().toString();
                if(Search_des !=null||!Search_des.equals("")){
                    Geocoder geocoder=new Geocoder(getApplicationContext());
                    try {
                        addresslist=geocoder.getFromLocationName(Search_des,1);
                        //mMap.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address=addresslist.get(0);
                    LatLng SearchPosition=new LatLng(address.getLatitude(),address.getLongitude());

                    //mMap.addMarker(new MarkerOptions().position(kolkata).title("Kolkata"));
                   // mMap.addMarker(new MarkerOptions().position(kolkata).title("Kolkata"));

                    mMap.addMarker(new MarkerOptions().position(SearchPosition).title("Search Place"));
                    //String str_origin = "origin=" + kolkata.latitude + "," + kolkata.longitude;

                    String str_origin = "origin=" + latLng.latitude + "," + latLng.longitude;
                    //String str_dest = "destination=" + chandigarh.latitude + "," + chandigarh.longitude;
                    //String str_dest1="destination=" +kolkata.latitude+"," +kolkata.longitude;
                    String str_dest2="destination=" +SearchPosition.latitude+"," +SearchPosition.longitude;
                    String sensor = "sensor=false";
                    //String parameters = str_origin + "&" + str_dest + "&" + str_dest1 + "&" + sensor;

                    String parameters = str_origin + "&"  + str_dest2 + "&" + sensor;
                    String output = "json";
                    String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

                    Log.d("onMapClick", url.toString());
                    FetchUrl FetchUrl = new FetchUrl();
                    FetchUrl.execute(url);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(SearchPosition));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    Location My_location = new Location("MyPosition");
                    My_location.setLatitude(latLng.latitude);
                    My_location.setLongitude(latLng.longitude);


                    Location Search_location=new Location("Search Location");
                    Search_location.setLatitude(SearchPosition.latitude);
                    Search_location.setLongitude(SearchPosition.longitude);
                    //double distance = (delhi_location.distanceTo(Search_location))* 0.000621371 ;
                    //double distance1= (delhi_location.distanceTo(kolkata_location))*0.000621371;
                    double distance = (My_location.distanceTo(Search_location))*0.00158;

                    AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                    alertDialog.setTitle("Info");
                    //alertDialog.setMessage("Distance between"+"\n"+"delhi and chadigarh: "+distance +" miles"+"\n"+"delhi and kolkata:"+distance1+"miles");

                    alertDialog.setMessage("Distance between"+"\n"+"MyPosition and Search Location: "+distance+" km");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private class FetchUrl extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask=new ParserTask();
            parserTask.execute(result);
        }

        private String downloadUrl(String strUrl)throws IOException {
            String data="";
            InputStream inputstream=null;
            HttpURLConnection httpurlConnection=null;
            try{
                URL url=new URL(strUrl);
                httpurlConnection=(HttpURLConnection)url.openConnection();
                httpurlConnection.connect();
                inputstream=httpurlConnection.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(inputstream));
                StringBuilder sb=new StringBuilder();
                String line="";
                while ((line=br.readLine())!=null){
                    sb.append(line);
                }
                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                inputstream.close();
                httpurlConnection.disconnect();
            }
            return data;
            }


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Current Location Latitude and Longitude
       /* Location location=new Location("current_location");
        double Latitude=location.getLatitude();
        double Longitude=location.getLongitude();
        final LatLng myPosition=new LatLng(Latitude,Longitude);*/

        /*mMap.addMarker(new MarkerOptions().position(kolkata).title("Kolkata"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(kolkata));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));*/

       /* mMap.addMarker(new MarkerOptions().position(myPosition).title("MyPosition"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));*/

    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                JSONParserTask parser = new JSONParserTask();
                Log.d("ParserTask", parser.toString());
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(6);
                lineOptions.color(Color.BLACK);
                lineOptions.clickable(true);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;

            }

        }
    }
    /////////////////////////////////////////////////////////////////////////////////////

    /*Nearby Place Search..........................................*/
    private class PlacesTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... url) {
            String data = null;
            // Invoked by execute() method of this object
            try{
                data = downloadUrl1(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            PerserPlaceTask parserTask = new PerserPlaceTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);

        }
    }
    private class PerserPlaceTask extends AsyncTask<String,Intent,List<HashMap<String,String>>>{

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;

            // Invoked by execute() method of this object
            List<HashMap<String, String>> places = null;
            PlaceJSONPersar placeJsonParser = new PlaceJSONPersar();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            // Clears all the existing markers
            //mMap.clear();

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                String name = hmPlace.get("place_name");

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
            }
        }
    }
    /** A method to download json data from url */
    private String downloadUrl1(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

}
