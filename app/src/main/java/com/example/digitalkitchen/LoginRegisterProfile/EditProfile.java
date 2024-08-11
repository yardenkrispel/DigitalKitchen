package com.example.digitalkitchen.LoginRegisterProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editEmail, editPassword;
    private TextView editUsername;
    private ImageView profileImage;
    private Button saveButton;
    private Uri imageUri;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private String nameUser, emailUser, usernameUser, passwordUser, imageUser, imageUrl;

    //  לבחירת תמונה
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    imageUri = result; // שמירת ה-Uri שנבחר
                    Utils.onImagePicked(this, imageUri, profileImage); // שימוש בפונקציה Utils להצגת התמונה
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        reference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users");

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);
        profileImage = findViewById(R.id.edit_IMG_user);

        showData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            Intent intent = new Intent(EditProfile.this, CategoryRecipeActivity.class);
            intent.putExtra("name", nameUser);
            intent.putExtra("email", emailUser);
            intent.putExtra("username", usernameUser);
            intent.putExtra("password", passwordUser);
            intent.putExtra("profileImage", imageUser);
            startActivity(intent);
        });

        saveButton.setOnClickListener(v -> {
            updateImageStorage();
        });
    }

    public void btnEditImage(View view) {
        Utils.pickImage(imagePickerLauncher); // קריאה לפונקציה לבחור תמונה מגלריה
    }

    private void updateImageStorage() {
        if (imageUri != null) {
            storageReference = FirebaseStorage.getInstance().getReference()
                    .child("ProfileImage")
                    .child(imageUri.getLastPathSegment());

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            imageUrl = task.getResult().toString();
                            updateUser(); // קריאה לפונקציה לעדכון המשתמש עם כתובת התמונה החדשה
                        } else {
                            Toast.makeText(EditProfile.this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show());
        } else {
            updateUser(); // אם לא נבחרה תמונה, פשוט עדכן את המשתמש עם הנתונים האחרים
        }
    }

    private void showData() {
        Intent intent = getIntent();
        nameUser = intent.getStringExtra("name");
        emailUser = intent.getStringExtra("email");
        usernameUser = intent.getStringExtra("username");
        passwordUser = intent.getStringExtra("password");
        imageUser = intent.getStringExtra("profileImage");

        editName.setText(nameUser);
        editEmail.setText(emailUser);
        editUsername.setText(usernameUser);
        editPassword.setText(passwordUser);

        Utils.loadImage(this, imageUser, profileImage); // שימוש בפונקציה להצגת התמונה הנוכחית
    }

    private void updateUser() {
        nameUser = editName.getText().toString();
        emailUser = editEmail.getText().toString();
        usernameUser = editUsername.getText().toString();
        passwordUser = editPassword.getText().toString();

        ProfileHolder helperClass = new ProfileHolder(nameUser, emailUser, usernameUser, passwordUser, imageUrl);
        reference.child(usernameUser).child("Profile").setValue(helperClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "הפרופיל עודכן", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfile.this, myProfilePage.class)
                                .putExtra("name", nameUser)
                                .putExtra("email", emailUser)
                                .putExtra("username", usernameUser)
                                .putExtra("password", passwordUser)
                                .putExtra("profileImage", imageUrl));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfile.this, "שגיאה בעדכון פרופיל", Toast.LENGTH_SHORT).show());
    }
}
