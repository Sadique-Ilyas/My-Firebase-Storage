package com.example.myfirebasestoragecodinginflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener{
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private ProgressBar progressBarCircle;
    private DatabaseReference databaseReference;
    private ValueEventListener mDBListener;
    private FirebaseStorage firebaseStorage;
    private List<Upload> uploads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBarCircle = findViewById(R.id.progress_circle);
        uploads = new ArrayList<>();
        adapter = new ImageAdapter(ImagesActivity.this,uploads);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(ImagesActivity.this);
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");

        mDBListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploads.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren())
                {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    uploads.add(upload);
                }
                adapter.notifyDataSetChanged();
                progressBarCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImagesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void OnItemClick(int position) {
        Toast.makeText(this, "Normal Click at position: " + (position + 1), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnWhateverClick(int position) {
        Toast.makeText(this, "Whatever Click at position: "+ (position + 1), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnDeleteClick(int position) {
        Upload selectedItem = uploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReference.child(selectedKey).removeValue();
                Toast.makeText(ImagesActivity.this, "Item Deleted !!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(mDBListener);
    }
}