package ch.widmer.yannick.arstechnicafeed;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            TABLE_ARTICLES = "articles";

    //Table columns for all tables
    private static final String KEY_ID="id",  KEY_TITLE = "title", KEY_TEXT = "text",
            KEY_READ = "read", KEY_TOBESAVED = "to_be_saved",
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
        db.execSQL(CREATE_LISTENTRYS);
    }

    // Upgrading database, we don't have any version so there is no reason to do anything smart in this method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
        // Create tables again
        onCreate(db);
    }

    ////////////////// List ENTRIES /////////////////////////////////////////

    public synchronized  void push(List<ArticleEntry> entries){
        ContentValues c;
        for(ArticleEntry entry:entries){
            c = new ContentValues();
            c.put(KEY_AUTHOR, entry.author);
            c.put(KEY_DATE, entry.publishedDate.getTime());
            c.put(KEY_DESCRIPTION, entry.description);
            c.put(KEY_URL, entry.url);
            c.put(KEY_URLTOIMAGE, entry.urlToImage);
            c.put(KEY_READ,entry.read);
            c.put(KEY_TOBESAVED,entry.toBeSaved);
            c.put(KEY_TITLE,entry.title);
            pushData(TABLE_LISTENTRIES,entry.id,c);
        }
    }

    public synchronized List<ArticleEntry> getEntries(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTENTRIES, new String[]{KEY_ID, KEY_AUTHOR, KEY_DATE, KEY_DESCRIPTION,
                        KEY_URL, KEY_URLTOIMAGE, KEY_READ, KEY_TOBESAVED,KEY_TITLE},
                null,null, null, null, null, null);

        ArticleEntry entry;
        ArrayList<ArticleEntry> res = new ArrayList<>();

        if(cursor.moveToFirst()){
            do{
                entry = new ArticleEntry();
                entry.id = getLong(cursor,KEY_ID);
                entry.author = getString(cursor,KEY_AUTHOR);
                entry.publishedDate = new Date(getLong(cursor, KEY_DATE));
                entry.description = getString(cursor,KEY_DESCRIPTION);
                entry.url = getString(cursor,KEY_URL);
                entry.urlToImage = getString(cursor,KEY_URLTOIMAGE);
                entry.read = getBool(cursor,KEY_READ);
                entry.toBeSaved = getBool(cursor,KEY_TOBESAVED);
                entry.title = getString(cursor,KEY_TITLE);
                res.add(entry);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return res;
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
