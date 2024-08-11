package com.example.digitalkitchen.LoginRegisterProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class RegisterActivity extends AppCompatActivity {

    private EditText signupName, signupEmail, signupUsername, signupPassword;
    private String name, email, username, password;
    private Button signupButton;
    private ImageView profileImage;
    private String imageUrl;
    private Uri uri;
    private ProgressDialog pd;

    // ActivityResultLauncher לבחירת תמונה
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    uri = result;
                    Utils.onImagePicked(this, uri, profileImage); // שימוש בפונקציה Utils להצגת התמונה
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // התחברות לרכיבים מתוך ממשק המשתמש
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        profileImage = findViewById(R.id.profile_register_user);
        ImageView leftIcon = findViewById(R.id.leftIcon);

        // כפתור לחזרה אחורה
        leftIcon.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, StartActivity.class)));

        // כפתור להרשמה
        signupButton.setOnClickListener(view -> {
            if (!validateInput()) return;

            pd = new ProgressDialog(RegisterActivity.this);
            pd.setMessage("מעלה משתמש...");
            pd.show();

            name = signupName.getText().toString();
            email = signupEmail.getText().toString();
            username = signupUsername.getText().toString();
            password = signupPassword.getText().toString();

            DatabaseReference reference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users");
            Log.d("RegisterActivity", "Database reference created: " + reference.toString());
            Query checkUserDatabase = reference.orderByChild("Profile/username").equalTo(username);

            checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        pd.dismiss();
                        signupUsername.setError("שם משתמש קיים");
                        signupUsername.requestFocus();
                    } else {
                        uploadImageAndRegisterUser();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "שגיאה בבדיקת שם המשתמש", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean validateInput() {
        if (signupName.getText().toString().isEmpty()) {
            signupName.setError("יש להזין שם");
            signupName.requestFocus();
            return false;
        }
        if (signupEmail.getText().toString().isEmpty()) {
            signupEmail.setError("יש להזין כתובת מייל");
            signupEmail.requestFocus();
            return false;
        }
        if (signupUsername.getText().toString().isEmpty()) {
            signupUsername.setError("יש להזין שם משתמש");
            signupUsername.requestFocus();
            return false;
        }
        if (signupPassword.getText().toString().isEmpty()) {
            signupPassword.setError("יש להזין סיסמה");
            signupPassword.requestFocus();
            return false;
        }
        if (uri == null) {
            Toast.makeText(RegisterActivity.this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadImageAndRegisterUser() {
        if (uri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("ProfileImage").child(uri.getLastPathSegment());

            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    imageUrl = task.getResult().toString();
                    Log.d("RegisterActivity", "Download URL received: " + imageUrl);
                    uploadUser();
                } else {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "לא הצלחנו לקבל את כתובת ה-URL של התמונה", Toast.LENGTH_SHORT).show();
                }
            })).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "העלאת התמונה נכשלה", Toast.LENGTH_SHORT).show();
            });
        } else {
            pd.dismiss();
            Toast.makeText(RegisterActivity.this, "לא נבחרה תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadUser() {
        ProfileHolder helperClass = new ProfileHolder(name, email, username, password, imageUrl);
        FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users")
                .child(username).child("Profile").setValue(helperClass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pd.dismiss();
                        Log.d("RegisterActivity", "User uploaded successfully");
                        Toast.makeText(RegisterActivity.this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, login_activity.class);
                        startActivity(intent);
                    } else {
                        Log.e("RegisterActivity", "Failed to upload user");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("RegisterActivity", "Failed to upload user: " + e.getMessage());
                    Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                });
    }

    public void btnSelectImage(View view) {
        Utils.pickImage(imagePickerLauncher); // שימוש בפונקציה מ-Utils לבחירת תמונה
    }
}