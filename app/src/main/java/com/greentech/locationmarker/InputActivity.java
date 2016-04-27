package com.greentech.locationmarker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputActivity extends AppCompatActivity {

    TextView tv_location;
    EditText et_info;
    EditText et_building;

    Intent resultIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        tv_location = (TextView) findViewById(R.id.tv_location);
        et_info = (EditText) findViewById(R.id.et_info);
        et_building = (EditText) findViewById(R.id.et_building);

    }

    public void onAddClick(View view) {

        resultIntent = new Intent();
        resultIntent.putExtra("Info", et_info.getText().toString());
        resultIntent.putExtra("Building", et_building.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }
}
