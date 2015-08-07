package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;

import java.util.List;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.view.accessibility.AccessibilityManager;
import android.widget.SeekBar;

import com.example.degince.smarttouch.preference.MyListPreference;
import com.example.degince.smarttouch.preference.MySeekBarPreference;

public class MainActivity extends PreferenceActivity implements OnPreferenceChangeListener, MySeekBarPreference.OnSeekBarPrefsChangeListener {
	private String TAG = "MainActivity";
	private static SharedPreferences sharedPreferences;
	private MySeekBarPreference alphaSb, widthSb, heightSb, distanceSb;
	private SwitchPreference started;
	private String preferencesName = "com.example.degince.smarttouch_preferences";
	private MyAccessibilityService accessibilityService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		addPreferencesFromResource(R.xml.main_preference);
		sharedPreferences = getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
		//获取辅助服务
		accessibilityService=MyAccessibilityService.getSharedInstance();

		started=(SwitchPreference)findPreference("started");
		started.setOnPreferenceChangeListener(this);
		if(accessibilityService!=null) {
			started.setChecked(accessibilityService.isFLoatViewCreated());
		}else {
			started.setChecked(false);
		}


		alphaSb = (MySeekBarPreference) findPreference("alpha");// 找到preference
		alphaSb.setDefaultProgressValue(Util.getAlpha(sharedPreferences));// 设置起始时的进度
		alphaSb.setMax(60);// 设置最大的数值，不超过10000。如果超过了请在seekbarPreference源码中进行修改max值
		alphaSb.setOnSeekBarPrefsChangeListener(this);// 设置监听器


		widthSb = (MySeekBarPreference) findPreference("size");
		widthSb.setMax(200);
		widthSb.setDefaultProgressValue(Util.getSize(sharedPreferences));
		widthSb.setOnSeekBarPrefsChangeListener(this);
	}

	@Override
	public void onResume() {
		boolean isEnabled = false;
		super.onResume();
		AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> list = AccessibilityManagerCompat.getEnabledAccessibilityServiceList(manager,
				AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
		System.out.println("list.size = " + list.size());
		for (int i = 0; i < list.size(); i++) {
			System.out.println("已经可用的服务列表 = " + list.get(i).getId());
			if ("com.example.degince.smarttouch/.MyAccessibilityService".equals(list.get(i).getId())) {
				System.out.println("已启用");
				isEnabled = true;
				break;
			}
		}
		if (!isEnabled) {
			AccessibilitySetting();
		}else{
			accessibilityService=MyAccessibilityService.getSharedInstance();
		}
	}

	//开启辅助服务设置
	private void AccessibilitySetting() {
		Log.i(TAG, "不存在,提示");
		new AlertDialog.Builder(MainActivity.this)
				.setTitle("提醒")
				.setMessage("打开悬浮窗需要开启辅助服务，点击设置开启。")
				.setPositiveButton("设置",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
							                    int whichButton) {
								Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								try {
									MainActivity.this.startActivity(intent);
								} catch (ActivityNotFoundException ex) {
									intent.setAction(Settings.ACTION_SETTINGS);
									try {
										MainActivity.this
												.startActivity(intent);
									} catch (Exception e) {
									}
								}
							}
						})
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
							                    int whichButton) {
							}
						}).show();

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.i(TAG,"onPreferenceChange+"+preference.getKey()+"*"+newValue);
		if(preference.getKey().equals("started")) {
			if (accessibilityService != null) {
				if (newValue.equals(true)) {
					accessibilityService.createFloatView();
				} else {
					accessibilityService.closeFloatView();
				}
			}else{
				accessibilityService=MyAccessibilityService.getSharedInstance();
				AccessibilitySetting();
				return false;
			}

		}
		return true;
	}

	@Override
	public void onStopTrackingTouch(String key, SeekBar seekBar) {
		Log.i(TAG,"onStopTrackingTouch");
		if(accessibilityService!=null) {
			accessibilityService.updateButton();
		}else {
			accessibilityService=MyAccessibilityService.getSharedInstance();
		}

	}

	@Override
	public void onStartTrackingTouch(String key, SeekBar seekBar) {
		Log.i(TAG,"onStartTrackingTouch");

	}

	@Override
	public void onProgressChanged(String key, SeekBar seekBar, int progress, boolean fromUser) {
		Log.i(TAG,"onProgressChanged");
		if(accessibilityService!=null) {
			accessibilityService.updateButton();
		}else {
			accessibilityService=MyAccessibilityService.getSharedInstance();
		}

	}
}
