package jkmdroid.likastore.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jkmdroid on 6/9/21.
 */
public class SearchHelper extends SQLiteOpenHelper {
    SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "drinks_search.db";
    private static final String TABLE_NAME = "drinks_search";

    private static final String COLUMN_PK = "ID";//0
    private static final String COLUMN_DRINK_ID = "DRINK_ID";//1
    private static final String COLUMN_NAME = "NAME";//2
    private static final String COLUMN_PRICE = "PRICE";//3
    private static final String COLUMN_CATEGORY = "CATEGORY";//4
    private static final String COLUMN_DESCRIPTION = "DESCRIPTION";//5
    private static final String COLUMN_POSTER_URL = "POSTERURL";//6

    public SearchHelper(Context context){
        super(context,DATABASE_NAME, null, 8);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DRINK_ID INTEGER, NAME TEXT, PRICE INTEGER, CATEGORY TEXT,DESCRIPTION TEXT, POSTERURL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting  drinks into the database
    public boolean insert_drink(int drink_id, String name,int price, String category,String description, String posterurl) {
        long result = 0;
        sqLiteDatabase = this.getWritableDatabase();
        //check if record exists

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DRINK_ID + "=" + drink_id;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor != null && cursor.getCount() > 0) {
            System.out.println("------------------------------record exists" + drink_id);
            return false;
        } else{
            System.out.println("---------------------inserting drink----------------------------");
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DRINK_ID, drink_id);//1
            contentValues.put(COLUMN_NAME, name);//2
            contentValues.put(COLUMN_PRICE, price);//3
            contentValues.put(COLUMN_CATEGORY, category);//4
            contentValues.put(COLUMN_DESCRIPTION, description);//5
            contentValues.put(COLUMN_POSTER_URL, posterurl);//6

            sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        sqLiteDatabase.close();
        return true;

    }

    //retrieving drinks from the database
    public Cursor get_drinks() {
        sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
    }

    //delete all the messages
    public void delete_all_drinks(){
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from " + TABLE_NAME);
        sqLiteDatabase.close();
    }
}


