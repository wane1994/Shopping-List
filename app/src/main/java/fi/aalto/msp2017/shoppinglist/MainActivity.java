package fi.aalto.msp2017.shoppinglist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import static fi.aalto.msp2017.shoppinglist.R.id.etLoginEmail;
import static fi.aalto.msp2017.shoppinglist.R.id.etLoginPassword;
import static fi.aalto.msp2017.shoppinglist.R.id.tvSwitch;


public class MainActivity extends AppCompatActivity {

    private EditText emailField, passwordField, signup_firstName, signup_lastName, login_email, login_password;
    private EditText login_Firstname, login_Lastname;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mDatabase;
    GoogleApiClient mGoogleApiClient;
    CallbackManager mCallbackManager;
    ProgressDialog mProgress;
    Switch switchLogin;
    TextView tvSocial, tvSwitch;
    Button btnLogin, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mCallbackManager = CallbackManager.Factory.create();

        emailField = (EditText)findViewById(R.id.etSignupEmail);
        passwordField = (EditText) findViewById(R.id.etSignupPassword);


        tvSocial=(TextView) findViewById(R.id.tvSocial);
        tvSwitch=(TextView) findViewById(R.id.tvSwitch);
        btnSignup=(Button) findViewById(R.id.btnSignup);
        btnLogin=(Button) findViewById(R.id.btnLogin);
        login_email=(EditText)findViewById(R.id.etSignupEmail);
        login_password=(EditText) findViewById(R.id.etSignupPassword);
        login_Firstname = (EditText) findViewById(R.id.etSignupFirstname);
        login_Lastname = (EditText) findViewById(R.id.etSignupLastname);


        ImageView imgFacebook = (ImageView)findViewById(R.id.imgFacebook);
        imgFacebook.setImageResource(R.drawable.facebook_image);
        ImageView imgGoogle = (ImageView) findViewById(R.id.imgGoogle);
        imgGoogle.setImageResource(R.drawable.google_image);


       /* switchLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    tvSocial.setText("You can sign in with your own accounts");
                    tvSwitch.setText("I have List account");
                    layoutSignup.setVisibility(View.INVISIBLE);
                    btnSignup.setVisibility(View.INVISIBLE);
                    layoutLogin.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ABOVE, R.id.layoutLogin);
                    layoutList.setLayoutParams(params);

                } else {
                    tvSocial.setText("You can sign up with your own accounts");
                    tvSwitch.setText("I don't have List account");
                    layoutLogin.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.INVISIBLE);
                    layoutSignup.setVisibility(View.VISIBLE);
                    btnSignup.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ABOVE, R.id.layoutSignup);
                    layoutList.setLayoutParams(params);
                }
            }

        });*/

        //attaching listener to LogIn button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailSignup();
            }
        });

        //attaching listener to LogIn button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogin();
            }
        });



        // GOOGLE Sign In integration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // GOOGLE Sign In integration
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Login error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // FACEBOOK Sign It integration
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.wtf("myTag", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.wtf("myTag", "facebook:onError", error);
                // ...
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /*if(firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }*/
            }
        };


        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance()
                        .logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
            }
        });

        imgGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = ProgressDialog.show(MainActivity.this, "Please wait...",null,true,true);
                googleSignIn();
            }
        });

    }

    //method to SIGN UP with email:password
    private void emailSignup() {

        String email = login_email.getText().toString();
        String password = login_password.getText().toString();
        final String firstname = login_Firstname.getText().toString();
        final String secondname = login_Lastname.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstname) || TextUtils.isEmpty(secondname)) {
            Toast.makeText(MainActivity.this, "Fill all the fields", Toast.LENGTH_SHORT).show();

        } else {
            mProgress = ProgressDialog.show(MainActivity.this, "Please wait...",null,true,true);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDb = mDatabase.child(userId);
                        currentUserDb.child("name").setValue(firstname + " " + secondname);
                        Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        Toast.makeText(MainActivity.this, "Your email is already registered or your password is less than 6 symbols", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                }
            });
        }
    }

    //method to LOG IN with email:password
    private void emailLogin(){

        String email = login_email.getText().toString();
        String password = login_password.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Fill all the fields", Toast.LENGTH_SHORT).show();

        } else {
            mProgress = ProgressDialog.show(MainActivity.this, "Please wait...",null,true,true);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(MainActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }



    //method to sign in with GOOGLE account
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 1);
    }

    // GOOGLE Sign In integration
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // GOOGLE Sign In integration
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.wtf("myTag", "signInWithCredential:onComplete:" + task.isSuccessful());

                        String username = mAuth.getCurrentUser().getDisplayName();;
                        String userId = mAuth.getCurrentUser().getUid();
                        String imageUrl = String.valueOf(mAuth.getCurrentUser().getPhotoUrl());
                        imageUrl = imageUrl.replace("/s96-c/","/s450-c/");
                        DatabaseReference currentUserDb = mDatabase.child(userId);
                        currentUserDb.child("name").setValue(username);
                        currentUserDb.child("imageUrl").setValue(imageUrl);



                        mProgress.dismiss();
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent);

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.wtf("myTag", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    // FACEBOOK Sing In integration
    private void handleFacebookAccessToken(AccessToken token) {
        Log.wtf("myTag", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.wtf("myTag", "signInWithCredential:onComplete:" + task.isSuccessful());

                        String username = mAuth.getCurrentUser().getDisplayName();;
                        String userId = mAuth.getCurrentUser().getUid();
                        String facebookId = "";
                        // find the Facebook profile and get the user's id
                        for(UserInfo profile : mAuth.getCurrentUser().getProviderData()) {
                            // check if the provider id matches "facebook.com"
                            if(profile.getProviderId().equals(getString(R.string.facebook_provider_id))) {
                                facebookId = profile.getUid();
                            }
                        }
                        String imageUrl = "https://graph.facebook.com/" + facebookId + "/picture?width=450&height=450";
                        DatabaseReference currentUserDb = mDatabase.child(userId);
                        currentUserDb.child("name").setValue(username);
                        currentUserDb.child("imageUrl").setValue(imageUrl);



                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent);

                        if (!task.isSuccessful()) {
                            Log.wtf("myTag", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // menu items
        switch (id) {
            case R.id.action_logout:
                // sign out
                mAuth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_exit:
                // close app
                finish();
                System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}
