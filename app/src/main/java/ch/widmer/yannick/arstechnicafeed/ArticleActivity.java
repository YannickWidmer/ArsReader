package ch.widmer.yannick.arstechnicafeed;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
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

    public void showSnackbar(int article_saved) {
        Snackbar.make(this.findViewById(android.R.id.content), article_saved, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void display(Article article) {
        ((ListView)findViewById(R.id.list)).setAdapter(new ArticlePartsAdapter(article, (RootApplication) getApplicationContext()));
        findViewById(R.id.progress_loader).setVisibility(View.GONE);
    }
}
