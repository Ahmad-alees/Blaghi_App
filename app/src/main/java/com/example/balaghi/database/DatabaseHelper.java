package com.example.balaghi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.balaghi.models.Report;
import com.example.balaghi.models.User;
import com.example.balaghi.utils.Constants;

import java.util.ArrayList;
import java.util.List;

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
                    Constants.REPORT_EXTRACTED_TEXT + " TEXT, " +
                    Constants.REPORT_OCR_IMAGE + " TEXT, " +
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
        if (isEmailExists(user.getEmail())) return false;
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
                Constants.TABLE_USERS, null,
                Constants.USER_EMAIL + " = ?",
                new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_USERS, null,
                Constants.USER_EMAIL + " = ? AND " + Constants.USER_PASSWORD + " = ?",
                new String[]{email, password}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) user = cursorToUser(cursor);
        cursor.close();
        db.close();
        return user;
    }

    public long insertReport(Report report) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.REPORT_TITLE, report.getTitle());
        values.put(Constants.REPORT_DESCRIPTION, report.getDescription());
        values.put(Constants.REPORT_CATEGORY, report.getCategory());
        values.put(Constants.REPORT_STATUS, report.getStatus());
        values.put(Constants.REPORT_LOCATION, report.getLocation());
        values.put(Constants.REPORT_IMAGE, report.getImage());
        values.put(Constants.REPORT_EXTRACTED_TEXT, report.getExtractedText());
        values.put(Constants.REPORT_OCR_IMAGE, report.getOcrImage());
        values.put(Constants.REPORT_DATE, report.getDate());
        values.put(Constants.REPORT_IS_ANONYMOUS, report.isAnonymous() ? 1 : 0);
        values.put(Constants.REPORT_USER_ID, report.getUserId());

        long result = db.insert(Constants.TABLE_REPORTS, null, values);
        db.close();
        return result;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_USERS, null,
                Constants.USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) user = cursorToUser(cursor);
        cursor.close();
        db.close();
        return user;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.USER_FULL_NAME, user.getFullName());
        values.put(Constants.USER_USERNAME, user.getUsername());
        values.put(Constants.USER_EMAIL, user.getEmail());
        values.put(Constants.USER_PHONE, user.getPhone());
        values.put(Constants.USER_BIRTH_DATE, user.getBirthDate());
        values.put(Constants.USER_PROFILE_IMAGE, user.getProfileImage());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            values.put(Constants.USER_PASSWORD, user.getPassword());
        }
        int rows = db.update(Constants.TABLE_USERS, values,
                Constants.USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rows > 0;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.USER_ID)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_FULL_NAME)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PHONE)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PASSWORD)));
        user.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_BIRTH_DATE)));
        user.setProfileImage(cursor.getString(cursor.getColumnIndexOrThrow(Constants.USER_PROFILE_IMAGE)));
        return user;
    }

    public long addReport(Report report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.REPORT_TITLE, report.getTitle());
        values.put(Constants.REPORT_DESCRIPTION, report.getDescription());
        values.put(Constants.REPORT_CATEGORY, report.getCategory());
        values.put(Constants.REPORT_STATUS, report.getStatus());
        values.put(Constants.REPORT_LOCATION, report.getLocation());
        values.put(Constants.REPORT_IMAGE, report.getImage());
        values.put(Constants.REPORT_EXTRACTED_TEXT, report.getExtractedText());
        values.put(Constants.REPORT_OCR_IMAGE, report.getOcrImage());
        values.put(Constants.REPORT_DATE, report.getDate());
        values.put(Constants.REPORT_IS_ANONYMOUS, report.isAnonymous() ? 1 : 0);
        values.put(Constants.REPORT_USER_ID, report.getUserId());
        long id = db.insert(Constants.TABLE_REPORTS, null, values);
        db.close();
        return id;
    }

    public List<Report> getAllReports(int userId) {
        List<Report> reports = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_REPORTS, null,
                Constants.REPORT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                Constants.REPORT_DATE + " DESC");
        if (cursor.moveToFirst()) {
            do { reports.add(cursorToReport(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reports;
    }

    public List<Report> getLatestReports(int userId, int limit) {
        List<Report> reports = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_REPORTS, null,
                Constants.REPORT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                Constants.REPORT_DATE + " DESC",
                String.valueOf(limit));
        if (cursor.moveToFirst()) {
            do { reports.add(cursorToReport(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reports;
    }

    public Report getReportById(int reportId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Constants.TABLE_REPORTS, null,
                Constants.REPORT_ID + " = ?",
                new String[]{String.valueOf(reportId)},
                null, null, null);
        Report report = null;
        if (cursor.moveToFirst()) report = cursorToReport(cursor);
        cursor.close();
        db.close();
        return report;
    }

    public boolean updateReport(Report report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.REPORT_TITLE, report.getTitle());
        values.put(Constants.REPORT_DESCRIPTION, report.getDescription());
        values.put(Constants.REPORT_CATEGORY, report.getCategory());
        values.put(Constants.REPORT_STATUS, report.getStatus());
        values.put(Constants.REPORT_LOCATION, report.getLocation());
        values.put(Constants.REPORT_IMAGE, report.getImage());
        values.put(Constants.REPORT_EXTRACTED_TEXT, report.getExtractedText());
        values.put(Constants.REPORT_OCR_IMAGE, report.getOcrImage());
        values.put(Constants.REPORT_DATE, report.getDate());
        values.put(Constants.REPORT_IS_ANONYMOUS, report.isAnonymous() ? 1 : 0);
        int rows = db.update(Constants.TABLE_REPORTS, values,
                Constants.REPORT_ID + " = ?", new String[]{String.valueOf(report.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteReport(int reportId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(Constants.TABLE_REPORTS,
                Constants.REPORT_ID + " = ?", new String[]{String.valueOf(reportId)});
        db.close();
        return rows > 0;
    }

    public int getTotalReports(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Constants.TABLE_REPORTS +
                        " WHERE " + Constants.REPORT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getReportCountByStatus(int userId, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Constants.TABLE_REPORTS +
                        " WHERE " + Constants.REPORT_USER_ID + " = ? AND " +
                        Constants.REPORT_STATUS + " = ?",
                new String[]{String.valueOf(userId), status});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    private Report cursorToReport(Cursor cursor) {
        Report report = new Report();
        report.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.REPORT_ID)));
        report.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_TITLE)));
        report.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_DESCRIPTION)));
        report.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_CATEGORY)));
        report.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_STATUS)));
        report.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_LOCATION)));
        report.setImage(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_IMAGE)));
        report.setExtractedText(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_EXTRACTED_TEXT)));
        report.setOcrImage(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_OCR_IMAGE)));
        report.setDate(cursor.getString(cursor.getColumnIndexOrThrow(Constants.REPORT_DATE)));
        report.setAnonymous(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.REPORT_IS_ANONYMOUS)) == 1);
        report.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.REPORT_USER_ID)));
        return report;
    }
}