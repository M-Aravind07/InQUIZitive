package com.example.aravind.quiztest;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;
import java.sql.Time;

public class LoginActivity extends AppCompatActivity
{
    String username,userpassword;
    private float x1,x2;
    static final int MIN_DISTANCE=70;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CheckNetworkConnection();
    }

    /*public void onBackPressed() {
        AlertDialog.Builder diag= new AlertDialog.Builder(this);
        diag.setTitle("Confirm Exit").setMessage("Press Confirm to quit");

        diag.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        diag.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
    } */


    public void CheckNetworkConnection()
    {
        if (!isNetworkConnected())
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Can't connect to the Internet");
            builder.setCancelable(true);

            builder.setPositiveButton("Mobile Data Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    enableDisableMobileData();
                }
            });

            builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    CheckNetworkConnection();
                    }
            });

            AlertDialog alert=builder.create();
            alert.show();
        }
    }

    public void enableDisableMobileData() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"));
        startActivityForResult(intent, 1);
    }

    private boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void SignUpSwitch(View view)
    {
        startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
    }



    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1=event.getX();
                break;

            case MotionEvent.ACTION_UP:
                x2=event.getX();
                float deltaX=x2-x1;

                if (Math.abs(deltaX)>MIN_DISTANCE)
                {
                    if (x1>x2) //Left Swipe
                    {
                        overridePendingTransition(R.anim.fade_out,R.anim.fade_in);
                        startActivity(new Intent(LoginActivity.this,SignUpActivity.class));

                    }
                }
        }

        return super.onTouchEvent(event);
    }


    public void ValidateLoginCredentials(final View view)
    {
        username=((EditText)findViewById(R.id.user_email)).getText().toString();
        userpassword=((EditText)findViewById(R.id.user_password)).getText().toString();

        //
        CheckNetworkConnection();

        if ((username.equals("") || userpassword.equals("") ))
        {
            ((TextView)findViewById(R.id.incompletedetails)).setText("Enter login credentials");
        }

        else
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading........");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            ((TextView)findViewById(R.id.incompletedetails)).setText("");
            firebaseAuth=FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(username, userpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressDialog.cancel();
                    if (task.isSuccessful())
                    {
                        Toast.makeText(LoginActivity.this, "Validated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getBaseContext(), StartQuiz.class);
                        intent.putExtra("UserName",username) ;
                        startActivity(intent);
                        finish(); //can't go back to login screen once logged in
                        //startActivity(new Intent(LoginActivity.this, StartQuiz.class));
                    }

                    else if (!task.isSuccessful() && isNetworkConnected())
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("Invalid Login Credentials");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                       /* builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });  */

                        AlertDialog alert=builder.create();
                        alert.show();

                    }

                }
            });
        }

    }
}
