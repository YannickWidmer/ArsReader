package ch.widmer.yannick.arstechnicafeed;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.MessagePattern;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ch.widmer.yannick.arstechnicafeed.article.Article;
import ch.widmer.yannick.arstechnicafeed.article.ArticlePart;
import ch.widmer.yannick.arstechnicafeed.article.Figure;
import ch.widmer.yannick.arstechnicafeed.article.Text;

/**
 * Created by yanni on 01.09.2017.
 */

public class MySQLiteExtender extends SQLiteOpenHelper {

    private static final String LOG="SQLLite";

    // All Static variables related to db
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "ARS";

    // table names
    private static final String
            TABLE_ARTICLES = "articles",
            TABLE_ARTICLEPARAGRAPHS = "paragraphs",
            TABLE_IMAGES = "images";


    //Table columns for all tables
    private static final String KEY_ID="id",  KEY_TITLE = "title", KEY_TEXT = "text", KEY_IMAGE ="image",
            KEY_READ = "read", KEY_TOBESAVED = "to_be_saved", KEY_ORDER = "order_of_paragraph",
            KEY_AUTHOR = "author", KEY_DESCRIPTION = "description",
            KEY_URL = "url", KEY_URLTOIMAGE = "url_to_image",
            KEY_DATE = "date";


    public MySQLiteExtender(RootApplication context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LISTENTRYS  = "CREATE TABLE " + TABLE_ARTICLES + "("
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
                + KEY_TEXT + " TEXT,"
                + " FOREIGN KEY ("+KEY_ID+") REFERENCES "+ TABLE_ARTICLES +" ("+KEY_ID+") ON DELETE CASCADE);";

        String CREATE_ARTICLE_IMAGES = "CREATE TABLE " + TABLE_IMAGES + "("
                + KEY_ID + " INTEGER,"
                + KEY_ORDER + " INTEGER,"
                + KEY_URL+ " TEXT,"
                + KEY_IMAGE + " BLOB,"
                + " FOREIGN KEY ("+KEY_ID+") REFERENCES "+ TABLE_ARTICLES +" ("+KEY_ID+") ON DELETE CASCADE);";


        db.execSQL(CREATE_LISTENTRYS);
        db.execSQL(CREATE_ARTICLES_PARAGRAPHS);
        db.execSQL(CREATE_ARTICLE_IMAGES);
    }

    // Upgrading database, we don't have any version so there is no reason to do anything smart in this method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
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
            entry.id = pushData(TABLE_ARTICLES,entry.id,c);
        }
    }

    public synchronized List<Article> getEntries(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ARTICLES, new String[]{KEY_ID, KEY_AUTHOR, KEY_DATE, KEY_DESCRIPTION,
                        KEY_URL, KEY_URLTOIMAGE, KEY_READ, KEY_TOBESAVED,KEY_TITLE},
                null,null, null, null, KEY_DATE+" DESC", null);
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

    public synchronized void push(final Article article){
        //in order to make sure we don't save anything several times we errase anything that is allready there
        // to do so we use that paragraphs and images arre errased on cascade hence we first delete the entry
        deleteEntry(TABLE_ARTICLES,article.id);
        // and now all tables are emptied of this entry. Now we save the article, it is a bit cumbersome to instantiate an array to
        // save on article but we don't want to reapeat the push method.
        ArrayList<Article> singleList = new ArrayList<>();
        singleList.add(article);
        push(singleList);
        // NOw we save the paragraphs and images of the article
        ContentValues c;
        int order = 0;
        for(ArticlePart p:article.paragraphs){
            switch (p.getType()){
                case TEXT:
                    c = new ContentValues();
                    c.put(KEY_ID, article.id);
                    c.put(KEY_ORDER,order++);
                    c.put(KEY_TEXT,((Text)p).getText());
                    pushData(TABLE_ARTICLEPARAGRAPHS,null,c);
                    break;
                case FIGURE:
                    c = new ContentValues();
                    c.put(KEY_ID, article.id);
                    c.put(KEY_ORDER,order++);
                    c.put(KEY_URL,((Figure)p).getUrl());
                    c.put(KEY_IMAGE,getBytes(((Figure)p).getBitmap()));
                    pushData(TABLE_IMAGES,null,c);
                    break;
            }
        }
    }

    public void push(Long id, int tempOrder, Figure fig) {
        getWritableDatabase().delete(TABLE_IMAGES,KEY_ID + " = ?,"+KEY_ORDER+ " =?",
                new String[]{String.valueOf(id),String.valueOf(tempOrder)});
        ContentValues c = new ContentValues();
        c.put(KEY_ID, id);
        c.put(KEY_ORDER,tempOrder);
        c.put(KEY_URL,fig.getUrl());
        c.put(KEY_IMAGE,getBytes(fig.getBitmap()));
        pushData(TABLE_ARTICLEPARAGRAPHS,id,c);
    }

    public synchronized ArrayList<ArticlePart> getArticleParagraphs(Long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ARTICLEPARAGRAPHS, new String[]{KEY_ORDER, KEY_TEXT},
                KEY_ID+"=?",new String[]{id.toString()}, null, null, null, null);

        HashMap<Long,ArticlePart> partsMap = new HashMap<>();

        if(cursor.moveToFirst()){
            do{
                partsMap.put(getLong(cursor,KEY_ORDER),new Text(getString(cursor,KEY_TEXT)));
            }while (cursor.moveToNext());
        }
        cursor.close();

        cursor = db.query(TABLE_IMAGES, new String[]{KEY_ORDER, KEY_IMAGE},
                KEY_ID+"=?",new String[]{id.toString()}, null, null, null, null);

        Figure fig;
        if(cursor.moveToFirst()){
            do{
                fig = new Figure(getString(cursor,KEY_URL));
                fig.setBitmap(getImage(getBlob(cursor,KEY_IMAGE)));
                partsMap.put(getLong(cursor,KEY_ORDER),fig);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        ArrayList<ArticlePart> res = new ArrayList<>();
        for(int i =0; i<partsMap.size();++i)
            res.add(partsMap.get(i));
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

    private byte[] getBlob(Cursor c,String key){
        return c.getBlob(c.getColumnIndexOrThrow(key));
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


    //////////////////////// Bitmap <-> byte array methods ////////////////////////////0
        // convert from bitmap to byte array
    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
