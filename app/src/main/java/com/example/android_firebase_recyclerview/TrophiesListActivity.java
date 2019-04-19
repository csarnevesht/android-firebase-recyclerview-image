package com.example.android_firebase_recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;

public class TrophiesListActivity extends AppCompatActivity {

    LinearLayoutManager mLayoutManager; //for search
    RecyclerView mRecyclerView;

    FirebaseFirestore mFirebaseDatabase;
    CollectionReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophies_list);

        // Access a Cloud Firestore instance from your Activity
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mRef = mFirebaseDatabase.collection("Data");

        mLayoutManager = new LinearLayoutManager(this);

        //RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        //set layout as LinearLayout
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    //search data
    private void firebaseSearch(String searchText) {

        //convert string entered in SearchView to lowercase
        String query = searchText.toLowerCase();
        Query firebaseSearchQuery = mRef.whereArrayContains("search", query);
        FirestoreRecyclerOptions<Model> options = new FirestoreRecyclerOptions.Builder<Model>()
                .setQuery(firebaseSearchQuery, Model.class)
                .build();
        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Model, ViewHolder>(options) {

            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, int position, Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row, parent, false);
                ViewHolder viewHolder = new ViewHolder(view);

                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        handleItemClick(view, position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //TODO do your own implementaion on long item click
                    }
                });
                return viewHolder;
            }
        };

        //set adapter to recyclerview
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    //load data into recycler view onStart
    @Override
    protected void onStart() {
        super.onStart();

        FirestoreRecyclerOptions<Model> options = new FirestoreRecyclerOptions.Builder<Model>()
                .setQuery(mRef, Model.class)
                .build();
        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Model, ViewHolder>(options) {

            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, int position, Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row, parent, false);
                ViewHolder viewHolder = new ViewHolder(view);

                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        handleItemClick(view, position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //TODO do your own implementaion on long item click
                    }
                });
                return viewHolder;
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        //set adapter to recyclerview
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public void handleItemClick(View view, int position) {
        //Views
        TextView mTitleTv = view.findViewById(R.id.rTitleTv);
        TextView mDescTv = view.findViewById(R.id.rDescriptionTv);
        ImageView mImageView = view.findViewById(R.id.rImageView);
        //get data from views
        String mTitle = mTitleTv.getText().toString();
        String mDesc = mDescTv.getText().toString();
        Drawable mDrawable = mImageView.getDrawable();
        Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();

        //pass this data to new activity
        Intent intent = new Intent(view.getContext(), TrophyDetailActivity.class);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        intent.putExtra("image", bytes); //put bitmap image as array of bytes
        intent.putExtra("title", mTitle); // put title
        intent.putExtra("description", mDesc); //put description
        startActivity(intent); //start activity
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it present
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Filter as you type
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}