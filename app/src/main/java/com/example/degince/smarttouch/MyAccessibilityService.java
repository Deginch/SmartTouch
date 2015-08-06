package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Service;
import android.app.UiAutomation;
import android.content.Intent;
import android.graphics.PixelFormat;
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
	//定义浮动窗口布局
	LinearLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	//创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	Button mFLoatButton;
	private boolean isLongClick = false;
	private float startX, startY, currentX, currentY;

	private String TAG = "MyAccessibilityService";
	private AccessibilityService accessibilityService;
	public MyAccessibilityService(){
		accessibilityService=this;
	}

	@Override
	public void onCreate(){
//		createFloatView();
		MyFloatingView myFloatingView= new MyFloatingView(this,this);
		myFloatingView.createFloatView();
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
		Log.i(TAG, "连接辅助服务");
	}

	@Override
	public void onInterrupt() {}



}
