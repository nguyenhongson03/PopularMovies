package com.popularmovies.vpaliy.data.source.local;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.popularmovies.vpaliy.data.entity.BackdropImage;
import com.popularmovies.vpaliy.data.entity.Movie;
import com.popularmovies.vpaliy.data.entity.MovieDetailEntity;
import com.popularmovies.vpaliy.data.source.DataSource;
import com.popularmovies.vpaliy.domain.ISortConfiguration;
import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import javax.inject.Inject;
import rx.Observable;

public class MovieLocalSource extends DataSource<Movie,MovieDetailEntity>{

    private final ContentResolver contentResolver;
    private final ISortConfiguration sortConfiguration;


    @Inject
    public MovieLocalSource(@NonNull Context context,
                            @NonNull ISortConfiguration sortConfiguration){
        this.contentResolver=context.getContentResolver();
        this.sortConfiguration=sortConfiguration;
    }


    /* No more movies */
    @Override
    public Observable<List<Movie>> requestMoreCovers() { return null; }

    @Override
    public Observable<MovieDetailEntity> getDetails(int ID) {
        return null;
    }

    @Override
    public Observable<List<Movie>> getCovers() {
        switch (sortConfiguration.getConfiguration()){
            case POPULAR:
                return Observable.fromCallable(()->
                        toMovies(contentResolver.query(MoviesContract.
                                MostPopularEntry.CONTENT_URI, null,null,null,null)));
            case TOP_RATED:
                return Observable.fromCallable(()->
                        toMovies(contentResolver.query(MoviesContract.
                                MostRatedEntry.CONTENT_URI, null,null,null,null)));
            case FAVORITE:
                String selection= MoviesContract.MovieEntry.COLUMN_IS_FAVORITE+" LIKE ?";
                String[] selectionArgs={Integer.toString(1)};
                return Observable.fromCallable(()->
                        toMovies(contentResolver.query(MoviesContract.MovieEntry.CONTENT_URI,
                                null,selection,selectionArgs,null)));
        }
        return null;
    }




    private List<Movie> toMovies(Cursor cursor){
        if(cursor!=null){
            if(cursor.moveToFirst()){
                List<Movie> movies=new ArrayList<>(cursor.getCount());
                while(cursor.moveToNext()){
                    movies.add(DatabaseUtils.convertToMovie(cursor));
                }
                return movies;
            }
            if(!cursor.isClosed()){
                cursor.close();
            }
        }
        return null;
    }

    @Override
    public void insert(Movie item) {
        final ContentValues values=DatabaseUtils.convertToValues(item);
        contentResolver.insert(MoviesContract.MovieEntry.CONTENT_URI,values);

        ContentValues configValues=new ContentValues();
        configValues.put(MoviesContract.MovieEntry.MOVIE_ID,item.getMovieId());
        switch (sortConfiguration.getConfiguration()){
            case POPULAR:
                contentResolver.insert(MoviesContract.MostPopularEntry.CONTENT_URI,configValues);
                break;
            case TOP_RATED:
                contentResolver.insert(MoviesContract.MostRatedEntry.CONTENT_URI,configValues);
                break;
        }

    }


    private Movie toMovie(Cursor cursor){
        if(cursor!=null){
            if(cursor.moveToFirst()){
                Movie movie=DatabaseUtils.convertToMovie(cursor);
                if(!cursor.isClosed()) cursor.close();
                return movie;
            }
        }
        return null;
    }

    @Override
    public Observable<Movie> getCover(int ID) {
        Uri uri= ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,ID);
        return Observable.fromCallable(()->toMovie(contentResolver.query(uri,null,null,null,null)));
    }

    @Override
    public Observable<List<Movie>> sortBy(@NonNull ISortConfiguration.SortType type) {
        sortConfiguration.saveConfiguration(type);
        return getCovers();
    }

    @Override
    public void insertDetails(MovieDetailEntity details) {

    }

    @Override
    public void update(Movie item) {
        ContentValues values=new ContentValues();
        values.put(MoviesContract.MovieEntry.COLUMN_IS_FAVORITE,item.isFavorite());
        Uri uri= ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,item.getMovieId());
        contentResolver.update(uri,values,null,null);
    }
}