package com.example.murat.recipesapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
/**
 * We will create Recycler View Adapter and Our Recipe XML item.
 * First we create a Data Model class for our recipe
 * We will use this class to show our recipe data in Recyclerview
 * Now we create a list and Add some dummy data to our list
 */
import com.example.murat.recipesapp.AllRecipesActivity;
import com.example.murat.recipesapp.SettingActivity;
import com.example.murat.recipesapp.adapters.HorizontalRecipeAdapter;
import com.example.murat.recipesapp.databinding.FragmentHomeBinding;
import com.example.murat.recipesapp.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * We will create recipe content in ChatGPT and then we will copy the content and paste it in our database with JSON format
 * We have 25 recipes in our Database but without images.
 * For HomeFragment we don't have recipes for popular and favourite categories
 * We select some recipes from our database and show them in our HomeFragment
 * We do this by random selection
 */

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRecipes();
        binding.etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH){
                performSearch();
                return true;
            }
            return false;
        });

        binding.tvSeeAllFavourite.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
            intent.putExtra("type","favourite");
            startActivity(intent);
        });

        binding.tvSeeAllPopulars.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
            intent.putExtra("type","popular");
            startActivity(intent);
        });

        binding.btnSetting.setOnClickListener(view1 -> {
            startActivity(new Intent(requireContext(), SettingActivity.class));
        });
    }

    private void performSearch() {
        String query = Objects.requireNonNull(binding.etSearch.getText()).toString().trim();
        Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
        intent.putExtra("type","search");
        intent.putExtra("query",query);
        startActivity(intent);
    }

    private void loadRecipes() {
        // We will load recipes from our Database
        binding.rvPopulars.setAdapter(new HorizontalRecipeAdapter());
        binding.rvFavouriteMeal.setAdapter(new HorizontalRecipeAdapter());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                loadPopularRecipes(recipes);
                loadFavouriteRecipes(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());
            }
        });
    }

    private void loadPopularRecipes(List<Recipe> recipes) {
        List<Recipe> popularRecipes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * recipes.size());
            popularRecipes.add(recipes.get(random));
        }
        HorizontalRecipeAdapter adapter = (HorizontalRecipeAdapter) binding.rvPopulars.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(popularRecipes);
        }

    }

    private void loadFavouriteRecipes(List<Recipe> recipes) {
        List<Recipe> favouriteRecipes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * recipes.size());
            favouriteRecipes.add(recipes.get(random));
        }
        HorizontalRecipeAdapter adapter = (HorizontalRecipeAdapter) binding.rvFavouriteMeal.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(favouriteRecipes);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}