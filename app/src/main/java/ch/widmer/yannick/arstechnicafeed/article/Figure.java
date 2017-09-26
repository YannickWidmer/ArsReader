package ch.widmer.yannick.arstechnicafeed.article;

import android.graphics.Bitmap;

/**
 * Created by yanni on 26.09.2017.
 */

public class Figure implements ArticlePart {

    private Bitmap mBitmap;
    private String mUrl;

    public Figure(String url){
        mUrl = url;
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
}
