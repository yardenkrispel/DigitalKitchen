package com.example.digitalkitchen.UploadUpdateRecipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.Category.MainActivityBreakfast;
import com.example.digitalkitchen.Category.MainActivityDessert;
import com.example.digitalkitchen.Category.MainActivityExtras;
import com.example.digitalkitchen.Category.MainActivityMain;
import com.example.digitalkitchen.DisplayRecipe.FoodData;
import com.example.digitalkitchen.LoginRegisterProfile.myProfilePage;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Locale;

public class Update_Recipe extends AppCompatActivity {

    private static final String TAG = "Update_Recipe"; // להוסיף TAG לכל ההודעות ב-Log

    ImageView recipeImage;
    Uri uri;
    EditText txt_name, txt_ingredients, txt_preparation, txt_time;
    String imageUrl;
    String key, oldImageUrl, category;
    ImageView homeBtn, profileBtn;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String recipeName, recipeIngredients, recipePreparation, recipeTime;
    String nameUser, emailUser, usernameUser, passwordUser, profileImage;

    // ActivityResultLauncher לבחירת תמונה
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    uri = result;
                    Utils.onImagePicked(this, uri, recipeImage); // שימוש בפונקציה Utils להצגת התמונה
                    Log.d(TAG, "Image selected: " + uri.toString());
                } else {
                    Log.d(TAG, "No image selected");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        recipeImage = findViewById(R.id.iv_foodImage);
        txt_name = findViewById(R.id.txt_recipe_name);
        txt_ingredients = findViewById(R.id.text_ingredients);
        txt_preparation = findViewById(R.id.text_preparation);
        txt_time = findViewById(R.id.text_time);
        ImageView leftIcon = findViewById(R.id.btnBack);
        homeBtn = findViewById(R.id.homeBtn);
        profileBtn = findViewById(R.id.profileBtn);

        leftIcon.setOnClickListener(v -> finish());

        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Update_Recipe.this, CategoryRecipeActivity.class);
            intent.putExtra("name", nameUser);
            intent.putExtra("email", emailUser);
            intent.putExtra("username", usernameUser);
            intent.putExtra("password", passwordUser);
            intent.putExtra("profileImage", profileImage);
            startActivity(intent);
        });

        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Update_Recipe.this, myProfilePage.class);
            intent.putExtra("name", nameUser);
            intent.putExtra("email", emailUser);
            intent.putExtra("username", usernameUser);
            intent.putExtra("password", passwordUser);
            intent.putExtra("profileImage", profileImage);
            startActivity(intent);
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Glide.with(Update_Recipe.this)
                    .load(bundle.getString("oldImageUrl"))
                    .into(recipeImage);
            txt_name.setText(bundle.getString("recipeNameKey"));
            txt_ingredients.setText(bundle.getString("ingredientsKey"));
            txt_preparation.setText(bundle.getString("preparationKey"));
            txt_time.setText(bundle.getString("timeKey"));
            key = bundle.getString("Key");
            oldImageUrl = bundle.getString("oldImageUrl");
            category = bundle.getString("category");
            nameUser = bundle.getString("name");
            emailUser = bundle.getString("email");
            usernameUser = bundle.getString("username");
            passwordUser = bundle.getString("password");
            profileImage = bundle.getString("profileImage");

            switch (category) {
                case "breakfast":
                    databaseReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeStarter").child(key);
                    break;
                case "mainCourse":
                    databaseReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeMain").child(key);
                    break;
                case "extras":
                    databaseReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("RecipeDrinks").child(key);
                    break;
                case "dessert":
                    databaseReference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users").child(usernameUser).child("dessertRecipe").child(key);
                    break;
            }
            Log.d(TAG, "Loaded recipe data for key: " + key);
        } else {
            Log.d(TAG, "No recipe data passed in the intent");
        }
    }

    public void btnUpdateImage(View view) {
        Utils.pickImage(imagePickerLauncher); // שימוש בפונקציה Utils לבחירת תמונה
        Log.d(TAG, "Image picker launched");
    }

    public void buttonUpdateRecipe(View view) {
        recipeName = txt_name.getText().toString().trim();
        recipeIngredients = txt_ingredients.getText().toString().trim();
        recipePreparation = txt_preparation.getText().toString().trim();
        recipeTime = txt_time.getText().toString();

        Log.d(TAG, "Updating recipe: " + recipeName);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("מעדכן מתכון...");
        progressDialog.show();

        if (uri != null) {
            Log.d(TAG, "Uploading new image to Firebase Storage");
            storageReference = FirebaseStorage.getInstance()
                    .getReference().child("RecipeImage").child(uri.getLastPathSegment().toLowerCase(Locale.ROOT));

            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");

                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        imageUrl = task.getResult().toString() + "?timestamp=" + System.currentTimeMillis();
                        Log.d(TAG, "Image download URL: " + imageUrl);
                        updateRecipe(progressDialog);
                    } else {
                        progressDialog.dismiss();
                        String errorMsg = "Failed to get download URL: " + task.getException();
                        Log.e(TAG, errorMsg);
                        Toast.makeText(Update_Recipe.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                String errorMsg = "Failed to upload image: " + e.getMessage();
                Log.e(TAG, errorMsg);
                Toast.makeText(Update_Recipe.this, errorMsg, Toast.LENGTH_SHORT).show();
            });
        } else {
            // אם לא נבחרה תמונה חדשה, שמור על התמונה הישנה
            imageUrl = oldImageUrl;
            updateRecipe(progressDialog);
        }
    }

    private void updateRecipe(ProgressDialog progressDialog) {
        Log.d(TAG, "Saving updated recipe to Firebase Database");

        FoodData foodData = new FoodData(
                recipeName,
                recipeIngredients,
                recipePreparation,
                recipeTime,
                imageUrl,
                category
        );

        databaseReference.setValue(foodData).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Log.d(TAG, "Recipe updated successfully in database");

                // מחיקת התמונה הישנה מה-Storage
                StorageReference storageReferenceNew = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                storageReferenceNew.delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Old image deleted from storage");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete old image: " + e.getMessage());
                    Toast.makeText(Update_Recipe.this, "נכשלה מחיקת התמונה הישנה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                Toast.makeText(Update_Recipe.this, "המידע עודכן", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                String errorMsg = "Failed to update recipe: " + task.getException().getMessage();
                Log.e(TAG, errorMsg);
                Toast.makeText(Update_Recipe.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            String errorMsg = "Failed to update recipe: " + e.getMessage();
            Log.e(TAG, errorMsg);
            Toast.makeText(Update_Recipe.this, errorMsg, Toast.LENGTH_SHORT).show();
        });

    }

    private void navigateToMainActivity() {
        Intent intent = null;
        switch (category) {
            case "breakfast":
                intent = new Intent(Update_Recipe.this, MainActivityBreakfast.class);
                break;
            case "mainCourse":
                intent = new Intent(Update_Recipe.this, MainActivityMain.class);
                break;
            case "extras":
                intent = new Intent(Update_Recipe.this, MainActivityExtras.class);
                break;
            case "dessert":
                intent = new Intent(Update_Recipe.this, MainActivityDessert.class);
                break;
            default:
                Log.w(TAG, "Unknown category: " + category);
                Toast.makeText(this, "קטגוריה לא ידועה: " + category, Toast.LENGTH_SHORT).show();
                return; // סיים את הפונקציה אם הקטגוריה לא תקינה
        }

        if (intent != null) {
            intent.putExtra("name", nameUser);
            intent.putExtra("email", emailUser);
            intent.putExtra("username", usernameUser);
            intent.putExtra("password", passwordUser);
            intent.putExtra("profileImage", profileImage);
            startActivity(intent);
            Log.d(TAG, "Navigating to " + category + " activity");
        }
    }
}
