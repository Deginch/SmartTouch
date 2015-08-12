package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class Util {
	public static float UnClickAlpha =0.1f;
	public static float ClickAlpha =0.7f;
	private static String TAG="Util";
	public static void virtualHome(Context mContext) {
		// 模拟HOME键
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 如果是服务里调用，必须加入new task标识
		i.addCategory(Intent.CATEGORY_HOME);
		mContext.startActivity(i);
	}



	public static void virtualBack(AccessibilityService service) {
		if (VERSION.SDK_INT < 16) {
			Toast.makeText(service, "Android 4.1及以上系统才支持此功能，请升级后重试", 1).show();
		} else {
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
		}
	}



	public static void recentApps(AccessibilityService service) {
		if (VERSION.SDK_INT < 16) {
			Toast.makeText(service, "Android 4.1及以上系统才支持此功能，请升级后重试", 1).show();
		} else {
			service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
		}
	}

	private static void doInStatusBar(Context mContext, String methodName) {
		try {
			Object service = mContext.getSystemService("statusbar");
			Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
			Method expand = statusBarManager.getMethod(methodName);
			expand.invoke(service);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void openStatusBar(Context mContext) {
		// 判断系统版本号
		String methodName = (VERSION.SDK_INT <= 16) ? "expand" : "expandNotificationsPanel";
		doInStatusBar(mContext, methodName);
	}

	public static void closeStatusBar(Context mContext) {
		// 判断系统版本号
		String methodName = (VERSION.SDK_INT <= 16) ? "collapse" : "collapsePanels";
		doInStatusBar(mContext, methodName);
	}

	@SuppressWarnings("deprecation")
	public static String getRunningActivityName(Context mContext) {
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
		System.out.println("running Activity = " + runningActivity);
		return runningActivity;
	}

	public static void openCamera(Context mContext) {
		Intent intentCamera = new Intent();
		intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentCamera.setAction("android.media.action.STILL_IMAGE_CAMERA");
		mContext.startActivity(intentCamera);
	}

	public static void lockScreen(Context context){
	}

	//震动
	public static void vibrate(long time,Context context) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(time);
		vibrator.cancel();
	}

	public static int getSize(SharedPreferences sharedPreferences) {
		Log.i(TAG, "尺寸为Size=" + sharedPreferences.getInt("size", 70));
		return sharedPreferences.getInt("size", 70);
	}

	public static int getAlpha(SharedPreferences sharedPreferences){
		int alpha =sharedPreferences.getInt("alpha", 40);
		Log.i(TAG, "透明度为" + alpha);
		UnClickAlpha=alpha/100f;
		return alpha;
	}

	public static int getButtonBackground(SharedPreferences sharedPreferences){
		String image=sharedPreferences.getString("shape","roundSquare");
		Log.i(TAG,"背景图片为"+image);
		int imageId=R.drawable.round_square;
		if(image.equals("round")){
			Log.i(TAG,"round"+image);
			imageId=R.drawable.round;
		}else if(image.equals("roundSquare")){
			Log.i(TAG,"roundSquare"+image);
			imageId=R.drawable.round_square;
		}
		else if(image.equals("square")){
			imageId=R.drawable.square;
		}
		else if(image.equals("heart")){
			Log.i(TAG,"heart"+image);
			imageId=R.drawable.heart;
		}else if(image.equals("star")){
			Log.i(TAG,"star"+image);
			imageId=R.drawable.star;
		}
		return imageId;
	}


}
