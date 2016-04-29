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

public class EditActivity extends AppCompatActivity {

    private TextView tv_info;
    private TextView tv_location;
    private DeleteDialogFragment delDialog;

    Intent resultIntent;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        tv_info = (TextView) findViewById(R.id.tv_info);
        tv_location = (TextView) findViewById(R.id.tv_location);

        tv_info.setText(getIntent().getStringExtra("Info"));
        tv_location.setText(getIntent().getStringExtra("location"));
        delDialog = new DeleteDialogFragment();
        bundle = new Bundle();

    }

    public void onDeleteClick(View view) {

        bundle.putString("Title", "Delete Marker");
        bundle.putString("Description", "Are you sure you want to delete this marker from the list?");
        delDialog.setArguments(bundle);
        delDialog.show(getFragmentManager(), "Blah");

    }

    public void deleteEntry()
    {
        resultIntent = new Intent();
        resultIntent.putExtra("Position", getIntent().getIntExtra("Position", 0));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
