package com.example.user.swiftly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_SHORT;

public class TravellerMapActivity extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout,mtest,mAccept,mReceived ;
    private  String senderId = "";
    private boolean isLogginOUt = false;
    private SupportMapFragment mapFragment;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private LatLng packLocation0,packLocation1;
    private  Marker packagemarker0,packagemarker1;
    private int count = 5;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveller_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TravellerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else {
            mapFragment.getMapAsync(this);
        }
        polylines = new ArrayList<>();
        mLogout = (Button) findViewById(R.id.logout);
        mAccept = (Button) findViewById(R.id.accept);
        mAccept.setVisibility(View.GONE);
        mReceived =(Button) findViewById(R.id.received);
        mReceived.setVisibility(View.GONE);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLogginOUt = true;
                disconnectTraveller();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TravellerMapActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedSender();


    }

    private void getAssignedSender(){

        Toast.makeText(getApplicationContext(), "Assigned method called",Toast.LENGTH_SHORT).show();

        String transporterId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference assignedSenderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporter").child(transporterId).child("deliveryId");
        assignedSenderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                            senderId = dataSnapshot.getValue().toString();
                            getPickupLocation();
                    Toast.makeText(getApplicationContext(), "pickup location found",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "pickup location not found",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private  void getPickupLocation(){
        DatabaseReference senderPickupLocation = FirebaseDatabase.getInstance().getReference().child("packageRequest").child(senderId).child("l");
        senderPickupLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    if(map.get(0) != null){
                        locationLat =Double.parseDouble( map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLong =Double.parseDouble( map.get(1).toString());
                    }

                    LatLng courierLatLng = new LatLng(locationLat,locationLong);

                     mMap.addMarker(new MarkerOptions().position(courierLatLng).title("Pick up Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.box)));

                }
                else
                    Toast.makeText(getApplicationContext(), "pickup method called but failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);


        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (getApplicationContext() != null) {
                        mLastLocation = location;

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


                    }
                }
            }
        };
        double locationLat0 = 23.81667066;
        double locationLong0 = 90.42721768;
         packLocation0 = new LatLng(locationLat0, locationLong0);

        double locationLat1 = 23.81336551;
        double locationLong1 = 90.42721768;
        packLocation1 = new LatLng(locationLat1, locationLong1);

        packagemarker0 = mMap.addMarker(new MarkerOptions().position(packLocation0).title("package Destination : Gulshan").icon(BitmapDescriptorFactory.fromResource(R.mipmap.box)));
        packagemarker1 =  mMap.addMarker(new MarkerOptions().position(packLocation1).title("package Destination : Uttara Sector 13 park ").icon(BitmapDescriptorFactory.fromResource(R.mipmap.box)));
        mMap.setOnInfoWindowClickListener(this);
    }




    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        mGoogleApiClient.connect();



    }


    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("transporterAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("transporterWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (senderId) {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                    break;

                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }


        }
    }




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TravellerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {



    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void disconnectTraveller(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transporterAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

    }

    private boolean getPackageAround = false;
    List<Marker> markers = new ArrayList<>();
    private void getAvailablePackage(){
        getPackageAround =true;
        Toast.makeText(getApplicationContext(), "Package method called",LENGTH_SHORT).show();
        DatabaseReference packageLocation = FirebaseDatabase.getInstance().getReference().child("packageRequest");

        GeoFire geoFire = new GeoFire(packageLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(),mLastLocation.getLatitude()),20);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng packageLocation = new LatLng(location.latitude,location.longitude);

                Marker mPackageMarker = mMap.addMarker(new MarkerOptions().position(packageLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.box)));
                mPackageMarker.setTag(key);
                markers.add(mPackageMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(Objects.equals(markerIt.getTag(), key)){
                        markerIt.remove();

                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers) {
                    if (Objects.equals(markerIt.getTag(), key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));

                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(!isLogginOUt) {
            disconnectTraveller();

        }

    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        if(marker.getTitle().equals("package Destination : Uttara Sector 13 park ")){
            getRouteToMarker(packLocation1);
            Toast.makeText(this,"Found",Toast.LENGTH_SHORT).show();
            count = 0;

        }
        else if (marker.getTitle().equals("package Destination : Gulshan")){
            getRouteToMarker(packLocation0);

            count = 1;

        }

        mAccept.setVisibility(View.VISIBLE);
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TravellerMapActivity.this, "Package Request Accepted", Toast.LENGTH_SHORT).show();
                mReceived.setVisibility(View.VISIBLE);
                mReceived.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(TravellerMapActivity.this, "Package received", Toast.LENGTH_SHORT).show();
                        if(count == 1) {
                            packagemarker0.remove();
                        }
                         if(count==0) {
                            packagemarker1.remove();
                        }
                    }
                });
            }
        });


    }
    private void getRouteToMarker(LatLng packLocation) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyAt3JjJC_Qv8K8_g0vaZOZXdEuwLUiVKCE")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                 .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), packLocation)
                .build();
        routing.execute();

        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRoutingCancelled() {

    }
}

