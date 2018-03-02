package com.example.huangdemo;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by huangsheng on 2018/2/28.
 */

public class UpLine {

	public TextView mTxtVText;


	public UpLine(View v) {
		initView(v);
	}

	private void initView(View v){
		mTxtVText = (TextView) v.findViewById(R.id.text);
	}
}
