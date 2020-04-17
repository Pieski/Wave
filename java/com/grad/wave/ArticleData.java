package com.grad.wave;

import java.util.ArrayList;

public class ArticleData {
    public ArticleInfo info;
    public String content;
    public ArrayList<CommentData> comments = new ArrayList<>();

    public ArticleData(ArticleInfo info,String content,ArrayList<CommentData> comments){
        this.info = info;
        this.content = content;
        this.comments = comments;
    }

    public ArticleData(ArticleInfo i,String c){
        info = i;
        content = c;
    }

    class Reply{
        String replier;
        String content;
        int time;

        Reply(String r,String c,int t){
            replier = r;
            content = c;
            time = t;
        }
    }
}
