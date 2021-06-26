package jkmdroid.likastore.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jkmdroid on 6/12/21.
 */
public class FlipperHelper extends SQLiteOpenHelper {
    SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "posters.db";
    private static final String TABLE_NAME = "posters";

    private static final String COLUMN_PK = "ID";//0
    private static final String COLUMN_POSTER_ID = "POSTER_ID";//1
    private static final String COLUMN_POSTER_NAME = "POSTER_NAME";//2
    private static final String COLUMN_POSTER_URL = "POSTERURL";//3

    public FlipperHelper(Context context){
        super(context,DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, POSTER_ID INTEGER, POSTER_NAME TEXT, POSTERURL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting  drinks into the database
    public boolean insert_poster(int poster_id, String poster_name, String posterurl) {
        long result = 0;
        sqLiteDatabase = this.getWritableDatabase();
        //check if record exists

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_POSTER_ID + "=" + poster_id;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor != null && cursor.getCount() > 0) {
            System.out.println("------------------------------poster exists" + poster_id);
            return false;
        } else{
            System.out.println("---------------------inserting poster----------------------------");
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_POSTER_ID, poster_id);//1
            contentValues.put(COLUMN_POSTER_NAME, poster_name);//2
            contentValues.put(COLUMN_POSTER_URL, posterurl);//3

            sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        sqLiteDatabase.close();
        return true;

    }

    //retrieving drinks from the database
    public Cursor get_posters() {
        sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
    }
    //delete all the messages
    public void delete_posters(){
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from " + TABLE_NAME);
        System.out.println("---------------deleted all posters--------------------");
        sqLiteDatabase.close();
    }
}



