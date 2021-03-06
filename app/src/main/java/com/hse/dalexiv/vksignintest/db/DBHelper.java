package com.hse.dalexiv.vksignintest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hse.dalexiv.vksignintest.model.Post;

import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Random;

/**
 * Created by dalex on 11/4/2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "posts.db";
    public static final String TABLE_POSTS = "posts";

    // One db row
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POST_TEXT = "txt";
    public static final String COLUMN_POST_URL = "post_url";
    public static final String COLUMN_POST_PREVIEW_PIC = "preview_url";
    public static final String COLUMN_POST_FULL_PIC = "full_url";
    public static final String COLUMN_POST_TIME_TEXT = "time_text";
    public static final String COLUMN_POST_TIME_H = "time_h";
    public static final String COLUMN_POST_TIME_M = "time_m";
    public static final String COLUMN_POST_PIC_URI = "pic";
    public static final String COLUMN_POST_LIKES = "likes";
    public static final String COLUMN_POST_COMMENTS = "comments";


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_POSTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_POST_TEXT + " TEXT, " +
                COLUMN_POST_URL + " TEXT, " +
                COLUMN_POST_PREVIEW_PIC + " TEXT, " +
                COLUMN_POST_FULL_PIC + " TEXT, " +
                COLUMN_POST_TIME_TEXT + " TEXT, " +
                COLUMN_POST_TIME_H + " INTEGER, " +
                COLUMN_POST_TIME_M + " INTEGER, " +
                COLUMN_POST_PIC_URI + " TEXT, " +
                COLUMN_POST_LIKES + " INTEGER, " +
                COLUMN_POST_COMMENTS + " INTEGER " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    public void insert(Post myPost) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_POST_TEXT, myPost.getText());
        contentValues.put(COLUMN_POST_URL, myPost.getPostURL());
        contentValues.put(COLUMN_POST_PREVIEW_PIC, myPost.getPreviewPicURL());
        contentValues.put(COLUMN_POST_FULL_PIC, myPost.getFullPicURL());
        contentValues.put(COLUMN_POST_TIME_TEXT, myPost.getTimeText());
        contentValues.put(COLUMN_POST_TIME_H, myPost.getHours());
        contentValues.put(COLUMN_POST_TIME_M, myPost.getMins());
        contentValues.put(COLUMN_POST_PIC_URI, myPost.getUriToImage());
        contentValues.put(COLUMN_POST_LIKES, myPost.getLikes());
        contentValues.put(COLUMN_POST_COMMENTS, myPost.getComments());


        db.insert(TABLE_POSTS, null, contentValues);
        db.close();
    }

    public void deleteById(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_POSTS, "id = ? ", new String[]{Integer.toString(id)});
    }

    public ArrayList<Post> getEverything() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Post> posts = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            posts.add(post);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return posts;
    }

    public boolean checkIfEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        cursor.moveToFirst();
        int check = -1;
            try {
             check = cursor.getInt(0);
        }
        catch (CursorIndexOutOfBoundsException e)
        {
            return true;
        }
        cursor.close();
        db.close();
        return check == 0;
    }
    public Post getClosestTime(Post initial_post)
    {
        SQLiteDatabase db = getReadableDatabase();
        Post currentPost = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            currentPost = cursorToPost(cursor);
            if (currentPost.compareTo(initial_post) > 0)
                break;
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return currentPost;
    }

    public Post getRandomPost()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        cursor.moveToFirst();
        int numberOfPosts = cursor.getCount();
        int myRandomPost = (new Random()).nextInt(numberOfPosts);
        for (int i = 0; i < myRandomPost; ++i)
            cursor.moveToNext();
        return cursorToPost(cursor);
    }

    private Post cursorToPost(Cursor cursor) {
        Post post = new Post(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getInt(6),
                cursor.getInt(7), cursor.getString(8), cursor.getInt(9), cursor.getInt(10));
        return post;
    }



}
