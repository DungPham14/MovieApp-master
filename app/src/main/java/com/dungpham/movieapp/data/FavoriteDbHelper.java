package com.dungpham.movieapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dungpham.movieapp.model.Movie;

import java.util.ArrayList;
import java.util.List;


public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite.db";

    private static final int DATABASE_VERSION = 1;

    public static final String LOGTAG = "FAVORITE";

    SQLiteOpenHelper dbhandler;
    SQLiteDatabase db;

    public FavoriteDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open(){
        Log.i(LOGTAG, "Database Opened");
        db = dbhandler.getWritableDatabase();
    }

    public void close(){
        Log.i(LOGTAG, "Database Closed");
        dbhandler.close();
    }


    /**
     *
     * tạo bảng phim ưa thích
     *
     * **/
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " +

                FavoriteContract.TABLE_NAME + " (" +

                FavoriteContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                FavoriteContract.COLUMN_MOVIEID + " INTEGER, " +

                FavoriteContract.COLUMN_TITLE + " TEXT NOT NULL, " +

                FavoriteContract.COLUMN_USERRATING + " REAL NOT NULL, " +

                FavoriteContract.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +

                FavoriteContract.COLUMN_PLOT_SYNOPSIS + " TEXT NOT NULL" + "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL ( "DROP TABLE IF EXISTS " + FavoriteContract.TABLE_NAME );
        onCreate(sqLiteDatabase);

    }

    /**
     *
     *
     * thêm phim ưa thích
     *
     * **/
    public void addFavorite(Movie movie){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put ( FavoriteContract.COLUMN_MOVIEID, movie.getId() );
        values.put ( FavoriteContract.COLUMN_TITLE, movie.getOriginalTitle() );
        values.put ( FavoriteContract.COLUMN_USERRATING, movie.getVoteAverage() );
        values.put ( FavoriteContract.COLUMN_POSTER_PATH, movie.getPosterPath() );
        values.put ( FavoriteContract.COLUMN_PLOT_SYNOPSIS, movie.getOverview() );

        db.insert ( FavoriteContract.TABLE_NAME, null, values);
        db.close();
    }


    /**
     *
     * hàm xóa
     * **/
    public void deleteFavorite(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete (
                FavoriteContract.TABLE_NAME,
                FavoriteContract.COLUMN_MOVIEID + "=" + id, null
        );
    }


    /**
     *
     * lấy toàn bộ các phim ưa thích
     *
     * **/
    public List<Movie> getAllFavorite(){

        String[] columns = {

                FavoriteContract._ID,
                FavoriteContract.COLUMN_MOVIEID,
                FavoriteContract.COLUMN_TITLE,
                FavoriteContract.COLUMN_USERRATING,
                FavoriteContract.COLUMN_POSTER_PATH,
                FavoriteContract.COLUMN_PLOT_SYNOPSIS

        };

        String sortOrder = FavoriteContract._ID + " ASC ";

        List<Movie> favoriteList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query (
                FavoriteContract.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if ( cursor.moveToFirst() ){
            do {

                Movie movie = new Movie();

                movie.setId(Integer.parseInt ( cursor.getString( cursor.getColumnIndex(
                        FavoriteContract.COLUMN_MOVIEID ) ) ) );

                movie.setOriginalTitle( cursor.getString( cursor.getColumnIndex(
                        FavoriteContract.COLUMN_TITLE ) ) );

                movie.setVoteAverage( Double.parseDouble( cursor.getString(cursor.getColumnIndex(
                        FavoriteContract.COLUMN_USERRATING ) ) ) );

                movie.setPosterPath( cursor.getString( cursor.getColumnIndex(
                        FavoriteContract.COLUMN_POSTER_PATH ) ) );

                movie.setOverview( cursor.getString( cursor.getColumnIndex(
                        FavoriteContract.COLUMN_PLOT_SYNOPSIS ) ) );

                favoriteList.add( movie );

            } while( cursor.moveToNext() );
        }
        cursor.close();
        db.close();

        return favoriteList;
    }

}
