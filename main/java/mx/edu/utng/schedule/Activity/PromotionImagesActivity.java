package mx.edu.utng.schedule.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import mx.edu.utng.schedule.Adapter.PromotionImageAdapter;
import mx.edu.utng.schedule.PromotionUpload;
import mx.edu.utng.schedule.R;

public class PromotionImagesActivity extends AppCompatActivity implements PromotionImageAdapter.OnItemClickListener{

    private RecyclerView rcvPromotions;
    private PromotionImageAdapter mAdapter;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    private List<PromotionUpload> mUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_images);

        rcvPromotions = findViewById(R.id.rcv_promotion);
        rcvPromotions.setHasFixedSize(true);
        rcvPromotions.setLayoutManager(new LinearLayoutManager(this));


        mUploads = new ArrayList<>();

        mAdapter = new PromotionImageAdapter(PromotionImagesActivity.this, mUploads);

        rcvPromotions.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(PromotionImagesActivity.this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("promotion_uploads");

        mStorage = FirebaseStorage.getInstance();

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUploads.clear();

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    PromotionUpload upload = postSnapshot.getValue(PromotionUpload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PromotionImagesActivity.this, databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {
        PromotionUpload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getUrlImage());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(PromotionImagesActivity.this, R.string.success_process, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}
