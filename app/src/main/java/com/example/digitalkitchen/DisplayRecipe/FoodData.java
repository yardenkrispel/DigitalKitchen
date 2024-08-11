package com.example.digitalkitchen.DisplayRecipe;

public class FoodData {

    private String itemName;
    private String itemIngredients;
    private String itemPreparation;
    private String itemTime;
    private String itemImage;
    private String category;
    private String key;


    public FoodData() {
    }

    public FoodData(String itemName, String itemIngredients, String itemPreparation, String itemPrice, String itemImage, String category) {

        this.itemName = itemName;
        this.itemIngredients = itemIngredients;
        this.itemPreparation = itemPreparation;
        this.itemTime = itemPrice;
        this.itemImage = itemImage;
        this.category = category;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemIngredients() {return  itemIngredients; }

    public String getItemPreparation() {
        return itemPreparation;
    }

    public String getItemTime() {
        return itemTime;
    }

    public String getItemImage() {
        return itemImage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }



}




