package com.example.murat.recipesapp.adapters;

import static com.example.murat.recipesapp.databinding.ItemRecipeBinding.inflate;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.murat.recipesapp.R;
import com.example.murat.recipesapp.RecipeDetailsActivity;
import com.example.murat.recipesapp.databinding.ItemRecipeBinding;
import com.example.murat.recipesapp.models.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * We use view binding here
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeHolder> {
    List<Recipe> recipeList = new ArrayList<>();
    public void setRecipeList(List<Recipe> recipeList){
        this.recipeList = recipeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeAdapter.RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeHolder(inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.RecipeHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.onBind(recipe);

    }

    @Override
    public int getItemCount() {

        return recipeList.size();
    }

    public static class RecipeHolder extends RecyclerView.ViewHolder {

        ItemRecipeBinding binding;

        public RecipeHolder(@NonNull ItemRecipeBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(Recipe recipe){
            Glide
                    .with(binding.getRoot().getContext())
                    .load(recipe.getImage())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.bgImgRecipe);
            binding.tvRecipeName.setText(recipe.getName());
            binding.getRoot().setOnClickListener(view -> {
                Intent intent = new Intent(binding.getRoot().getContext(), RecipeDetailsActivity.class);
                intent.putExtra("recipe",recipe);
                binding.getRoot().getContext().startActivity(intent);
            });
        }
    }
}
