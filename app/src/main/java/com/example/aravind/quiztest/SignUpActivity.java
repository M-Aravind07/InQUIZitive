package com.example.aravind.quiztest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class    SignUpActivity extends AppCompatActivity
{
    final int QUIZZES = 3;
    static final int MIN_DISTANCE=70;
    private float x1,x2;
    String uname,username,phoneno,pwd,pwdconfirm;
    TextView t;
    FirebaseDatabase db;
    DatabaseReference ref;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firebaseAuth=FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("UserAttempt");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                    if (x1<x2) //Right Swipe
                    {
                        overridePendingTransition(R.anim.fade_out,R.anim.fade_in);
                        startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                    }
                }
        }

        return super.onTouchEvent(event);
    }

    public void BackToLoginActivity(View view)
    {
        startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
    }


    public void ValidateSignUpCredentials(View view)
    {
        uname=((EditText)findViewById(R.id.Name)).getText().toString().trim();
        username=((EditText)findViewById(R.id.Email)).getText().toString().trim();
        pwd=((EditText)findViewById(R.id.Password)).getText().toString().trim();
        pwdconfirm=((EditText)findViewById(R.id.ConfirmPassword)).getText().toString().trim();

        t=findViewById(R.id.ErrorMsg);

        if (uname==null || uname.length()==0 || username.length()==0 || username==null || pwd==null || pwd.length()==0 || pwdconfirm.length()==0 || pwdconfirm==null)
        {
            t.setText("Incomplete form.. Please fill it up! ");
        }

        else if (pwd.length()<=6)
            t.setText("Password should atleast be 7 characters");

        else if (!(pwd.equals(pwdconfirm)))
        {
            t.setText("Passwords do not match");
        }


        else
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading........");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(username,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    createSpace();
                    mProgressDialog.cancel();
                    if (task.isSuccessful())
                    {
                        AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
                        builder.setMessage("Registration Successful!");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                                finish(); //Blocks back button
                            }
                        });

                        AlertDialog alert=builder.create();
                        alert.show();
                    }

                    else
                    {
                        FirebaseAuthException e=(FirebaseAuthException)task.getException();
                        Toast.makeText(SignUpActivity.this,"Registration Failed :(",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    public void createSpace(){
        String uname = username.split("@")[0];
        Log.d("Quiz", "Creating space for user-1");
        Map<String, String> quiz = new HashMap<>();
        quiz.put("Quiz1", "1");
        quiz.put("Quiz2", "0");
        quiz.put("Quiz3", "0");
        ref.child(uname).push().setValue(quiz);
    }
}
