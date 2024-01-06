package com.example.murat.recipesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.murat.recipesapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

/**
 * MainActivity.java
 * Purpose: Main activity for the app. This activity is the entry point for the app.
 * To Manage fragments, we need to use Navigation Component
 * We need 3 fragments for this app. Home, Profile and Category.
 * We need to add one more feature to our app, which is the ability to login as Guest
 * Guest will be able to view All recipes, but not add or edit recipes
 * We will use Firebase Authentication to implement this feature
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ActivityMainBinding binding;
        super.onCreate(savedInstanceState);
        // binding = ActivityMainBinding.inflate(getLayoutInflater());
        com.example.murat.recipesapp.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        binding.floatingActionButton.setOnClickListener(view -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null){
                Toast.makeText(this,"Please login to add recipe",Toast.LENGTH_SHORT).show();
            }else{
                startActivity(new Intent(MainActivity.this, AddRecipeActivity.class));
            }
        });
    }
}