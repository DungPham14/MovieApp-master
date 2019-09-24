package com.dungpham.movieapp.fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dungpham.movieapp.R;
import com.dungpham.movieapp.activity.MainActivity;
import com.dungpham.movieapp.adapter.MoviesAdapter;
import com.dungpham.movieapp.data.FavoriteDbHelper;
import com.dungpham.movieapp.model.Movie;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dung Pham on 14/08/2017.
 * hiển thị tab phim được yêu thích
 */

public class Favorite extends Fragment {


    private RecyclerView recycler_View;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    private FavoriteDbHelper favoriteDbHelper;


    @Override
    public View onCreateView ( LayoutInflater inflater, ViewGroup container,
                               Bundle savedInstanceState ) {

        View rootView = inflater.inflate(R.layout.favorite, container, false);


        final Context context = container.getContext();

        recycler_View = (RecyclerView) rootView.findViewById(R.id.recycler_View);

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(context, movieList);

        if (getActivity().getResources().
                getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recycler_View.setLayoutManager(new GridLayoutManager(context, 2));
        }

        else {
            recycler_View.setLayoutManager(new GridLayoutManager(context, 4));
        }
        Log.e("Favorite", "" +context.toString());
        recycler_View.setItemAnimator( new DefaultItemAnimator() );
        recycler_View.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        favoriteDbHelper = new FavoriteDbHelper(context);


        getAllFavorite();

        return rootView;
    }


    private void getAllFavorite(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params){
                movieList.clear();
                movieList.addAll(favoriteDbHelper.getAllFavorite());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
