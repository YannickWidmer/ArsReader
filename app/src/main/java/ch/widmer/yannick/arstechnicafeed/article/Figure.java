package ch.widmer.yannick.arstechnicafeed.article;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by yanni on 26.09.2017.
 */

public class Figure implements ArticlePart {

    private Bitmap mBitmap;
    private String mUrl;
    private String mCaption;

    public Figure(String url, String caption){
        mUrl = url; mCaption = caption;
    }

    public void setBitmap(Bitmap btmp){
        mBitmap = btmp;
    }

    public Type getType(){
        return Type.FIGURE;
    }

    public String getUrl() {
        return mUrl;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getCaption(){
        return mCaption;
    }
}
