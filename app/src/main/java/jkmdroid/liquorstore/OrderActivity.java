package jkmdroid.liquorstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by jkmdroid on 6/4/21.
 */
public class OrderActivity extends AppCompatActivity {
    SharedPreferences preferences;
    TextView orderView, shopMore, loadingView;
    String orderId, orderStatus;
    LinearLayout layoutConfirmed, layoutPending, layoutDelivered;
    boolean isPending, isConfirmed, isDelivered = false;
    SqlLiteHelper sqlLiteHelper;
    OrdersHelper ordersHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        preferences = getSharedPreferences(OrderPreferences.OrderDetails.NAME, MODE_PRIVATE);
        sqlLiteHelper = new SqlLiteHelper(OrderActivity.this);
        ordersHelper = new OrdersHelper(OrderActivity.this);

        orderView = findViewById(R.id.order_number);
        shopMore = findViewById(R.id.shop_more);
        loadingView = findViewById(R.id.loading);
        loadingView.setVisibility(View.VISIBLE);

        layoutConfirmed = findViewById(R.id.layout_confirmed);
        layoutPending = findViewById(R.id.layout_pending);
        layoutDelivered = findViewById(R.id.layout_delivered);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Order Status");
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        startOrderConfirmation();
    }

    private void startOrderConfirmation() {
        Cursor cursor = ordersHelper.get_orders();
        if (cursor != null && cursor.getCount() > 0 ){
            cursor.moveToNext();
            orderId = cursor.getString(1);
            orderStatus = cursor.getString(5);
            System.out.println(orderId+"-------------------"+orderStatus+"--------------------");
        }
//        orderId = preferences.getString(OrderPreferences.OrderDetails.ORDER_ID, "");
//        orderStatus = preferences.getString(OrderPreferences.OrderDetails.STATUS, "");
        orderView.setText("Order "+orderId);
        System.out.println(orderStatus+"-----------------0-----------------");

        if (orderStatus.equalsIgnoreCase("confirmed")){
            ordersHelper.update_order_status(orderId, "confirmed");

            loadingView.setVisibility(View.GONE);
            layoutConfirmed.setVisibility(View.VISIBLE);
        }else if (orderStatus.equalsIgnoreCase("delivered")){
            loadingView.setVisibility(View.GONE);
            layoutDelivered.setVisibility(View.VISIBLE);
        }else {
            loadingView.setVisibility(View.GONE);
            layoutPending.setVisibility(View.VISIBLE);
            String data = "";
            try {
                data += URLEncoder.encode("order_confirmation", "UTF-8") + "=" + URLEncoder.encode("drinks", "UTF-8") + "&";
                data += URLEncoder.encode("order_id", "UTF-8") + "=" + URLEncoder.encode(orderId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "https://liquorstore.mblog.co.ke/orders/order_confirmation.php";
            checkOrderStatus(url, data);
        }

        shopMore.setOnClickListener(v -> {
            startActivity(new Intent(OrderActivity.this, MainActivity.class));
            finish();
        });
    }

    private void checkOrderStatus(String link, String data) {
        @SuppressLint("HandlerLeak")Handler handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(((String)msg.obj).startsWith("{") && (((String) msg.obj).endsWith("}"))){
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject((String)msg.obj);
                        if (jsonObject.getInt("status_code") == 200){
                            String status = jsonObject.getString("order_status");
                            //pending order
                            if(status.equalsIgnoreCase("pending")) {
                                System.out.println(status + "---------------1---pending------------");
                                isPending = true;

                                //confirmed order
                            }else if(status.equalsIgnoreCase("confirmed")){
                                System.out.println(status+"----------------2---confirmed-----------");
                                layoutPending.setVisibility(View.GONE);
                                layoutConfirmed.setVisibility(View.VISIBLE);
//                                SharedPreferences.Editor editor = preferences.edit();
//                                editor.putString(OrderPreferences.OrderDetails.STATUS, "confirmed");
//                                editor.apply();
                                ordersHelper.update_order_status(orderId, "confirmed");
                                isConfirmed = true;
                                //delivered order
                            }else if (status.equalsIgnoreCase("delivered")){
                                System.out.println(status+"----------------3--delivered-----------");
                                layoutConfirmed.setVisibility(View.GONE);
                                layoutDelivered.setVisibility(View.VISIBLE);
//                                SharedPreferences.Editor editor = preferences.edit();
//                                editor.putString(OrderPreferences.OrderDetails.STATUS, "delivered");
//                                editor.apply();
                                ordersHelper.update_order_status(orderId, "delivered");
                                isDelivered = true;
                            }
                            sqlLiteHelper.delete_all_drinks();
                        }else if (jsonObject.getInt("status_code") == 201){
                            System.out.println("An error occurred");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    if (isDelivered)
                        return;
                    if (isPending || isConfirmed)
                        sleep(120000);
                    sleep(10000);
                    String response = MyHelper.connectOnline(link, data);
                    System.out.println(response);
                    Message message = new Message();
                    message.arg1 = 1;
                    message.obj = response;
                    handler.sendMessage(message);
                    System.out.println(response);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                run();
            }
        };
        thread.start();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            back();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void back() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}