package com.greentech.locationmarker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class InputActivity extends AppCompatActivity {

    private TextView tv_location;
    private EditText et_building;
    private Spinner floor_spinner;
    private Spinner type_spinner;
    private String floor, type;
    Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        tv_location = (TextView) findViewById(R.id.tv_location);
        et_building = (EditText) findViewById(R.id.et_building);
        floor_spinner = (Spinner) findViewById(R.id.spinner_floor);
        type_spinner = (Spinner) findViewById(R.id.spinner_type);

        //LOCATION INFO
        StringBuilder l_string = new StringBuilder();

        l_string.append("Longitude: ");
        l_string.append(getIntent().getStringExtra("lng"));
        l_string.append("\n");
        l_string.append("Latitude: ");
        l_string.append(getIntent().getStringExtra("lat"));

        tv_location.setText(l_string.toString());

        //FLOOR SPINNER INFLATION
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> floor_adapter = ArrayAdapter
                .createFromResource(this, R.array.floors_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        floor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floor_spinner.setAdapter(floor_adapter);
        floor_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                floor = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TYPE SPINNER INFLATION
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> type_adapter = ArrayAdapter
                .createFromResource(this, R.array.type_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(type_adapter);
        type_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type  = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void onAddClick(View view) {

        resultIntent = new Intent();
        resultIntent.putExtra("Info", floor + " " + type);
        resultIntent.putExtra("Building", et_building.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }

}
