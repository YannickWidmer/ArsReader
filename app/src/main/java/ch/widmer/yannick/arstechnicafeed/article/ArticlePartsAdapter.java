package ch.widmer.yannick.arstechnicafeed.article;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import ch.widmer.yannick.arstechnicafeed.MyVolleyRequestQueue;
import ch.widmer.yannick.arstechnicafeed.R;
import ch.widmer.yannick.arstechnicafeed.RootApplication;


/**
 * Created by yanni on 03.09.2017.
 */

public class ArticlePartsAdapter extends BaseAdapter {

    Article mArticle;
    RootApplication mContext;
    LayoutInflater mLayoutInflater;


    public ArticlePartsAdapter(Article article, RootApplication context) {
        super();
        mArticle = article;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position){
        return mArticle.paragraphs.get(position).getType().ordinal();
    }

    @Override
    public int getViewTypeCount(){
        return ArticlePart.Type.values().length;
    }

    @Override
    public int getCount() {
        return mArticle.paragraphs.size();
    }

    @Override
    public ArticlePart getItem(int position) {
        return mArticle.paragraphs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ArticlePart part = getItem(position);
        if(convertView == null)  // Since we implement getViewType and Count if the view is non null it should be of the right type
            convertView = mLayoutInflater.inflate(part.getType().layout, null);



        switch(part.getType()){
            case TITLE: // almost the same as in ArticleListAdapter
                Article article = (Article)part;
                ((TextView) convertView.findViewById(R.id.title)).setText(article.title);
                ((TextView) convertView.findViewById(R.id.description)).setText(article.description);
                ((TextView) convertView.findViewById(R.id.date)).setText(Article.showFormat.format(article.publishedDate));
                ((TextView) convertView.findViewById(R.id.author)).setText(article.author);
                break;
            case TEXT:
                ((TextView)convertView).setText(Html.fromHtml(((Text) part).getText()));
                break;
            case FIGURE:
                NetworkImageView view =(NetworkImageView)convertView;
                ImageLoader imageLoader = MyVolleyRequestQueue.getInstance(mContext).getImageLoader();
                imageLoader.get(((Figure)part).getUrl(), ImageLoader.getImageListener(view,R.mipmap.ic_launcher, android.R.drawable.ic_dialog_alert));
                view.setImageUrl(((Figure)part).getUrl(),imageLoader);
                break;
        }

        return convertView;
    }
}
