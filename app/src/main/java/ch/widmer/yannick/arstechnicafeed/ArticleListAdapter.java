package ch.widmer.yannick.arstechnicafeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import ch.widmer.yannick.arstechnicafeed.article.Article;


/**
 * Created by yanni on 03.09.2017.
 */

public class ArticleListAdapter extends BaseAdapter {

    List<Article> mList;
    RootApplication mContext;
    LayoutInflater mLayoutInflater;


    public ArticleListAdapter(List<Article> list, RootApplication context) {
        super();
        mList = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Article getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if(mList.get(position).id != null)
            return mList.get(position).id;
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
            convertView = mLayoutInflater.inflate(R.layout.list_item,null);

        final Article entry = getItem(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(entry.title);
        title.setTextAppearance(entry.read?R.style.BaseTextGreyedout:R.style.BaseText);

        ((TextView) convertView.findViewById(R.id.description)).setText(entry.description);
        ((TextView) convertView.findViewById(R.id.author)).setText(entry.author);

        ((TextView) convertView.findViewById(R.id.date)).setText(Article.showFormat.format(entry.publishedDate));

        final CheckBox box = (CheckBox)convertView.findViewById(R.id.save_box);
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.toBeSaved = box.isChecked();
                if(entry.toBeSaved)
                    mContext.saveArticle(entry.id);
            }
        });

        return convertView;
    }
}
