package ch.widmer.yannick.arstechnicafeed;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private ArticleListAdapter mAdapter;
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

        mAdapter = new ArticleListAdapter(((RootApplication)getApplication()).getEntrys(),this);
        lv.setAdapter(mAdapter);
        ((RootApplication)getApplicationContext()).setActivity(this);
    }


    public void refresh(){
        mAdapter.notifyDataSetChanged();
        Snackbar.make(this.findViewById(android.R.id.content), "refresh successfull", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
