package com.labhall.stylefinder001;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.labhall.stylefinder001.ui.Store;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class RecActivity extends AppCompatActivity {

    private LinearLayout container;
    private ArrayList<String> favoriteStores = new ArrayList<>();
    private DatabaseReference mDatabase;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (getSupportActionBar() != null) {  //hide action bar
            getSupportActionBar().hide();
        }

        String selectedStyle = "";
        String selectedPrice = "";
        Store[] stores = new Store[10];

        container = findViewById(R.id.container);


        Intent intent = getIntent();
        if (intent != null) {
            selectedStyle = intent.getStringExtra("selectedStyle").toLowerCase(Locale.ROOT);
            selectedPrice = intent.getStringExtra("selectedPrice");
        }



        //FIREBASE DATA RETRIEVING

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://stylefinder-2bf8c-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
            DatabaseReference userRef = databaseReference.child("Users").child(currentUser.getUid());

            // Retrieve favorite stores from Firebase
            userRef.child("favoriteStores").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot!=null) {
                        favoriteStores.clear();
                        for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                            String storeName = storeSnapshot.getValue(String.class);
                            favoriteStores.add(storeName);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if(error!=null){
                        // Handle error while retrieving data from Firebase
                        Toast.makeText(RecActivity.this, "Failed to retrieve favorite stores", Toast.LENGTH_SHORT).show();
                }}
            });
        } else {
            // Handle the case when the user is not logged in
            // You can redirect the user to the login screen or handle it according to your app logic
        }






        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("stores_list.csv");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            i = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // Assuming comma-separated values
                stores[i] = new Store(data[0], data[1], Integer.parseInt(data[2]), data[3]);
                i++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stores = giveScores(stores, selectedStyle, selectedPrice);
        stores = sortStores(stores);

        for (Store store : stores) {
            LinearLayout storeLayout = createStoreLayout(store);
            storeLayout.setBackgroundColor(Color.TRANSPARENT);
            container.addView(storeLayout);
        }
    }

    public Store[] giveScores(Store[] stores, String style, String price) {
        int p = 0, count;
        if (price.equals("$")) p = 1;
        if (price.equals("$$")) p = 2;
        if (price.equals("$$$")) p = 3;

        for (int i = 0; i < 10; i++) {
            count = 0;
            if (stores[i].getStyle().equals(style)) count += 2;
            if (stores[i].getPrice() == p) count += 2;
            else if ((Math.abs(stores[i].getPrice() - p)) == 1) count += 1;

            stores[i].setScore(count);
        }
        return stores;
    }

    public Store[] sortStores(Store[] stores) {
        int count = 0;
        ArrayList<Store>[] lists = new ArrayList[5];
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new ArrayList<Store>();
        }
        for (int i = 0; i < 10; i++) {
            lists[stores[i].getScore()].add(stores[i]);
        }
        for (int i = 4; i >= 0; i--) {
            for (int j = 0; j < lists[i].size(); j++) {
                stores[count] = lists[i].get(j);
                count++;
            }
        }
        return stores;
    }

    private LinearLayout createStoreLayout(Store store) {
        LinearLayout storeLayout = new LinearLayout(this);
        storeLayout.setOrientation(LinearLayout.VERTICAL);
        storeLayout.setPadding(16, 16, 16, 16);

        TextView nameTextView = new TextView(this);
        LinearLayout.LayoutParams layoutParamsName = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParamsName.setMargins(0, dpToPx(32), 0, 0); // 16dp top margin
        nameTextView.setLayoutParams(layoutParamsName);
        nameTextView.setText("Name: " + store.getName());


        TextView styleTextView = new TextView(this);
        styleTextView.setText("Style: " + store.getStyle());

        TextView priceTextView = new TextView(this);
        String price="";
        if(store.getPrice()==1){
            price="$";
        }
        if(store.getPrice()==2){
            price="$$";
        }
        if(store.getPrice()==3){
            price="$$$";
        }
        priceTextView.setText("Price: " + price);

        TextView addressTextView = new TextView(this);
        addressTextView.setText("Address: " + store.getAddress());

        Button addToFavoritesButton = new Button(this);
        addToFavoritesButton.setTextSize(20);
        addToFavoritesButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#000080")));
        addToFavoritesButton.setTextColor(Color.parseColor("#FFD700"));
        addToFavoritesButton.setText("Add to Favorites");
        addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shopName = nameTextView.getText().toString().replace("Name: ", "");
                addToFavorites(shopName);
                // Update the favoriteStores array in Firebase here
                // You can use the favoriteStores array to update the Firebase database or Firestore
                Toast.makeText(RecActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            }
        });



        storeLayout.addView(nameTextView);
        storeLayout.addView(styleTextView);
        storeLayout.addView(priceTextView);
        storeLayout.addView(addressTextView);
        storeLayout.addView(addToFavoritesButton);

        //Create an ImageView for each image in the store's images array
        String shop = store.getName();
        shop = shop.replace(" ", "_");
        shop = shop.replace("&", "");
        shop = shop.toLowerCase();

        for (int i = 0; i < 5; i++) {
            try {
                ImageView imageView = new ImageView(this);
                String imageName = shop + Integer.toString(i);
                String resourceName = "drawable/" + imageName;

                int resId = getResources().getIdentifier(resourceName, null, getPackageName());
                imageView.setImageResource(resId);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, 16, 0, 16);

                storeLayout.addView(imageView, layoutParams);
            } finally {
                // Handling code for the exception
            }
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 24);

        storeLayout.setLayoutParams(layoutParams);
        storeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.pink_nude));

        return storeLayout;
    }

    private void addToFavorites(String shopName) {
        if (!favoriteStores.contains(shopName)) {
            if (favoriteStores.size() < 10) {
                favoriteStores.add(shopName);

                // Update favorite stores in Firebase
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://stylefinder-2bf8c-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                    DatabaseReference userRef = databaseReference.child("Users").child(currentUser.getUid());

                    userRef.child("favoriteStores").setValue(favoriteStores)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Favorite stores updated successfully in Firebase
                                    Toast.makeText(RecActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    // Failed to update favorite stores in Firebase
                                    Toast.makeText(RecActivity.this, "Failed to add to Favorites", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Handle the case when the user is not logged in
                    // You can redirect the user to the login screen or handle it according to your app logic
                }
            } else {
                // Handle the case when the favoriteStores array is already full
                Toast.makeText(this, "Favorites array is full", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case when the shop is already in the favoriteStores array
            Toast.makeText(this, "Shop is already in favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Apply fade animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}