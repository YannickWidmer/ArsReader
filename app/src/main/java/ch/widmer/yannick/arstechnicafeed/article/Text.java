package ch.widmer.yannick.arstechnicafeed.article;

/**
 * Created by yanni on 26.09.2017.
 */

public class Text implements ArticlePart{
    private String mText;

    public Text( String text){
        mText = text;
    }

    @Override
    public Type getType(){
        return Type.TEXT;
    }

    public String getText(){
        return mText;
    }
}
