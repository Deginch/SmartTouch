package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Degince on 2015/8/5.
 */
public class MyFloatingView extends LinearLayout {
	private boolean CanMoving = false;
	private AccessibilityService accessibilityService;
	private final Context context;
	private String TAG = "MyFloatingView";
	//定义浮动窗口布局
	LinearLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	//创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	Button mFLoatButton;
	private GestureDetector gestureDetector;
	private static SharedPreferences sharedPreferences;
	private String preferencesName = "com.example.degince.smarttouch_preferences";

	public MyFloatingView(Context context, AccessibilityService accessibilityService) {
		super(context);
		this.context = context;
		this.accessibilityService = accessibilityService;
		sharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
	}


	public void createFloatView() {
		wmParams = new WindowManager.LayoutParams();
		//获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		Log.i(TAG, "mWindowManager--->" + mWindowManager);
		//设置window type
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		//设置图片格式，效果为背景透明
//		wmParams.format = PixelFormat.RGB_565;
		wmParams.format = PixelFormat.RGBA_8888;
		//设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		//调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = 300;
		wmParams.y = 800;

		//设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		 /*// 设置悬浮窗口长宽数据
		wmParams.width = 200;
        wmParams.height = 80;*/

		LayoutInflater inflater = LayoutInflater.from(context);
		//获取浮动窗口视图所在布局
		mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
		//添加mFloatLayout
		mWindowManager.addView(mFloatLayout, wmParams);
		//浮动窗口按钮
		mFLoatButton = (Button) mFloatLayout.findViewById(R.id.float_id);
		mFLoatButton.setBackgroundResource(Util.getButtonBackground(sharedPreferences));
		mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		Log.i(TAG, "Width/2--->" + mFLoatButton.getMeasuredWidth() / 2);
		Log.i(TAG, "Height/2--->" + mFLoatButton.getMeasuredHeight() / 2);
		//设置监听浮动窗口的触摸移动

		gestureDetector = new GestureDetector(context, new MyOnGestureListener());
		mFLoatButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						Log.i(TAG, "TouchListener ACTION_UP");
						mFLoatButton.setAlpha(Util.UnClickAlpha);
						CanMoving = false;
						break;
					case MotionEvent.ACTION_DOWN:
						mFLoatButton.setAlpha(Util.ClickAlpha);
						break;
					case MotionEvent.ACTION_MOVE:
						if (CanMoving) {
							//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
							wmParams.x = (int) event.getRawX() - mFLoatButton.getMeasuredWidth() / 2;
							//减25为状态栏的高度
							wmParams.y = (int) event.getRawY() - mFLoatButton.getMeasuredHeight() / 2;
							//刷新
							Log.i(TAG, "move");
							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
						}
						break;
				}

				// 一定要返回true，不然获取不到完整的事件
				return true;
			}
		});
		updateFloatButton();
	}

	public void closeFloatView(){
		if(mFloatLayout!=null) {
			mWindowManager.removeView(mFloatLayout);
			mFLoatButton = null;
			mFloatLayout = null;
		}
	}

	public void updateFloatButton(){
		if(mFLoatButton!=null) {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(5, 5);
			Log.i(TAG, "重新刷新按钮");
//		//刷新透明度
			mFLoatButton.setAlpha(Util.getAlpha(sharedPreferences) / 100f);
//		mFLoatButton.setBackgroundColor(Util.getColor(sharedPreferences));
			mFLoatButton.setLayoutParams(new LayoutParams(Util.getSize(sharedPreferences), Util.getSize(sharedPreferences)));
		}
	}

	//更新图标
	public void updateShape(String shape){
		int imageId=R.drawable.round_square;
		if(shape.equals("round")){
			imageId=R.drawable.round;
		}else if(shape.equals("roundSquare")){
			imageId=R.drawable.round_square;
		}
		else if(shape.equals("square")){
			imageId=R.drawable.square;
		}
		else if(shape.equals("heart")){
			imageId=R.drawable.heart;
		}else if(shape.equals("star")){
			imageId=R.drawable.star;
		}
		try {
			mFLoatButton.setBackgroundResource(imageId);
		}catch (Exception e){
			Log.i(TAG,"更新形状失败+"+e.getMessage());
		}
	}

	private String getActionName(int action) {
		String name = "";
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				name = "ACTION_DOWN";
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				name = "ACTION_MOVE";
				break;
			}
			case MotionEvent.ACTION_UP: {
				name = "ACTION_UP";
				break;
			}
			default:
				break;
		}
		return name;
	}

	private void swipeUp() {
		doGestureOperation(sharedPreferences.getString("swipeUp", "nothing"));
	}

	private void swipeDown() {
		doGestureOperation(sharedPreferences.getString("swipeDown", "nothing"));
	}

	private void swipeRight() {
		doGestureOperation(sharedPreferences.getString("swipeRight", "nothing"));
	}

	private void swipeLeft() {
		doGestureOperation(sharedPreferences.getString("swipeLeft", "nothing"));
	}

	private void singleClick() {
		doGestureOperation(sharedPreferences.getString("singleClick", "nothing"));
	}

	private void doubleClick() {
		doGestureOperation(sharedPreferences.getString("doubleClick", "nothing"));
	}

	private void longClik() {
		doGestureOperation(sharedPreferences.getString("longClick", "nothing"));
	}

	private void doGestureOperation(String gesture) {
		if (gesture.equals("openBar")) {
			Util.openStatusBar(context);
		} else if (gesture.equals("recents")) {
			Util.recentApps(accessibilityService);
		} else if (gesture.equals("back")) {
			Util.virtualBack(accessibilityService);
		} else if (gesture.equals("home")) {
			Util.virtualHome(context);
		} else if (gesture.equals("camera")) {
			Util.openCamera(context);
		}

	}

	class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i(TAG, "onSingleTapUp-----" + getActionName(e.getAction()));
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress-----" + getActionName(e.getAction()));
			Util.vibrate(1500,context);
			CanMoving = true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG,
					"onScroll-----" + getActionName(e2.getAction()) + ",(" + e1.getX() + "," + e1.getY() + ") ,("
							+ e2.getX() + "," + e2.getY() + ")");
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.i(TAG,
					"onFling-----" + getActionName(e2.getAction()) + ",(" + e1.getX() + "," + e1.getY() + ") ,("
							+ e2.getX() + "," + e2.getY() + ")");
			if (!CanMoving) {
				int dy = (int) (e2.getY() - e1.getY()); // 计算滑动的距离,纵向操作
				int dx = (int) (e2.getX() - e1.getX());

				if (dy < -20 && Math.abs(velocityY) > Math.abs(velocityX)) {
					Log.i(TAG, "向上");
					swipeUp();
				}

				if (dy > 20 && Math.abs(velocityY) > Math.abs(velocityX)) {
					Log.i(TAG, "向下");
					swipeDown();
				}

				if (dx > 20 && Math.abs(velocityX) > Math.abs(velocityY)) {
					Log.i(TAG, "向右");
					swipeRight();
				}
				if (dx < -20 && Math.abs(velocityX) > Math.abs(velocityY)) {
					Log.i(TAG, "向左");
					swipeLeft();
				}
			}
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Log.i(TAG, "onShowPress-----" + getActionName(e.getAction()));
		}

		@Override
		public boolean onDown(MotionEvent e) {
			Log.i(TAG, "onDown-----" + getActionName(e.getAction()));
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(TAG, "onDoubleTap-----" + getActionName(e.getAction()));
			doubleClick();
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.i(TAG, "onDoubleTapEvent-----" + getActionName(e.getAction()));
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.i(TAG, "onSingleTapConfirmed-----" + getActionName(e.getAction()));
			//开启了双击功能,单击需要更多时间
			singleClick();
			return false;
		}
	}


}
