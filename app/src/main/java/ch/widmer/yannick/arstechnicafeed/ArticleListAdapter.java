package ch.widmer.yannick.arstechnicafeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yanni on 03.09.2017.
 */

public class ArticleListAdapter extends BaseAdapter {

    List<ArticleEntry> mList;
    Context mContext;
    LayoutInflater mLayoutInflater;


    public ArticleListAdapter(List<ArticleEntry> list, Context context) {
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
    public ArticleEntry getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if(mList.get(position).id != null)
            return mList.get(position).id;
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
            convertView = mLayoutInflater.inflate(R.layout.list_item,null);

        ArticleEntry entry = getItem(position);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(entry.title);
        title.setTextAppearance(entry.read?R.style.BaseTextGreyedout:R.style.BaseText);

        ((TextView) convertView.findViewById(R.id.description)).setText(entry.description);
        if(entry.author == null)
            ((TextView) convertView.findViewById(R.id.author)).setText("-");
        else
            ((TextView) convertView.findViewById(R.id.author)).setText(entry.author);

        ((TextView) convertView.findViewById(R.id.date)).setText(ArticleEntry.format.format(entry.publishedDate));


        return convertView;
    }
}
