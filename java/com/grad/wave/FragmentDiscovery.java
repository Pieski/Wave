package com.grad.wave;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.*;

public class FragmentDiscovery extends Fragment {
    private static ArrayList<DiscoveryItem> data = new ArrayList<DiscoveryItem>();
    private View view;
    private RecyclerView rv;

    public FragmentDiscovery(@NonNull ArrayList<DiscoveryItem> dat){
        data = dat;
    }

    public FragmentDiscovery(){
        data.add(new DiscoveryItem("设", "设置", new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),SettingActivity.class);
                startActivity(intent);
            }
        }));
        data.add(new DiscoveryItem("花", "飞花令", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!AppManager.AppIO.RequestFlyingOrder()){
                    Toast.makeText(getContext(),"服务器拒绝了请求",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getContext(),FlyingOrderActivity.class);
                startActivity(intent);
            }
        }));
        data.add(new DiscoveryItem("管", "进入管理员模式", new OnClickListener() {
            @Override
            public void onClick(View v) {
                AdminDialog dialog = new AdminDialog(getContext());
                dialog.show();
            }
        }));
    }

    public static void addNewItem(DiscoveryItem item){
        data.add(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle saveInstanceState){
        //把分段页面从XML文件载入到container中
        view = inflater.inflate(R.layout.discovery_fragment,container,false);
        //获取此分段下的View
        rv = view.findViewById(R.id.discovery_recyclerview);
        rv.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        //设置列表适配器，向适配器传递data
        DiscoveryAdapter adapter = new DiscoveryAdapter(data,view.getContext());
        rv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    class DiscoveryAdapter extends RecyclerView.Adapter{

        private ArrayList<DiscoveryItem> data = new ArrayList<DiscoveryItem>();
        private Context context;

        public DiscoveryAdapter(ArrayList<DiscoveryItem> data,Context context){
            this.data = data;
            this.context = context;
        }

        @Override
        public DiscoveryViewHolder onCreateViewHolder(ViewGroup container,int pos){
            View view = LayoutInflater.from(context).inflate(R.layout.discovery_item,container,false);
            return new DiscoveryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int pos){
            if(data.get(pos).is_image){
                ((DiscoveryViewHolder) holder).maintext.setVisibility(View.INVISIBLE);
                ((DiscoveryViewHolder) holder).scndtext.setVisibility(View.INVISIBLE);
                ((DiscoveryViewHolder) holder).imageview.setVisibility(View.VISIBLE);
                ((DiscoveryViewHolder) holder).imageview.setBackgroundResource(data.get(pos).image);
            }
            ((DiscoveryViewHolder) holder).maintext.setText(data.get(pos).main);
            ((DiscoveryViewHolder) holder).scndtext.setText(data.get(pos).scnd);
            if(data.get(pos).listener != null)
                ((DiscoveryViewHolder) holder).maintext.setOnClickListener(data.get(pos).listener);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        class DiscoveryViewHolder extends RecyclerView.ViewHolder{
            private TextView maintext;
            private TextView scndtext;
            private ImageView imageview;
            public DiscoveryViewHolder(View view){
                super(view);
                maintext = view.findViewById(R.id.discovery_item_main_text);
                scndtext = view.findViewById(R.id.discovery_item_secondary_text);
                imageview = view.findViewById(R.id.discovery_item_image);
            }
        }
    }
}

class DiscoveryItem{
    public OnClickListener listener;
    public String main;
    public String scnd;
    @DrawableRes
    public int image;
    public boolean is_image = false;
    public DiscoveryItem(String main,String scnd,OnClickListener listener){
        this.main = main;
        this.scnd = scnd;
        this.listener = listener;
    }
    public DiscoveryItem(@DrawableRes int image,OnClickListener listener){
        this.image = image;
        is_image = true;
        this.listener = listener;
    }
}

class AdminDialog extends Dialog{

    private Context context;
    private Button confirm;
    private Button cancelbtn;
    private EditText editkey;

    public AdminDialog(Context context){
        super(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.dialog_enter_admin);
        setCanceledOnTouchOutside(false);
        confirm = findViewById(R.id.admin_confirm);
        cancelbtn = findViewById(R.id.admin_cancel);
        editkey = findViewById(R.id.admin_key);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editkey.getText().toString().equals(getContext().getResources().getString(R.string.admin_key))){
                    AppManager.is_admin = true;
                    Toast.makeText(context,"欢迎，管理员",Toast.LENGTH_LONG).show();

                    //在发现页面中添加服务器设置选项
                    FragmentDiscovery.addNewItem(new DiscoveryItem("服", "服务器设置", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(),ServerSettingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }));
                    cancel();
                }else
                    Toast.makeText(context,"秘钥错误，即将执行枪决",Toast.LENGTH_SHORT).show();
            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() {
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
