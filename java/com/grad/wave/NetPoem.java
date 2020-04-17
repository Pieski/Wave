package com.grad.wave;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class NetPoem implements Parcelable {
    public String verse;
    public String title;
    public String author;
    public NetPoem(String verse,String title,String author){
        this.verse = verse;
        this.title = title;
        this.author = author;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(verse);
        dest.writeString(title);
        dest.writeString(author);
    }

    public static final Parcelable.Creator<NetPoem> CREATOR = new Parcelable.Creator<NetPoem>(){
        @Override
        public NetPoem createFromParcel(Parcel fr){
            return new NetPoem(fr.readString(),fr.readString(),fr.readString());
        }

        @Override
        public NetPoem[] newArray(int size){
            return new NetPoem[size];
        }
    };
}
