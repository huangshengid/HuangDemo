package com.example.huangdemo;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "LoginActivity";
    private EditText mVedtMobile = null;
    private EditText mVedtPwd = null;
    private View mUpLine;
    private static final int SUCCESS =1;
    private static final int ERROR =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mUpLine = findViewById(R.id.up_line);
        UpLine upline = new UpLine(mUpLine);
        upline.mTxtVText.setText("登录");
        mVedtMobile = (EditText) findViewById(R.id.mobile);
        mVedtPwd = (EditText) findViewById(R.id.pwd);

        Button loginButton = (Button) findViewById(R.id.login);
        Button registButton = (Button) findViewById(R.id.to_register);
        registButton.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        loginButton.setOnClickListener(this);
        registButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                String mobile = mVedtMobile.getText().toString();
                String pwd = mVedtPwd.getText().toString();
                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(LoginActivity.this,"电话号码为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this,"密码为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                checkUser(mobile,pwd);
                break;
            case R.id.to_register:
                Intent registerIntent = new Intent(this,RegisterActivity.class);
                startActivity(registerIntent);
                break;
        }
    }

    private void checkUser(final String mobile, final String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username",mobile)
                            .add("password",pwd)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://customer.kiwiyun.com/api/customer/user/token")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String  result = response.body().string();
                    Log.d(TAG, "run: result ="+result);
                    parseJSONData(result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void parseJSONData(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String code = json.getString("code");
            String msg = json.getString("msg");
            JSONObject data = json.getJSONObject("data");
            String cashAsset = data.getString("cashAsset");
            String headUrl = data.getString("headUrl");
            String phone = data.getString("phone");
            String token = data.getString("token");
            Log.d(TAG, "parseJSONData: code ="+code+"   msg = "+msg);
            if (code.equals("200")&& msg.equals("success")){
                Message message = new Message();
                message.what = SUCCESS;
                mHandler.sendMessage(message);
                Intent pensonIntent = new Intent(LoginActivity.this,PensonActivity.class);
                pensonIntent.putExtra("Asset",cashAsset);
                pensonIntent.putExtra("URL",headUrl);
                pensonIntent.putExtra("Phone",phone);
                pensonIntent.putExtra("TOKEN",token);
                startActivity(pensonIntent);
            }else {
                Message message = new Message();
                message.what = ERROR;
                mHandler.sendMessage(message);
            }
        }catch (Exception e){

        }
    }
    private Handler mHandler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SUCCESS:
                    Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    Toast.makeText(LoginActivity.this,"账号或密码错误",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
