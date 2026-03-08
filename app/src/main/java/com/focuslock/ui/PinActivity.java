package com.focuslock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.focuslock.R;
import com.focuslock.utils.SessionManager;

public class PinActivity extends AppCompatActivity {
    private TextView tvTitle, tvDots, tvError;
    private StringBuilder input = new StringBuilder();
    private String mode, firstPin;
    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        sm = SessionManager.get(this);
        mode = getIntent().getStringExtra("mode");
        tvTitle = findViewById(R.id.tvPinTitle);
        tvDots  = findViewById(R.id.tvPinDots);
        tvError = findViewById(R.id.tvPinError);
        tvTitle.setText("setup".equals(mode)
            ? "Create a 4-digit PIN\nYou'll need this to stop sessions early"
            : "Enter PIN to continue");

        int[] ids = {R.id.btn0,R.id.btn1,R.id.btn2,R.id.btn3,R.id.btn4,
                     R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9};
        for (int i=0;i<10;i++) {
            final int d=i;
            View v=findViewById(ids[i]);
            if (v!=null) v.setOnClickListener(x->digit(d));
        }
        View del=findViewById(R.id.btnDelete);
        if (del!=null) del.setOnClickListener(x->del());
    }

    private void digit(int d) {
        if (input.length()>=4) return;
        input.append(d); updateDots();
        if (input.length()==4) complete();
    }

    private void del() {
        if (input.length()>0) { input.deleteCharAt(input.length()-1); updateDots(); tvError.setVisibility(View.GONE); }
    }

    private void updateDots() {
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<4;i++) sb.append(i<input.length()?"● ":"○ ");
        tvDots.setText(sb.toString().trim());
    }

    private void complete() {
        String pin = input.toString();
        if ("setup".equals(mode)) {
            if (firstPin==null) {
                firstPin=pin; input.setLength(0); updateDots();
                tvTitle.setText("Confirm your PIN"); tvError.setVisibility(View.GONE);
            } else if (pin.equals(firstPin)) {
                sm.setPin(pin); sm.setPinEnabled(true);
                Toast.makeText(this,"✅ PIN set!",Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); finish();
            } else {
                tvError.setText("PINs don't match. Try again.");
                tvError.setVisibility(View.VISIBLE);
                firstPin=null; input.setLength(0); updateDots();
                tvTitle.setText("Create a 4-digit PIN");
            }
        } else {
            if (sm.verifyPin(pin)) {
                setResult(RESULT_OK, new Intent().putExtra("verified",true)); finish();
            } else {
                tvError.setText("Incorrect PIN. Try again.");
                tvError.setVisibility(View.VISIBLE);
                input.setLength(0); updateDots();
                tvDots.animate().translationX(12).setDuration(50)
                    .withEndAction(()->tvDots.animate().translationX(-12).setDuration(50)
                    .withEndAction(()->tvDots.animate().translationX(0).setDuration(50).start()).start()).start();
            }
        }
    }

    @Override public void onBackPressed() {
        if ("verify".equals(mode)) Toast.makeText(this,"Enter PIN to exit 💪",Toast.LENGTH_SHORT).show();
        else super.onBackPressed();
    }
}
