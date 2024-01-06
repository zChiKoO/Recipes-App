package com.example.murat.recipesapp.fragment;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.murat.recipesapp.R;
import com.example.murat.recipesapp.adapters.RecipeAdapter;
import com.example.murat.recipesapp.databinding.FragmentProfileBinding;
import com.example.murat.recipesapp.models.Recipe;
import com.example.murat.recipesapp.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * We will create a profile screen.
 * We will use the following things;
 * 1. Constraint Layout
 * 2. Card View
 * 3. Text View
 * 4. Image View and Circle Image View (Using 'de.hdodenhof:circleimageview:3.1.0')
 * 5. Material Design
 * 6. Material Button
 * 7. Recycler View
 * A user data model class used to insert and retrieve data from firebase
 * First of all, we need to create some Nodes in our Firebase Database
 * For Retrieving data from Firebase, we need to add some dependencies in our app level buil.gradle file
 * For Image Loading, we will use Glide Library
 * We will upload data from Firebase Realtime Database and store images in Firebase Storage
 * First we will set click listener on the button of Edit Profile
 * Now we will pick image from gallery and upload it to Firebase Storage
 * We will load image from Firebase Storage and display it in our app with Download URL
 * Now we will upload data to Firebase Realtime Database Cover Image
 * We will create a new Screen for Adding New Recipe
 * We will use Firebase Realtime Database to store our Recipes
 * We will use Firebase Storage to store our Recipe Images
 * We will use Firebase Authentication to Authenticate our Users
 * For adding new Recipe we will create a new Activity named AddRecipeActivity
 * To navigate to this Activity we will create a button in MainActivity
 * All recipes publish under my Profile will be shown in ProfileFragment
 */

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Login Required")
                    .setMessage("You need to login to view your profile")
                    .show();
        }else {
            loadProfile();
            loadUserRecipes();
            init();
        }
    }

    private void init() {
        binding.imgEditProfile.setOnClickListener(v -> {
            PickImageDialog.build(new PickSetup()).show(requireActivity()).setOnPickResult(r -> {
                Log.e("ProfileFragment","onPickResult: " + r.getUri());
                binding.imgProfile.setImageBitmap(r.getBitmap());
                binding.imgCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                uploadImage(r.getBitmap());
            }).setOnPickCancel(() -> Toast.makeText(requireContext(),"Cancelled",Toast.LENGTH_SHORT).show());
        });
        binding.imgEditCover.setOnClickListener(view ->
                PickImageDialog.build(new PickSetup()).show(requireActivity()).setOnPickResult(r -> {
                    Log.e("ProfileFragment","onPickResult: " + r.getUri());
                    binding.imgCover.setImageBitmap(r.getBitmap());
                    binding.imgCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    uploadCoverImage(r.getBitmap());
                }).setOnPickCancel(() -> Toast.makeText(requireContext(),"Cancelled",Toast.LENGTH_SHORT).show()));
    }

    private void uploadCoverImage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getUid()+"cover.jpg");
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
                Toast.makeText(requireContext(),"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                user.setCover(Objects.requireNonNull(downloadUri).toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user);
            } else {
                Log.e("ProfileFragment","onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    private void uploadImage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getUid()+"image.jpg");
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
                Toast.makeText(requireContext(),"Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                user.setImage(Objects.requireNonNull(downloadUri).toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user);
            } else {
                Log.e("ProfileFragment","onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });

    }

    private void loadUserRecipes() {
        binding.rvProfile.setLayoutManager(new GridLayoutManager(getContext(),3));
        binding.rvProfile.setAdapter(new RecipeAdapter());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Recipes").orderByChild("authorId").equalTo(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                ((RecipeAdapter) Objects.requireNonNull(binding.rvProfile.getAdapter())).setRecipeList(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment","onCancelled: " + error.getMessage());
            }
        });
    }

    private void loadProfile() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    binding.tvUserName.setText(user.getName());
                    binding.tvEmail.setText(user.getEmail());
                    Glide
                            .with(requireContext())
                            .load(user.getImage())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(binding.imgProfile);
                    Glide
                            .with(requireContext())
                            .load(user.getCover())
                            .centerCrop()
                            .placeholder(R.drawable.bg_default_recipe)
                            .into(binding.imgCover);

                }else {
                    Log.e("ProfileFragment","onDataChange: User is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment","onCancelled: " + error.getMessage());

            }
        });
        User user = new User(); // Load from Firebase here
        user.setName("Murat");
        user.setEmail("muratozgur2828@gmail.com");
        binding.tvUserName.setText(user.getName());
        binding.tvEmail.setText(user.getEmail());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}