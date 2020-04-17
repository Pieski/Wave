package com.grad.wave;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

public class CommentDialog extends Dialog {

    private EditText editcomment;
    private int articleid = 0;
    private byte blockid = 0;
    private Button confirm;
    private Button cancel;
    private Context context;
    private Drawable original;

    public CommentDialog(Context con,int id,byte bcode){
        super(con);
        context = con;
        articleid = id;
        blockid = bcode;
    }


    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.dialog_comment);
        setCanceledOnTouchOutside(false);
        confirm = findViewById(R.id.comment_confirm);
        cancel = findViewById(R.id.comment_cancel);
        editcomment = findViewById(R.id.comment_edit);
        original = editcomment.getBackground();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editcomment.getText().toString().equals("")){
                    editcomment.setBackgroundResource(R.drawable.attention_background);
                    return;
                }
                if(editcomment.getText().toString().getBytes().length > 765){
                    editcomment.setBackgroundResource(R.drawable.attention_background);
                    Toast.makeText(context,"超出" + (editcomment.getText().toString().getBytes().length-765) + "Bytes",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(AppManager.AppIO.UploadComment(editcomment.getText().toString(),articleid,blockid))
                    cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        DisplayMetrics metric = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metric);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = metric.widthPixels;
        getWindow().setAttributes(params);
    }
}
