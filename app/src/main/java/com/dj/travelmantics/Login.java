package com.dj.travelmantics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity{
    private static final String TAG = "LoginActivity";
    // UI references.
    private EditText mEmailView,mPasswordView;
    private ProgressBar progressBar;
    private Button signinBtn;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 9071;
    private String userType;
    private SharedPreferences pref;
    private DatabaseReference mref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ImageView imageView = findViewById(R.id.ImageView);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mref = FirebaseDatabase.getInstance().getReference();

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GlideApp.with(this)
                .load(R.drawable.logo_one)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .circleCrop()
                .into(imageView);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        signinBtn = findViewById(R.id.signinBtn);

        setupFirebaseAuth();
        hideSoftKeyboard();
        // [START config_signin]
        // Configure Google Sign In

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }



    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        //check if the fields are filled out
        if(isEmpty(email)
                && isEmpty(password)){
            Log.d(TAG, "onClick: attempting to authenticate.");

            showProgressDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,
                    password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Login failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI_google_signin(null);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            });
        }else{
            Toast.makeText(Login.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmpty(String string){
        return !string.equals("");
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    private boolean validateForm() {
        String email = mEmailView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Email can't be empty.");
            mEmailView.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmailView.getText().toString().trim()).matches()) {
            mEmailView.setError("Enter correct email.");
            mEmailView.requestFocus();
            return false;
        } else {
            mEmailView.setError(null);
        }


        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Password can't be empty.");
            mPasswordView.requestFocus();
            return false;
        } else {
            mPasswordView.setError(null);
        }
        return true;
    }



    public void showProgressDialog() {
      if (progressBar.getVisibility()==View.GONE){
          progressBar.setVisibility(View.VISIBLE);
          signinBtn.setEnabled(false);
      }
    }

    public void hideProgressDialog() {
        if (progressBar.getVisibility()==View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            signinBtn.setEnabled(true);

        }
    }

    public void GoToSignup(View view) {
     startActivity(new Intent(Login.this,Signup.class));
    }


    public void Signinmeton(View view) {
        if (Utils.haveNetworkConnection(this))
        signIn(mEmailView.getText().toString(), mPasswordView.getText().toString());
        else{
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    //check if email is verified
                  // if(user.isEmailVerified()){
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                       // Toast.makeText(Login.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();

    Intent intent = new Intent(Login.this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();


                    //}
//                    else{
//                        Toast.makeText(Login.this, "Email is not Verified\nCheck your Inbox", Toast.LENGTH_SHORT).show();
//                        FirebaseAuth.getInstance().signOut();
//                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (pref.getString("userType", null) != null) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    public void resetPass(View view) {
if (emailChk())
    sendPasswordResetEmail(mEmailView.getText().toString().trim());
    }

    private boolean emailChk() {
        String email = mEmailView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Email can't be empty.");
            mEmailView.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmailView.getText().toString().trim()).matches()) {
            mEmailView.setError("Enter correct email.");
            mEmailView.requestFocus();
            return false;
        } else {
            mEmailView.setError(null);
        }
        return true;
    }


    public void sendPasswordResetEmail(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Password Reset Email sent.");
//                            Toast.makeText(mContext, "Password Reset Link Sent to Email",
//                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(Login.this, "Password Reset Link Sent to Email!", Toast.LENGTH_SHORT).show();

                        }else{
                            Log.d(TAG, "onComplete: No user associated with that email.");
                            Toast.makeText(Login.this, "No User is Associated with that Email",
                                    Toast.LENGTH_SHORT).show();
//                            Toasty.error(Login.this, "No User is Associated with that Email", Toast.LENGTH_SHORT, true).show();


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, "Error Occurred Try Again!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void googleSignIn(View view) {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);//pass the declared request code here

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);

                }
                showProgressDialog();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                hideProgressDialog();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI_google_signin(user);
                            hideProgressDialog();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI_google_signin(null);
                            hideProgressDialog();
                        }

                        // ...
                    }
                });
    }
// [END auth_with_google]


    private void updateUI_google_signin(FirebaseUser user) {
        if (user != null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("userType", "gmail");
            editor.apply();
            Intent intent = new Intent(Login.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
    }

    private void updateUI(final FirebaseUser user) {
        if (user != null) {
            if (pref.getString("userType", null) != null &&
                    !Objects.equals(pref.getString("userType", null), "gmail")) {

                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                mref.child(FilePaths.travel_deals).child("users").child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                showData(dataSnapshot);


                                Intent intent = new Intent(Login.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }


        }

    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Database post = dataSnapshot.getValue(Database.class);
            userType = Objects.requireNonNull(post).getRole();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("userType", userType);
            editor.apply();

            Log.d("userType", userType);
        }

    }
}

