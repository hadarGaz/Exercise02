package com.example.hadar.exercise02;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    public static final String TAG = "RegistrationActivity";
    private static final int RESULT_LOAD_IMAGE = 1;
    private FirebaseAuth m_firebaseAuth;
    boolean imageUploaded = false;
    EditText mEmail, mName, mPassword;
    ImageView mImageView;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        findViews();
        checkIfEmailHasAlreadyBeenWritten();
    }

    private void findViews()
    {
        m_firebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.editTextEmail);
        mPassword = findViewById(R.id.editTextPassword);
        mName = findViewById(R.id.editTextPersonName);
        mImageView = findViewById(R.id.imageViewSelectImage);
    }
    private void checkIfEmailHasAlreadyBeenWritten()
    {
        String emailStr;
        if( (emailStr = (String) getIntent().getSerializableExtra("Email")) != null)
        {
            mEmail.setText(emailStr);
        }
    }

    public void onSubmit(View v)
    {
        //check validation
        if(detailsValidation()) {

            Task<AuthResult> authResult;
            authResult = m_firebaseAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString());

            authResult.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.e(TAG, "Email/Pass Auth: onComplete() >> " + task.isSuccessful());

                    if (!task.isSuccessful())
                        Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    else {
                        updateNameAndUriToUserAndUpdateUI();

                    }
                    Log.e(TAG, "Email/Pass Auth: onComplete() <<");
                }
            });
        }
    }

    private void updateNameAndUriToUserAndUpdateUI()
    {
        UserProfileChangeRequest.Builder updateProfile = new UserProfileChangeRequest.Builder();
        updateProfile.setDisplayName(mName.getText().toString());
        updateProfile.setPhotoUri(imageUri);
        Task authResult = m_firebaseAuth.getCurrentUser().updateProfile(updateProfile.build());
        authResult.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                updateUI();
            }
        });
    }

    private void updateUI()
    {
        Intent userDetailsIntent = new Intent(getApplicationContext(), UserDetailsActivity.class);
        UserDetails userDetails = new UserDetails(m_firebaseAuth.getCurrentUser());
        userDetailsIntent.putExtra("User Details", userDetails);
        startActivity(userDetailsIntent);
        // overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void onSelectImageClick(View v)
    {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            mImageView.setImageURI(imageUri);

            imageUploaded = true;
        }
    }

    private boolean detailsValidation()
    {
        boolean resReturn = true;
        if (!verifyName(mName.getText().toString())) {
            Toast.makeText(RegistrationActivity.this, "Invalid Name", Toast.LENGTH_SHORT).show();
            resReturn = false;
        }
        else if (!verifyEmail(mEmail.getText().toString())) {
            Toast.makeText(RegistrationActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
            resReturn = false;
        }
        else if (!verifyPassword(mPassword.getText().toString())) {
            Toast.makeText(RegistrationActivity.this, "\n" +
                    "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show();
            resReturn = false;
        }
        else if(!imageUploaded) {
            Toast.makeText(RegistrationActivity.this, "Must select an image", Toast.LENGTH_SHORT).show();
            resReturn = false;
        }
        return resReturn;
    }
    public boolean verifyName(String i_fullName)
    {
        String RegEx = "^[a-zA-Z\\s]*$";

        if (i_fullName.matches(""))
            return false;

        Pattern pattern = Pattern.compile(RegEx);
        Matcher matcher = pattern.matcher(i_fullName);

        return matcher.matches();
    }

    //Verifying an Email:
    public boolean verifyEmail(String i_Email)
    {
        String RegEx = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (i_Email.matches(""))
            return false;

        Pattern pattern = Pattern.compile(RegEx);
        Matcher matcher = pattern.matcher(i_Email);

        return matcher.matches();
    }

    public boolean verifyPassword(String i_Password)
    {
        if(i_Password.length() < 6 || i_Password == null || i_Password.isEmpty())
            return false;

        return true;
    }
}