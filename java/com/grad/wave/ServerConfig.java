package com.grad.wave;

import java.sql.Ref;

public class ServerConfig {
    int MaxClient = 0;
    int ConnectedClient = 0;
    boolean RegPermitted = true;
    boolean UploadPermitted = true;
    boolean CommentPermitted = true;
    boolean RefreshPermitted = true;
    boolean LoginPermitted = true;
    boolean FlyingOrderPermitted = true;
    boolean checkpass = false;

    public ServerConfig(){}

    public ServerConfig(int maxc,int conc,boolean reg,boolean up,boolean com,boolean ref,boolean log,boolean fo){
        MaxClient = maxc;
        ConnectedClient = conc;
        RegPermitted = reg;
        UploadPermitted = up;
        CommentPermitted = com;
        RefreshPermitted = ref;
        LoginPermitted = log;
        FlyingOrderPermitted = fo;
    }

    public int Check(){
        int check = MaxClient;
        if(RegPermitted)    check+=1;
        if(UploadPermitted) check+=1;
        if(CommentPermitted) check+=1;
        if(RefreshPermitted) check+=1;
        if(LoginPermitted)  check+=1;
        if(FlyingOrderPermitted) check+=1;
        return check;
    }
}
