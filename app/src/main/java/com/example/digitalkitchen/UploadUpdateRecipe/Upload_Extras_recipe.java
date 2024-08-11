package com.example.digitalkitchen.UploadUpdateRecipe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.Category.MainActivityExtras;
import com.example.digitalkitchen.DisplayRecipe.FoodData;
import com.example.digitalkitchen.LoginRegisterProfile.myProfilePage;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;

public class Upload_Extras_recipe extends AppCompatActivity {

    private static final String TAG = "Upload_Extras_recipe";

    ImageView recipeImage, home, profile;
    Uri uri;
    EditText txt_name, txt_ingredients, txt_preparation, txt_time;
    TextView category;
    String imageUrl;
    String nameUser, emailUser, usernameUser, passwordUser, imageUser;

    // ActivityResultLauncher לבחירת תמונה
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    uri = result;
                    Utils.onImagePicked(this, uri, recipeImage); // שימוש בפונקציה Utils להצגת התמונה
                } else {
                    Toast.makeText(this, "לא נבחרה תמונה", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onActivityResult: No image selected");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_extras_recipe);

        recipeImage = findViewById(R.id.iv_foodImage);
        txt_name = findViewById(R.id.text_recipe_name);
        txt_ingredients = findViewById(R.id.text_ingredients);
        txt_preparation = findViewById(R.id.text_preparation);
        txt_time = findViewById(R.id.text_time);
        home = findViewById(R.id.homeBtn);
        profile = findViewById(R.id.profileBtn);
        ImageView leftIcon = findViewById(R.id.btnBack);
        category = findViewById(R.id.category);

        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        imageUser = intent.getStringExtra("profileImage");

        home.setOnClickListener(v -> startActivity(new Intent(Upload_Extras_recipe.this, CategoryRecipeActivity.class)
                .putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser)));

        profile.setOnClickListener(v -> startActivity(new Intent(Upload_Extras_recipe.this, myProfilePage.class)
                .putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser)));

        leftIcon.setOnClickListener(v -> finish());

        Log.d(TAG, "onCreate: Activity created");
    }

    public void btnSelectImage(View view) {
        Utils.pickImage(imagePickerLauncher); // שימוש בפונקציה Utils לבחירת תמונה
    }

    public void uploadImage() {
        if (uri != null) {
            Log.d(TAG, "uploadImage: Uploading image with URI " + uri.toString());

            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("RecipeImage").child(uri.getLastPathSegment());

            // יצירת ProgressBar והגדרת AlertDialog להצגת התקדמות
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setIndeterminate(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("מעלה מתכון...")
                    .setView(progressBar)
                    .setCancelable(false);
            AlertDialog progressDialog = builder.create();
            progressDialog.show();

            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "uploadImage: Image uploaded successfully");

                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        imageUrl = task.getResult().toString();
                        Log.d(TAG, "uploadImage: Download URL obtained: " + imageUrl);
                        uploadRecipe(progressDialog);
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Upload_Extras_recipe.this, "העלאת התמונה נכשלה", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "uploadImage: Failed to get download URL");
                    }
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Upload_Extras_recipe.this, "העלאת התמונה נכשלה", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "uploadImage: Failed to upload image", e);
            });
        } else {
            Toast.makeText(this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "uploadImage: No image URI available");
        }
    }

    public void btnUploadRecipe(View view) {
        Log.d(TAG, "btnUploadRecipe: Upload button clicked");
        uploadImage();
    }

    public void uploadRecipe(AlertDialog progressDialog) {
        Log.d(TAG, "uploadRecipe: Starting to upload recipe");

        FoodData foodData = new FoodData(
                txt_name.getText().toString(),
                txt_ingredients.getText().toString(),
                txt_preparation.getText().toString(),
                txt_time.getText().toString(),
                imageUrl,
                category.getText().toString()
        );

        String myCurrentDateTime = DateFormat.getDateTimeInstance()
                .format(Calendar.getInstance().getTime());

        FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users")
                .child(usernameUser).child("RecipeDrinks").child(myCurrentDateTime).setValue(foodData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.playSound(this, R.raw.success_sound);
                        Toast.makeText(Upload_Extras_recipe.this, "המתכון הועלה בהצלחה", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "uploadRecipe: Recipe uploaded successfully");
                        startActivity(new Intent(getApplicationContext(), MainActivityExtras.class)
                                .putExtra("name", nameUser)
                                .putExtra("email", emailUser)
                                .putExtra("username", usernameUser)
                                .putExtra("password", passwordUser)
                                .putExtra("profileImage", imageUser));
                    } else {
                        Toast.makeText(Upload_Extras_recipe.this, "שמירת המתכון נכשלה", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "uploadRecipe: Recipe upload failed");
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(Upload_Extras_recipe.this, "שמירת המתכון נכשלה", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "uploadRecipe: Failed to upload recipe", e);
                });
    }
}
