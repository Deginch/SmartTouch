package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

	private String TAG = "MyAccessibilityService";
	private static MyAccessibilityService sharedInstance;
	private MyFloatingView myFloatingView;
	private boolean isFLoatViewCreated=false;
	public MyAccessibilityService(){
	}

	@Override
	public void onCreate(){
		myFloatingView= new MyFloatingView(this,this);
	}

	public void createFloatView(){
		myFloatingView.createFloatView();
		isFLoatViewCreated=true;
	}

	public void closeFloatView(){
		myFloatingView.closeFloatView();
		isFLoatViewCreated=false;
	}

	public boolean isFLoatViewCreated(){
		return  this.isFLoatViewCreated;
	}
	//重载悬浮窗
	public void updateButton(){
		Log.i(TAG,"重新刷新按钮");
		myFloatingView.updateFloatButton();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "service onDestroy");
//		if (mFloatLayout != null) {
//			//移除悬浮窗口
//			mWindowManager.removeView(mFloatLayout);
//		}
		super.onDestroy();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

	}

	@Override
	protected void onServiceConnected() {
		sharedInstance=this;
		Log.i(TAG, "连接辅助服务");
	}

	@Override
	public void onInterrupt() {}

	@Override
	public boolean onUnbind(Intent intent) {
		sharedInstance = null;
		return super.onUnbind(intent);
	}


	//获取辅助服务实例,用于改变button
	public static MyAccessibilityService getSharedInstance() {
		return sharedInstance;
	}

}
