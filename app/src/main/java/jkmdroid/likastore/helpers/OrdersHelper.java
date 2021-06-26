package jkmdroid.likastore.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jkmdroid on 6/7/21.
 */
public class OrdersHelper extends SQLiteOpenHelper {
    SQLiteDatabase sqLiteDatabase;
    private static final String DATABASE_NAME = "orders.db";
    private static final String TABLE_NAME = "orders";

    private static final String COLUMN_PK = "ID";
    private static final String COLUMN_ORDER_ID = "ORDER_ID";
    private static final String COLUMN_PHONE_NUMBER = "PHONE_NUMBER";
    private static final String COLUMN_LOCATION = "LOCATION";
    private static final String COLUMN_ITEMS = "ITEMS";
    private static final String COLUMN_STATUS = "STATUS";


    public OrdersHelper(Context context){
        super(context,DATABASE_NAME, null, 2);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, ORDER_ID TEXT, PHONE_NUMBER TEXT, LOCATION TEXT, ITEMS TEXT, STATUS TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i3) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    //inserting  drinks into the database
    public boolean insert_order(String order_id, String phone, String location, String items, String status) {
        sqLiteDatabase = this.getReadableDatabase();
        //check if record exists
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + "='" + order_id+ "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor != null && cursor.getCount() > 0) {
            System.out.println("------------------------------record exists" + order_id);
            return false;
        } else{
            System.out.println("---------------------inserting order----------------------------");
            sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_ORDER_ID, order_id);//1
            contentValues.put(COLUMN_PHONE_NUMBER, phone);//2
            contentValues.put(COLUMN_LOCATION, location);//3
            contentValues.put(COLUMN_ITEMS, items);//4
            contentValues.put(COLUMN_STATUS, status);//5

            sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        sqLiteDatabase.close();
        return true;

    }

    public Cursor get_orders(){
        sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
    }

    //get a single row
    public void update_order_status(String order_id, String status){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + "='" + order_id+ "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_STATUS, status);
           int saved =  sqLiteDatabase.update(TABLE_NAME, contentValues, "ORDER_ID=?", new String[] {order_id});
            System.out.println("------sql lite---------------"+saved+"--------------------"+status);
        }
        cursor.close();

    }

    //delete all the orders
    public void delete_all_orders(){
        sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from " + TABLE_NAME);
        sqLiteDatabase.close();
    }
}


