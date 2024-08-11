package com.example.digitalkitchen.DisplayRecipe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitalkitchen.R;

public class foodPreparation extends AppCompatActivity {

    TextView textPreparation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_preparation);

        ImageView leftIcon = (ImageView)findViewById(R.id.btnBack);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textPreparation = (TextView) findViewById(R.id.textView_preparation);
        Bundle mBundle = getIntent().getExtras();
        if(mBundle!=null){
            textPreparation.setText(mBundle.getString("preparationKey"));
        }
    }
}