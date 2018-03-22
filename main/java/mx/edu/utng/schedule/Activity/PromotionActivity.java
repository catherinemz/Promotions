package mx.edu.utng.schedule.Activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import mx.edu.utng.schedule.PromotionUpload;
import mx.edu.utng.schedule.R;


public class PromotionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnChooseImage;
    private Button btnUpload;
    private Button btnShow;
    private EditText edtDescription;
    private ImageView imvPromotionImage;
    private ProgressBar pgbLoading;

    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        btnChooseImage = findViewById(R.id.btn_choose_file_prom);
        btnUpload = findViewById(R.id.btn_upload_prom);
        btnShow = findViewById(R.id.btn_show_prom);
        edtDescription = findViewById(R.id.edt_promotion);
        imvPromotionImage = findViewById(R.id.imv_promotion_image);
        pgbLoading = findViewById(R.id.pgb_loading_prom);

        mStorageRef = FirebaseStorage.getInstance().getReference("promotion_uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("promotion_uploads");

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(PromotionActivity.this, R.string.loading, Toast.LENGTH_SHORT).show();
                }else {
                    uploadFile();
                }
            }
        });

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagesActivity();
            }
        });

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(imvPromotionImage);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadFile(){
       if(mImageUri != null){
           StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                   + "." + getFileExtension(mImageUri));
           mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   Handler handler = new Handler();
                   handler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           pgbLoading.setProgress(0);
                       }
                   }, 5000);

                   Toast.makeText(PromotionActivity.this, R.string.success_process,
                           Toast.LENGTH_LONG).show();
                   PromotionUpload upload = new PromotionUpload(edtDescription.getText()
                           .toString().trim(), taskSnapshot.getDownloadUrl().toString());
                   String uploadId = mDatabaseRef.push().getKey();
                   mDatabaseRef.child(uploadId).setValue(upload);
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(PromotionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
               }
           }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                   double progress = (100.0 * taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                   pgbLoading.setProgress((int) progress);
               }
           });
       }else{
           Toast.makeText(this, R.string.do_not_selected, Toast.LENGTH_SHORT).show();
       }
    }

    private void openImagesActivity(){
        Intent intent = new Intent(this, PromotionImagesActivity.class);
        startActivity(intent);
    }
}
