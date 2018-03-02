package com.example.huangdemo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReNameActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ReNameActivity";
    private View mUpLine;
    private TextView mCurNameView;
    private EditText mEditName;
    private Button mOkButton ;
    private String nickName;
    private String mPhoneNumber;
    private static final int SUCCESS =1;
    private static final int ERROR =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_name);
        getData();
        initview();
    }

    private void getData() {
        mPhoneNumber = getIntent().getStringExtra("Phone");
    }

    private void initview() {
        mUpLine = findViewById(R.id.up_line);
        UpLine upline = new UpLine(mUpLine);
        upline.mTxtVText.setText("修改名称");
        mCurNameView = (TextView) findViewById(R.id.current_nickname);
        mEditName = (EditText) findViewById(R.id.edit_nickname);
        mOkButton = (Button) findViewById(R.id.rename_ok);
        mOkButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rename_ok:
                nickName = mEditName.getText().toString();
                if(!"".equals(nickName)){
                    changeName();
                }else{
                    Toast.makeText(ReNameActivity.this,"昵称不可为空", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private void changeName() {
        final String captcha = "000000";
        nickName = mEditName.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("username",mPhoneNumber)
                            .add("nickname",nickName)
                            .build();
                    Request request = new Request.Builder()
                            .url("http://customer.kiwiyun.com/api/customer/user")
                            .patch(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String  result = response.body().string();
                    Log.d(TAG, "run: huangsheng result ="+result);
                    parseJSONReNameData(result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void parseJSONReNameData(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String code = json.getString("code");
            String msg = json.getString("msg");
            if (code.equals("200")&& msg.equals("success")){
                Message message = new Message();
                message.what = SUCCESS;
                mHandler.sendMessage(message);
            }else {
                Message message = new Message();
                message.what = ERROR;
                mHandler.sendMessage(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SUCCESS:
                    Toast.makeText(ReNameActivity.this,"修改名称成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ERROR:
                    Toast.makeText(ReNameActivity.this,"修改名称失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
