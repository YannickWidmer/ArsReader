package ch.widmer.yannick.arstechnicafeed;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.ExecutorDelivery;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanni on 01.09.2017.
 */

public class DataManager {

    private static String KEY = "add197f14f0d4251b9e5e8c94ad18894";
    // Wasn't able to freakin put thos parameters into the StringRequest and hence had to do it like this, won't work with POST!
    private static String URL = "https://newsapi.org/v1/articles?apiKey="+KEY+"&source=ars-technica&sortBy=latest";
    private MySQLiteExtender mSQLiteExtender;
    private RootApplication mRoot;

    /*
     mEntries is the list of entries which will be displayed. It is first populated from the local SQLdb and is then refreshed with the data obtained from the url request
     When new entries are obtained they are first stored in newEntries until they are in saved the db.
      */
    private List<ArticleEntry> mEntries = new ArrayList<>(), mNewEntries = new ArrayList<>();

    private static String LOG = "DATAMANAGER";

    public DataManager(RootApplication context){
        mSQLiteExtender = new MySQLiteExtender(context);
        mRoot = context;
        new AsyncPreparation().execute();
    }

    public List<ArticleEntry> getEntrys() {
        return mEntries;
    }

    public void retrieveEntries(){
        RequestQueue queue = Volley.newRequestQueue(mRoot);


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
        queue.add(request);
    }

    private class AsyncRetrieve extends AsyncTask<Void, Void, Integer> {
        public static final int OK =0, FAILED = -1;
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
                ArticleEntry temp;
                int index;
                for(int i=0; i <articles.length();++i){
                    try {
                        temp = new ArticleEntry(articles.getJSONObject(i));
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
                //new AsyncWritingTheList().execute();
            }
        }
    }


    private class AsyncWritingTheList extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params){
            mSQLiteExtender.push(mNewEntries);
            mNewEntries.clear();
            return null;
        }
    }

    // We only read from the database when starting, after that we maintain the list and make requests from the server and save
    // what we obtain into the db.
    private class AsyncPreparation extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            mEntries.addAll(mSQLiteExtender.getEntries());
            return null;
        }

        @Override
        protected void onPostExecute(Long v) {
            mRoot.reactToNewEntries();
        }
    }
}
