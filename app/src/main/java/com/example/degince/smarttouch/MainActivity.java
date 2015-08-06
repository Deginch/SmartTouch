package com.example.degince.smarttouch;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceActivity;
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

public class MainActivity extends PreferenceActivity implements PreferenceChangeListener{
	private String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_list);
		addPreferencesFromResource(R.xml.main_preference);
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
		}
	}

	private void AccessibilitySetting() {
		Log.i(TAG, "不存在,提示");
		new AlertDialog.Builder(MainActivity.this)
				.setTitle("提醒")
				.setMessage("是否开启辅助服务")
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
	public void preferenceChange(PreferenceChangeEvent pce) {
		Log.i(TAG,"配置发生改变");
	}
}
