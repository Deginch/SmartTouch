package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
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
	private ComponentName componentName;
	private Handler mMainHandler, mAutoMovingHandler;
	private final int UpdateButtonLocation = 1;
	private final int movingSleepTime=2,movingX=2;
	public MyFloatingView(Context context, AccessibilityService accessibilityService, ComponentName componentName) {
		super(context);
		this.context = context;
		this.accessibilityService = accessibilityService;
		this.componentName = componentName;
		sharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
		handlerInit();
	}


	public void createFloatView() {
		//在创建之前先清除已经存在的图标
		closeFloatView();
		//创建自动贴墙的子线程
		new AutoMovingThread().start();

		wmParams = new WindowManager.LayoutParams();
		//获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		Display display = mWindowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		final int screenWidth = size.x;
		final int screenHeight = size.y;
		Log.i(TAG, "mWindowManager--->" + mWindowManager + ",screenWidth=" + screenWidth);

		//设置window type
//		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
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
		wmParams.x = screenWidth / 2;
		wmParams.y = screenHeight / 2;

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
						//手势抬起时判断是否需要自动移动至边缘处
						mFLoatButton.setAlpha(Util.UnClickAlpha);
						if (CanMoving) {
							CanMoving = false;
							if (Util.isAutoMove(sharedPreferences)) {
								//给子线程发送消息自动将按钮移动置屏幕边缘
								if (mAutoMovingHandler != null) {
									int destinationX;
									if(wmParams.x>screenWidth/2){
										destinationX=screenWidth-mFLoatButton.getMeasuredWidth()/2;
									}else{
										destinationX=0-mFLoatButton.getMeasuredWidth()/2;
									}
									//将目的地和当前位置发送给自动移动位置线程
									Message childMsg = mAutoMovingHandler.obtainMessage();
									Bundle bundle=new Bundle();
									bundle.putInt("destinationX",destinationX);
									bundle.putInt("currentX",wmParams.x);
									childMsg.setData(bundle);
									childMsg.what=UpdateButtonLocation;
									mAutoMovingHandler.sendMessage(childMsg);
									Log.i(TAG, "Send a message to the child thread - ");
								}
							}
						}
						break;
					case MotionEvent.ACTION_DOWN:
						//如果不是长按移动的话，则单击可移动
						if(!Util.isLongPressMoving(sharedPreferences)){
							CanMoving=true;
						}
						break;
					case MotionEvent.ACTION_MOVE:
						if (CanMoving&&Util.isLongPressMoving(sharedPreferences)) {
							//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
							//如果触摸点位置在按钮外面，则更新按钮位置
//							if(Math.abs(wmParams.x-event.getRawX())>mFLoatButton.getMeasuredWidth()/4&&Math.abs(wmParams.y-event.getRawY())>mFLoatButton.getMeasuredHeight()/4) {
								wmParams.x = (int) event.getRawX() - mFLoatButton.getMeasuredWidth() / 2;
								wmParams.y = (int) event.getRawY() - mFLoatButton.getMeasuredHeight() / 2;
								//刷新
								Log.i(TAG, "move");
								mWindowManager.updateViewLayout(mFloatLayout, wmParams);
								Log.i(TAG, "x=" + wmParams.x + ",y=" + wmParams.y);
//							}
						}
						break;
				}
				// 一定要返回true，不然获取不到完整的事件
				return true;
			}
		});
		mFLoatButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Log.i(TAG,"LONG CLICK");
				return false;
			}
		});
		updateFloatButton();
	}


	private void handlerInit() {
		//控制页面更新
		mMainHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case UpdateButtonLocation: {
						Bundle bundle = msg.getData();
						int x = bundle.getInt("buttonX", 0);
						//收到子线程发来的位置信息后则开始更新位置。
						if(wmParams!=null&&mFloatLayout!=null) {
							wmParams.x = x;
							mWindowManager.updateViewLayout(mFloatLayout, wmParams);
						}
						break;
					}
					default:
						break;
				}
			}
		};
	}

	class AutoMovingThread extends Thread {
		@Override
		public void run() {
			Looper.prepare();
			mAutoMovingHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch(msg.what){
						case UpdateButtonLocation:{
							Log.i(TAG,"开启自动移动线程");
							//初始化发送handler
							Bundle sendBundle=new Bundle();

							Bundle bundle=msg.getData();
							int destinationX=bundle.getInt("destinationX", 0);
							int currentX=bundle.getInt("currentX",0);
							if(currentX<destinationX){
								while((currentX+=movingX)<destinationX){
									try {
										sleep(movingSleepTime);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									Message message=new Message();
									message.what=UpdateButtonLocation;
									sendBundle.putInt("buttonX", currentX);
									message.setData(sendBundle);
									mMainHandler.sendMessage(message);

								}
							}else{
								while((currentX-=movingX)>destinationX){
									try {
										sleep(movingSleepTime);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									Log.i(TAG,"currentX="+currentX);
									Message message=new Message();
									message.what=UpdateButtonLocation;
									sendBundle.putInt("buttonX",currentX);
									message.setData(sendBundle);
									mMainHandler.sendMessage(message);
								}
							}
							currentX=destinationX;
							Message message=new Message();
							message.what=UpdateButtonLocation;
							sendBundle.putInt("buttonX",currentX);
							message.setData(sendBundle);
							mMainHandler.sendMessage(message);
							break;
						}
						default:
							break;
					}
				}
			};
			//启动子线程消息循环队列
			Looper.loop();
		}
	}

	public void closeFloatView() {
		if (mFloatLayout != null && mWindowManager != null) {
			mWindowManager.removeView(mFloatLayout);
			mFLoatButton = null;
			mFloatLayout = null;
			if(mAutoMovingHandler!=null){
				mAutoMovingHandler.getLooper().quit();
			}
		}
	}

	public void updateFloatButton() {
		if (mFLoatButton != null) {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(5, 5);
			Log.i(TAG, "重新刷新按钮");
//		//刷新透明度
			mFLoatButton.setAlpha(Util.getAlpha(sharedPreferences) / 100f);
//		mFLoatButton.setBackgroundColor(Util.getColor(sharedPreferences));
			mFLoatButton.setLayoutParams(new LayoutParams(Util.getSize(sharedPreferences), Util.getSize(sharedPreferences)));
		}
	}

	//更新图标
	public void updateShape(String shape) {
		int imageId = R.drawable.round_square;
		if (shape.equals("round")) {
			imageId = R.drawable.round;
		} else if (shape.equals("roundSquare")) {
			imageId = R.drawable.round_square;
		} else if (shape.equals("square")) {
			imageId = R.drawable.square;
		} else if (shape.equals("heart")) {
			imageId = R.drawable.heart;
		} else if (shape.equals("star")) {
			imageId = R.drawable.star;
		}
		try {
			mFLoatButton.setBackgroundResource(imageId);
		} catch (Exception e) {
			Log.i(TAG, "更新形状失败+" + e.getMessage());
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
		if (Util.isVibrate(sharedPreferences)) {
			Util.vibrate(1000, context);
		}
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
		} else if (gesture.equals("lock")) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Util.lockScreen(context, componentName);
		}

	}

	class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i(TAG, "onSingleTapUp-----" + getActionName(e.getAction()));
			if (!Util.isDoubleClickable(sharedPreferences)) {
				singleClick();
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress-----" + getActionName(e.getAction()));
			Util.vibrate(2000, context);
			if(Util.isLongPressMoving(sharedPreferences)) {
				CanMoving = true;
			}else{
				mFLoatButton.setAlpha(Util.UnClickAlpha);
				longClik();
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			Log.i(TAG,"onScroll e1.x="+e1.getX()+",e1.y="+e1.getY()+",e1.RawX="+e1.getRawX()+",e1.RawY="+e1.getRawY());
			Log.i(TAG,"onScroll e2.x="+e2.getX()+",e2.y="+e2.getY()+",e2.RawX="+e2.getRawX()+",e2.RawY="+e2.getRawY());
			if(CanMoving&&!Util.isLongPressMoving(sharedPreferences)){
					wmParams.x = (int) e2.getRawX() - mFLoatButton.getMeasuredWidth() / 2;
					wmParams.y = (int) e2.getRawY() - mFLoatButton.getMeasuredHeight() / 2;
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
			mFLoatButton.setAlpha(Util.ClickAlpha);
			return false;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(TAG, "onDoubleTap-----" + getActionName(e.getAction()));
			//连续两次设置透明度，是为了解决双击锁屏后按钮显示为黑色的bug
			mFLoatButton.setAlpha(Util.UnClickAlpha);
			doubleClick();
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.i(TAG, "onDoubleTapEvent-----" + getActionName(e.getAction()));
			mFLoatButton.setAlpha(Util.UnClickAlpha);
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.i(TAG, "onSingleTapConfirmed-----" + getActionName(e.getAction()));
			//开启了双击功能,单击需要更多时间
			if (Util.isDoubleClickable(sharedPreferences)) {
				singleClick();
			}
			return false;
		}
	}


}
