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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import ch.widmer.yannick.arstechnicafeed.MyVolleyRequestQueue;
import ch.widmer.yannick.arstechnicafeed.R;
import ch.widmer.yannick.arstechnicafeed.RootApplication;

import static android.view.View.GONE;


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
                ((CheckBox) convertView.findViewById(R.id.save_box)).setChecked(article.toBeSaved);
                break;
            case TEXT:
                ((TextView)convertView).setText(Html.fromHtml(((Text) part).getText()));
                break;
            case FIGURE:
                // Snce NetworkImageView doesn't work with bitmaps we need two imageviews and put ones visibility
                // to GONE and use the other depending on the case
                Figure fig = (Figure) part;
                ((TextView)convertView.findViewById(R.id.caption)).setText(Html.fromHtml(fig.getCaption()));
                NetworkImageView img_retrieve =(NetworkImageView)convertView.findViewById(R.id.image_retrieve);
                ImageView img_local = (ImageView)convertView.findViewById(R.id.image_local);
                if(fig.getBitmap() == null) {
                    img_local.setVisibility(GONE);
                    ImageLoader imageLoader = MyVolleyRequestQueue.getInstance(mContext).getImageLoader();
                    imageLoader.get(((Figure) part).getUrl(), ImageLoader.getImageListener(img_retrieve, R.mipmap.ic_launcher, android.R.drawable.ic_dialog_alert));
                    img_retrieve.setImageUrl(((Figure) part).getUrl(), imageLoader);
                }else{
                    img_retrieve.setVisibility(GONE);
                    img_local.setScaleType(ImageView.ScaleType.FIT_XY);
                    img_local.setImageBitmap(fig.getBitmap());
                }
                break;
        }

        return convertView;
    }
}
