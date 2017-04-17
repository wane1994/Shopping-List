package fi.aalto.msp2017.shoppinglist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText email, password, first_name, last_name;
    ProgressDialog mProgress;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email=(EditText)findViewById(R.id.etSignupEmail);
        password=(EditText)findViewById(R.id.etSignupPassword);
        first_name=(EditText)findViewById(R.id.etSignupFirstname);
        last_name=(EditText)findViewById(R.id.etSignupLastname);
        btnSignUp=(Button)findViewById(R.id.btnSignup);
        mAuth= FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailSignup();
            }
        });
    }




    //method to SIGN UP with email:password
    private void emailSignup() {

        String emailAuth = email.getText().toString();
        String passwordAuth = password.getText().toString();
        final String firstnameAuth = first_name.getText().toString();
        final String lastnameAuth =last_name.getText().toString();

        if(TextUtils.isEmpty(emailAuth) || TextUtils.isEmpty(passwordAuth) || TextUtils.isEmpty(firstnameAuth) || TextUtils.isEmpty(lastnameAuth)) {
            Toast.makeText(SignUpActivity.this, "Fill all the fields", Toast.LENGTH_SHORT).show();

        } else {
            mProgress = ProgressDialog.show(SignUpActivity.this, "Please wait...",null,true,true);
            mAuth.createUserWithEmailAndPassword(emailAuth, passwordAuth).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDb = mDatabase.child(userId);
                        currentUserDb.child("name").setValue(firstnameAuth + " " + lastnameAuth);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("log", "Email sent.");
                                        }
                                    }
                                });
                        Toast.makeText(SignUpActivity.this, "Registration successful, please verify your email", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        mProgress.dismiss();

                    } else {
                        Toast.makeText(SignUpActivity.this, "Your email is already registered or your password is less than 6 symbols", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                }
            });
        }
    }
}
