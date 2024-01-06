package com.example.murat.recipesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.murat.recipesapp.databinding.ActivityAddRecipeBinding;
import com.example.murat.recipesapp.databinding.ActivityRecipeDetailsBinding;
import com.example.murat.recipesapp.models.FavouriteRecipe;
import com.example.murat.recipesapp.models.Recipe;
import com.example.murat.recipesapp.room.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Will be simple and similar to our Add new Recipe Page
 * Design is ready, now we will load data from our database
 * We will use the same method that we used in our Home Page
 * Add some more features like Edit Page
 * Edit works fine, just need to update data in app Automatically
 * We will add feature of favourite and unfavourite and store Data in SQLite Database
 * For SQLite Database we will use Room Library
 */

public class RecipeDetailsActivity extends AppCompatActivity {
    ActivityRecipeDetailsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        binding.tvName.setText(recipe.getName());
        binding.tcCategory.setText(recipe.getCategory());
        binding.tvDescription.setText(recipe.getDescription());
        binding.tvCalories.setText(String.format("%s Calories", recipe.getCalories()));
        Glide
                .with(RecipeDetailsActivity.this)
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(binding.imgRecipe);

        if (recipe.getAuthorId().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())){
            binding.imgEdit.setVisibility(View.VISIBLE);
        }else {
            binding.imgEdit.setVisibility(View.GONE);
        }

        binding.imgEdit.setOnClickListener(view -> {
            Intent intent = new Intent(binding.getRoot().getContext(), AddRecipeActivity.class);
            intent.putExtra("recipe",recipe);
            intent.putExtra("isEdit", true);
            binding.getRoot().getContext().startActivity(intent);
        });

        checkFavorite(recipe);
        binding.imgFvrt.setOnClickListener(view -> favouriteRecipe(recipe));

        updateDataWithFirebase(recipe.getId());
    }

    private void checkFavorite(Recipe recipe) {
        RecipeRepository repository = new RecipeRepository(getApplication());
        boolean isFavourite = repository.isFavourite(recipe.getId());
        if (isFavourite) {
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.black));
        }
    }

    private void favouriteRecipe(Recipe recipe) {
        RecipeRepository repository = new RecipeRepository(getApplication());
        boolean isFavourite = repository.isFavourite(recipe.getId());
        if (isFavourite) {
            repository.delete(new FavouriteRecipe(recipe.getId()));
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.black));
        } else {
            repository.insert(new FavouriteRecipe(recipe.getId()));
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.accent));
        }
    }

    private void updateDataWithFirebase(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
        reference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                binding.tvName.setText(recipe.getName());
                binding.tcCategory.setText(recipe.getCategory());
                binding.tvDescription.setText(recipe.getDescription());
                binding.tvCalories.setText(String.format("%s Calories", recipe.getCalories()));
                Glide
                        .with(RecipeDetailsActivity.this)
                        .load(recipe.getImage())
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(binding.imgRecipe);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG","onCancelled: ",error.toException());
            }
        });
    }
}