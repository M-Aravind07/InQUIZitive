package com.example.aravind.quiztest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Scores extends AppCompatActivity {

    private FirebaseDatabase db;
    private DatabaseReference ref;
    private String uname;
    private TextView score;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Submissions");
        score = findViewById(R.id.score);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(getIntent() != null)
            uname = getIntent().getStringExtra("uname");
        Score();
    }

    public void Score(){
        //Log.d("Quiz", "Scores:" + getIntent().getSerializableExtra("responses").toString());
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        ref.child(uname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressDialog.cancel();
                Map<String, String> response = new HashMap<>();
                for(DataSnapshot d: dataSnapshot.getChildren()) {
                    Log.d("Quiz", "This Scores: " + d.getKey().toString() + "," + d.getValue().toString());
                    response = (Map<String, String>) d.getValue();
                }
                Log.d("Quiz", "Response: " + response.toString());
                score.setText(calcScore(response));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String calcScore(Map<String, String> response){
        float score = 0;
        for(String qAndA : getIntent().getStringExtra("responses").split(",")){
            float tt = Float.valueOf(response.get(qAndA.split(":")[0]));
            score += (tt/15) * 10;
        }
        return String.valueOf(score);
    }

    public void OpenReportActivity(View view)
    {
        Intent intent = new Intent(Scores.this,Report.class);
        intent.putExtra("uname", uname);
        intent.putExtra("responses", getIntent().getStringExtra("responses"));
        startActivity(intent);
    }

}
