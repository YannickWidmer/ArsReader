package ch.widmer.yannick.arstechnicafeed;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import ch.widmer.yannick.arstechnicafeed.article.Article;
import ch.widmer.yannick.arstechnicafeed.article.ArticlePartsAdapter;

public class ArticleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ((RootApplication)getApplicationContext()).getArticle(getIntent().getLongExtra("id",-1), this);
    }

    public void display(Article parts) {
        ((ListView)findViewById(R.id.list)).setAdapter(new ArticlePartsAdapter(parts, (RootApplication) getApplicationContext()));
        findViewById(R.id.progress_loader).setVisibility(View.GONE);
        parts.read = true;
    }
}
