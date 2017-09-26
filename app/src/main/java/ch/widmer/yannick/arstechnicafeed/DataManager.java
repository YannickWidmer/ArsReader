package ch.widmer.yannick.arstechnicafeed;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.widmer.yannick.arstechnicafeed.article.Article;
import ch.widmer.yannick.arstechnicafeed.article.ArticlePart;
import ch.widmer.yannick.arstechnicafeed.article.Figure;
import ch.widmer.yannick.arstechnicafeed.article.Text;

/**
 * Created by yanni on 01.09.2017.
 */

public class DataManager {

    private static String KEY = "add197f14f0d4251b9e5e8c94ad18894";
    // Wasn't able to freakin put thos parameters into the StringRequest and hence had to do it like this, won't work with POST!
    private static String URL = "https://newsapi.org/v1/articles?apiKey="+KEY+"&source=ars-technica&sortBy=latest";
    private MySQLiteExtender mSQLiteExtender;
    private RootApplication mRoot;

    // results of asynctasks
    public static final int OK =0, FAILED = -1;
    /*
     mEntries is the list of entries which will be displayed. It is first populated from the local SQLdb and is then refreshed with the data obtained from the url request
     When new entries are obtained they are first stored in newEntries until they are in saved the db.
      */
    private List<Article> mEntries = new ArrayList<>(), mNewEntries = new ArrayList<>();
    private Map<Long,Article> mEntryMap = new HashMap<>();

    private static String LOG = "DATAMANAGER";

    public DataManager(RootApplication context){
        mSQLiteExtender = new MySQLiteExtender(context);
        mRoot = context;
        new AsyncPreparation().execute();
    }

    public List<Article> getEntrys() {
        return mEntries;
    }

    public void retrieveEntries(){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,URL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new AsyncRetrieve(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG,"didnt work");
                    }
                });

        // Add the request to the RequestQueue.
        MyVolleyRequestQueue.getInstance(mRoot).getRequestQueue().add(request);
    }

    public Article getArticle(Long id){
        if(mEntryMap.containsKey(id))
            return mEntryMap.get(id);
        return null;
    }

    public void retrieveArticle(final Long id) {
        if(mSQLiteExtender.hasArticle(id)){
            //TODO
        }else{
            StringRequest articleRequest = new StringRequest(Request.Method.GET,mEntryMap.get(id).url, new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    if(response != null){
                        new AsyncArticleParser(id,response).execute();
                    }else{
                        Log.d(LOG,"response was null");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG", error.toString());
                }
            });
            MyVolleyRequestQueue.getInstance(mRoot).getRequestQueue().add(articleRequest);
        }
    }

    private class AsyncRetrieve extends AsyncTask<Void, Void, Integer> {
        private JSONObject response;

        public AsyncRetrieve(JSONObject json){
            response = json;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if(!response.getString("status").equals("ok"))
                    return FAILED;

                JSONArray articles = response.getJSONArray("articles");
                Article temp;
                int index;
                for(int i=0; i <articles.length();++i){
                    try {
                        temp = new Article(articles.getJSONObject(i));
                        Log.d(LOG,temp.toString());
                        if(!mEntries.contains(temp)) {
                            mEntries.add(temp);
                            mNewEntries.add(temp);
                        }
                    }catch(Exception e){}
                }
                return OK;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer status) {
            Log.d(LOG, "onPostExecute");
            Log.d(LOG,"status " +status);
            if(status == OK) {
                mRoot.reactToNewEntries();
                new AsyncWriteNewEntries().execute();
            }
        }
    }

    private class AsyncArticleParser extends AsyncTask<Void,Void,Long>{

        private Long id;
        private String html;
        public AsyncArticleParser(Long id, String html){
            this.html = html;
            this.id = id;
        }

        @Override
        protected Long doInBackground(Void... params) {
            Document doc = Jsoup.parse(html);
            ArrayList<ArticlePart> parts = new ArrayList<>();
            parts.add(mEntryMap.get(id)); // The article stands for its title
            Elements sections = doc.select("div.article-content");

            for(Element el:sections){
                for(Element child:el.children()){
                    switch(child.tag().toString()){
                        // Ignored tags in div.article-content so far "aside"
                        case "blockquote": // This contains one paragrph and we wont
                            parts.add(new Text(ArticlePart.Type.BLOCKQUOTE,child.child(0).html()));
                            break;
                        case "p":
                            parts.add(new Text(ArticlePart.Type.PARAGRAPH,child.html()));
                            break;
                        case "h1":
                            parts.add(new Text(ArticlePart.Type.H1,child.html()));
                            break;
                        case "h2":
                            parts.add(new Text(ArticlePart.Type.H2,child.html()));
                            break;
                        case "h3":
                            parts.add(new Text(ArticlePart.Type.H3,child.html()));
                            break;
                        case "figure":
                            final Figure fig = new Figure(child.select("img").first().attr("src"));
                            parts.add(fig);
                    /*ImageRequest request = new ImageRequest(fig.getUrl(),
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap bitmap) {
                                            fig.setBitmap(bitmap);
                                        }
                                    }, mScreenWidth, mScreenWidth, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565,
                                    new Response.ErrorListener() {
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    });
                            MyVolleyRequestQueue.getInstance(mRoot).getRequestQueue().add(request)*/;
                    }
                }
            }
            mEntryMap.get(id).paragraphs  = parts;
            return id;
        }

        @Override
        protected void onPostExecute(Long id){
            mRoot.displayArticle(id);
        }
    }


    private class AsyncWriteNewEntries extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params){
            mSQLiteExtender.push(mNewEntries);
            for(Article entry:mNewEntries)
                mEntryMap.put(entry.id,entry);
            mNewEntries.clear();
            return null;
        }
    }

    // We only read from the database when starting, after that we maintain the list and make requests from the server and save
    // what we obtain into the db.
    private class AsyncPreparation extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            for(Article entry:mSQLiteExtender.getEntries()){
                mEntries.add(entry);
                mEntryMap.put(entry.id,entry);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long v) {
            mRoot.reactToNewEntries();
        }
    }
}
