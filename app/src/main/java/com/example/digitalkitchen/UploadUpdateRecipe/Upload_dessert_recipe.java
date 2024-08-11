package com.example.digitalkitchen.UploadUpdateRecipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitalkitchen.Category.MainActivityDessert;
import com.example.digitalkitchen.DisplayRecipe.FoodData;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.LoginRegisterProfile.myProfilePage;
import com.example.digitalkitchen.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.text.DateFormat;
import java.util.Calendar;

public class Upload_dessert_recipe extends AppCompatActivity {

    private ImageView recipeImage, home, profile;
    private EditText txt_name, txt_ingredients, txt_preparation, txt_time;
    private TextView category;
    private String imageUrl;
    private String nameUser, emailUser, usernameUser, passwordUser, imageUser;
    private Uri imageUri;

    // ActivityResultLauncher לבחירת תמונה
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    imageUri = result;
                    Utils.onImagePicked(this, imageUri, recipeImage); // שימוש בפונקציה Utils להצגת התמונה
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_dessert_recipe);

        recipeImage = findViewById(R.id.iv_foodImage);
        txt_name = findViewById(R.id.text_recipe_name);
        txt_ingredients = findViewById(R.id.text_ingredients);
        txt_preparation = findViewById(R.id.text_preparation);
        txt_time = findViewById(R.id.text_time);
        category = findViewById(R.id.category);

        home = findViewById(R.id.homeBtn);
        profile = findViewById(R.id.profileBtn);

        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        imageUser = intent.getStringExtra("profileImage");

        home.setOnClickListener(v -> startActivity(new Intent(Upload_dessert_recipe.this, CategoryRecipeActivity.class)
                .putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser)));

        profile.setOnClickListener(v -> startActivity(new Intent(Upload_dessert_recipe.this, myProfilePage.class)
                .putExtra("name", nameUser)
                .putExtra("email", emailUser)
                .putExtra("username", usernameUser)
                .putExtra("password", passwordUser)
                .putExtra("profileImage", imageUser)));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    public void btnSelectImage(View view) {
        Utils.pickImage(imagePickerLauncher); // שימוש בפונקציה לבחור תמונה מגלריה
    }

    public void uploadImage() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("RecipeImage").child(imageUri.getLastPathSegment());

            // יצירת ProgressBar והגדרת AlertDialog להצגת התקדמות
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.setIndeterminate(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("מעלה מתכון...")
                    .setView(progressBar)
                    .setCancelable(false);
            AlertDialog progressDialog = builder.create();
            progressDialog.show();

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            imageUrl = task.getResult().toString();
                            uploadRecipe(progressDialog);
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(Upload_dessert_recipe.this, "העלאת התמונה נכשלה", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(Upload_dessert_recipe.this, "העלאת התמונה נכשלה", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnUploadRecipe(View view) {
        uploadImage();
    }

    public void uploadRecipe(AlertDialog progressDialog) {
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
                .child(usernameUser).child("dessertRecipe").child(myCurrentDateTime).setValue(foodData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.playSound(this, R.raw.success_sound);
                        Toast.makeText(Upload_dessert_recipe.this, "המתכון הועלה", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivityDessert.class)
                                .putExtra("name", nameUser)
                                .putExtra("email", emailUser)
                                .putExtra("username", usernameUser)
                                .putExtra("password", passwordUser)
                                .putExtra("profileImage", imageUser));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(Upload_dessert_recipe.this, "שמירת המתכון נכשלה", Toast.LENGTH_SHORT).show());
    }
}
