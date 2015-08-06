package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Degince on 2015/8/5.
 */
public class MyFloatingView extends LinearLayout {
	private AccessibilityService accessibilityService;
	private final Context context;
	private String TAG = "MyFloatingView";
	//定义浮动窗口布局
	LinearLayout mFloatLayout;
	WindowManager.LayoutParams wmParams;
	//创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	Button mFLoatButton;
	private boolean isLongClick = false;
	private float startX, startY, currentX, currentY;
	private GestureDetector gestureDetector;

	public MyFloatingView(Context context, AccessibilityService accessibilityService) {
		super(context);
		this.context = context;
		this.accessibilityService = accessibilityService;
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
		wmParams.x = 0;
		wmParams.y = 0;

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
		mFLoatButton.setAlpha(Util.UnClickAlpha);
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
				// 一定要返回true，不然获取不到完整的事件
				return true;
//				//获取当前坐标
//				float x = event.getX();
//				float y = event.getY();
//				switch (event.getAction()) {
//					case MotionEvent.ACTION_DOWN:    //捕获手指触摸按下动作
//						startX = x;
//						startY = y;
//						Log.i(TAG, "down");
//						mFLoatButton.setAlpha(Util.ClickAlpha);
//						break;
//
//					case MotionEvent.ACTION_MOVE:    //捕获手指触摸移动动作
//						Log.i(TAG,"Move");
//						if (isLongClick) {
//							//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
//							wmParams.x = (int) event.getRawX() - mFLoatButton.getMeasuredWidth() / 2;
//							//减25为状态栏的高度
//							wmParams.y = (int) event.getRawY() - mFLoatButton.getMeasuredHeight() / 2;
//							//刷新
////                            Log.i(TAG, "move");
//							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
//						} else {
//
//						}
//						break;
//
//					case MotionEvent.ACTION_UP:    //捕获手指触摸离开动作
//						mFLoatButton.setAlpha(Util.UnClickAlpha);
//						currentX = x;
//						currentY = y;
//						float differX = currentX - startX;
//						float differY = currentY - startY;
//						if (Math.abs(differX) > 8 && Math.abs(differY) > 8) {
//							if (Math.abs(differX) / Math.abs(differY) > 1.5) {
//								//左右滑
//								if (differX > 0) {
//									Log.i(TAG, "sliding right");
//									vibrate(1000);
//								} else {
//									Log.i(TAG, "sliding left");
//									vibrate(1000);
//									mFLoatButton.clearFocus();
//								}
//							} else {
//								//上下滑
//								if (differY > 0) {
//									Log.i(TAG, "sliding down");
//									vibrate(1000);
//									Util.openStatusBar(context);
//								} else {
//									Intent intent = new Intent();
//									intent.setAction(Intent.ACTION_MAIN);
//									intent.addCategory(Intent.CATEGORY_HOME);
//									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//									context.startActivity(intent);
//									Log.i(TAG, "sliding up");
//									vibrate(1000);
//								}
//							}
//						}
//						isLongClick = false;
//						Log.i(TAG, "up");
//						break;
//					default:
//						break;
//				}
//				//刷新
////                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
//
//				return false;  //此处必须返回false，否则OnClickListener获取不到监听
			}
		});

//		mFLoatButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Log.i(TAG, "onClick");
//				Util.virtualBack(accessibilityService);
//				vibrate(1000);
//			}
//		});
//		mFLoatButton.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				Log.i(TAG, "onLongClick");
//				vibrate(1500);
//				isLongClick = true;
//				return false;
//			}
//		});
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

	class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
		private boolean CanMoving = false;

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i(TAG, "onSingleTapUp-----" + getActionName(e.getAction()));
			CanMoving = false;
			if(e.getAction()==MotionEvent.ACTION_UP){
				Util.virtualBack(accessibilityService);
				mFLoatButton.setAlpha(Util.UnClickAlpha);
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress-----" + getActionName(e.getAction()));
			Util.vibrate(1000, context);
			mFLoatButton.setAlpha(Util.UnClickAlpha);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG,
					"onScroll-----" + getActionName(e2.getAction()) + ",(" + e1.getX() + "," + e1.getY() + ") ,("
							+ e2.getX() + "," + e2.getY() + ")");

			if (CanMoving) {
				//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
				wmParams.x = (int) e2.getRawX() - mFLoatButton.getMeasuredWidth() / 2;
				//减25为状态栏的高度
				wmParams.y = (int) e2.getRawY() - mFLoatButton.getMeasuredHeight() / 2;
				//刷新
				Log.i(TAG, "move");
				mWindowManager.updateViewLayout(mFloatLayout, wmParams);
			}
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
					Util.openRecentApp(accessibilityService);
				}

				if (dy > 20 && Math.abs(velocityY) > Math.abs(velocityX)) {
					Log.i(TAG, "向下");
					Util.virtualHome(context);
				}

				if (dx > 20 && Math.abs(velocityX) > Math.abs(velocityY)) {
					Log.i(TAG, "向右");
				}
				if (dx < -20 && Math.abs(velocityX) > Math.abs(velocityY)) {
					Log.i(TAG, "向左");
				}
			}
			CanMoving=false;
			mFLoatButton.setAlpha(Util.UnClickAlpha);
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Log.i(TAG, "onShowPress-----" + getActionName(e.getAction()));
			Util.vibrate(1000, context);
			CanMoving = true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			Log.i(TAG, "onDown-----" + getActionName(e.getAction()));
			mFLoatButton.setAlpha(Util.ClickAlpha);
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(TAG, "onDoubleTap-----" + getActionName(e.getAction()));
			Util.virtualHome(context);
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.i(TAG, "onDoubleTapEvent-----" + getActionName(e.getAction()));
			if(e.getAction()==MotionEvent.ACTION_UP){
				mFLoatButton.setAlpha(Util.UnClickAlpha);
			}
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.i(TAG, "onSingleTapConfirmed-----" + getActionName(e.getAction()));
			mFLoatButton.setAlpha(Util.UnClickAlpha);
			return false;
		}
	}


}
