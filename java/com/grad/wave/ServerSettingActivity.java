package com.grad.wave;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Ref;

public class ServerSettingActivity extends AppCompatActivity {

    private RadioGroup RegPermission;
    private RadioGroup UploadPermission;
    private RadioGroup CommentPermission;
    private RadioGroup RefreshPermission;
    private RadioGroup LoginPermission;
    private RadioGroup FlyingOrderPermission;
    private EditText MaxClientInput;
    private EditText AdminPassword;
    private TextView ConnectedClientView;
    private Button ApplyButton;

    private ServerConfig conf = new ServerConfig();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_setting);

        RegPermission = findViewById(R.id.server_setting_reg_allowance);
        UploadPermission = findViewById(R.id.server_setting_upload_allowance);
        CommentPermission = findViewById(R.id.server_setting_comment_allowance);
        RefreshPermission = findViewById(R.id.server_setting_refresh_allowance);
        LoginPermission = findViewById(R.id.server_setting_login_allowance);
        FlyingOrderPermission = findViewById(R.id.server_setting_flying_order_allowance);
        MaxClientInput = findViewById(R.id.server_setting_maxclnt);
        ConnectedClientView = findViewById(R.id.server_setting_conclnt);
        ApplyButton = findViewById(R.id.server_setting_apply);
        AdminPassword = findViewById(R.id.server_setting_password);

        ApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conf = new ServerConfig(conf.MaxClient,conf.ConnectedClient,true,true,true,true,true,true);
                if(RegPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.RegPermitted = false;
                if(UploadPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.UploadPermitted = false;
                if(CommentPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.CommentPermitted = false;
                if(RefreshPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.RefreshPermitted = false;
                if(LoginPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.LoginPermitted = false;
                if(FlyingOrderPermission.getCheckedRadioButtonId() == R.id.disallow)
                    conf.FlyingOrderPermitted = false;
                if(Integer.parseInt(MaxClientInput.getText().toString()) <= 0)
                    return;
                conf.MaxClient = Integer.parseInt(MaxClientInput.getText().toString());

                if(!AdminPassword.getText().toString().equals(getApplicationContext().getResources().getString(R.string.admin_key))){
                    Toast.makeText(getApplicationContext(),"口令错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(AppManager.AppIO.SetServerStatus(conf)) {
                    Toast.makeText(getApplicationContext(), "成功", Toast.LENGTH_SHORT).show();
                    UpdateStatus();
                }else
                    Toast.makeText(getApplicationContext(),"失败",Toast.LENGTH_SHORT).show();
            }
        });

        UpdateStatus();
    }

    void UpdateStatus(){
        conf = AppManager.AppIO.RequestServerConfig();
        if(conf==null){
            Toast.makeText(getApplicationContext(),"获取状态失败，建议退出重试",Toast.LENGTH_SHORT).show();
            return;
        }
        if(conf.RegPermitted)
            RegPermission.check(R.id.allow);
        else
            RegPermission.check(R.id.disallow);
        if(conf.LoginPermitted)
            LoginPermission.check(R.id.allow);
        else
            LoginPermission.check(R.id.disallow);
        if(conf.FlyingOrderPermitted)
            FlyingOrderPermission.check(R.id.allow);
        else
            FlyingOrderPermission.check(R.id.disallow);
        if(conf.RefreshPermitted)
            RefreshPermission.check(R.id.allow);
        else
            RefreshPermission.check(R.id.disallow);
        if(conf.CommentPermitted)
            CommentPermission.check(R.id.allow);
        else
            CommentPermission.check(R.id.disallow);
        if(conf.UploadPermitted)
            UploadPermission.check(R.id.allow);
        else
            UploadPermission.check(R.id.disallow);
        MaxClientInput.setText(String.valueOf(conf.MaxClient));
        ConnectedClientView.setText(String.valueOf(conf.ConnectedClient));
    }
}
