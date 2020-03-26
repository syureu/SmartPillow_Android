package com.example.dblab_d.seung_ossleeper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SleepActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_main);

        Button wakeupButton = (Button) findViewById(R.id.wakeupButton);
        TextView textView = (TextView) findViewById(R.id.wakeupTimeTextView);
        textView.setText("기상예정 시간은 "+getIntent().getIntExtra("ahour",0) + "시 "+getIntent().getIntExtra("amin",0) +"분입니다.");

        wakeupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}
