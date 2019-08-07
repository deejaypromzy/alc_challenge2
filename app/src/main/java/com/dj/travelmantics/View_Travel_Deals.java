package com.dj.travelmantics;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class View_Travel_Deals extends AppCompatActivity {
    final static int Gallery_Pick = 1;
    private static final int REQUEST_CODE = 112;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    public static String markerString;
    private Button amtImage, save, delete;
    private TextView city, desc, amt, place;
    private ImageView proImage;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mref;
    private ProgressDialog mProgress;
    private String userid;
    private StorageTask storageTask;
    private StorageReference ImageRef;
    private DateFormat df;
    private Date date;
    private Uri resultUri;
    private Calendar myCalendar;
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view__travel__deals);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mref = FirebaseDatabase.getInstance().getReference();
        mProgress = new ProgressDialog(this);
        ImageRef = FirebaseStorage.getInstance().getReference().child(FilePaths.travel_deals_images);

        city = findViewById(R.id.city);
        desc = findViewById(R.id.Desc);
        amt = findViewById(R.id.amt);
        place = findViewById(R.id.place);
        proImage = findViewById(R.id.productImage);


        city.setText(getIntent().getStringExtra("city"));
        desc.setText(getIntent().getStringExtra("desc"));
        amt.setText(getIntent().getStringExtra("amt"));
        place.setText(getIntent().getStringExtra("place"));
        GlideApp.with(this)
                .load(getIntent().getStringExtra("photoUrl"))
                .placeholder(R.drawable.no_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .into(proImage);


    }
}
