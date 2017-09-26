package ch.widmer.yannick.arstechnicafeed;

import android.app.Application;
import android.graphics.Point;

import java.util.List;

import ch.widmer.yannick.arstechnicafeed.article.Article;

/**
 * Created by yanni on 31.08.2017.
 */

public class RootApplication extends Application {

    /*
    The root application is the intersection between all activities and display classes and the data side. Everytime the user
    choses an action the activity calls an according method of the rootapplication. The method in the root application will most of the time
    make use of the DataManager singleton it has, where these actions are performed asyncroneusly where possible. Then when the actionn is done
    the root application will get feedback when needed and send it to the displaying activity if appropriate.
     */

    private static final String LOG = "root application";
    private DataManager mManager;
    private MainActivity mActivity;
    private ArticleActivity mArticleActivity;

    @Override
    public void onCreate(){
        super.onCreate();
        mManager = new DataManager(this);
        mManager.retrieveEntries();
    }



    public void setMainActivity(MainActivity act,int screenSize){
        mActivity = act;
    }

    public List<Article> getEntrys(){
        return mManager.getEntrys();
    }

    public void getArticle(Long id, ArticleActivity act){
        mArticleActivity = act;
        mManager.retrieveArticle(id);
    }

    public void refresh(){
        mManager.retrieveEntries();
    }

    public void reactToNewEntries() {
        if(mActivity != null)
            mActivity.refresh();
    }

    public void displayArticle(Long id) {
        if(mArticleActivity != null){
            mArticleActivity.display(mManager.getArticle(id));
        }
    }
}
