package com.dungpham.movieapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dungpham.movieapp.BuildConfig;
import com.dungpham.movieapp.R;
import com.dungpham.movieapp.adapter.MoviesAdapter;
import com.dungpham.movieapp.api.Client;
import com.dungpham.movieapp.api.Service;
import com.dungpham.movieapp.data.FavoriteDbHelper;
import com.dungpham.movieapp.model.Movie;
import com.dungpham.movieapp.model.MoviesResponse;
import com.dungpham.movieapp.sort.SortByAlphabet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    private FavoriteDbHelper favoriteDbHelper;
    private AppCompatActivity activity = MainActivity.this;
    public static final String LOG_TAG = MoviesAdapter.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        initViews();


    }

    /**
     * menu setting
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, ScreenTab.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;

    }

    /**
     * cài đặt màn hình chính
     **/
    private void initViews() {


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);


        // thay doi view khi xoay man hinh
        if (getActivity().getResources().
                getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        favoriteDbHelper = new FavoriteDbHelper(activity);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_content);
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(
                        MainActivity.this,
                        "Movies Refreshed",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        checkSortOrder();

    }


    /**
     * load sorting favourite movie
     **/
    private void initViews2() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);

        if (getActivity().getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        favoriteDbHelper = new FavoriteDbHelper(activity);

        getAllFavorite();
    }


    /**
     * load most popular
     **/
    private void loadJSON() {

        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please obtain API Key firstly from themoviedb.org",
                        Toast.LENGTH_SHORT
                ).show();
                pd.dismiss();
                return;
            }

//            Client Client = new Client();

            Service apiService = Client.getClient().create(Service.class);

            Call<MoviesResponse> call = apiService.getPopularMovies(
                    BuildConfig.THE_MOVIE_DB_API_TOKEN
            );
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                    List<Movie> movies = response.body().getResults();

                    Collections.sort(movies, SortByAlphabet.BY_NAME_ALPHABETICAL);

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);

                    if (swipeContainer.isRefreshing()) {

                        swipeContainer.setRefreshing(false);

                    }

                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {

                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Error Fetching Data!",
                            Toast.LENGTH_SHORT
                    ).show();

                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * lấy dữ liệu phim có lương vote cao nhat
     */
    private void loadJSON1() {

        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(this,
                        "Please obtain API Key firstly from themoviedb.org",
                        Toast.LENGTH_SHORT
                ).show();
                pd.dismiss();
                return;
            }

            Service apiService = Client.getClient().create(Service.class);

            Call<MoviesResponse> call = apiService.getTopRatedMovies(
                    BuildConfig.THE_MOVIE_DB_API_TOKEN
            );
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(
                            MainActivity.this,
                            "Error Fetching Data!",
                            Toast.LENGTH_SHORT
                    ).show();

                }
            });
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(
                    this,
                    e.toString(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(LOG_TAG, "Preferences updated");
        checkSortOrder();
    }

    private void checkSortOrder() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String sortOrder = preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );

        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {
            Log.d(LOG_TAG, "Sorting by most popular");
            loadJSON();
        } else if (sortOrder.equals(this.getString(R.string.favorite))) {
            Log.d(LOG_TAG, "Sorting by favorite");
            initViews2();
        } else {
            Log.d(LOG_TAG, "Sorting by vote average");
            loadJSON1();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSortOrder();
    }

    private void getAllFavorite() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                movieList.clear();
                movieList.addAll(favoriteDbHelper.getAllFavorite());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

}
