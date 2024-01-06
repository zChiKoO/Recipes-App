package com.example.murat.recipesapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.murat.recipesapp.adapters.CategoryAdapter;
import com.example.murat.recipesapp.databinding.FragmentCategoryBinding;
import com.example.murat.recipesapp.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This is Categories Fragment and we will design categories screen here and adapter for categories
 * Now we have 2 categories in our Database, Now we will retrieve data from Firebase Database and show it in our RecyclerView
 * For design, we need to update our Category Text color and background color
 */


public class CategoriesFragment extends Fragment {

    private FragmentCategoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvCategories.setAdapter(new CategoryAdapter());
        loadCategories();
    }

    private void loadCategories() {
        binding.rvCategories.setAdapter(new CategoryAdapter());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Category category = dataSnapshot.getValue(Category.class);
                    categories.add(category);
                }

                CategoryAdapter adapter = (CategoryAdapter) binding.rvCategories.getAdapter();
                if (adapter != null) {
                    adapter.setCategoryList(categories);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}