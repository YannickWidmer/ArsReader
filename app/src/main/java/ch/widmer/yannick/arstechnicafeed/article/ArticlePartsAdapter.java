package ch.widmer.yannick.arstechnicafeed.article;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import ch.widmer.yannick.arstechnicafeed.MyVolleyRequestQueue;
import ch.widmer.yannick.arstechnicafeed.R;


/**
 * Created by yanni on 03.09.2017.
 */

public class ArticlePartsAdapter extends BaseAdapter {

    Article mArticle;
    Context mContext;
    LayoutInflater mLayoutInflater;


    public ArticlePartsAdapter(Article article, Context context) {
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

        ArticlePart part = getItem(position);
        if(convertView == null) // Since we implement getViewType and Count if the view is non null it should be of the right type
            convertView = mLayoutInflater.inflate(part.getType().layout,null);


        switch(part.getType()){
            case TITLE: // almost the same as in ArticleListAdapter
                Article article = (Article)part;
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(article.title);
                title.setTextAppearance(R.style.BaseText);

                ((TextView) convertView.findViewById(R.id.description)).setText(article.description);
                if(article.author == null)
                    ((TextView) convertView.findViewById(R.id.author)).setText("-");
                else
                    ((TextView) convertView.findViewById(R.id.author)).setText(article.author);

                ((TextView) convertView.findViewById(R.id.date)).setText(Article.format.format(article.publishedDate));

                break;
            case H1:
            case H2:
            case H3:
            case PARAGRAPH:
            case BLOCKQUOTE:
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
