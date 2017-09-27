package ch.widmer.yannick.arstechnicafeed.article;

import ch.widmer.yannick.arstechnicafeed.R;

/**
 * Created by yanni on 26.09.2017.
 */

public interface ArticlePart {

    Type getType();



    enum Type{
        TITLE(R.layout.list_item), TEXT(R.layout.list_paragraph), FIGURE(R.layout.list_figure);

        int layout;
        Type(int layout){
            this.layout = layout;
        }
    }
}
