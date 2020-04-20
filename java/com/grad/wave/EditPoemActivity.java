package com.grad.wave;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EditPoemActivity extends AppCompatActivity {

    private Button selectedbutton;
    private Button overbutton;
    private TextView titleview;
    private TextView authorview;
    private TextView contentview;
    private RadioGroup typeview;
    private Drawable edit_original;
    private Drawable content_original;
    private Drawable radio_original;
    private boolean is_type_selected = false;
    private boolean is_title_attention = false;
    private boolean is_author_attention = false;
    private boolean is_content_attention = false;
    private boolean is_type_attention = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_poem);

        overbutton = findViewById(R.id.new_over);
        titleview = findViewById(R.id.new_title);
        authorview = findViewById(R.id.new_author);
        contentview = findViewById(R.id.new_content);
        typeview = findViewById(R.id.new_type);
        selectedbutton = findViewById(R.id.new_selected);

        typeview.clearCheck();

        edit_original = titleview.getBackground();
        radio_original = typeview.getBackground();
        content_original = contentview.getBackground();

        if(AppManager.is_traChinese)
            this.setTheme(R.style.TraTheme);
        else
            this.setTheme(R.style.SimTheme);

        overbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(titleview.getText().toString().equals("")){
                    titleview.setBackgroundResource(R.drawable.attention_background);
                    is_title_attention = true;
                    return;
                }
                if(authorview.getText().toString().equals("")){
                    authorview.setBackgroundResource(R.drawable.attention_background);
                    is_author_attention = true;
                    return;
                }
                if(contentview.getText().toString().equals("")){
                    contentview.setBackgroundResource(R.drawable.attention_background);
                    is_content_attention = true;
                    return;
                }
                if(typeview.getCheckedRadioButtonId() == 0 || !is_type_selected){
                    typeview.setBackgroundResource(R.drawable.attention_background);
                    is_type_attention = true;
                    return;
                }
                if(titleview.getText().toString().length() > 256){
                    Toast.makeText(getApplicationContext(),getString(R.string.title_over_length),Toast.LENGTH_SHORT).show();
                    titleview.setBackgroundResource(R.drawable.attention_background);
                    is_title_attention = true;
                    return;
                }
                if(authorview.getText().toString().length() > 256){
                    Toast.makeText(getApplicationContext(),getString(R.string.author_over_length),Toast.LENGTH_SHORT).show();
                    authorview.setBackgroundResource(R.drawable.attention_background);
                    is_author_attention = true;
                    return;
                }

                ArticleType type = ArticleType.Foreign;
                if(typeview.getCheckedRadioButtonId() == R.id.new_type_modern_chinese)
                    type = ArticleType.ModernChinese;
                else if(typeview.getCheckedRadioButtonId() == R.id.new_type_classic_chinese)
                    type = ArticleType.ClassicChinese;

                ArticleInfo info = new ArticleInfo(titleview.getText().toString(),authorview.getText().toString(),
                        contentview.getText().toString().getBytes().length,type);
                ArticleData data = new ArticleData(info,contentview.getText().toString(),null);
                if(AppManager.AppIO.UploadArticle(data)){
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.net_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(AppManager.is_admin) {
            selectedbutton.setVisibility(View.VISIBLE);
            selectedbutton.setEnabled(true);
            selectedbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (titleview.getText().toString().equals("")) {
                        titleview.setBackgroundResource(R.drawable.attention_background);
                        is_title_attention = true;
                        return;
                    }
                    if (authorview.getText().toString().equals("")) {
                        authorview.setBackgroundResource(R.drawable.attention_background);
                        is_author_attention = true;
                        return;
                    }
                    if (contentview.getText().toString().equals("")) {
                        contentview.setBackgroundResource(R.drawable.attention_background);
                        is_content_attention = true;
                        return;
                    }
                    if (typeview.getCheckedRadioButtonId() == 0 || !is_type_selected) {
                        typeview.setBackgroundResource(R.drawable.attention_background);
                        is_type_attention = true;
                        return;
                    }
                    if (titleview.getText().toString().length() > 256) {
                        Toast.makeText(getApplicationContext(), getString(R.string.title_over_length), Toast.LENGTH_SHORT).show();
                        titleview.setBackgroundResource(R.drawable.attention_background);
                        is_title_attention = true;
                        return;
                    }
                    if (authorview.getText().toString().length() > 256) {
                        Toast.makeText(getApplicationContext(), getString(R.string.author_over_length), Toast.LENGTH_SHORT).show();
                        authorview.setBackgroundResource(R.drawable.attention_background);
                        is_author_attention = true;
                        return;
                    }

                    ArticleType type = ArticleType.Foreign;
                    if (typeview.getCheckedRadioButtonId() == R.id.new_type_modern_chinese)
                        type = ArticleType.ModernChinese;
                    else if (typeview.getCheckedRadioButtonId() == R.id.new_type_classic_chinese)
                        type = ArticleType.ClassicChinese;

                    ArticleInfo info = new ArticleInfo(titleview.getText().toString(), authorview.getText().toString(),
                            contentview.getText().toString().getBytes().length, type);
                    info.BlockID = 0;
                    ArticleData data = new ArticleData(info, contentview.getText().toString(), null);
                    if (AppManager.AppIO.UploadArticle(data)) {
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        typeview.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                group.check(checkedId);
                is_type_selected = true;
                if(is_type_attention) {
                    group.setBackground(radio_original);
                    is_type_attention = false;
                }
            }
        });

        titleview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(is_title_attention){
                    titleview.setBackground(edit_original);
                    is_title_attention= false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        authorview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(is_author_attention) {
                    authorview.setBackground(edit_original);
                    is_author_attention = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contentview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(is_content_attention){
                    contentview.setBackground(content_original);
                    is_content_attention= false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
