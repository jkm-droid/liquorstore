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
    private static final String COLUMN_QUANTITY = "QUANTITY";
    private static final String COLUMN_PRICE_DRINK = "PRICE_DRINK";
    private static final String COLUMN_POSTER_URL = "POSTERURL";
    private static final String COLUMN_DATE = "DATE";

    public SqlLiteHelper(Context context){
        super(context,DATABASE_NAME, null, 6);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, DRINK_ID INTEGER, NAME TEXT, PRICE INTEGER, CATEGORY TEXT,QUANTITY INTEGER, PRICE_DRINK INTEGER, POSTERURL TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting  drinks into the database
    public boolean insert_drink(int drink_id, String name,int price, String category, int quantity, String posterurl, String date) {
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
            contentValues.put(COLUMN_PRICE, price);//3 the price of one quantity
            contentValues.put(COLUMN_CATEGORY, category);//4
            contentValues.put(COLUMN_QUANTITY, quantity);//5
            contentValues.put(COLUMN_PRICE_DRINK, (quantity * price));//6 the total price per drink based on quantity(s)
            contentValues.put(COLUMN_POSTER_URL, posterurl);//7
            contentValues.put(COLUMN_DATE, date);//8

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

    //change the drink quantity
    public void change_quantity(int drink_id, String keyword){
        sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DRINK_ID + "=" + drink_id;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.getCount() > 0){
            cursor.moveToNext();
            int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
            int price = cursor.getInt(cursor.getColumnIndex(COLUMN_PRICE));

            sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            if (keyword.equalsIgnoreCase("add")){
                quantity += 1;
                contentValues.put(COLUMN_QUANTITY, quantity);
                contentValues.put(COLUMN_PRICE_DRINK, (quantity * price));
                sqLiteDatabase.update(TABLE_NAME, contentValues, COLUMN_DRINK_ID + "=" + drink_id, null);
            }else if (keyword.equalsIgnoreCase("minus")){
                if (quantity != 1) {
                    quantity -= 1;
                    contentValues.put(COLUMN_QUANTITY, quantity);
                    contentValues.put(COLUMN_PRICE_DRINK, (quantity * price));
                    sqLiteDatabase.update(TABLE_NAME, contentValues, COLUMN_DRINK_ID + "=" + drink_id, null);
                }
            }
        }
    }

    //get the quantity per drink
    public int get_drink_quantity(int drink_id){
        sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DRINK_ID + "=" + drink_id;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        int quantity = 0;
        if (cursor.getCount() > 0){
            cursor.moveToNext();
            quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
        }

        return quantity;
    }
}

