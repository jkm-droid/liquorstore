package jkmdroid.liquorstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Created by jkmdroid on 5/29/21.
 */
public class SqlLiteHelper extends SQLiteOpenHelper {
    SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "drinks.db";
    private static final String TABLE_NAME = "drinks";

    private static final String COLUMN_PK = "ID";
    private static final String COLUMN_DRINK_ID = "DRINK_ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PRICE = "PRICE";
    private static final String COLUMN_CATEGORY = "CATEGORY";
    private static final String COLUMN_POSTER_URL = "POSTERURL";
    private static final String COLUMN_DATE = "DATE";

    public SqlLiteHelper(Context context){
        super(context,DATABASE_NAME, null, 4);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DRINK_ID INTEGER, NAME TEXT, PRICE INTEGER, CATEGORY TEXT,POSTERURL TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting  drinks into the database
    public boolean insert_drink(int drink_id, String name,int price, String category, String posterurl, String date) {
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
            contentValues.put(COLUMN_DRINK_ID, drink_id);
            contentValues.put(COLUMN_NAME, name);
            contentValues.put(COLUMN_PRICE, price);
            contentValues.put(COLUMN_CATEGORY, category);
            contentValues.put(COLUMN_POSTER_URL, posterurl);
            contentValues.put(COLUMN_DATE, date);

            result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        sqLiteDatabase.close();
        return true;

    }

    //retrieving drinks from the database
    public Cursor get_drinks() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
    }

    //delete a single row
    public boolean delete_drink(int drink_id){
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME,COLUMN_DRINK_ID+"="+drink_id, null);
        System.out.println("-----------------------drink removed-----------------------------");
        return true;
    }

    //get the total records
    public int count_drinks(){
        sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME;
        int messages = sqLiteDatabase.rawQuery(query, null).getCount();
        sqLiteDatabase.close();

        return messages;
    }
}

