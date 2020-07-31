package com.grad.wave;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class FlyingOrderActivity extends AppCompatActivity {

    private TextView verseView;
    private TextView roundView;
    private TextView authorView;
    private TextView titleView;
    private EditText answerInput;
    private Button applyButton;
    private GifImageView loadingView;
    private char word;
    private int page = 1;
    private int db_index = 0;
    private int round = 1;
    private boolean is_long = false;
    private ArrayList<NetPoem> db = new ArrayList<>();
    private ArrayList<String> usedVerse = new ArrayList<>();

    Handler db_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flying_order);
        verseView = findViewById(R.id.flying_verse);
        roundView = findViewById(R.id.flying_round);
        authorView = findViewById(R.id.flying_author);
        titleView = findViewById(R.id.flying_title);
        answerInput = findViewById(R.id.flying_input);
        applyButton = findViewById(R.id.flying_apply);
        loadingView = findViewById(R.id.flying_loading);

        if(AppManager.is_traChinese)
            this.setTheme(R.style.TraTheme);
        else
            this.setTheme(R.style.SimTheme);

        db_handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Bundle data = msg.getData();
                wordset((ArrayList<NetPoem>)(data.get("db")));
            }
        };

        FlyingOrderDialog dialog = new FlyingOrderDialog(this,this);
        dialog.show();
    }

    private String getnextverse(){
        //已经检索到链表末端，则获取下一页
        if(db_index == db.size()-1){
            page ++;
            db = AppManager.AppIO.GetPoems(word,page);
            if(db==null) return null;
            db_index = 0;
        }
        //指向下一首
        db_index++;
        //检查是否出现过
        for(int i=0;i<usedVerse.size();++i){
            if(usedVerse.get(i).equals(db.get(db_index).verse)){
                //递归获取下一首
                return getnextverse();
            }
        }
        //未出现过则返回
        usedVerse.add(db.get(db_index).verse);
        return db.get(db_index).verse;
    }

    //设置诗句中必须包含的汉字，由FlyingOrderDialog调用
    public void startwordset(final char word,FlyingOrderDialog dialog,boolean is_long) {
        dialog.cancel();
        this.word = word;
        this.is_long = is_long;
        //loadingView.setVisibility(View.VISIBLE);
        DownloadDatabaseThread dl = new DownloadDatabaseThread(word,page,FlyingOrderActivity.this,is_long);
        dl.start();
    }

    //数据库下载完毕后由DownloadDatabaseThread调用
    public void wordset(final ArrayList<NetPoem> db){
        this.db = db;
        GifDrawable gif = (GifDrawable)(loadingView.getDrawable());
        gif.stop();
        loadingView.setVisibility(View.INVISIBLE);
        roundView.setVisibility(View.VISIBLE);

        round = 1;
        verseView.setText(db.get(0).verse);
        authorView.setText(db.get(0).author);
        titleView.setText(db.get(0).title);
        usedVerse.add(db.get(0).verse);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //句号个数
                    int stopcount = 0;
                    String answer = answerInput.getText().toString();

                    //检查答案是否为空
                    if(answer.equals("")){
                        Toast.makeText(getApplicationContext(), "请勿虚晃一枪", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //检查句子是否完整
                    if (answer.toString().toCharArray()[answer.toString().toCharArray().length - 1] != '。'
                            && answer.toString().toCharArray()[answer.toString().toCharArray().length - 1] != '！'
                            && answer.toString().toCharArray()[answer.toString().toCharArray().length - 1] != '？') {
                        Toast.makeText(getApplicationContext(), "请输入完整的句子，以标点结尾", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (int i = 0; i < answer.length(); ++i) {
                        //检查是否有多个句子
                        if(answer.toCharArray()[i] == '。'){
                            stopcount ++;
                            if(stopcount >= 1){
                                Toast.makeText(getApplicationContext(), "请仅输入一个以句号/问号/感叹号结尾的句子", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        //检查句子是否包含指定的汉字
                        if (answer.toCharArray()[i] == word) {

                            //检查本句是否出现过
                            for (int j = 0; j < usedVerse.size(); ++j)
                                if (usedVerse.get(j).equals(answer)) {
                                    Toast.makeText(getApplicationContext(), "这句诗出现过了", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            //确认答案是否存在，并获取下一个句子
                            if (AppManager.AppIO.CheckVerseExists(answer)) {
                                Toast.makeText(getApplicationContext(), answer, Toast.LENGTH_SHORT).show();
                                usedVerse.add(answer);
                                String nextverse = getnextverse();
                                //如果已没有下一条诗句，或在简单模式通过回合10，则宣布人类获胜
                                if (nextverse == null || (!is_long && round == 10)){
                                    verseView.setText(getResources().getString(R.string.flying_order_win));
                                    answerInput.setEnabled(false);
                                    answerInput.setVisibility(View.INVISIBLE);
                                    answerInput.setText("");
                                    roundView.setText("胜");
                                    return;
                                }
                                round++;
                                roundView.setText("----回合" + round + "----");
                                verseView.setText(getnextverse());
                                authorView.setText(db.get(db_index).author);
                                titleView.setText(db.get(db_index).title);
                                answerInput.setText("");
                                return;
                            } else {
                                Toast.makeText(getApplicationContext(), "没有找到这句诗", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                    Toast.makeText(getApplicationContext(), "没有出现" + word, Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    roundView.setText(getResources().getString(R.string.flying_order_error));
                    answerInput.setEnabled(false);
                    answerInput.setVisibility(View.INVISIBLE);
                    answerInput.setText("");
                }
            }
        });
    }

    class DownloadDatabaseThread extends Thread{
        public ArrayList<NetPoem> db = new ArrayList<>();
        public boolean is_got = false;
        public FlyingOrderActivity parent;
        char word;
        int page;
        boolean is_long = false;

        public DownloadDatabaseThread(char word,int page,FlyingOrderActivity caller,boolean is_long){
            this.word = word;
            this.page = page;
            this.is_long = is_long;
            parent = caller;
        }

        public void run(){
            if(is_long)
                db = AppManager.AppIO.GetPoems(word,page);
            else
                db = AppManager.AppIO.GetPoemsShort(word,10,(int)(System.currentTimeMillis()%10));
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("db",db);
            msg.setData(bundle);
            db_handler.sendMessage(msg);
            is_got = true;
            DownloadDatabaseThread.this.interrupt();
        }
    }
}

class FlyingOrderDialog extends Dialog {

    private Context context;
    private Button confirm;
    private Button cancelbtn;
    private EditText wordinput;
    private Activity activity;
    private RadioGroup typegroup;

    public FlyingOrderDialog(Context context,FlyingOrderActivity activity){
        super(context);
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.dialog_flying_order);
        setCanceledOnTouchOutside(false);
        confirm = findViewById(R.id.flying_confirm);
        cancelbtn = findViewById(R.id.flying_cancel);
        wordinput = findViewById(R.id.flying_word);
        typegroup = findViewById(R.id.flying_dialog_radio_group);

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        typegroup.check(R.id.flying_type_short);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wordinput.getText().toString().equals("")){
                    Toast.makeText(context,"请勿虚晃一枪",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(String.valueOf(wordinput.getText().charAt(0)).getBytes().length <= 1) {
                    Toast.makeText(context,"请输入中文字符",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(typegroup.getCheckedRadioButtonId() == R.id.flying_type_long)
                    ((FlyingOrderActivity)activity).startwordset(wordinput.getText().charAt(0),FlyingOrderDialog.this,true);
                else
                    ((FlyingOrderActivity)activity).startwordset(wordinput.getText().charAt(0),FlyingOrderDialog.this,false);
            }
        });

        DisplayMetrics metric = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metric);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = metric.widthPixels;
        getWindow().setAttributes(params);
    }
}
