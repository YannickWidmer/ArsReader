package ch.widmer.yannick.arstechnicafeed;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

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
    }

    public Activity getActivity(){
        if(mArticleActivity != null)
            return mArticleActivity;
        return mActivity;
    }


    public void setMainActivity(MainActivity act,int screenSize){
        mManager.setScreenWidth(screenSize);
        mActivity = act;
    }

    public List<Article> getEntrys(){
        return mManager.getEntrys();
    }

    public void getArticle(Long id, ArticleActivity act){
        mArticleActivity = act;
        mManager.retrieveArticle(id,AsyncTaskResponse.TODISPLAY);
    }

    public void refresh(){
        mManager.retrieveEntries();
    }

    public void reactToNewEntries() {
        if(mActivity != null)
            mActivity.refresh();
    }

    public void displayArticle(Long id) {
        Log.d(LOG,"display article");
        if(mArticleActivity != null){
            mArticleActivity.display(mManager.getArticle(id));
        }
    }

    public void saveArticle(Long id) {
        mManager.retrieveArticle(id,AsyncTaskResponse.TOSAVE);
    }

    public void reactToError(AsyncTaskResponse response) {
        if(mArticleActivity !=null){
            mArticleActivity.finish();
        }
        if(mActivity != null){
            mActivity.showSnackbar(response.result.stringId);
        }
        if(mActivity != null)
            mActivity.refresh();
    }

    public void displayToastArticleSaved() {
        if(mArticleActivity !=null){
            mArticleActivity.showSnackbar(R.string.article_saved);
        }else if(mActivity != null){
            mActivity.showSnackbar(R.string.article_saved);
        }

    }
}
