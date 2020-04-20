package com.grad.wave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class SettingActivity extends AppCompatActivity {

    RadioGroup settingGroup;
    Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingGroup = findViewById(R.id.setting_chinese_type);
        SharedPreferences preferences = getSharedPreferences("chinese_type", Context.MODE_PRIVATE);
        if(preferences.getBoolean("is_traditional",false)){
            settingGroup.check(R.id.setting_chinese_simplified);
        }else
            settingGroup.check(R.id.setting_chinese_traditional);

        applyButton = findViewById(R.id.setting_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("chinese_type", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (settingGroup.getCheckedRadioButtonId() == R.id.setting_chinese_simplified) {
                    AppManager.is_traChinese = false;
                    editor.putBoolean("is_traditional", false);
                    editor.apply();
                    finish();
                } else if (settingGroup.getCheckedRadioButtonId() == R.id.setting_chinese_traditional) {
                    AppManager.is_traChinese = true;
                    editor.putBoolean("is_traditional", true);
                    editor.apply();
                    finish();
                }
            }
        });
    }
}
