package com.grad.wave;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TimeZone;

public class ArticleInfoAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<ArticleInfo> data;

    public ArticleInfoAdapter(ArrayList<ArticleInfo> d,Context c){
        context = c;
        data = d;
    }

    public void UpdateData(ArrayList<ArticleInfo> dat){
        data = dat;
    }

    @Override
    public int getItemCount()   {
        if(data != null)
            return data.size();
        return 0;
    }

    @Override
    public ArticleInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_info_item_view,parent,false);
        return new ArticleInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int pos){
        ((ArticleInfoViewHolder) holder).authorview.setText(data.get(pos).GetUploader());
        ((ArticleInfoViewHolder) holder).titleview.setText(data.get(pos).GetTitle());
        ((ArticleInfoViewHolder) holder).createdtimeview.setText(
                new Timestamp((long)data.get(pos).CreatedTimeS * (long)1000).toString());
        switch(data.get(pos).Type){
            case ModernChinese:
                ((ArticleInfoViewHolder) holder).typeview.setText(R.string.modern_chinese);
                break;
            case ClassicChinese:
                ((ArticleInfoViewHolder) holder).typeview.setText(R.string.classic_chinese);
                break;
            case Foreign:
                ((ArticleInfoViewHolder) holder).typeview.setText(R.string.foreign);
                break;
            case Appreciation:
                ((ArticleInfoViewHolder) holder).typeview.setText(R.string.appreciation);
                break;
        }

        ((ArticleInfoViewHolder) holder).background.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println(event.getAction());
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    ((ArticleInfoViewHolder) holder).background.setBackgroundColor(ContextCompat.getColor(context,R.color.colorGrey));
                    return false;
                }
                if(event.getAction() == MotionEvent.ACTION_CANCEL){
                    ((ArticleInfoViewHolder) holder).background.setBackground(ContextCompat.getDrawable(context,R.drawable.articleinfo_item_background));
                    return false;
                }
                return false;
            }
        });


        ((ArticleInfoViewHolder) holder).background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"加载中",Toast.LENGTH_SHORT).show();
                ((ArticleInfoViewHolder) holder).background.setBackground(ContextCompat.getDrawable(context,R.drawable.articleinfo_item_background));
                ((ArticleInfoViewHolder) holder).background.setEnabled(false);
                if(data.get(pos).Type == ArticleType.Appreciation){
                    Intent intent = new Intent(context,AppreciationArticleActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Artini",data.get(pos));
                    intent.putExtra("data",bundle);
                    context.startActivity(intent);
                    ((ArticleInfoViewHolder) holder).background.setEnabled(true);
                    return;
                }
                Intent intent = new Intent(context,PoemArticleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Artini",data.get(pos));
                intent.putExtra("data",bundle);
                context.startActivity(intent);
                ((ArticleInfoViewHolder) holder).background.setEnabled(true);
            }
        });
    }

    public class ArticleInfoViewHolder extends RecyclerView.ViewHolder{
        private TextView titleview;
        private TextView authorview;
        private TextView createdtimeview;
        private TextView typeview;
        private View background;
        public ArticleInfoViewHolder(View view){
            super(view);
            titleview = view.findViewById(R.id.item_title_view);
            authorview = view.findViewById(R.id.item_author_view);
            createdtimeview = view.findViewById(R.id.item_created_time_view);
            typeview = view.findViewById(R.id.item_type_view);
            background = view.findViewById(R.id.item_background_view);
        }
    }
}