package com.grad.wave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewPager viewpager = this.findViewById(R.id.page_view);
        viewpager.setOffscreenPageLimit(2);
        final BottomNavigationView navigation = this.findViewById(R.id.navi_view);
        final Button newarticle = this.findViewById(R.id.new_article_button);

        //向导航适配器添加版块片段并设置导航适配器
        NavigationAdapter naviadapter = new NavigationAdapter(getSupportFragmentManager());
        naviadapter.addFragment(new FragmentSelected());
        naviadapter.addFragment(new FragmentWorks());
        naviadapter.addFragment(new FragmentDiscovery());
        viewpager.setAdapter(naviadapter);

        //设置导航栏监听器
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.selected_block_item:
                        viewpager.setCurrentItem(0);
                        return true;
                    case R.id.works_block_item:
                        viewpager.setCurrentItem(1);
                        return true;
                    case R.id.discover_block_item:
                        viewpager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });

        //设置分段显示栏监听器
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                return;
            }

            @Override
            public void onPageSelected(int pos) {
                navigation.getMenu().getItem(pos).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                return;
            }
        });

        //设置新建文章按钮监听器
        newarticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewArticleTypeDialog dialog = new NewArticleTypeDialog(MainActivity.this);
                dialog.show();
            }
        });

        //显示每日一句
        DailyVerseDialog dailyVerseDialog = new DailyVerseDialog(this);
        dailyVerseDialog.show();
    }

    @Override
    public void onBackPressed(){
        return;
    }
}

class NavigationAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments = new ArrayList<>();

    public NavigationAdapter(FragmentManager manager){
        super(manager);
    }

    @Override
    public Fragment getItem(int pos){
        return fragments.get(pos);
    }

    @Override
    public int getCount(){
        return fragments.size();
    }

    public void addFragment(Fragment fr){
        fragments.add(fr);
    }
}

class NewArticleTypeDialog extends Dialog {
    private RadioGroup typegroup;
    private int articleid = 0;
    private byte blockid = 0;
    private Button confirm;
    private Button cancel;
    private Context context;

    public NewArticleTypeDialog(Context con){
        super(con);
        context = con;
    }


    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.dialog_article_type);
        setCanceledOnTouchOutside(false);
        confirm = findViewById(R.id.new_article_confirm_button);
        cancel = findViewById(R.id.new_article_cancel_button);
        typegroup = findViewById(R.id.new_article_type_group);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typegroup.getCheckedRadioButtonId() == R.id.type_poem){
                    Intent intent = new Intent(context, EditPoemActivity.class);
                    context.startActivity(intent);
                    cancel();
                }else if(typegroup.getCheckedRadioButtonId() == R.id.type_appreciation){
                    Intent intent = new Intent(context,EditAppreciationActivity.class);
                    context.startActivity(intent);
                    cancel();
                }else{
                    Toast.makeText(context,"请勿虚晃一枪",Toast.LENGTH_SHORT).show();
                }
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

class DailyVerseDialog extends Dialog{

    TextView contentView;
    TextView authorView;
    TextView sourceView;
    Button confirmButton;

    public DailyVerseDialog(Context context){
        super(context);
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.dialog_daily_verse);
        setCanceledOnTouchOutside(false);
        contentView = findViewById(R.id.daily_verse_content);
        authorView = findViewById(R.id.daily_verse_author);
        sourceView = findViewById(R.id.daily_verse_source);
        confirmButton = findViewById(R.id.daily_verse_confirm);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        NetPoem verse = AppManager.AppIO.GetDailyVerse();
        contentView.setText(verse.verse);
        authorView.setText(verse.author);
        sourceView.setText(verse.title);

        DisplayMetrics metric = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metric);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = metric.widthPixels;
        getWindow().setAttributes(params);
    }
}
