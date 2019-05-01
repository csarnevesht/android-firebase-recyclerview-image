package com.example.android_firebase_recyclerview;

import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.ByteArrayOutputStream;

public class TrophiesListActivity extends AppCompatActivity {

    LinearLayoutManager mLayoutManager; //for search
    RecyclerView mRecyclerView;

    private MaterialSearchBar searchBar;

    FirebaseFirestore mFirebaseDatabase;
    CollectionReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trophies_list);

        // Access a Cloud Firestore instance from your Activity
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mRef = mFirebaseDatabase.collection("Data");

        mLayoutManager = new GridLayoutManager(this, 3);

        //RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        //set layout as LinearLayout
        mRecyclerView.setLayoutManager(mLayoutManager);

        searchBar = findViewById(R.id.searchBar);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
                firebaseSearch(searchBar.getText());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " after text changed " + searchBar.getText());
                firebaseSearch(searchBar.getText());

            }

        });

        searchBar.enableSearch();

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
                        .inflate(R.layout.trophy_list_item, parent, false);
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


    //search data
    private void firebaseSearch(String searchText) {

        //convert string entered in SearchView to lowercase
        String query = searchText.toLowerCase();
        Query firebaseSearchQuery = mRef.whereArrayContains("search", query);
        FirestoreRecyclerOptions<Model> options = new FirestoreRecyclerOptions.Builder<Model>()
                .setQuery(query.isEmpty() ?  mRef : firebaseSearchQuery, Model.class)
                .build();
        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Model, ViewHolder>(options) {

            @Override
            protected void onBindViewHolder(ViewHolder viewHolder, int position, Model model) {
                viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getDescription(), model.getImage());
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.trophy_list_item, parent, false);
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
}