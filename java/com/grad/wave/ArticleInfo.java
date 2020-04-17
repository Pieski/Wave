package com.grad.wave;

import android.icu.text.CaseMap;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingDeque;

public class ArticleInfo implements Serializable {
    public int ID = 0;
    public byte BlockID = 0;
    public int CreatedTimeS = 0;
    private String Title;
    private String Author = null;
    private String Uploader = null;
    private byte[] TitleBytes = new byte[256];
    private byte[] AuthorBytes = new byte[256];
    private byte[] UploaderBytes = new byte[256];
    public int Length = 0;
    public ArticleType Type;
    public short RepCount = 0;
    public int UserID = 0;


    public ArticleInfo(int id,
                       byte blockid,
                       int createdtime,
                       String title,
                       String author,
                       String uploader,
                       int length,
                       ArticleType type,
                       short repcount,
                       int userid){
        ID = id;
        BlockID = blockid;
        Type = type;
        CreatedTimeS = createdtime;
        SetTitle(title);
        SetAuthor(author);
        SetUploader(uploader);
        Length = length;
        RepCount = repcount;
        UserID = userid;
    }
    public ArticleInfo(String title,String author,int length,ArticleType type){
        ID = 0;
        BlockID = 1;
        CreatedTimeS = (int)(System.currentTimeMillis()/1000);
        SetTitle(title);
        SetAuthor(author);
        SetUploader(AppManager.UserName);
        UserID = AppManager.UserID;
        Length = length;
        Type = type;
    }

    public int SetTitle(String title) {
        byte[] titlebytes = title.getBytes(Charset.forName("utf8"));
        if(titlebytes.length > 256){
            for (int i = 0; i < 256; ++i)
                TitleBytes[i] = titlebytes[i];
            Title = new String(TitleBytes);
            return -1;
        } else{
            for (int i = 0; i < titlebytes.length; ++i)
                TitleBytes[i] = titlebytes[i];
            Title = new String(TitleBytes);
            return 1;
        }
    }

    private int SetUploader(String uploader){
        byte[] uploaderbytes = uploader.getBytes(Charset.forName("utf8"));
        if(uploaderbytes.length > 256){
            for (int i = 0; i < 256; ++i)
                UploaderBytes[i] = uploaderbytes[i];
            Uploader = new String(UploaderBytes);
            return -1;
        } else{
            for (int i = 0; i < uploaderbytes.length; ++i)
                UploaderBytes[i] = uploaderbytes[i];
            Uploader = new String(UploaderBytes);
            return 1;
        }
    }

    public int SetAuthor(String author){
        byte[] authorbytes = author.getBytes(Charset.forName("utf8"));
        if(authorbytes.length > 256){
            for (int i = 0; i < 256; ++i)
                AuthorBytes[i] = authorbytes[i];
            Author = new String(AuthorBytes);
            return -1;
        } else{
            for (int i = 0; i < authorbytes.length; ++i)
                AuthorBytes[i] = authorbytes[i];
            Author = new String(AuthorBytes);
            return 1;
        }
    }

    public String GetTitle(){
        return Title;
    }

    public String GetAuthor(){
        return Author;
    }

    public String GetUploader(){return Uploader;}

    public byte[] GetTitleBytes(){return TitleBytes;}

    public byte[] GetAuthorBytes(){return AuthorBytes;}

    public byte[] GetUploaderBytes(){return UploaderBytes;}
}
