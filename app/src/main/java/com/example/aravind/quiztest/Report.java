package com.example.aravind.quiztest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class Report extends AppCompatActivity
{
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private ProgressDialog mProgressDialog;
    private Map<String, Map<String, String>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Questions");
        data = new HashMap<>();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getDataFromServer();
    }

    public void getDataFromServer(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        //Get data from server and store in data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d:dataSnapshot.getChildren()) {
                    data.put(d.getKey(), (Map<String, String>) d.getValue());
                    //String ques = d.getKey();
                    //Map<String, String> info = (Map<String, String>) d.getValue();
                }
                mProgressDialog.cancel();
                report();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Report.this, "Error retrieving data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void report(){
        String chosenoption,correctoption;
        final int[] QIds = {R.id.question1,R.id.question2,R.id.question3,R.id.question4,R.id.question5,R.id.question6,R.id.question7,
                R.id.question8,R.id.question9,R.id.question10}, CIds = {R.id.ch1, R.id.ch2, R.id.ch3, R.id.ch4, R.id.ch5, R.id.ch6,
                R.id.ch7, R.id.ch8, R.id.ch9, R.id.ch10}, AIds = {R.id.ans1, R.id.ans2, R.id.ans3, R.id.ans4, R.id.ans5, R.id.ans6,
                R.id.ans7, R.id.ans8, R.id.ans9, R.id.ans10};
        int i = 0;
        for(String qAndA : getIntent().getStringExtra("responses").split(",")){
            ((TextView)findViewById(QIds[i])).setText(data.get(qAndA.split(":")[0]).get("question"));
            chosenoption=qAndA.split(":")[1];
            correctoption=data.get(qAndA.split(":")[0]).get("answer");

            if (chosenoption.equals(correctoption))
            {
                ((TextView)findViewById(CIds[i])).setTextColor(Color.parseColor("#488c4e"));
            }

            ((TextView)findViewById(CIds[i])).setText("Your Answer   : " + data.get(qAndA.split(":")[0]).get("option" + chosenoption ));
            ((TextView)findViewById(AIds[i])).setText("Correct Answer: " + data.get(qAndA.split(":")[0]).get("option" + correctoption ));
            i++;
        }
    }

    public void BacktoMainScreen(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(Report.this,LoginActivity.class));
        finish();
    }
}
