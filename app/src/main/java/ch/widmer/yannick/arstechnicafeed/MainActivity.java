package ch.widmer.yannick.arstechnicafeed;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private ArticleListAdapter mAdapter;
    private static String LOG = "Adapter";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RootApplication)getApplicationContext()).refresh();
                Snackbar.make(view, "fetching", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView lv = (ListView) findViewById(R.id.list);

        mAdapter = new ArticleListAdapter(((RootApplication)getApplication()).getEntrys(),((RootApplication)getApplication()));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id == -1){
                    // That means the article is  not saved yet in the db, which would mean the Article was selected fast.
                    // In that case we make the user wait
                    Snackbar.make(view, "one moment please", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else {
                    Intent readArticle = new Intent(MainActivity.this, ArticleActivity.class);
                    readArticle.putExtra("id", id);
                    startActivity(readArticle);
                }
            }
        });
        lv.setAdapter(mAdapter);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        ((RootApplication)getApplicationContext()).setMainActivity(this,size.x);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public void showSnackbar(int article_saved) {
        Snackbar.make(this.findViewById(android.R.id.content), article_saved, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void refresh(){
        mAdapter.notifyDataSetChanged();
    }

}
