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
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by jkm-droid on 05/04/2021.
 */

public class MyHelper {
    public static String generateRating() {
        double min = 4.0, max = 4.7;
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        double rating = min + Math.random() * (max - min);
        return decimalFormat.format(rating);
    }

    public static String capitalizeCategory(String word){
        String firstLetter = word.substring(0,1);
        String otherLetters = word.substring(1, word.length());
        firstLetter = firstLetter.toUpperCase();

        return firstLetter + otherLetters;
    }
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
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