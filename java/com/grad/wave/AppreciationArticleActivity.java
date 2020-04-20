package com.grad.wave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;

import static android.app.PendingIntent.getActivity;

public class AppreciationArticleActivity extends AppCompatActivity {

    private ArticleData data;
    private TextView title;
    private TextView author;
    private TextView content;
    private TextView infoview;
    private RecyclerView repliesview;
    private Button commentbutton;
    private Button deletebutton;

    static Handler dlCompleteHandler;

    boolean swiping_up = false;
    int lastvisible = 0;
    int commentoffset = 0;
    int commentsize = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_appreciation_article);

            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("data");
            final ArticleInfo info = (ArticleInfo) bundle.get("Artini");
            data = new ArticleData(info,"加载中",null);
            if (data == null) {
                finish();
            }

            if(AppManager.is_traChinese)
                this.setTheme(R.style.TraTheme);
            else
                this.setTheme(R.style.SimTheme);

            //data.comments = AppManager.AppIO.GetComments(info.ID, info.BlockID);

            title = findViewById(R.id.appreciation_title);
            author = findViewById(R.id.appreciation_author);
            content = findViewById(R.id.appreciation_content);
            infoview = findViewById(R.id.appreciation_info);
            repliesview = findViewById(R.id.appreciation_comment);
            commentbutton = findViewById(R.id.appreciation_make_comment);
            deletebutton = findViewById(R.id.appreciation_delete);
            title.setText(info.GetTitle());
            author.setText(info.GetAuthor());
            infoview.setText(info.GetUploader() + " " +
                    new Timestamp((long) info.CreatedTimeS * (long) 1000).toString());

            //下载完毕后触发
            dlCompleteHandler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Bundle dat = msg.getData();
                    data.comments = ((ArrayList<CommentData>)dat.get("comments"));
                    data.content = (String)dat.get("content");
                    content.setText(data.content);

                    CommentAdapter adapter = new CommentAdapter(data.comments);
                    repliesview.setLayoutManager(new LinearLayoutManager(repliesview.getContext()));
                    repliesview.setAdapter(adapter);

                    commentbutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommentDialog dialog = new CommentDialog(AppreciationArticleActivity.this, info.ID, info.BlockID);
                            dialog.show();
                        }
                    });

                    if (AppManager.is_admin || data.info.UserID == AppManager.UserID) {
                        deletebutton.setEnabled(true);
                        deletebutton.setVisibility(View.VISIBLE);
                        deletebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppManager.AppIO.DeleteArticle(data.info);
                                finish();
                            }
                        });
                    }
                }
            };


            repliesview.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    try{
                        if (newState == RecyclerView.SCROLL_STATE_IDLE
                                && lastvisible == data.comments.size() - 1 && swiping_up){
                            commentoffset += commentsize;
                            ArrayList<CommentData> append = AppManager.AppIO.GetComments(data.info.ID,data.info.BlockID,commentoffset,commentsize);
                            for(int i=0;i<append.size();++i)
                                data.comments.add(append.get(i));
                            CommentAdapter ra = (CommentAdapter)repliesview.getAdapter();
                            ra.data = data.comments;
                            ra.notifyDataSetChanged();
                        }
                    }catch(Exception ex){
                        //Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(dy > 0)
                        swiping_up = true;
                    else
                        swiping_up = false;
                    lastvisible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                }
            });

            //开始异步读取
            DownloadDataThread thread = new DownloadDataThread(info,this);
            thread.start();

        }catch(Exception ex){
            Toast.makeText(this, "读取错误"+ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class CommentAdapter extends RecyclerView.Adapter {
        private ArrayList<CommentData> data;

        public CommentAdapter(ArrayList<CommentData> dat){
            data = dat;
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup container, int pos){
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.comment_item,container,false);
            return new ReplyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int pos){
            ((ReplyViewHolder)holder).replierview.setText(data.get(pos).maker);
            ((ReplyViewHolder)holder).contentview.setText(data.get(pos).comment);
        }
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder{
        TextView replierview;
        TextView contentview;
        public ReplyViewHolder(View view){
            super(view);
            replierview = view.findViewById(R.id.comment_replier);
            contentview = view.findViewById(R.id.comment_content);
            contentview.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
    }

    class DownloadDataThread extends Thread{
        public ArrayList<CommentData> comments = new ArrayList<>();
        public String content = null;
        public ArticleInfo info;
        public boolean is_got = false;
        public AppreciationArticleActivity parent;

        public DownloadDataThread(ArticleInfo info,AppreciationArticleActivity caller){
            this.info = info;
            parent = caller;
        }

        public void run(){
            ArticleData data = AppManager.AppIO.GetArticleData(info);
            content = data.content;
            comments = AppManager.AppIO.GetComments(info.ID,info.BlockID,commentoffset,commentsize);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("comments",comments);
            bundle.putString("content",content);
            msg.setData(bundle);
            dlCompleteHandler.sendMessage(msg);
            AppreciationArticleActivity.DownloadDataThread.this.interrupt();
        }
    }
}
