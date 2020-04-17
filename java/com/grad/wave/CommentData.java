package com.grad.wave;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentData implements Parcelable {
    public String comment;
    public String maker;
    public CommentData(String maker,String comment){
        this.comment = comment;
        this.maker = maker;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(maker);
        dest.writeString(comment);
    }

    public static final Parcelable.Creator<CommentData> CREATOR = new Parcelable.Creator<CommentData>(){
        @Override
        public CommentData createFromParcel(Parcel fr){
            return new CommentData(fr.readString(),fr.readString());
        }

        @Override
        public CommentData[] newArray(int size){
            return new CommentData[size];
        }
    };
}
