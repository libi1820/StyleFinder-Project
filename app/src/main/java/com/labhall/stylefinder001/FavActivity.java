package com.labhall.stylefinder001;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labhall.stylefinder001.ui.Store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FavActivity extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseReference favoritesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fav_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        container = findViewById(R.id.container);
        mAuth = FirebaseAuth.getInstance();
        LinearLayout storeLayout;
        if (getSupportActionBar() != null) {  //hide action bar
            getSupportActionBar().hide();
        }

        Store[] stores = new Store[10];

        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("stores_list.csv");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // Assuming comma-separated values
                stores[i] = new Store(data[0], data[1], Integer.parseInt(data[2]), data[3]);
                i++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            favoritesRef = FirebaseDatabase.getInstance("https://stylefinder-2bf8c-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference()
                    .child("Users")
                    .child(userId)
                    .child("favoriteStores");

            favoritesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> favoriteStores = new ArrayList<>();
                    for (DataSnapshot storeSnapshot : dataSnapshot.getChildren()) {
                        String storeName = storeSnapshot.getValue(String.class);
                        favoriteStores.add(storeName);
                    }

                    if (favoriteStores.size() == 0) {
                        Toast.makeText(FavActivity.this, "You have no favorite yet", Toast.LENGTH_SHORT).show();
                    }

                    for (Store store : stores) {
                        if (favoriteStores.contains(store.getName())) {
                            LinearLayout storeLayout=createStoreLayout(store);
                            storeLayout.setBackgroundColor(Color.TRANSPARENT);
                            container.addView(storeLayout);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle the error
                }
            });
        }

    }

    private LinearLayout createStoreLayout(Store store) {
        LinearLayout storeLayout = new LinearLayout(this);
        storeLayout.setOrientation(LinearLayout.VERTICAL);
        storeLayout.setPadding(16, 16, 16, 16);

        TextView nameTextView = new TextView(this);

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


        storeLayout.addView(nameTextView);
        storeLayout.addView(styleTextView);
        storeLayout.addView(priceTextView);
        storeLayout.addView(addressTextView);

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
    @Override
    protected void onPause() {
        super.onPause();
        // Apply fade animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
