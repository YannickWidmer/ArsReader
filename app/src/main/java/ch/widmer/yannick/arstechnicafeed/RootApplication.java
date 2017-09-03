package ch.widmer.yannick.arstechnicafeed;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanni on 31.08.2017.
 */

public class RootApplication extends Application {

    private static final String LOG = "root application";

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public List<ArticleEntry> getEntrys(){
        // TODO replace dummy
        ArrayList<ArticleEntry> res = new ArrayList<>();

        ArticleEntry one = new ArticleEntry();
        one.title = "Not read";

        ArticleEntry two = new ArticleEntry();
        two.title = "read not saved";
        two.read = true;

        ArticleEntry three = new ArticleEntry();
        three.title = "read and to be saved";
        two.read = true;
        two.toBeSaved = true;


        res.add(one);
        res.add(two);
        res.add(three);
        return res;
    }

    public Article getArticle(Article art){
        //TODO
        return new Article();
    }
}
