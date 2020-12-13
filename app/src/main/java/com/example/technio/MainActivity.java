package com.example.technio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.squareup.picasso.Picasso;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private FirebaseAuth mfirebaseauth;
    private FirebaseAuth.AuthStateListener authlistner;
    private TextView textviewuser;
    private ImageView logo;
    private LoginButton Loginbutton;
   private AccessTokenTracker accessTokenTracker;
    private static final String TAG="FacebookAuthentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mfirebaseauth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());


        textviewuser=findViewById(R.id.textView);
        logo=findViewById(R.id.imagelogo);
        Loginbutton=findViewById(R.id.login_button);
        Loginbutton.setReadPermissions("email","public_profile");


        callbackManager=CallbackManager.Factory.create();

        Loginbutton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"On success"+loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"On cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"On error"+error);

            }
        });


        authlistner =new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    updateUI(user);
                }
                else
                {
                    updateUI(null);
                }
            }
        };

        accessTokenTracker =new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null)
                {
                    mfirebaseauth.signOut();
                }
            }
        };


    }
   private void handleFacebookToken(AccessToken token)
   {
       Log.d(TAG,"Handle Facebook Token!"+token);
       AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
       mfirebaseauth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful())
               {
                   Log.d(TAG,"Sign in with credential : successfull");
                   FirebaseUser user=mfirebaseauth.getCurrentUser();
                   updateUI(user);
               }
               else
               {
                   Log.d(TAG,"Sign in with credential : faliure",task.getException());
                   Toast.makeText(MainActivity.this,"Aunthtication Failed",Toast.LENGTH_SHORT).show();
                   updateUI(null);
               }
           }
       });

   }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null){
            textviewuser.setText(user.getDisplayName());
            if(user.getPhotoUrl()!=null)
            {
                String url=user.getPhotoUrl().toString();
                url=url+"?type=large";
                Picasso.get().load(url).into(logo);
            }
            else
            {
                textviewuser.setText(" ");
                logo.setImageResource(R.drawable.logo);
            }

        }
    }

    protected  void onStart()
    {
        super.onStart();
        mfirebaseauth.addAuthStateListener(authlistner);
    }

    protected  void onStop()
    {
        super.onStop();
        if(authlistner !=null)
        {
            mfirebaseauth.removeAuthStateListener(authlistner);
        }
    }

}