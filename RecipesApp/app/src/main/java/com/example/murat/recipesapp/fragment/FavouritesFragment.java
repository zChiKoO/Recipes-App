package com.example.murat.recipesapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.murat.recipesapp.R;
import com.example.murat.recipesapp.adapters.RecipeAdapter;
import com.example.murat.recipesapp.databinding.FragmentFavouritesBinding;
import com.example.murat.recipesapp.models.FavouriteRecipe;
import com.example.murat.recipesapp.models.Recipe;
import com.example.murat.recipesapp.room.RecipeRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * In this fragment,we will show all favourite recipes.
 * We load all favourite recipes from database and show them in a RecyclerView.
 * We will use the same adapter we used in RecipeFragment, Room Library and Firebase combination.
 */
public class FavouritesFragment extends Fragment {

    FragmentFavouritesBinding binding;
    RecipeRepository recipeRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        recipeRepository = new RecipeRepository(requireActivity().getApplication());
        List<FavouriteRecipe> favouriteRecipes = recipeRepository.getAllFavourites();
        if (favouriteRecipes.isEmpty()) {
            Toast.makeText(requireContext(), "No Favourites", Toast.LENGTH_SHORT).show();
            binding.rvFavourites.setVisibility(View.GONE);
            binding.noFavourites.setVisibility(View.VISIBLE);
        } else {
            binding.rvFavourites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            binding.rvFavourites.setAdapter(new RecipeAdapter());
            List<Recipe> recipes = new ArrayList<>();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            for (FavouriteRecipe favouriteRecipe : favouriteRecipes) {
                                if (dataSnapshot.getKey().equals(favouriteRecipe.getRecipeId())) {
                                    recipes.add(dataSnapshot.getValue(Recipe.class));
                                }
                            }
                        }
                        binding.rvFavourites.setVisibility(View.VISIBLE);
                        binding.noFavourites.setVisibility(View.GONE);
                        RecipeAdapter adapter = (RecipeAdapter) binding.rvFavourites.getAdapter();
                        if (adapter != null) {
                            adapter.setRecipeList(recipes);
                        }

                    } else {
                        binding.noFavourites.setVisibility(View.VISIBLE);
                        binding.rvFavourites.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FavouritesFragment", "onCancelled: " + error.getMessage());
                }
            });
        }
    }
}