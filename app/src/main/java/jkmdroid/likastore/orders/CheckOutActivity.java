package jkmdroid.likastore.orders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import jkmdroid.likastore.R;
import jkmdroid.likastore.helpers.MyHelper;
import jkmdroid.likastore.helpers.OrdersHelper;
import jkmdroid.likastore.helpers.SqlLiteHelper;
import jkmdroid.likastore.mpesa.AccessToken;
import jkmdroid.likastore.mpesa.DarajaApiClient;
import jkmdroid.likastore.mpesa.STKPush;
import jkmdroid.likastore.mpesa.Utilities;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static jkmdroid.likastore.Constants.ACCOUNT_REFERENCE;
import static jkmdroid.likastore.Constants.BUSINESS_SHORT_CODE;
import static jkmdroid.likastore.Constants.CALLBACK_URL;
import static jkmdroid.likastore.Constants.PARTY_B;
import static jkmdroid.likastore.Constants.PASSKEY;
import static jkmdroid.likastore.Constants.TRANSACTION_DESCRIPTION;
import static jkmdroid.likastore.Constants.TRANSACTION_TYPE;

/**
 * Created by jkmdroid on 5/31/21.
 */
public class CheckOutActivity extends AppCompatActivity {
    TextView messageView, totalView;
    Button confirmButton;
    SqlLiteHelper sqlLiteHelper;
    OrdersHelper ordersHelper;
    String price, quantity, name;
    Intent intent;
    String paymentMethod,phonenumber, location, amount;
    RadioGroup paymentRadio;
    ProgressDialog progressDialog;
    StringBuilder items;
    String order_id;
    DarajaApiClient darajaApiClient;
    int finalCost;
    AlertDialog alertDialog;
    DecimalFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        sqlLiteHelper = new SqlLiteHelper(getApplicationContext());
        ordersHelper = new OrdersHelper(getApplicationContext());
        darajaApiClient = new DarajaApiClient();
        darajaApiClient.setIsDebug(true);

        paymentRadio = findViewById(R.id.cash_mpesa);
        confirmButton = findViewById(R.id.confirm_order);

        messageView = findViewById(R.id.message);
        totalView = findViewById(R.id.total_amount);
        confirmButton = findViewById(R.id.confirm_order);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Confirm Order");
        setSupportActionBar(toolbar) ;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        accessToken();
        set_up();
        confirmButton.setOnClickListener(v -> checkPaymentMethod());

    }

    private void accessToken() {
        darajaApiClient.setGetAccessToken(true);
        darajaApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.isSuccessful()){
                    darajaApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {

            }
        });
    }

    private void set_up() {
        Cursor cursor = sqlLiteHelper.get_drinks();
        format = new DecimalFormat("#,###,###");
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
            finalCost = totalPrice + (10 * cursor.getCount());
            totalView.setText("Total: Kshs " + format.format(finalCost));
        }
    }

    private void checkPaymentMethod() {
        int selectedId = paymentRadio.getCheckedRadioButtonId();
        if (selectedId == -1){
            Toast.makeText(getApplicationContext(), "Please select a payment method", Toast.LENGTH_LONG).show();
        }else{
            RadioButton radioButton = findViewById(selectedId);
            paymentMethod = radioButton.getText().toString();
            confirmOrder(paymentMethod);
        }
    }

    private void confirmOrder(String paymentM) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.user_form, null);
        builder.setView(R.layout.user_form);
        builder.setView(view);
        builder.setIcon(R.drawable.delivery);
        builder.setTitle("Payment & Delivery Details");
        builder.setCancelable(false);
        TextView  amountText = view.findViewById(R.id.amount);
        amountText.setText(String.format("Amount payable Ksh %s", format.format(finalCost)));

        builder.setPositiveButton("PAY & PROCEED", (dialog, which) -> {

        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean isError = false;
            EditText phoneEdit = view.findViewById(R.id.phonenumber), locationEdit = view.findViewById(R.id.location);
            TextView error = view.findViewById(R.id.alert_error);
            phonenumber = phoneEdit.getText().toString();
            location = locationEdit.getText().toString();
            String err = "";
            //phone number
            if (phonenumber.isEmpty()) {
                isError = true;
                err += "\nFill phone number";
            }

            if (phonenumber.length() < 10) {
                isError = true;
                err += "\nToo short phone number";
            }

            //location
            if (location.isEmpty()) {
                isError = true;
                err +="\nFill location";
            }

            if (err.trim().length() > 4)
                error.setText(err);
            else {
                if(paymentM.equalsIgnoreCase("Lipa Na M-pesa")) {
                    performPaymentActivity();
                }else if(paymentM.equalsIgnoreCase("Cash On Delivery")) {
                    performServerActivity();
                }
            }
        });
    }

    private void performPaymentActivity() {
        System.out.println("inside payment---------------------------------");
        alertDialog.dismiss();
        progressDialog = new ProgressDialog(CheckOutActivity.this);
        progressDialog.setMessage("Processing your order...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        if (progressDialog != null)
                progressDialog.show();
        String timestamp = Utilities.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utilities.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(1),
                Utilities.sanitizePhoneNumber(phonenumber),
                PARTY_B,
                Utilities.sanitizePhoneNumber(phonenumber),
                CALLBACK_URL,
                ACCOUNT_REFERENCE,
                TRANSACTION_DESCRIPTION
        );

        darajaApiClient.setGetAccessToken(false);

        darajaApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(Call<STKPush> call, Response<STKPush> response) {
                progressDialog.dismiss();
                try {
                    if (response.isSuccessful()){
                        Timber.e("Message %s", response.body());
                        performServerActivity();
                    }else {
                        Timber.e("Message %s", response.errorBody().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<STKPush> call, Throwable t) {
                progressDialog.dismiss();
                Timber.e(t);
            }
        });
    }

    private void performServerActivity() {
        if (paymentMethod.equalsIgnoreCase("Cash On Delivery")){
            if (alertDialog != null)
                alertDialog.dismiss();
            progressDialog = new ProgressDialog(CheckOutActivity.this);
            progressDialog.setMessage("Sending details for order confirmation");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            if (progressDialog != null)
                progressDialog.show();
        }

        Cursor cursor = sqlLiteHelper.get_drinks();
        DecimalFormat format = new DecimalFormat("#,###,###");
        int totalPrice = 0, finalCost = 0;
        items = new StringBuilder();
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                price = cursor.getString(6);//contains the total prices based on drink quantity
                quantity = cursor.getString(5);
                name = cursor.getString(2);
                items.append(quantity).append(" ").append(name).append(" for Kshs ").append(format.format(Integer.parseInt(price))).append("\n/");
                totalPrice += Integer.parseInt(price);
            }
            cursor.close();
            finalCost = totalPrice + (10 * cursor.getCount());
        }

        order_id = generateOrderId(phonenumber);
        System.out.println(order_id+"------------------------");
        String data = "";
        try {
            data += URLEncoder.encode("order_details", "UTF-8") + "=" + URLEncoder.encode("drinks", "UTF-8") + "&";
            data += URLEncoder.encode("order_id", "UTF-8") + "=" + URLEncoder.encode(order_id, "UTF-8") + "&";
            data += URLEncoder.encode("phonenumber", "UTF-8") + "=" + URLEncoder.encode(phonenumber, "UTF-8") + "&";
            data += URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(location, "UTF-8")+ "&";
            data += URLEncoder.encode("paymentmethod", "UTF-8") + "=" + URLEncoder.encode(paymentMethod, "UTF-8")+ "&";
            data += URLEncoder.encode("totalprice", "UTF-8") + "=" + URLEncoder.encode("Kshs "+format.format(finalCost), "UTF-8")+ "&";
            data += URLEncoder.encode("drinks", "UTF-8") + "=" + URLEncoder.encode(items.toString(), "UTF-8");

            String url = "https://liquorstore.mblog.co.ke/orders/orders.php";
            sendDetailsOnline(url, data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void sendDetailsOnline(String url, String data) {
        @SuppressLint("HandlerLeak")Handler handler = new Handler(){
            @Override
            public void dispatchMessage(@NonNull Message msg) {
                super.dispatchMessage(msg);
                if (progressDialog != null)
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckOutActivity.this);
                if (((String)msg.obj).equalsIgnoreCase("order received")){
                    //delete current orders
                    ordersHelper.delete_all_orders();
                    //save the details in shared preferences
                    saveOrderDetails(order_id, phonenumber, location, items);

                    builder.setMessage("Your order has been received and will be approved shortly...Please wait for a confirmation via a call")
                            .setTitle("Order Received")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) ->{
                                startActivity(new Intent(CheckOutActivity.this, OrderActivity.class));
                                finish();
                            });
                    builder.show();
                }else{
                    builder.setMessage(((String)msg.obj))
                            .setTitle("Error Occurred")
                            .setCancelable(false)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    String response = MyHelper.connectOnline(url, data);
                    System.out.println(response+"--------------------------");
                    Message message = new Message();
                    message.arg1 = 1;
                    message.obj = response;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void saveOrderDetails(String order_id, String phone, String location, StringBuilder items) {
//        SharedPreferences preferences = getSharedPreferences(OrderPreferences.OrderDetails.NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        System.out.println("saving preferences-------------------------------");
//        System.out.println(order_id+"\n"+phone+"\n"+location+"\n"+items);
//        editor.putString(OrderPreferences.OrderDetails.PHONE_NUMBER, phone);
//        editor.putString(OrderPreferences.OrderDetails.LOCATION, location);
//        editor.putString(OrderPreferences.OrderDetails.ORDER_ID, order_id);
//        editor.putString(OrderPreferences.OrderDetails.ITEMS, items.toString());
//        editor.putString(OrderPreferences.OrderDetails.STATUS, "pending");
//        editor.apply();
        ordersHelper.insert_order(order_id, phone, location, items.toString(), "pending");
    }

    private String generateOrderId(String phone){
        String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"1234567890";
        StringBuilder stringBuilder = new StringBuilder(4);
        String ph = "LQS"+phone.substring(phone.length() - 3);

        for (int n = 0; n < 4; n++){
            int length = (int)(alphaNumeric.length() * Math.random());
            stringBuilder.append(alphaNumeric.charAt(length));
        }
        return ph+stringBuilder.toString();
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
        intent = new Intent(getApplicationContext(), CartActivity.class);
        intent.putExtra("activity", "check_out");
        startActivity(intent);
        finish();
    }
}