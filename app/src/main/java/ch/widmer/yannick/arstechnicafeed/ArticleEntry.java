package ch.widmer.yannick.arstechnicafeed;

import java.util.Date;

/**
 * Created by yanni on 31.08.2017.
 */

public class ArticleEntry {
    /*
    this class represents the entrys in the article list, the same attribute can be found in the Article_Entry table
     id: datbase key, might be null
     title : The title of the entry in the list
     read: if read is true, the entry title greyed out
     toBesaved: The save box is checked and the article will be saved when possible
     saved: the entry is to be saved and has been saved in the data base
     */
    //Infos obtained from Ars APi
    public String title;
    public String author;
    public String description;
    public String url;
    public String urlToImage;
    public Date publishedDate;

    // Display and save purpose in app
    public Long id;
    public boolean read = false;
    public boolean toBeSaved = false;
    public boolean saved = false;
}
