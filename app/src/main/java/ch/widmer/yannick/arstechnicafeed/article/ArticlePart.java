package ch.widmer.yannick.arstechnicafeed.article;

import ch.widmer.yannick.arstechnicafeed.R;

/**
 * Created by yanni on 26.09.2017.
 */

public interface ArticlePart {

    Type getType();



    enum Type{
        TITLE(R.layout.list_item), PARAGRAPH(R.layout.list_paragraph),
        BLOCKQUOTE(R.layout.list_blockquotes), FIGURE(R.layout.list_figure),
        H1(R.layout.list_h1), H2(R.layout.list_h2),H3(R.layout.list_h3);

        int layout;
        Type(int layout){
            this.layout = layout;
        }
    }
}
