package ch.widmer.yannick.arstechnicafeed.article;

/**
 * Created by yanni on 26.09.2017.
 */

public class Text implements ArticlePart{
    private String mText;
    private ArticlePart.Type mType;

    public Text(ArticlePart.Type type, String text){
        mText = text;
        mType = type;
    }

    @Override
    public Type getType(){
        return mType;
    }

    public String getText(){
        return mText;
    }
}
