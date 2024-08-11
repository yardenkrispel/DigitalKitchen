package com.example.digitalkitchen.Category;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.digitalkitchen.DisplayRecipe.FoodData;
import com.example.digitalkitchen.DisplayRecipe.MyAdapter;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.UploadUpdateRecipe.Upload_Breakfast_Recipe;
import com.example.digitalkitchen.LoginRegisterProfile.myProfilePage;
import com.example.digitalkitchen.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivityBreakfast extends AppCompatActivity {

    RecyclerView mRecyclerView;
    List<FoodData> myFoodList;
    AlertDialog progressDialog; // שינוי סוג המשתנה ל-AlertDialog
    MyAdapter myAdapter;
    EditText txt_Search;
    String nameUser, emailUser, usernameUser, passwordUser, imageUser;
    ImageView homeBtn, profileBtn, uploadRecipeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_breakfast);

        homeBtn = findViewById(R.id.homeBtn);
        profileBtn = findViewById(R.id.profileBtn);
        uploadRecipeBtn = findViewById(R.id.uploadRecipeBtn);

        Intent intent = getIntent();

        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        imageUser = intent.getStringExtra("profileImage");

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityBreakfast.this, CategoryRecipeActivity.class)
                        .putExtra("name", nameUser)
                        .putExtra("email", emailUser)
                        .putExtra("username", usernameUser)
                        .putExtra("password", passwordUser)
                        .putExtra("profileImage", imageUser));
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityBreakfast.this, myProfilePage.class)
                        .putExtra("name", nameUser)
                        .putExtra("email", emailUser)
                        .putExtra("username", usernameUser)
                        .putExtra("password", passwordUser)
                        .putExtra("profileImage", imageUser));
            }
        });

        uploadRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityBreakfast.this, Upload_Breakfast_Recipe.class)
                        .putExtra("name", nameUser)
                        .putExtra("email", emailUser)
                        .putExtra("username", usernameUser)
                        .putExtra("password", passwordUser)
                        .putExtra("profileImage", imageUser));
            }
        });

        mRecyclerView = findViewById(R.id.RecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivityBreakfast.this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        txt_Search = findViewById(R.id.txt_searchText);

        ImageView leftIcon = findViewById(R.id.btnBack);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // יצירת AlertDialog עם ProgressBar
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);

        progressDialog = new AlertDialog.Builder(this)
                .setTitle("טוען נתונים")
                .setView(progressBar)
                .setCancelable(false)
                .create();

        myFoodList = new ArrayList<>();
        myAdapter = new MyAdapter(MainActivityBreakfast.this, myFoodList, nameUser, emailUser, usernameUser, passwordUser, imageUser);
        mRecyclerView.setAdapter(myAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeStarter");

        progressDialog.show();
        ValueEventListener eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFoodList.clear();

                Log.d("MainActivityBreakfast", "DataSnapshot received: " + dataSnapshot.toString());

                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        FoodData foodData = itemSnapshot.getValue(FoodData.class);
                        if (foodData != null) {
                            foodData.setKey(itemSnapshot.getKey());
                            myFoodList.add(foodData);
                        }
                    }

                    Log.d("MainActivityBreakfast", "Data loaded successfully, foodList size: " + myFoodList.size());
                    myAdapter.notifyDataSetChanged();
                } else {
                    Log.d("MainActivityBreakfast", "No data found, foodList is empty");
                    Toast.makeText(MainActivityBreakfast.this, "ספר המתכונים ריק", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Log.e("MainActivityBreakfast", "Error loading data: " + databaseError.getMessage());
                Toast.makeText(MainActivityBreakfast.this, "שגיאה בטעינת הנתונים", Toast.LENGTH_SHORT).show();
            }
        });

        txt_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        ArrayList<FoodData> filterList = new ArrayList<>();
        for (FoodData item : myFoodList) {
            if (item.getItemName().toLowerCase().contains(text.toLowerCase())) {
                filterList.add(item);
            }
        }
        myAdapter.filteredList(filterList);
    }
}

