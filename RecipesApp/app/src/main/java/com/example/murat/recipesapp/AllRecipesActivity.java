package com.example.murat.recipesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.util.Log;

import com.example.murat.recipesapp.adapters.RecipeAdapter;
import com.example.murat.recipesapp.databinding.ActivityAllRecipesBinding;
import com.example.murat.recipesapp.models.Category;
import com.example.murat.recipesapp.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * We will load and filter by category
 */

public class AllRecipesActivity extends AppCompatActivity {
    ActivityAllRecipesBinding binding;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reference = FirebaseDatabase.getInstance().getReference("Recipes");
        binding.rvRecipes.setLayoutManager(new GridLayoutManager(this,2));
        binding.rvRecipes.setAdapter(new RecipeAdapter());
        String type = getIntent().getStringExtra("type");
        if (type.equalsIgnoreCase("category")){
            filterByCategory();
        }else if (type.equalsIgnoreCase("search")){
            loadByRecipes();
        }else {
            loadAllRecipes();
        }
    }

    private void loadByRecipes() {
        // Search Recipes by Name
        String query = getIntent().getStringExtra("query");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe.getName().toLowerCase().contains(query.toLowerCase()))
                        recipes.add(recipe);
                }
                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null){
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());
            }
        });
    }

    private void loadAllRecipes() {
        // Load All Recipes
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                Collections.shuffle(recipes);
                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null){
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());
            }
        });
    }

    private void filterByCategory() {
        // Filter Recipes by Category
        String category = getIntent().getStringExtra("category");
        reference.orderByChild("category").equalTo(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }

                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null){
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());
            }
        });
    }
}