package com.grad.wave;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private EditText passwordconfirm;
    private EditText identify;
    private Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        username = findViewById(R.id.reg_username);
        password = findViewById(R.id.reg_password);
        passwordconfirm = findViewById(R.id.reg_confirm_password);
        identify = findViewById(R.id.reg_identify);
        confirm = findViewById(R.id.reg_confirm_button);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pw = password.getText().toString();
                String pwcf = passwordconfirm.getText().toString();
                if(!pw.equals(pwcf)) {
                    Toast.makeText(getApplicationContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!identify.getText().toString().equals(getString(R.string.identify_answer))){
                    Toast.makeText(getApplicationContext(),"并不是"+identify.getText().toString() , Toast.LENGTH_SHORT).show();
                    return;
                }
                confirm.setEnabled(false);
                if(AppManager.AppIO.Regist(username.getText().toString(),pw)){
                    if(AppManager.AppIO.Login(username.getText().toString(),pw)) {
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(getApplicationContext(),"注册出错，请稍后重试",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                confirm.setEnabled(true);
            }
        });
    }
}
