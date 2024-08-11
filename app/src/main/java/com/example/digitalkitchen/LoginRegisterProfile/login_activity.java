package com.example.digitalkitchen.LoginRegisterProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.digitalkitchen.R;
import com.example.digitalkitchen.Category.CategoryRecipeActivity;
import com.example.digitalkitchen.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class login_activity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        ImageView leftIcon = findViewById(R.id.leftIcon);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login_activity.this, StartActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassword()) {
                    // לא עושים כלום במקרה של שדות לא תקינים
                } else {
                    pd = new ProgressDialog(login_activity.this);
                    pd.setMessage("מתחבר...");
                    pd.show();
                    checkUser();
                }
            }
        });
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("שם משתמש ריק");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("סיסמא ריקה");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance(Utils.DATABASE_URL).getReference("users");
        Query checkUserDatabase = reference.orderByChild("Profile/username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pd.dismiss();

                if (snapshot.exists()) {
                    Log.d("login_activity", "User found: " + snapshot.toString());

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        Log.d("login_activity", "Checking user: " + userSnapshot.getKey());
                        String passwordFromDB = userSnapshot.child("Profile").child("password").getValue(String.class);

                        if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                            Log.d("login_activity", "Password match");

                            String nameFromDB = userSnapshot.child("Profile").child("name").getValue(String.class);
                            String emailFromDB = userSnapshot.child("Profile").child("email").getValue(String.class);
                            String usernameFromDB = userSnapshot.child("Profile").child("username").getValue(String.class);
                            String imageFromDB = userSnapshot.child("Profile").child("profileImage").getValue(String.class);

                            Intent intentCategory = new Intent(login_activity.this, CategoryRecipeActivity.class);

                            intentCategory.putExtra("name", nameFromDB);
                            intentCategory.putExtra("email", emailFromDB);
                            intentCategory.putExtra("username", usernameFromDB);
                            intentCategory.putExtra("password", passwordFromDB);
                            intentCategory.putExtra("profileImage", imageFromDB);
                            startActivity(intentCategory);
                            return;
                        } else {
                            loginPassword.setError("סיסמא לא נכונה");
                            loginPassword.requestFocus();
                            Log.d("login_activity", "Password does not match");
                        }
                    }
                } else {
                    loginUsername.setError("משתמש לא קיים");
                    loginUsername.requestFocus();
                    Log.d("login_activity", "User not found in snapshot");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(login_activity.this, "שגיאה בבדיקה", Toast.LENGTH_SHORT).show();
                Log.e("login_activity", "DatabaseError: " + error.getMessage());
            }
        });
    }
}
