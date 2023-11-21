package com.example.weatheralert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataHandler {

    private DBHelper dbHelper;

    public DataHandler(Context context) {
        dbHelper = new DBHelper(context);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public long insertData(String city, String temperature, String feelsLike, String humidity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", getCurrentDate().toString());
        values.put("time", getCurrentTime().toString());
        values.put("city", city);
        values.put("temperature", temperature);
        values.put("feelsLike", feelsLike);
        values.put("humidity", humidity);


        long id = db.insert("mytable", null, values);
        db.close();
        return id;
    }

    public String[] fetchData(String city_name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"date", "time", "city", "temperature", "feelsLike", "humidity"};
        String selection = "city = ?";
        String[] selectionArgs = {city_name};
        String orderBy = "id DESC"; // Assuming 'id' is the auto-incremented primary key
        Cursor cursor = db.query("mytable", columns, selection, selectionArgs, null, null, orderBy);

        String[] result = new String[6]; // Initialize with default or empty values

        if (cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex("date");
            int timeIndex = cursor.getColumnIndex("time");
            int cityIndex = cursor.getColumnIndex("city");
            int temperatureIndex = cursor.getColumnIndex("temperature");
            int feelsLikeIndex = cursor.getColumnIndex("feelsLike");
            int humidityIndex = cursor.getColumnIndex("humidity");

            result[0] = cursor.getString(dateIndex);
            result[1] = cursor.getString(timeIndex);
            result[2] = cursor.getString(cityIndex);
            result[3] = cursor.getString(temperatureIndex);
            result[4] = cursor.getString(feelsLikeIndex);
            result[5] = cursor.getString(humidityIndex);
        }

        cursor.close();
        db.close();

        return result;
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase("mydatabase"); // Replace "mydatabase" with the actual name of your database
    }
}
