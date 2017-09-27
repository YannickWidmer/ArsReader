package ch.widmer.yannick.arstechnicafeed;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.widmer.yannick.arstechnicafeed.article.Article;
import ch.widmer.yannick.arstechnicafeed.article.ArticlePart;
import ch.widmer.yannick.arstechnicafeed.article.Figure;
import ch.widmer.yannick.arstechnicafeed.article.Text;

import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.RETRIEVEARTICLE;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.RETRIEVEENTRIES;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.AUTHFAILERROR;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.FAILED;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.NETWORKTIMEOUTERROR;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.OK;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.PARSEERROR;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.Results.SERVERERROR;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.TODISPLAY;
import static ch.widmer.yannick.arstechnicafeed.AsyncTaskResponse.WRITEARTICLE;

/**
 * Created by yanni on 01.09.2017.
 */

public class DataManager {

    private static String KEY = "add197f14f0d4251b9e5e8c94ad18894";
    // Wasn't able to freakin put thos parameters into the StringRequest and hence had to do it like this, won't work with POST!
    private static String URL = "https://newsapi.org/v1/articles?apiKey=" + KEY + "&source=ars-technica&sortBy=latest";
    private MySQLiteExtender mSQLiteExtender;
    private RootApplication mRoot;
    private static String LOG = "DATAMANAGER";


    // Whenever an asynctask finishes it sends a response via this method.
    public void dealWithResponse(AsyncTaskResponse response){
        Log.d(LOG,"dealing wiht response "+response.result.toString()+" reason "+response.reason);
        if(response.result == OK){
            switch (response.task){
                case RETRIEVEENTRIES:
                    mRoot.reactToNewEntries();
                    new AsyncWriteNewEntries().execute();
                    break;
                case RETRIEVEARTICLE:
                    if(response.reason == TODISPLAY)
                        mRoot.displayArticle(response.id);
                    else{
                        writeArticle(response.id);
                    }
                    break;
                case WRITEARTICLE: // nothing to do after that
                    break;
            }
        }else{
            mRoot.reactToError(response);
        }
    }
    /*
     mEntries is the list of entries which will be displayed. It is first populated from the local SQLdb and is then refreshed with the data obtained from the url request
     When new entries are obtained they are first stored in newEntries until they are in saved the db.
      */
    private List<Article> mEntries = new ArrayList<>(), mNewEntries = new ArrayList<>();
    private Map<Long, Article> mEntryMap = new HashMap<>();

    public DataManager(RootApplication context) {
        mSQLiteExtender = new MySQLiteExtender(context);
        mRoot = context;
        new AsyncPreparation().execute();
    }

    public List<Article> getEntrys() {
        return mEntries;
    }


    public void retrieveEntries() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new AsyncParseEntries(response).execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handlVolleyError(error,RETRIEVEENTRIES,TODISPLAY,null);
                    }
                });
        // Add the request to the RequestQueue.
        MyVolleyRequestQueue.getInstance(mRoot).getRequestQueue().add(request);
    }

    private class AsyncParseEntries extends AsyncTask<Void, Void, AsyncTaskResponse.Results> {
        private JSONObject response;

        public AsyncParseEntries(JSONObject json) {
            response = json;
        }

        @Override
        protected AsyncTaskResponse.Results doInBackground(Void... params) {
            try {
                if (!response.getString("status").equals("ok"))
                    return FAILED;

                JSONArray articles = response.getJSONArray("articles");
                Article temp;
                int index;
                for (int i = 0; i < articles.length(); ++i) {
                    try {
                        temp = new Article(articles.getJSONObject(i));
                        Log.d(LOG, temp.toString());
                        if (!mEntries.contains(temp)) {
                            mEntries.add(temp);
                            mNewEntries.add(temp);
                        }
                    } catch (Exception e) {
                    }
                }
                return OK;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Collections.sort(mEntries);
            return null;
        }

        @Override
        protected void onPostExecute(AsyncTaskResponse.Results status) {
            dealWithResponse(new AsyncTaskResponse(RETRIEVEENTRIES,TODISPLAY,status,null));
        }
    }


    public Article getArticle(Long id) {
        if (mEntryMap.containsKey(id))
            return mEntryMap.get(id);
        return null;
    }

    public void retrieveArticle(final Long id, final int reason) {
        if (mEntryMap.get(id).saved) {
            Log.d(LOG,"article is saved");
            if (mEntryMap.get(id).paragraphs != null) {
                dealWithResponse(new AsyncTaskResponse(RETRIEVEARTICLE,reason,OK,id));
                return;
            }
            new AsyncTask<Void, Void, Long>() {
                @Override
                protected Long doInBackground(Void... params) {
                    mEntryMap.get(id).paragraphs = mSQLiteExtender.getArticleParagraphs(id);
                    return null;
                }

                @Override
                protected void onPostExecute(Long nothing){
                    dealWithResponse(new AsyncTaskResponse(RETRIEVEARTICLE,reason,OK,id));
                }
            }.execute();
            return;
        }
        StringRequest articleRequest = new StringRequest(Request.Method.GET, mEntryMap.get(id).url,
                new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        new AsyncArticleParser(id, reason, response).execute();
                    } else {
                        dealWithResponse(new AsyncTaskResponse(RETRIEVEARTICLE,reason,FAILED,null));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handlVolleyError(error,RETRIEVEARTICLE,reason,id);
                }
        });
        MyVolleyRequestQueue.getInstance(mRoot).getRequestQueue().add(articleRequest);
    }

    // I need to put Long as last class inorder to have a postexecute
    private class AsyncArticleParser extends AsyncTask<Void, Void, Long> {
        private int reason;
        private Long id;
        private String html;

        public AsyncArticleParser(Long id, int reason, String html) {
            this.html = html;
            this.id = id;
            this.reason = reason;
        }

        @Override
        protected Long doInBackground(Void... params) {
            Document doc = Jsoup.parse(html);
            ArrayList<ArticlePart> parts = new ArrayList<>();
            parts.add(mEntryMap.get(id)); // The article stands for its title
            Elements sections = doc.select("div.article-content");
            String tempText = "";
            for (Element el : sections) {
                for (Element child : el.children()) {
                    switch (child.tag().toString()) {
                        // Ignored tags in div.article-content so far "aside"
                        case "blockquote": // This contains one paragrph and we wont
                        case "p":
                        case "h1":
                        case "h2":
                        case "h3":
                            tempText +=  child.outerHtml();
                            break;
                        case "div":
                        case "figure":
                            if (!tempText.equals("")) {
                                parts.add(new Text(tempText));
                                tempText = "";
                            }

                            if (child.hasClass("video"))
                                parts.add(new Text("video, not supported yet"));
                            else {
                                for (Element element : child.select("img")) {
                                    final Figure fig = new Figure(element.attr("src"));
                                    parts.add(fig);
                                }
                            }
                    }
                }
            }
            if (!tempText.equals("")) {
                parts.add(new Text(tempText));
            }

            mEntryMap.get(id).paragraphs = parts;
            return id;
        }

        @Override
        protected void onPostExecute(final Long id) {
            dealWithResponse(new AsyncTaskResponse(RETRIEVEARTICLE,reason,OK,id));
        }
    }

    private void writeArticle(Long id){
        Article article = mEntryMap.get(id);
        for(ArticlePart part:article.paragraphs){
            if(part instanceof Figure){
                
            }
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
                // no need to sort the list since the SQL queries them ordered.
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long v) {
            mRoot.reactToNewEntries();
        }
    }

    private void handlVolleyError(VolleyError error,int task, int reason, Long id){
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            dealWithResponse(new AsyncTaskResponse(task,reason,NETWORKTIMEOUTERROR,id));
        } else if (error instanceof AuthFailureError) {
            dealWithResponse(new AsyncTaskResponse(task,reason,AUTHFAILERROR,id));
        } else if (error instanceof ServerError) {
            dealWithResponse(new AsyncTaskResponse(task,reason,SERVERERROR,id));
        } else if (error instanceof NetworkError) {
            dealWithResponse(new AsyncTaskResponse(task,reason,NETWORKTIMEOUTERROR,id));
        } else if (error instanceof ParseError) {
            dealWithResponse(new AsyncTaskResponse(task,reason,PARSEERROR,id));
        }
    }
}
