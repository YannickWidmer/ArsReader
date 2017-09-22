package ch.widmer.yannick.arstechnicafeed;

import android.app.Application;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yanni on 31.08.2017.
 */

public class RootApplication extends Application {

    private static final String LOG = "root application";
    private DataManager mManager;
    private MainActivity mActivity;

    @Override
    public void onCreate(){
        super.onCreate();
        mManager = new DataManager(this);
        mManager.retrieveEntries();
    }

    public void setActivity(MainActivity act){
        mActivity = act;
    }

    public List<ArticleEntry> getEntrys(){
        return mManager.getEntrys();
    }

    public Article getArticle(Article art){
        //TODO
        return new Article();
    }

    public void refresh(){
        mManager.retrieveEntries();
    }

    public void reactToNewEntries() {
        if(mActivity != null)
            mActivity.refresh();
    }
}
