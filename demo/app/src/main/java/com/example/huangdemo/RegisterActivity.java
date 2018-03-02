package com.example.huangdemo;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private View mUpLine;
    private EditText mEdtPhone = null;
    private EditText mEdtMsg = null;
    private EditText mEdtPsd = null;
    private EditText mEdtPsdAgain = null;
    private Button mBtnRegist = null;
    private Button mVerificaButton = null;
    private static final int SUCCESS =1;
    private static final int ERROR1 =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        mUpLine = findViewById(R.id.up_line);
        UpLine upline = new UpLine(mUpLine);
        upline.mTxtVText.setText("注册");
        mEdtPhone = (EditText) findViewById(R.id.mobile);
        mEdtMsg = (EditText) findViewById(R.id.code);
        mEdtPsd = (EditText) findViewById(R.id.pwd1);
        mEdtPsdAgain = (EditText) findViewById(R.id.pwd2);
        mBtnRegist = (Button) findViewById(R.id.register);
        mVerificaButton = (Button) findViewById(R.id.code_get);
        mVerificaButton.setOnClickListener(this);
        mBtnRegist.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.code_get:
                checkUserRegister();//获取验证码
                break;
            case R.id.register://提交注册
                if (!mEdtPsd.getText().toString().equals(mEdtPsdAgain.getText().toString())) {//两次输入的密码不一致
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else {//一致
//                    mBtnRegist.setEnabled(false);
                    sendOkHttp();
                }
                break;
        }
    }

    private void checkUserRegister() {
        Toast.makeText(RegisterActivity.this, "默认验证码000000", Toast.LENGTH_SHORT).show();
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SUCCESS:
                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    break;
                case ERROR1:
                    Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void sendOkHttp(){
        final String username = mEdtPhone.getText().toString();
        final String password = mEdtPsd.getText().toString();
        String msgCode = mEdtMsg.getText().toString();
        final String captcha = "000000";
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "账号或密码为空", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username",username)
                            .add("password",password)
                            .add("captcha",captcha)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://customer.kiwiyun.com/api/customer/user")
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
                finish();
            }else {
                Message message = new Message();
                message.what = ERROR1;
                mHandler.sendMessage(message);
            }
        }catch (Exception e){

        }
    }
}
