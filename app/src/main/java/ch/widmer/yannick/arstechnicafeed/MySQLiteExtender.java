package ch.widmer.yannick.arstechnicafeed;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.widmer.yannick.arstechnicafeed.article.Article;

/**
 * Created by yanni on 01.09.2017.
 */

public class MySQLiteExtender extends SQLiteOpenHelper {

    private static final String LOG="SQLLite";

    // All Static variables related to db
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ARS";

    // table names
    private static final String
            TABLE_LISTENTRIES = "list_entries",
            TABLE_ARTICLEPARAGRAPHS = "articles";

    //Table columns for all tables
    private static final String KEY_ID="id",  KEY_TITLE = "title", KEY_TEXT = "text",
            KEY_READ = "read", KEY_TOBESAVED = "to_be_saved", KEY_ORDER = "order_of_paragraph",
            KEY_AUTHOR = "author", KEY_DESCRIPTION = "description",
            KEY_URL = "url", KEY_URLTOIMAGE = "url_to_image",
            KEY_DATE = "date";


    public MySQLiteExtender(RootApplication context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LISTENTRYS  = "CREATE TABLE " + TABLE_LISTENTRIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_READ + " BOOLEAN,"
                + KEY_TOBESAVED + " BOOLEAN,"
                + KEY_AUTHOR + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_URL + " TEXT,"
                + KEY_URLTOIMAGE + " TEXT,"
                + KEY_DATE + " INT"
                + ");";

        String CREATE_ARTICLES_PARAGRAPHS = "CREATE TABLE " + TABLE_ARTICLEPARAGRAPHS + "("
                + KEY_ID + " INTEGER,"
                + KEY_ORDER + " INTEGER,"
                + " FOREIGN KEY ("+KEY_ID+") REFERENCES "+ TABLE_LISTENTRIES +" ("+KEY_ID+") ON DELETE CASCADE);";

        db.execSQL(CREATE_LISTENTRYS);
        db.execSQL(CREATE_ARTICLES_PARAGRAPHS);
    }

    // Upgrading database, we don't have any version so there is no reason to do anything smart in this method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLEPARAGRAPHS);
        // Create tables again
        onCreate(db);
    }

    ////////////////// List ENTRIES /////////////////////////////////////////

    public synchronized  void push(List<Article> entries){
        ContentValues c;
        for(Article entry:entries){
            c = new ContentValues();
            c.put(KEY_AUTHOR, entry.author);
            c.put(KEY_DATE, entry.publishedDate.getTime());
            c.put(KEY_DESCRIPTION, entry.description);
            c.put(KEY_URL, entry.url);
            c.put(KEY_URLTOIMAGE, entry.urlToImage);
            c.put(KEY_READ,entry.read);
            c.put(KEY_TOBESAVED,entry.toBeSaved);
            c.put(KEY_TITLE,entry.title);
            entry.id = pushData(TABLE_LISTENTRIES,entry.id,c);
        }
    }

    public synchronized List<Article> getEntries(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTENTRIES, new String[]{KEY_ID, KEY_AUTHOR, KEY_DATE, KEY_DESCRIPTION,
                        KEY_URL, KEY_URLTOIMAGE, KEY_READ, KEY_TOBESAVED,KEY_TITLE},
                null,null, null, null, null, null);

        Article entry;
        ArrayList<Article> res = new ArrayList<>();

        if(cursor.moveToFirst()){
            do{
                // Article(Long id,String title, String author, String description,
                // String url, String urlToImage, Date publishedDate)
                entry = new Article(getLong(cursor,KEY_ID),
                        getString(cursor,KEY_TITLE),
                        getString(cursor,KEY_AUTHOR),
                        getString(cursor,KEY_DESCRIPTION),
                        getString(cursor,KEY_URL),
                        getString(cursor,KEY_URLTOIMAGE),
                        new Date(getLong(cursor, KEY_DATE)),
                        getBool(cursor,KEY_READ),
                        getBool(cursor,KEY_TOBESAVED));
                res.add(entry);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return res;
    }

    /////////////////  PARAGRAPHS   /////////////////////////////////////////

    public synchronized void push(Article article){
        // TODO
    }

    public boolean hasArticle(Long id){
        // TODO
        return false;
    }

    public synchronized Article getArticle(Long id){
        return new Article();
    }

    //////////////7// Help methods   ///////////////////////////////////////////

    private int get(Cursor c, String key){
        return c.getInt(c.getColumnIndexOrThrow(key));
    }

    private boolean getBool(Cursor c, String key) {
        return c.getInt(c.getColumnIndex(key))==1;
    }

    private long getLong(Cursor c, String key) {
        return c.getLong(c.getColumnIndexOrThrow(key));
    }

    private String getString(Cursor c,String key){
        return c.getString(c.getColumnIndexOrThrow(key));
    }

    private long pushData(String table,Long id, ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        if(id == null){
            Log.d(LOG,"inserting ");
            id=db.insert(table, null,values);
        }else{
            Log.d(LOG,"updating ");
            db.update(table, values, KEY_ID+" = ?", new String[]{String.valueOf(id)});
        }
        db.close();
        return id;
    }

    private void deleteEntry(String table,Long id){
        if(id != null)
            getWritableDatabase().delete(table,KEY_ID + " = ?",
                    new String[]{String.valueOf(id)});
    }
}
