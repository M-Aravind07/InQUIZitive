package com.example.aravind.quiztest;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Question_Answer extends AppCompatActivity
{
    private RadioGroup radioGroup;
    private RadioButton radioButton,correctradiobutton;
    private TextView time;
    //private TextView Qno;
    String question_render,OptA,OptB,OptC,OptD,ChosenOption;
    String correctAnswer;
    private FirebaseDatabase db;
    private DatabaseReference qRef, aRef, uRef;    //reference to questions, answers and attempts in the db
    private Map<String, Map<String, String>> data;
    private Map<String, String> response;
    private Map<String, String> responseOptions;
    private ArrayList<Integer> rIndexes;
    private int questionIndex = -1;
    private String uname = "";
    private String round;
    private Boolean submitted = false;
    private CountDownTimer timer;
    private ProgressDialog mProgressDialog;
    private Map<String, Integer> quiz;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question__answer);

        //unpacking extras
        if(getIntent() != null){
            uname = getIntent().getStringExtra("uname");
            round = getIntent().getStringExtra("round");
            Log.d("Quiz", "Name: " + uname);
            Log.d("Quiz", "Round: " + round);
        }

        //assigning values
        time = findViewById(R.id.Time_value);
        rIndexes = new ArrayList<>();
        quiz = new HashMap<>();
        db = FirebaseDatabase.getInstance();
        qRef = db.getReference("Questions" + round);
        aRef = db.getReference("Submissions");
        uRef = db.getReference("UserAttempt");
        data = new HashMap<>();
        response = new HashMap<>();
        responseOptions = new HashMap<>();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        CheckNetworkConnection();

        findViewById(R.id.Submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        getDataFromServer();
    }

    public void CheckNetworkConnection()
    {
        if (!isNetworkConnected())
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(Question_Answer.this);
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


    public void getDataFromServer(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        uRef.child(uname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren()) {
                    //Log.d("Quiz", "User Attempt: " + d.getKey().toString() + "," + d.getValue().toString());
                    quiz = (Map<String, Integer>) d.getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Get data from server and store in data
        qRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d:dataSnapshot.getChildren()) {
                    data.put(d.getKey(), (Map<String, String>) d.getValue());
                    //String ques = d.getKey();
                    //Map<String, String> info = (Map<String, String>) d.getValue();
                }
                //No. of attempts made zero
                quiz.put("Quiz", 0);
                uRef.child(uname).removeValue();
                uRef.child(uname).push().setValue(quiz);
                mProgressDialog.cancel();
                getQuestionsForRound();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Question_Answer.this, "Error retrieving data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getQuestionsForRound(){
        //Select a random question no from 0 to (n-1)-10
        Random random = new Random();
        int rQno = random.nextInt(data.size()-10);
        //Select the questions from rQno to rQno+10
        ArrayList<Integer> qIndexes = new ArrayList<>(Arrays.asList(rQno, rQno+1, rQno+2, rQno+3, rQno+4, rQno+5, rQno+6, rQno+7, rQno+8, rQno+9, rQno+10));
        //int[] qIndexes = {rQno, rQno+1, rQno+2, rQno+3, rQno+4, rQno+5, rQno+6, rQno+7, rQno+8, rQno+9, rQno+10};
        int i;
        //Select a random question from rQno to rQno+10 and show it to the user and start a timer
        for(i = 0; i < 10; i++) {
            Random random1 = new Random();
            int randomIndex = random1.nextInt(10 - i);
            int qIndex = qIndexes.get(randomIndex);
            qIndexes.remove(randomIndex);
            rIndexes.add(qIndex + 1);
        }
        renderQuestion();
    }

    public int renderQuestion(){
        submitted = false;
        questionIndex++;
        Log.d("Quiz", response.toString());
        if(questionIndex == 10) {
            aRef.child(uname).push().setValue(response);
            Intent intent = new Intent(Question_Answer.this,Scores.class);
            intent.putExtra("uname", uname);
            String responses = getResponses();
            intent.putExtra("responses", responses);
            intent.putExtra("round", round);
            //intent.putExtra("responses", responses);
            startActivity(intent);
            finish(); //Prevents from going to the questions once score is generated
            return 0;
        }
        Map<String, String> questionInfo = data.get("Question" + String.valueOf(rIndexes.get(questionIndex)));

        question_render = questionInfo.get("question");
        OptA = questionInfo.get("optionA");
        OptB = questionInfo.get("optionB");
        OptC = questionInfo.get("optionC");
        OptD = questionInfo.get("optionD");

        correctAnswer = questionInfo.get("answer");

        ((TextView) findViewById(R.id. Qno)).setText(String.valueOf(questionIndex+1));
        ((TextView) findViewById(R.id.Question)).setText(question_render);
        ((TextView) findViewById(R.id.A)).setText(OptA);
        ((TextView) findViewById(R.id.B)).setText(OptB);
        ((TextView) findViewById(R.id.C)).setText(OptC);
        ((TextView) findViewById(R.id.D)).setText(OptD);
        Log.d("Quiz", "Value of i: " + questionIndex);
        //latch = new CountDownLatch(1);
        time.setText("15.00");
        timer = new CountDownTimer(15000, 10) {
            @Override
            public void onTick(long l) {
                time.setText(String.valueOf((float) l / 1000));
            }

            @Override
            public void onFinish() {
                time.setText("0.00");
                //Toast.makeText(Question_Answer.this, "Done!", Toast.LENGTH_SHORT).show();
                if(submitted) {
                    Log.d("Quiz", response.toString());
                    clearAnswers();
                    renderQuestion();
                }else{
                    submit();
                }
                //latch.countDown();
            }
        };
        timer.start();
        return 0;
    }

    public void submit(){
        submitted = true;
        String question = "Question" + String.valueOf(rIndexes.get(questionIndex)+1);
        String answer;
        String t = time.getText().toString();
        radioGroup = (RadioGroup)findViewById(R.id.OptionsGroup);
        int SelectedOpt = radioGroup.getCheckedRadioButtonId();
        radioGroup.clearCheck();

        Log.d("Quiz", "Selected option: " + SelectedOpt);
        switch (SelectedOpt){
            case R.id.A: answer="A";break;
            case R.id.B: answer="B";break;
            case R.id.C: answer="C";break;
            case R.id.D: answer="D";break;
            default: answer="Nil";
        }
        Toast.makeText(this, answer , Toast.LENGTH_SHORT).show();
        Log.d("Quiz", "Answer: " + answer);
        String time;
        if(answer.equals(data.get(question).get("answer")))
            time = t;
        else
            time = "0";
        response.put(question, time);
        responseOptions.put(question, answer);
        Log.d("Quiz", response.toString());
        //Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
        timer.cancel();
        timer.onFinish();
    }

    public void CheckAnswer(View view)
    {
        radioGroup = (RadioGroup)findViewById(R.id.OptionsGroup);
        int SelectedOpt = radioGroup.getCheckedRadioButtonId();
        radioButton=(RadioButton)findViewById(SelectedOpt);
        ChosenOption=radioButton.getText().toString();

        /*if (ChosenOption.equals(correctAnswer))
        {
            radioButton.setBackgroundColor(Color.parseColor("#399615"));

            //((TextView)findViewById(R.id.Score_value)).setText("10");

            radioButton = (RadioButton) findViewById(R.id.Option1);

            radioButton = (RadioButton) findViewById(R.id.Option2);
            //radioButton.setChecked(false);

            radioButton = (RadioButton) findViewById(R.id.Option3);

            radioButton = (RadioButton) findViewById(R.id.Option4);
        }

        else
        {
            radioButton.setBackgroundColor(Color.parseColor("#f23c32"));
           // ((TextView)findViewById(R.id.Score_value)).setText("-5");

            radioButton=(RadioButton)findViewById(R.id.Option2);
            radioButton.setBackgroundColor(Color.parseColor("#399615"));

            radioButton = (RadioButton) findViewById(R.id.Option1);
            //radioButton.setChecked(false);
            //radioButton.setBackgroundColor(Color.parseColor("#d39965"));

            radioButton = (RadioButton) findViewById(R.id.Option2);
            radioButton.setChecked(false);
            //radioButton.setBackgroundColor(Color.parseColor("#d39965"));

            radioButton = (RadioButton) findViewById(R.id.Option3);;

            radioButton = (RadioButton) findViewById(R.id.Option4);
        }*/

    }

    public String getResponses(){
        StringBuilder t = new StringBuilder();
        Iterator it = responseOptions.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            if(!t.toString().equals(""))
                t.append(",");
            t.append(pair.getKey()).append(":").append(pair.getValue());
            it.remove();
        }
        return t.toString();
    }

    public void clearAnswers()
    {
        // ((TextView)findViewById(R.id.Score_value)).setText("0");

        radioButton = (RadioButton) findViewById(R.id.A);
        radioButton.setChecked(false);
        radioButton.setBackgroundColor(Color.parseColor("#e0e8ff"));

        radioButton = (RadioButton) findViewById(R.id.B);
        radioButton.setChecked(false);
        radioButton.setBackgroundColor(Color.parseColor("#e0e8ff"));

        radioButton = (RadioButton) findViewById(R.id.C);
        radioButton.setChecked(false);
        radioButton.setBackgroundColor(Color.parseColor("#e0e8ff"));

        radioButton = (RadioButton) findViewById(R.id.D);
        radioButton.setChecked(false);
        radioButton.setBackgroundColor(Color.parseColor("#e0e8ff"));

    }

}

