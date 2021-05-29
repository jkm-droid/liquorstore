package jkmdroid.liquorstore;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkm-droid on 05/04/2021.
 */

public class PostJson{
    private String url;
    private JSONObject object;
    private Context context;
    private OnSuccessListener onSuccessListener;
    private boolean isPost = false;

    public void setOnSuccessListener(OnSuccessListener onSuccessListener){
        this.onSuccessListener = onSuccessListener;
    }

    public PostJson(String url, JSONObject object, Context context){
        this.url = url;
        this.object = object;
        this.context = context;
        isPost = true;
    }

    public PostJson( Context context, String url){
        this.url = url;
        this.context = context;
        isPost = false;
    }

    public void post(){
        RequestQueue queue= Volley.newRequestQueue(context);
        System.out.println(object);

        JsonObjectRequest postRequest = new JsonObjectRequest( Request.Method.POST, url, object,
                response -> {
                    try {
                        onSuccessListener.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    //   Handle Error
                    System.out.println("-------------------------------------Error response ----------------------------------------------------------------------------");
                    if (error != null) {
                        System.out.print("Error: " + error);
                        if (error.networkResponse != null) {
                            System.out.print("Status Code " + error.networkResponse.statusCode);
                            System.out.print("Response Data " + error.networkResponse.data);
                        }
                        System.out.print("Cause " + error.getCause());
                        System.out.println("message" + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String,String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(postRequest);
    }
    public void get(){
        System.out.println("URL: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        onSuccessListener.onSuccess(response);
                        System.out.println(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    System.out.println("-------------------------------------Error response ----------------------------------------------------------------------------");

                    if (error != null) {
                        System.out.print("Error: " + error);
                        if (error.networkResponse != null) {
                            System.out.print("Status Code " + error.networkResponse.statusCode);
                            System.out.print("Response Data " + error.networkResponse.data);
                        }
                        System.out.print("Cause " + error.getCause());
                        System.out.println("message" + error.getMessage());
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                12000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES*2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);
    }
    static interface OnSuccessListener{
        void onSuccess(JSONObject object) throws JSONException;
    }
}