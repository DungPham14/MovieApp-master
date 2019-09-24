package com.dungpham.movieapp.fragment;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.dungpham.movieapp.BuildConfig;
import com.dungpham.movieapp.R;
import com.dungpham.movieapp.adapter.MoviesAdapter;
import com.dungpham.movieapp.api.Client;
import com.dungpham.movieapp.api.Service;
import com.dungpham.movieapp.data.FavoriteDbHelper;
import com.dungpham.movieapp.model.Movie;
import com.dungpham.movieapp.model.MoviesResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dung Pham on 14/08/2017.
 *
 * hiển thị tab phim có lương rate cao nhất
 */

public class HighestRate extends Fragment {


    private RecyclerView recyclerView;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    private MoviesAdapter adapter;
    private List<Movie> movieList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.highest_rate, container, false);

        final Context context = container.getContext();


        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_View);
        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(context, movieList);

        if (getActivity().getResources().
                getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        }
        else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.main_content);

        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(
                        context,
                        "Please obtain API Key firstly from themoviedb.org",
                        Toast.LENGTH_SHORT
                ).show();
                pd.dismiss();

            }

//            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);

            Call<MoviesResponse> call = apiService.getTopRatedMovies(
                    BuildConfig.THE_MOVIE_DB_API_TOKEN
            );

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                    Log.e("HighestRate","response ="+new Gson().toJson(response));
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter( context, movies ) );
                    Log.e("HighestRate","List<Movie> ="+new Gson().toJson(movies));
                    recyclerView.smoothScrollToPosition(0);

                    if (swipeContainer.isRefreshing() ) {
                        swipeContainer.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.e("HighestRate","eror ="+t.getLocalizedMessage());
                    Toast.makeText(
                            context,
                            "Error Fetching Data!",
                            Toast.LENGTH_SHORT
                    ).show();

                }
            });
        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(
                    context,
                    e.toString(),
                    Toast.LENGTH_SHORT
            ).show();
        }

        return rootView;
    }

}