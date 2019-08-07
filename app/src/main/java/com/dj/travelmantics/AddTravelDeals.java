package com.dj.travelmantics;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class AddTravelDeals extends AppCompatActivity implements View.OnClickListener, ChangePhotoDialog.OnPhotoReceivedListener {
    private static final int REQUEST_CODE =112 ;
    private Button amtImage,save;
    private EditText city,desc,amt,place;
    private ImageView proImage;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mref;
    private ProgressDialog mProgress;
    private String userid;
    final static int Gallery_Pick = 1;
    private StorageTask storageTask;
    private StorageReference ImageRef;
    private DateFormat df;
    public static String markerString;
    private Date date;
    private  Uri resultUri;
    private Calendar myCalendar;


    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;

           GlideApp.with(this)
                    .load(imagePath.toString())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .into(proImage);
        }

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            //  Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
            GlideApp.with(this)
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                    .into(proImage);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_travel_deals);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mref= FirebaseDatabase.getInstance().getReference();
        mProgress=new ProgressDialog(this);
        ImageRef = FirebaseStorage.getInstance().getReference().child(FilePaths.travel_deals_images);


        amtImage=findViewById(R.id.productImageBtn);
        save=findViewById(R.id.publish);
        city=findViewById(R.id.city);
        desc=findViewById(R.id.Desc);
        amt=findViewById(R.id.amt);
        place=findViewById(R.id.place);
        proImage=findViewById(R.id.productImage);

        save.setOnClickListener(this);
        amtImage.setOnClickListener(this);

    }


        private boolean validateForm() {
            if (proImage.getDrawable()== null) {
                Toast.makeText(this, "Image Cant be empty", Toast.LENGTH_SHORT).show();
                return true;
            }


        String mycity = city.getText().toString();
        if (TextUtils.isEmpty(mycity)) {
            city.setError("City cant be empty.");
            city.requestFocus();
            return true;
        } else {
            city.setError(null);
        }

        String myamt = amt.getText().toString();
        if (TextUtils.isEmpty(myamt)) {
            amt.setError("Amount cant be empty.");
            amt.requestFocus();
            return true;
        } else {
            amt.setError(null);
        } String myplace = place.getText().toString();
        if (TextUtils.isEmpty(myplace)) {
            place.setError("Place cant be empty.");
            place.requestFocus();
            return true;
        } else {
            place.setError(null);
        }
String mydesc = desc.getText().toString();
        if (TextUtils.isEmpty(mydesc)) {
            desc.setError("Amount cant be empty.");
            desc.requestFocus();
            return true;
        } else {
            desc.setError(null);
        }


        return false;
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.publish:
                if (validateForm()){
                    return;
                }
                showProgressDialog();


                if (mSelectedImageUri!=null) {
                    uploadNewPhoto(mSelectedImageUri);
                }else
                if (mSelectedImageBitmap!=null) {
                    uploadNewPhoto(mSelectedImageBitmap);
                }
                break;
            case R.id.productImageBtn:
                if(verifyStoragePermissions()){
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(),("Choose one"));
                }else{
                    verifyStoragePermissions();
                }
                break;
                
        }
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageUri***
     * @param imageUri
     */
    public void uploadNewPhoto(Uri imageUri){
        /*
            upload a new profile photo to firebase storage
         */
        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageBitmap***
     * @param imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        /*
            upload a new profile photo to firebase storage
         */
        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(AddTravelDeals.this.getContentResolver(), params[0]);
                } catch (IOException ignored) {
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(AddTravelDeals.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }
    private void executeUploadTask(){
        showProgressDialog();
        FilePaths filePaths = new FilePaths();
//specify where the photo will be stored
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(FilePaths.travel_deals + "/" + city.getText().toString()); //just replace the old image with the new one

        if(mBytes.length/MB < MB_THRESHHOLD) {
            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);


            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri firebaseURL = task.getResult();

                        Toast.makeText(AddTravelDeals.this, "Saved Successfully", Toast.LENGTH_SHORT).show();

                        Database dbUser = new Database(
                                city.getText().toString(),
                                amt.getText().toString(),
                                place.getText().toString(),
                                desc.getText().toString(),
                                firebaseURL.toString()

                        );


                        FirebaseDatabase.getInstance().getReference()
                                .child(FilePaths.travel_deals).child("deals").child(city.getText().toString())
                                .setValue(dbUser);

                        hideProgressDialog();
                        finish();


                    } else {

                        Toast.makeText(AddTravelDeals.this, "Error  ..Try again", Toast.LENGTH_SHORT).show();

                        hideProgressDialog();
                        // Handle failures
                        // ...
                    }
                }
            });


        }else
            Toast.makeText(this, "Image too large", Toast.LENGTH_SHORT).show();


    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
    public boolean verifyStoragePermissions(){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(
                    AddTravelDeals.this,
                    permissions,
                    REQUEST_CODE
            );
            return false;
        }
    }
    public ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(("Please wait..."));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();

        }
    }

}
