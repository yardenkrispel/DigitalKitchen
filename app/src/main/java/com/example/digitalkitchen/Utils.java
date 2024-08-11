package com.example.digitalkitchen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.activity.result.ActivityResultLauncher;
import com.bumptech.glide.Glide;

public class Utils {

    public static final String DATABASE_URL = "https://digitalkitchen-70782-default-rtdb.europe-west1.firebasedatabase.app";

    // Function to load an image from a URL into an ImageView using Glide
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);
    }

    // Function to launch image picker and handle the result
    public static void pickImage(ActivityResultLauncher<String> launcher) {
        launcher.launch("image/*");
    }

    // Function to handle the image picked result
    public static void onImagePicked(Context context, Uri uri, ImageView imageView) {
        if (uri != null) {
            imageView.setImageURI(uri);
        } else {
            // Handle the case where the user didn't pick an image
            Toast.makeText(context, "לא בחרת תמונה", Toast.LENGTH_SHORT).show();
        }
    }
    public static void shareRecipe(Context context, String recipeTitle, String recipeBody) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipeTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, recipeBody);
        context.startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
    }

    public static void playSound(Context context, int soundResourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResourceId);
        mediaPlayer.start();

        // הגדרת listener לשחרור המשאבים לאחר סיום ניגון הצליל
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }
}
