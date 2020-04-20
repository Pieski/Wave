package com.grad.wave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private Button confirmbutton;
    private Button registbutton;
    private Button offlinebutton;
    private TextView offlinedeclare;
    private EditText username;
    private EditText password;
    private CheckBox remember;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppManager.NetIO.StartUp(getApplicationContext());
        AppManager manager = new AppManager();
        manager.StartKeepingAlive();

        pref = getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
        confirmbutton = this.findViewById(R.id.login_confirm_button);
        registbutton = this.findViewById(R.id.regist_button);
        offlinebutton = this.findViewById(R.id.offline_activity_button);
        offlinedeclare = this.findViewById(R.id.offline_declare);
        username = this.findViewById(R.id.login_username);
        password = this.findViewById(R.id.login_password);
        remember = this.findViewById(R.id.login_save_password);

        //读取简繁设置
        SharedPreferences preferences = getSharedPreferences("chinese_type", Context.MODE_PRIVATE);
        AppManager.is_traChinese = preferences.getBoolean("is_traditional",false);
        if(AppManager.is_traChinese)
            this.setTheme(R.style.TraTheme);
        else
            this.setTheme(R.style.SimTheme);

        if (!AppManager.NetIO.is_contconnect) {

            //读取保存的密码
            username.setText(pref.getString("username",null));
            password.setText(pref.getString("password",null));
            if(!username.getText().toString().equals(""))
                remember.setChecked(true);

            confirmbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (username.getText().toString().equals("") || password.getText().toString().equals(""))
                        return;
                    confirmbutton.setEnabled(false);
                    AppManager manager = (AppManager) getApplication();
                    if (AppManager.AppIO.Login(username.getText().toString(), password.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "登陆成功，用户ID：" + AppManager.UserID, Toast.LENGTH_SHORT).show();

                        if(remember.isChecked()){
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("username",username.getText().toString());
                            editor.putString("password",password.getText().toString());
                            editor.apply();
                        }else{
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("username","");
                            editor.putString("password","");
                            editor.apply();
                        }

                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                    confirmbutton.setEnabled(true);
                }
            });
            registbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    Intent intent = new Intent(getApplicationContext(), RegistActivity.class);
                    startActivity(intent);
                }
            });
        }else{
            offlinebutton.setVisibility(View.VISIBLE);
            offlinebutton.setEnabled(true);
            offlinebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this,FlyingOrderActivity.class);
                    startActivity(intent);
                }
            });
            offlinedeclare.setVisibility(View.VISIBLE);
        }
    }
}
