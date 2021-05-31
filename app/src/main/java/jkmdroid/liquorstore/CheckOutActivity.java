package jkmdroid.liquorstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by jkmdroid on 5/31/21.
 */
public class CheckOutActivity extends AppCompatActivity {
    TextView messageView, totalView;
    Button confirmButton;
    SqlLiteHelper sqlLiteHelper;
    String price, quantity, name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());

        messageView = findViewById(R.id.message);
        totalView = findViewById(R.id.total_amount);
        confirmButton = findViewById(R.id.confirm_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Confirm Order");
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        set_up();
    }

    private void set_up() {
        Cursor cursor = sqlLiteHelper.get_drinks();
        DecimalFormat format = new DecimalFormat("#,###,###");
        int totalPrice = 0;
        StringBuilder items = new StringBuilder();
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                price = cursor.getString(6);//contains the total prices based on drink quantity
                quantity = cursor.getString(5);
                name = cursor.getString(2);
                items.append(quantity).append(" ").append(name).append(" for Kshs ").append(format.format(Integer.parseInt(price))).append("\n");
                totalPrice += Integer.parseInt(price);
            }
            cursor.close();

            messageView.setText(items.toString());
            int finalCost = totalPrice + (10 * cursor.getCount());
            totalView.setText("Total: Kshs " + format.format(finalCost));
        }
    }
}