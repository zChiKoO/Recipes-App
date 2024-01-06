package com.example.murat.recipesapp;

import static java.lang.System.currentTimeMillis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.murat.recipesapp.databinding.ActivityAddRecipeBinding;
import com.example.murat.recipesapp.models.Category;
import com.example.murat.recipesapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AddRecipeActivity is the activity that allows the user to add a recipe to the database.
 * It is called when the user clicks on the add recipe button in the MainActivity.
 * We use Name, Description, Cooking Time, Calories, Image and Category.
 * Chains are same as Layout Weight in Linear Layout but with more control and flexibility.
 * Our Design is not good but not responsive. So we need to use ConstraintLayout and ScrollView
 * Let's convert et category to spinner.
 * Need to add a dummy image for Recipe Image. Let's use https://picsum.photos/200/300
 * 1. We will get Data from the user and validate it.
 * 2. We will validate the data.
 * 3. We will create a Recipe Object.
 * 4. We will pick the image from the Gallery.
 * 5. We will upload the image to the Firebase Storage.
 * 6. We will save the Recipe Object in the Firebase Database.
 * 7. We will show a Toast message to the user.
 * 8. We will finish the activity.
 *
 */

public class AddRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;
    private boolean isImageSelected = false;
    private ProgressDialog dialog;
    boolean isEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 9. Load the categories from the Firebase Database.
        loadCategories();
        binding.btnAddRecipe.setOnClickListener(view -> {
            // 1. We will get Data from the user and validate it.
            getdata();
        });
        binding.imgRecipe.setOnClickListener(view -> {
            // 4. We will pick the image from Gallery.
            pickImage();
        });

        // For Edit Purpose
        isEdit = getIntent().getBooleanExtra("isEdit",false);
        if (isEdit){
            editRecipe();
        }
    }

    private void editRecipe() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        isImageSelected = true;
        binding.etRecipeName.setText(recipe.getName());
        binding.etDescription.setText(recipe.getDescription());
        binding.etCookingTime.setText(recipe.getTime());
        binding.etCategory.setText(recipe.getCategory());
        binding.etCalories.setText(recipe.getCalories());
        Glide
                .with(binding.getRoot().getContext())
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imgRecipe);
        binding.btnAddRecipe.setText("Update Recipe");
    }

    private void loadCategories() {
        List<String> categories = new ArrayList<>();
        // Instead of writing code, we use Chat GPT to generate code.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,categories);
        binding.etCategory.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        categories.add(dataSnapshot.getValue(Category.class).getName());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pickImage() {
        // Instead of writing the code for picking image from the Gallery, we will use a library and copy from other activity.
        PickImageDialog.build(new PickSetup()).show(AddRecipeActivity.this).setOnPickResult(r -> {
            Log.e("ProfileFragment","onPickResult: " + r.getUri());
            binding.imgRecipe.setImageBitmap(r.getBitmap());
            binding.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_CROP);
            isImageSelected = true;
        }).setOnPickCancel(() -> Toast.makeText(AddRecipeActivity.this,"Cancelled",Toast.LENGTH_SHORT).show());
    }

    private void getdata() {
        // Fetch all the data from the user in variables.
        String recipeName = Objects.requireNonNull(binding.etRecipeName.getText()).toString();
        String recipeDescription = Objects.requireNonNull(binding.etDescription.getText()).toString();
        String cookingTime = Objects.requireNonNull(binding.etCookingTime.getText()).toString();
        String recipeCategory = binding.etCategory.getText().toString();
        String calories = Objects.requireNonNull(binding.etCalories.getText()).toString();
        // 2. We will validate the data.
        if (recipeName.isEmpty()){
            binding.etRecipeName.setError("Please enter Recipe Name");
        }else if (recipeDescription.isEmpty()){
            binding.etDescription.setError("Please enter Recipe Description");
        }else if (cookingTime.isEmpty()){
            binding.etCookingTime.setError("Please enter Cooking Time");
        }else if (recipeCategory.isEmpty()){
            binding.etCategory.setError("Please enter Recipe Category");
        }else if (calories.isEmpty()){
            binding.etCalories.setError("Please enter Calories");
        }else if (!isImageSelected){
            Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show();
        } else {
            // 3. We will create a Recipe Object.
            // ID will be auto generated.
            dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading Recipe...");
            dialog.setCancelable(false);
            dialog.show();
            Recipe recipe = new Recipe(recipeName, recipeDescription, cookingTime, recipeCategory, calories, "", FirebaseAuth.getInstance().getUid());
            // We also need to pick image and make sure it is not null.

            // 5. We will upload the image to the Firebase Storage.
            uploadImage(recipe);
        }
    }

    private String uploadImage(Recipe recipe) {
        final String[] url = {""};
        // We will upload the image to the Firebase Storage.
        binding.imgRecipe.setDrawingCacheEnabled(true);
        Bitmap bitmap = ((BitmapDrawable) binding.imgRecipe.getDrawable()).getBitmap();
        binding.imgRecipe.setDrawingCacheEnabled(false);
        String id = isEdit ? recipe.getId() : currentTimeMillis() + "";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + id +"image.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        storageRef.getDownloadUrl();
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }

            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                // We need to save this download url in Firebase Storage
                // So that we can load it in our app
                url[0] = downloadUri.toString();
                Toast.makeText(AddRecipeActivity.this,"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                saveDataInDatabase(recipe, url[0]);
            } else {
                Toast.makeText(AddRecipeActivity.this,"Error in uploading image",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Log.e("ProfileFragment","onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
        return url[0];
    }

    private void saveDataInDatabase(Recipe recipe, String url) {
        recipe.setImage(url);
        // 6. We will save the recipe object in the Firebase Database.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        if (isEdit){
            reference.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(AddRecipeActivity.this, "Recipe Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(AddRecipeActivity.this, "Error in updating recipe", Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            String id = reference.push().getKey();
            recipe.setId(id);
            if (id != null) {
                reference.child(id).setValue(recipe).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(AddRecipeActivity.this, "Recipe Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddRecipeActivity.this, "Error in adding recipe", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}