package ch.widmer.yannick.arstechnicafeed.article;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yanni on 31.08.2017.
 */

public class Article implements ArticlePart {
    /*
    this class represents the entrys in the article list, the same attribute can be found in the Article_Entry table
     id: datbase key, might be null
     title : The title of the entry in the list
     read: if read is true, the entry title greyed out
     toBesaved: The save box is checked and the article will be saved when possible
     saved: the entry is to be saved and has been saved in the data base
     */
    //Infos obtained from Ars APi
    public String title, author, description, url, urlToImage;
    public Date publishedDate;

    // Display and save purpose in app
    public Long id;
    public boolean read = false;
    public boolean toBeSaved = false;
    public boolean saved = false;

    public ArrayList<ArticlePart> paragraphs;


    public Article(Long id, String title, String author, String description, String url, String urlToImage, Date publishedDate,
                   boolean read, boolean tobeSaved) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedDate = publishedDate;
        this.read = read;
        this.toBeSaved = tobeSaved;
    }

    @Override
    public Type getType(){
        return Type.TITLE;
    }

    public Article(){
        // To be used in MySQLExtender
    }

    // For the date obtained from the url request we need following formatter
    // the dates obtained look like e.g. 2017-09-21T21:57:41Z , 2017-09-21T22:20:58Z
    public static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);


    // Constructing an Article from a JSONObject obtained from the urlrequest to "https://newsapi.org/v1/articles?apiKey="+KEY+"&source=ars-technica&sortBy=latest";
    public Article(JSONObject json) throws Exception{
        try {
            title = json.getString("title");
            author = json.getString("author");
            description = json.getString("description");
            url = json.getString("url");
            urlToImage = json.getString("urlToImage");
            publishedDate = format.parse(json.getString("publishedAt"));
        }catch(Exception e){
            throw e;
        }
    }


    @Override
    public boolean equals(Object other){
        try {
            Article otherEntry = (Article) other;
            return title.equals(otherEntry.title);
        }catch(Exception e){
            return false;
        }
    }

    public boolean isPublishedAfter(Article other){
        return publishedDate.after(other.publishedDate);
    }

    @Override
    public String toString(){
        return author + "   "+format.format(publishedDate)+ " "+ url +" "+urlToImage;
    }
}
