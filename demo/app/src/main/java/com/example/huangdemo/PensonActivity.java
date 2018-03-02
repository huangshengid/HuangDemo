package com.example.huangdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.huangdemo.bean.PensonAsset;
import com.example.huangdemo.bean.PesonMessage;

import org.json.JSONObject;


import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PensonActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PensonActivity";
    private View mUpLine;
    private TextView mPhone;
    private ImageView mHeand;
    private TextView mCashAsset;
    private String mPhoneNumber;
    private String mUrl;
    private String mAsset;
    private String mToken;
    private Button mChangeHead,mChangeMessage,mPensonMessage,mPensonAsset;

    private static final int PensonMessageSucess =1;
    private static final int PensonAssetSucess =2;
    private static final int IMAGE = 3;
    private static final int SELECT_PHOTO = 4;
    private static final int PensonPhotoSucess =5;
    private static final int PensonPhotoError =6;
    private static final int PensonMessageError =7;
    private static final int PensonAssetError =8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penson);
        getData();
        initView();
        ininData();
    }

    private void getData() {
        mPhoneNumber = getIntent().getStringExtra("Phone");
        mUrl = getIntent().getStringExtra("URL");
        mAsset = getIntent().getStringExtra("Asset");
        mToken = getIntent().getStringExtra("TOKEN");
        Log.d(TAG, "getData: huangsheng token = "+mToken);
    }

    private void ininData() {
        mPhone.setText(mPhoneNumber);
        Glide.with(this).load(mUrl).into(mHeand);
        mCashAsset.setText(mAsset);

    }

    private void initView() {
        mUpLine = findViewById(R.id.up_line);
        UpLine upline = new UpLine(mUpLine);
        upline.mTxtVText.setText("个人资料");
        mPhone = (TextView)findViewById(R.id.phone);
        mHeand = (ImageView)findViewById(R.id.heand_image);
        mCashAsset = (TextView)findViewById(R.id.cashAsset);
        mChangeHead = (Button)findViewById(R.id.change_henad);
        mChangeMessage = (Button)findViewById(R.id.change_message) ;
        mPensonMessage = (Button)findViewById(R.id.penson_message);
        mPensonAsset = (Button)findViewById(R.id.penson_asset);
        mChangeHead.setOnClickListener(this);
        mChangeMessage.setOnClickListener(this);
        mPensonMessage.setOnClickListener(this);
        mPensonAsset.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.change_henad:
//                onStartAlbum();
                select_photo();
                break;
            case R.id.change_message:
                changeMessage();
                break;
            case R.id.penson_message:
                getPensonMessage();
                break;
            case R.id.penson_asset:
                getPensonAsset();
                break;
        }
    }

    private void onStartAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE);
    }

    public void select_photo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    /**
     * 打开相册的方法
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO);
    }
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        Log.d(TAG, "handleImgeOnKitKat: imagePath = "+imagePath);
        changHead(imagePath);
    }

    /**
     * 4.4及以上系统处理图片的方法
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImgeOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("uri=intent.getData :", "" + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);        //数据表里指定的行
            Log.d("getDocumentId(uri) :", "" + docId);
            Log.d("uri.getAuthority() :", "" + uri.getAuthority());
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        Log.d(TAG, "handleImgeOnKitKat: imagePath = "+imagePath);
        changHead(imagePath);
    }
    /**
     * 通过uri和selection来获取真实的图片路径,从相册获取图片时要用
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //打开相册后返回
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImgeOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                //判断是否有权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();//打开相册
                } else {
                    Toast.makeText(this, "你需要许可", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }


//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //获取图片路径
//        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
//            Uri selectedImage = data.getData();
//            String[] filePathColumns = {MediaStore.Images.Media.DATA};
//            Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
//            c.moveToFirst();
//            int columnIndex = c.getColumnIndex(filePathColumns[0]);
//            String imagePath = c.getString(columnIndex);
//            Log.d(TAG, "onActivityResult: imagePath = "+imagePath);
//            c.close();
//        }
//    }


    private void changeMessage() {
        Intent intent = new Intent(PensonActivity.this,ChangPensonMessageActivity.class);
        intent.putExtra("Phone",mPhoneNumber);
        startActivity(intent);
    }

    private void getPensonAsset() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("token",mToken)
                            .url("http://customer.kiwiyun.com/api/customer/user/asset-total")
                            .build();
                    Response response = client.newCall(request).execute();
                    String  result = response.body().string();
                    Log.d(TAG, "run: huangsheng result ="+result);
                    parseJSONPensonAssetData(result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }
    private void parseJSONPensonAssetData(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String code = json.getString("code");
            String msg = json.getString("msg");
            JSONObject data = json.getJSONObject("data");
            String totalStock = data.getString("totalStock");
            String totalCash = data.getString("totalCash");
            PensonAsset pensonAssetItem = new PensonAsset();
            pensonAssetItem.setmTotalStock(totalStock);
            pensonAssetItem.setmTotalCash(totalCash);
            Log.d(TAG, "parseJSONData: code ="+code+"   msg = "+msg);
            if (code.equals("200")&& msg.equals("success")){
                Message message = new Message();
                message.what = PensonAssetSucess;
                message.obj = pensonAssetItem;
                handler.sendMessage(message);
            }else {
                Message message = new Message();
                message.what = PensonAssetError;
                handler.sendMessage(message);
            }
        }catch (Exception e){

        }
    }

    private void getPensonMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("token",mToken)
                            .url("http://customer.kiwiyun.com/api/customer/user")
                            .build();
                    Response response = client.newCall(request).execute();
                    String  result = response.body().string();
                    Log.d(TAG, "run: huangsheng result ="+result);
                    parseJSONPensonMessageData(result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void parseJSONPensonMessageData(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String code = json.getString("code");
            String msg = json.getString("msg");
            JSONObject data = json.getJSONObject("data");
            String phone = data.getString("phone");
            String headUrl = data.getString("headUrl");
            String cashAsset = data.getString("cashAsset");
            String scoreAsset = data.getString("scoreAsset");
            String coinAsset = data.getString("coinAsset");
            String realName = data.getString("realName");
            String bankCardNo = data.getString("bankCardNo");
            String bankCardName = data.getString("bankCardName");
            boolean hasUserAuthFreeze = data.getBoolean("hasUserAuthFreeze");
            boolean hasSetLoginPwd = data.getBoolean("hasSetLoginPwd");
            boolean hasSetWithdrawPwd = data.getBoolean("hasSetWithdrawPwd");
            PesonMessage messageItem = new PesonMessage();
            messageItem.setmPhone(phone);
            messageItem.setmHandUrl(headUrl);
            messageItem.setmCashAsset(cashAsset);
            messageItem.setmScoreAsset(scoreAsset);
            messageItem.setmCoinAsset(coinAsset);
            messageItem.setmRealName(realName);
            messageItem.setmBankCardNo(bankCardNo);
            messageItem.setmBankCardName(bankCardName);
            messageItem.setmHasUserAuthFreeze(hasUserAuthFreeze);
            messageItem.setmHasSetLoginPwd(hasSetLoginPwd);
            messageItem.setmHasSetWithdrawPwd(hasSetWithdrawPwd);
            Log.d(TAG, "parseJSONData: code ="+code+"   msg = "+msg);
            if (code.equals("200")&& msg.equals("success")){
                Message message = new Message();
                message.what = PensonMessageSucess;
                message.obj = messageItem;
                handler.sendMessage(message);
            }else {
                Message message = new Message();
                message.what = PensonMessageError;
                handler.sendMessage(message);
            }
        }catch (Exception e){

        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case PensonMessageSucess:
                    PesonMessage pesonMessage = (PesonMessage) message.obj;
                    Toast.makeText(PensonActivity.this,"phone = "+pesonMessage.getmPhone()+"\nheadUrl = "+pesonMessage.getmHandUrl()+"\ncashAsset = "+pesonMessage.getmCashAsset()
                            +"\nscoreAsset = "+pesonMessage.getmScoreAsset()+"\ncoinAsset = "+pesonMessage.getmCoinAsset()
                            +"\nrealName = "+pesonMessage.getmRealName()+"\nbankCardNo = "+pesonMessage.getmBankCardNo()
                            +"\nbankCardName = "+pesonMessage.getmBankCardName()+"\nhasUserAuthFreeze = "+pesonMessage.getmHasUserAuthFreeze()
                            +"\nhasSetLoginPwd = "+pesonMessage.getmHasSetLoginPwd()+"\nhasSetWithdrawPwd = "+pesonMessage.getmHasSetWithdrawPwd(),Toast.LENGTH_SHORT).show();
                    break;
                case PensonAssetSucess:
                    PensonAsset pensonAsset = (PensonAsset) message.obj;
                    Toast.makeText(PensonActivity.this,"totalStock = "+pensonAsset.getmTotalStock()+"\ntotalCash = "+pensonAsset.getmTotalCash(),Toast.LENGTH_SHORT).show();
                    break;
                case PensonPhotoSucess:
                    Toast.makeText(PensonActivity.this,"上传图像成功",Toast.LENGTH_SHORT).show();
                    break;
                case PensonPhotoError:
                    Toast.makeText(PensonActivity.this,"上传图像失败",Toast.LENGTH_SHORT).show();
                    break;
                case PensonMessageError:
                    Toast.makeText(PensonActivity.this,"获取个人信息失败",Toast.LENGTH_SHORT).show();
                    break;
                case PensonAssetError:
                    Toast.makeText(PensonActivity.this,"获取资产信息失败",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;

            }
        }
    };


    private void changHead(String path) {
        final String imageType = "multipart/form-data";
//        String absolutePath = Environment.getExternalStorageDirectory().getPath();
//        String path = absolutePath + "/1/1234.png";
        File file = new File(path);//imgUrl为图片位置
        final RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", "imageName", fileBody)
                            .addFormDataPart("imagetype", imageType)
                            .build();
                    Request request = new Request.Builder()
                            .addHeader("token",mToken)
                            .url("http://customer.kiwiyun.com/api/customer/user/avatar")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String  result = response.body().string();
                    Log.d(TAG, "run: result ="+result);
                    parseJSONPensonPhotoData(result);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void parseJSONPensonPhotoData(String result) {
        try{
            JSONObject json = new JSONObject(result);
            String code = json.getString("code");
            String msg = json.getString("msg");
            if (code.equals("0")&& msg.equals("success")){
                Message message = new Message();
                message.what = PensonPhotoSucess;
                handler.sendMessage(message);
            }else {
                Message message = new Message();
                message.what = PensonPhotoError;
                handler.sendMessage(message);
            }
        }catch (Exception e){
        }
    }
}
