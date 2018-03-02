package com.example.huangdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChangPensonMessageActivity extends AppCompatActivity implements View.OnClickListener {
    private View mUpLine;
    private Button mReName, mRePassWord;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chang_penson_message);
        getData();
        initview();
    }

    private void initview() {
        mUpLine = findViewById(R.id.up_line);
        UpLine upline = new UpLine(mUpLine);
        upline.mTxtVText.setText("修改信息");
        mReName = (Button)findViewById(R.id.rename);
        mRePassWord = (Button)findViewById(R.id.repassword);
        mReName.setOnClickListener(this);
        mRePassWord.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rename:
                Intent nameintent = new Intent(ChangPensonMessageActivity.this,ReNameActivity.class);
                nameintent.putExtra("Phone",mPhoneNumber);
                startActivity(nameintent);
                break;
            case R.id.repassword:
                Intent psdintent = new Intent(ChangPensonMessageActivity.this,RePsdActivity.class);
                psdintent.putExtra("Phone",mPhoneNumber);
                startActivity(psdintent);
                break;
        }
    }

    public void getData() {
        mPhoneNumber = getIntent().getStringExtra("Phone");
    }
}
