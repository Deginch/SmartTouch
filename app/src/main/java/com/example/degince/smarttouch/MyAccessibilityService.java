package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Service;
import android.app.UiAutomation;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.degince.smarttouch.lockscreen.LockScreenAdmin;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

	private String TAG = "MyAccessibilityService";
	private static MyAccessibilityService sharedInstance;
	private MyFloatingView myFloatingView;
	private boolean isFLoatViewCreated = false;
	public MyAccessibilityService() {
	}

	@Override
	public void onCreate() {
		ComponentName componentName = new ComponentName(this, LockScreenAdmin.class);
		myFloatingView = new MyFloatingView(this, this,componentName);

		createFloatView();
	}

	public void createFloatView() {
		if(!isFLoatViewCreated) {
			Log.i(TAG, "创建悬浮窗");
			myFloatingView.createFloatView();
			isFLoatViewCreated = true;
		}
	}

	public void closeFloatView() {
		Log.i(TAG,"关闭悬浮窗");
		if(myFloatingView!=null) {
			myFloatingView.closeFloatView();
		}
		isFLoatViewCreated = false;
	}

	public void updateShape(String shape){
		if(myFloatingView!=null) {
			myFloatingView.updateShape(shape);
		}
	}

	public boolean isFLoatViewCreated() {
		return this.isFLoatViewCreated;
	}

	//重载悬浮窗
	public void updateButton() {
		Log.i(TAG, "重新刷新悬浮窗");
		myFloatingView.updateFloatButton();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "service onDestroy");
		this.closeFloatView();
		super.onDestroy();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.i(TAG, "检测到事件,getEventType="+AccessibilityEvent.eventTypeToString(event.getEventType()));
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen=imm.isActive();
		if(isOpen){
			Log.i(TAG,"输入法打开");
		}
	}
	@Override
	protected void onServiceConnected() {
		sharedInstance = this;
		Log.i(TAG, "连接辅助服务");
	}

	@Override
	public void onInterrupt() {
	}

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
