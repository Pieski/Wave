package com.grad.wave;

public enum ArticleType {
    ModernChinese((byte)0),
    ClassicChinese((byte)1),
    Foreign((byte)2),
    Appreciation((byte)3),
    Unknown((byte)4);

    private final byte value;

    ArticleType(byte v){
        value = v;
    }

    public byte getValue(){
        return value;
    }
}