package com.example.digitalkitchen.DisplayRecipe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.LoginRegisterProfile.myProfilePage;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.UploadUpdateRecipe.Update_Recipe;
import com.example.digitalkitchen.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    TextView foodIngredients, foodPreparation, RecipeName, RecipeTime, category;
    TextView btnUpdate;
    ImageView foodImage, deleteRecipe, ingredientsRecipe, preparationRecipe;
    String Key = "";
    String imageUrl = "";
    ImageView homeBtn, profileBtn;
    DatabaseReference starterReference, mainReference, desertReference, drinksReference;
    String nameUser, emailUser, usernameUser, passwordUser, imageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        RecipeName = findViewById(R.id.txtRecipeName);
        RecipeTime = findViewById(R.id.timePrice);
        foodIngredients = findViewById(R.id.txtIngredients);
        foodPreparation = findViewById(R.id.txtPreparation);
        category = findViewById(R.id.txtCategory);
        btnUpdate = findViewById(R.id.btnUpdateRecipe);
        foodImage = findViewById(R.id.ivImage2);
        deleteRecipe = findViewById(R.id.deleteRecipe);
        ingredientsRecipe = findViewById(R.id.textIngredients);
        preparationRecipe = findViewById(R.id.textPreparation);
        homeBtn = findViewById(R.id.homeBtn);
        profileBtn = findViewById(R.id.profileBtn);
        ImageView leftIcon = findViewById(R.id.btnBack);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdateRecipe(v);
            }
        });

        deleteRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDeleteRecipe(v);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailActivity.this, CategoryRecipeActivity.class)
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
                startActivity(new Intent(DetailActivity.this, myProfilePage.class)
                        .putExtra("name", nameUser)
                        .putExtra("email", emailUser)
                        .putExtra("username", usernameUser)
                        .putExtra("password", passwordUser)
                        .putExtra("profileImage", imageUser));
            }
        });

        ingredientsRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), foodIngredients.class)
                        .putExtra("ingredientsKey", foodIngredients.getText().toString()));
            }
        });

        preparationRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), foodPreparation.class)
                        .putExtra("preparationKey", foodPreparation.getText().toString()));
            }
        });

        Bundle mBundle = getIntent().getExtras();

        if (mBundle != null) {
            foodIngredients.setText(mBundle.getString("Ingredients"));
            foodIngredients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), foodIngredients.class)
                            .putExtra("ingredientsKey", foodIngredients.getText().toString()));
                }
            });

            foodPreparation.setText(mBundle.getString("Preparation"));
            foodPreparation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), foodPreparation.class)
                            .putExtra("preparationKey", foodPreparation.getText().toString()));
                }
            });
            category.setText(mBundle.getString("category"));

            Key = mBundle.getString("keyValue");
            imageUrl = mBundle.getString("Image");
            RecipeName.setText(mBundle.getString("RecipeName"));
            RecipeTime.setText(mBundle.getString("time"));
            nameUser = mBundle.getString("name");
            emailUser = mBundle.getString("email");
            usernameUser = mBundle.getString("username");
            passwordUser = mBundle.getString("password");
            imageUser = mBundle.getString("profileImage");

            Glide.with(this)
                    .load(mBundle.getString("Image"))
                    .into(foodImage);
        }

    }

    public void btnDeleteRecipe(View view) {

        starterReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeStarter");
        mainReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeMain");
        desertReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("dessertRecipe");
        drinksReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeDrinks");

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);

        storageReference.delete();
        drinksReference.child(Key).removeValue();
        starterReference.child(Key).removeValue();
        mainReference.child(Key).removeValue();
        desertReference.child(Key).removeValue();
        Toast.makeText(DetailActivity.this, "המתכון נמחק", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), CategoryRecipeActivity.class).putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser));
        finish();
    }

    public void btnBack(View view) {
        finish();
    }

    public void btnUpdateRecipe(View view) {

        startActivity(new Intent(getApplicationContext(), Update_Recipe.class)
                .putExtra("recipeNameKey", RecipeName.getText().toString())
                .putExtra("ingredientsKey", foodIngredients.getText().toString())
                .putExtra("preparationKey", foodPreparation.getText().toString())
                .putExtra("timeKey", RecipeTime.getText().toString())
                .putExtra("category", category.getText().toString())
                .putExtra("oldImageUrl", imageUrl)
                .putExtra("Key", Key)
                .putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser)
        );
    }
}
