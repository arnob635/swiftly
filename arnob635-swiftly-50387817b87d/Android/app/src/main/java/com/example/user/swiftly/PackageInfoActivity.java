package com.example.user.swiftly;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PackageInfoActivity extends AppCompatActivity {

    private Button mCbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_info);


        mCbtn = (Button) findViewById(R.id.cbtn);
        mCbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PackageInfoActivity.this,"BUtton works" , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PackageInfoActivity.this, CustomerMapActivity.class);
                startActivity(intent);

            }
        });

    }


}
