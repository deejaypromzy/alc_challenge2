package com.dj.travelmantics;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {


    private ArrayList<Database> DealsData;
    private TravelDeals dealsAdapter;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mref;
    private ProgressBar mProgress;
    private boolean admin;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FloatingActionButton fab;
    private String role = "";

    long lastPress;
    private SharedPreferences pref;


    void isAdmin(FirebaseUser user) {
        Query userQuery = mref.child(FilePaths.travel_deals).child("users").child(user.getUid());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //this loop will return a single result

                Database userDetails = dataSnapshot.getValue(Database.class);
                if ((userDetails != null ? userDetails.getRole() : null) != null) {

                    role = userDetails.getRole();
                    if (role.equals("1")) {
                        showFloatingActionButton(fab);

                    } else {
                        hideFloatingActionButton(fab);
                    }

                    Toast.makeText(MainActivity.this, role, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showData(DataSnapshot dataSnapshot) {
        //Create the ArrayList of Sports objects with the titles, images
        // and information about each sport
        DealsData.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()){
            Database userDatabase = ds.getValue(Database.class);


            DealsData.add(new Database(
                    userDatabase != null ? userDatabase.getCity() : null,
                    userDatabase != null ? userDatabase.getAmount() : null,
                    userDatabase != null ? userDatabase.getPlace() : null,
                    userDatabase != null ? userDatabase.getDesc() : null,
                    userDatabase != null ? userDatabase.getPhoto_url() : null));


        }
        dealsAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setIcon(R.mipmap.ic_launcher_round);
            // Setting Dialog Message
            alertDialog.setMessage("Do you really want to log out?");


            // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    // Write your code here to invoke YES event
                    signOut();
                }
            });


            // Setting Negative "NO" Button
            alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to invoke NO event
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sign out the current user
     */
    private void signOut(){
        Log.d("tag", "signOut: signing out");
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void checkAuthenticationState(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.i("u","user!=null");
        }
    }

    private void setupFirebaseAuth(){
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mref = mfirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void hideFloatingActionButton(FloatingActionButton fab) {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FloatingActionButton.Behavior behavior =
                (FloatingActionButton.Behavior) params.getBehavior();

        if (behavior != null) {
            behavior.setAutoHideEnabled(false);
        }

        fab.hide();
    }

    private void showFloatingActionButton(FloatingActionButton fab) {
        fab.show();
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FloatingActionButton.Behavior behavior =
                (FloatingActionButton.Behavior) params.getBehavior();

        if (behavior != null) {
            behavior.setAutoHideEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupFirebaseAuth();
        mProgress = findViewById(R.id.progressBar);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        final RecyclerView mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Initialize the ArrayList that will contain the data
        DealsData = new ArrayList<>();
        dealsAdapter = new TravelDeals(this, DealsData);
        mRecyclerView.setAdapter(dealsAdapter);


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddTravelDeals.class));
            }
        });

        hideFloatingActionButton(fab);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    hideFloatingActionButton(fab);
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    if (role.equals("1")) {
                        showFloatingActionButton(fab);
                    }
                    // showFloatingActionButton(fab);

                }
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if ((user != null ? user.getDisplayName() : null) != null) {
            Toast.makeText(this, "welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

        }


        new CountDownTimer(2000, 1000) {
            public void onTick(long ms) {

                mProgress.setVisibility(VISIBLE);
            }

            public void onFinish() {


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                isAdmin(user);
                Query dealsQuery = mref.child(FilePaths.travel_deals).child("deals").orderByKey();

                dealsQuery.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        showData(dataSnapshot);
                        mProgress.setVisibility(GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//c.close();
            }
        }.start();


    }

    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPress > 5000) {
            Toast.makeText(this, "Press back again to exit !", Toast.LENGTH_SHORT).show();
            lastPress = currentTime;
        } else {
            super.onBackPressed();
        }

    }

}
