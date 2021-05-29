package jkmdroid.liquorstore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by jkm-droid on 05/04/2021.
 */

public class MyHelper {
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static String toPostDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        calendar.setTimeInMillis(millis);
        if (millis < System.currentTimeMillis()){
            if (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR)) {
                if (hour == calendar.get(Calendar.HOUR_OF_DAY)) {
                    return "Started "+(minute - calendar.get(Calendar.MINUTE)) + " minutes ago";
                }
                return "Today, "+DateFormat.format("hh:mm a", millis).toString();
            } else if (dayOfYear - calendar.get(Calendar.DAY_OF_YEAR) == 1) {
                return "Yesterday, " + DateFormat.format("hh:mm a", millis).toString();
            } else if (weekOfYear == calendar.get(Calendar.WEEK_OF_YEAR)) {
                return DateFormat.format("E, hh:mm a", millis).toString();
            }
        }else {
            if (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR)){
                if (calendar.get(Calendar.HOUR_OF_DAY) <= 1+hour){
                    return (calendar.get(Calendar.MINUTE)-minute) + " minutes remaining";
                }
                return "Today "+DateFormat.format("hh:mm a", millis).toString();
            } else if (dayOfYear - calendar.get(Calendar.DAY_OF_YEAR) == -1){
                return "Tomorrow, " + DateFormat.format("hh:mm a", millis).toString();
            }
            else if (weekOfYear == calendar.get(Calendar.WEEK_OF_YEAR)) {
                return DateFormat.format("E, hh:mm a", millis).toString();
            }
        }
        return DateFormat.format("dd MMM yyyy, hh:mm a", millis).toString();
    }

    public static void writeError(String error){
        String fileName = "Error.txt";
        try {
            System.out.println("Writing ("+error+")");
            File root = new File(Environment.getExternalStorageDirectory(),"odds");
            if (!root.exists()){
                root.mkdirs();
            }

            File gpxfile = new File(root, fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gpxfile, true));
            bufferedWriter.append(error);
            bufferedWriter.newLine();
            bufferedWriter.close();
        }catch (IOException e){
        }
    }

    public static void restart(){
        String fileName = "Error.txt";
        try {
            File root = new File(Environment.getExternalStorageDirectory(),"golpredicts");
            File gpxfile = new File(root, fileName);
            if (!root.exists()){
                root.mkdirs();
            }
            FileWriter writer = new FileWriter(gpxfile, false);
            writer.write("");
            writer.flush();
            writer.close();
        }catch (IOException e){
        }
    }
    static void runtime() {
        String fileName = "Error.txt";
        File root = new File(Environment.getExternalStorageDirectory(),"golpredicts");
        File gpxfile = new File(root, fileName);

        try {
            Runtime.getRuntime().exec("logcat -c");
            Runtime.getRuntime().exec("logcat -v time -f"+gpxfile.getAbsolutePath());
        } catch (IOException e) {
            writeError("Error in runtime");
        }
    }

    public static String connectOnline(String link, String encodedData) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(15000);

        System.out.println("Url: " + connection.getURL());

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(encodedData);
        bufferedWriter.flush();
        bufferedWriter.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String getOnline(String link, String encodedData) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);

        System.out.println("Url: " + connection.getURL());

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(encodedData);
        bufferedWriter.flush();
        bufferedWriter.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}