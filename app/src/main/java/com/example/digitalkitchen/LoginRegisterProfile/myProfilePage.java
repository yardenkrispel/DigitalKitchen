package com.example.digitalkitchen.LoginRegisterProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.Utils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class myProfilePage extends AppCompatActivity {

    TextView logOutBtn;
    ShapeableImageView profileImage;
    TextView profileName, profileEmail, profileUsername, profilePassword;
    TextView titleName, titleUsername;
    ImageView homeBtn;
    ImageView editProfile;
    String nameUser, emailUser, usernameUser, passwordUser, imageUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_page);

        profileImage = findViewById(R.id.profile_IMG_user);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        profilePassword = findViewById(R.id.profilePassword);
        titleName = findViewById(R.id.titleName);
        logOutBtn = (TextView)findViewById(R.id.logOutBtn);
        homeBtn = (ImageView)findViewById(R.id.homeBtn);
        editProfile = (ImageView)findViewById(R.id.editButton);
        ImageView btnBack = (ImageView)findViewById(R.id.btnBack);

        showUserData();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(myProfilePage.this, CategoryRecipeActivity.class)
                        .putExtra("name", nameUser)
                        .putExtra("email", emailUser)
                        .putExtra("username", usernameUser)
                        .putExtra("password", passwordUser)
                        .putExtra("profileImage", imageUser));
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passUserData();
            }
        });

    }

    public void showUserData(){

        Intent intent = getIntent();

        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        imageUser = intent.getStringExtra("profileImage");

        titleName.setText(nameUser);
        profileName.setText(nameUser);
        profileEmail.setText(emailUser);
        profileUsername.setText(usernameUser);
        profilePassword.setText(passwordUser);
        Glide.with(myProfilePage.this)
                .load(imageUser).into(profileImage);
    }


    public void passUserData(){

        String userUsername = profileUsername.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users");
        Query checkUserDatabase = reference.orderByChild("Profile/username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String nameFromDB = snapshot.child(userUsername).child("Profile").child("name").getValue(String.class);
                    String emailFromDB = snapshot.child(userUsername).child("Profile").child("email").getValue(String.class);
                    String usernameFromDB = snapshot.child(userUsername).child("Profile").child("username").getValue(String.class);
                    String passwordFromDB = snapshot.child(userUsername).child("Profile").child("password").getValue(String.class);
                    String imageFromDB = snapshot.child(userUsername).child("Profile").child("profileImage").getValue(String.class);

                    Intent intent = new Intent(myProfilePage.this, EditProfile.class);

                    intent.putExtra("name", nameFromDB);
                    intent.putExtra("email", emailFromDB);
                    intent.putExtra("username", usernameFromDB);
                    intent.putExtra("password", passwordFromDB);
                    intent.putExtra("profileImage", imageFromDB);

                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logOutBtn(View view) {
        startActivity(new Intent(myProfilePage.this, StartActivity.class));
    }
}