package com.grad.wave;

import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;
import android.os.Handler;


public class FragmentGetListThread extends Thread {

    int blockID = 0;
    int offset = 0;
    int size = 0;
    Handler doneHandler;
    public boolean is_done = false;
    ArrayList<ArticleInfo> list = new ArrayList<ArticleInfo>();

    public FragmentGetListThread(int blockID,int offset,int size,Handler doneHandler){
        this.blockID = blockID;
        this.offset = offset;
        this.size = size;
        this.doneHandler = doneHandler;
    }

    @Override
    public void run(){
        list = AppManager.AppIO.GetList(blockID,offset,size);
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list",list);
        msg.setData(bundle);
        doneHandler.sendMessage(msg);
        is_done = true;
        this.interrupt();
    }
}
