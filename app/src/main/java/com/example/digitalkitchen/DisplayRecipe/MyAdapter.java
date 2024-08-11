package com.example.digitalkitchen.DisplayRecipe;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.digitalkitchen.R;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<FoodViewHolder>{

    private Context mContext;
    private List <FoodData> myFoodList;
    private int lastPosition = -1;
    String nameUser, emailUser, usernameUser, passwordUser, imageUser;


    public MyAdapter(Context context, List<FoodData> myFoodList, String nameUser, String emailUser, String usernameUser, String passwordUser, String imageUser) {
        this.mContext = context;
        this.myFoodList = myFoodList;
        this.nameUser = nameUser;
        this.emailUser = emailUser;
        this.usernameUser = usernameUser;
        this.passwordUser = passwordUser;
        this.imageUser = imageUser;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_item,viewGroup,false);

        return new FoodViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i) {

        Glide.with(mContext)
                .load(myFoodList.get(i).getItemImage())
                .into(foodViewHolder.imageView);

        foodViewHolder.mTitle.setText(myFoodList.get(i).getItemName());

        foodViewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("Image", myFoodList.get(foodViewHolder.getAdapterPosition()).getItemImage());
                intent.putExtra("Ingredients", myFoodList.get(foodViewHolder.getAdapterPosition()).getItemIngredients());
                intent.putExtra("Preparation", myFoodList.get(foodViewHolder.getAdapterPosition()).getItemPreparation());
                intent.putExtra("RecipeName", myFoodList.get(foodViewHolder.getAdapterPosition()).getItemName());
                intent.putExtra("time", myFoodList.get(foodViewHolder.getAdapterPosition()).getItemTime());
                intent.putExtra("category", myFoodList.get(foodViewHolder.getAdapterPosition()).getCategory());
                intent.putExtra("keyValue", myFoodList.get(foodViewHolder.getAdapterPosition()).getKey());
                intent.putExtra("name", nameUser);
                intent.putExtra("email", emailUser);
                intent.putExtra("username", usernameUser);
                intent.putExtra("password", passwordUser);
                intent.putExtra("profileImage", imageUser);

                mContext.startActivity(intent);
            }
        });

        foodViewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareRecipe(myFoodList.get(foodViewHolder.getAdapterPosition()));
            }
        });

        setAnimation(foodViewHolder.imageView, i);
    }

    private void shareRecipe(FoodData foodData) {
        String shareBody = "תראו את המתכון המדהים הזה ל-" + foodData.getItemName() + "! \n\nמצרכים:\n" + foodData.getItemIngredients() + "\n\nהוראות הכנה:\n" + foodData.getItemPreparation() + "\n\nבתיאבון!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "מתכון ל-" + foodData.getItemName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        mContext.startActivity(Intent.createChooser(shareIntent, "שתף דרך"));
    }

    public void setAnimation(View viewToAnimate, int position){

        if (position > lastPosition){

            ScaleAnimation animation = new ScaleAnimation(0.0f,1.0f,0.0f,1.0f
                    , Animation.RELATIVE_TO_SELF,0.5f
                    ,Animation.RELATIVE_TO_SELF,0.5f);

            animation.setDuration(1500);
            viewToAnimate.setAnimation(animation);
            lastPosition = position;

        }


    }


    @Override
    public int getItemCount() {
        return myFoodList.size();
    }

    public void filteredList(ArrayList<FoodData> filterList) {

        myFoodList = filterList;
        notifyDataSetChanged();

    }
}

class FoodViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView, ivShare;
    TextView mTitle;
    CardView mCardView;

    public FoodViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.ivImage);
        ivShare = itemView.findViewById(R.id.ivShare); // הגדרת כפתור השיתוף
        mTitle = itemView.findViewById(R.id.tvTitle);
        mCardView = itemView.findViewById(R.id.myCardView);
    }
}
