package com.example.user.swiftly;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ReceiverInfoActivity extends AppCompatActivity {

    private Button mbtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_info);

        mbtn3 = (Button) findViewById(R.id.btn3);
        mbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiverInfoActivity.this, PackageInfoActivity.class);
                startActivity(intent);
            }
        });


    }
}
