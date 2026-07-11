package com.example.balaghi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.balaghi.utils.Constants;
import android.content.ContentValues;
import android.database.Cursor;
import com.example.balaghi.models.User;


public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                    Constants.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.USER_FULL_NAME + " TEXT NOT NULL, " +
                    Constants.USER_USERNAME + " TEXT NOT NULL, " +
                    Constants.USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    Constants.USER_PHONE + " TEXT, " +
                    Constants.USER_PASSWORD + " TEXT NOT NULL, " +
                    Constants.USER_BIRTH_DATE + " TEXT, " +
                    Constants.USER_PROFILE_IMAGE + " TEXT" +
                    ");";
    private static final String CREATE_REPORTS_TABLE =
            "CREATE TABLE " + Constants.TABLE_REPORTS + " (" +
                    Constants.REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.REPORT_TITLE + " TEXT NOT NULL, " +
                    Constants.REPORT_DESCRIPTION + " TEXT NOT NULL, " +
                    Constants.REPORT_CATEGORY + " TEXT NOT NULL, " +
                    Constants.REPORT_STATUS + " TEXT NOT NULL, " +
                    Constants.REPORT_LOCATION + " TEXT NOT NULL, " +
                    Constants.REPORT_IMAGE + " TEXT, " +
                    Constants.REPORT_DATE + " TEXT NOT NULL, " +
                    Constants.REPORT_IS_ANONYMOUS + " INTEGER NOT NULL, " +
                    Constants.REPORT_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + Constants.REPORT_USER_ID + ") REFERENCES "
                    + Constants.TABLE_USERS + "(" + Constants.USER_ID + ")" +
                    ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_REPORTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_REPORTS);

        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);

        onCreate(db);
    }
    public boolean registerUser(User user) {
        if (isEmailExists(user.getEmail())) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Constants.USER_FULL_NAME, user.getFullName());
        values.put(Constants.USER_USERNAME, user.getUsername());
        values.put(Constants.USER_EMAIL, user.getEmail());
        values.put(Constants.USER_PHONE, user.getPhone());
        values.put(Constants.USER_PASSWORD, user.getPassword());
        values.put(Constants.USER_BIRTH_DATE, user.getBirthDate());
        values.put(Constants.USER_PROFILE_IMAGE, user.getProfileImage());

        long result = db.insert(Constants.TABLE_USERS, null, values);

        db.close();

        return result != -1;

    }

    public boolean isEmailExists(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                null,
                Constants.USER_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;

    }
    public User loginUser(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                Constants.TABLE_USERS,
                null,
                Constants.USER_EMAIL + " = ? AND " +
                        Constants.USER_PASSWORD + " = ?",
                new String[]{email, password},
                null,
                null,
                null
        );

        User user = null;

        if (cursor.moveToFirst()) {

            user = new User();

            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.USER_ID)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_FULL_NAME)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_EMAIL)));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PHONE)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PASSWORD)));
            user.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_BIRTH_DATE)));
            user.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PROFILE_IMAGE)));

        }

        cursor.close();
        db.close();

        return user;
    }
}