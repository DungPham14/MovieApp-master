package com.dungpham.movieapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dungpham.movieapp.BuildConfig;
import com.dungpham.movieapp.R;
import com.dungpham.movieapp.adapter.TrailerAdapter;
import com.dungpham.movieapp.api.Client;
import com.dungpham.movieapp.api.Service;
import com.dungpham.movieapp.data.FavoriteDbHelper;
import com.dungpham.movieapp.model.Movie;
import com.dungpham.movieapp.model.Trailer;
import com.dungpham.movieapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailActivity extends AppCompatActivity {

    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;
    private RecyclerView recyclerView;
    private TrailerAdapter adapter;
    private List<Trailer> trailerList;
    private FavoriteDbHelper favoriteDbHelper;
    private Movie favorite;
    private final AppCompatActivity activity = DetailActivity.this;
    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initCollapsingToolbar();

        imageView = (ImageView) findViewById(R.id.thumbnail_image_header);
        nameOfMovie = (TextView) findViewById(R.id.title);
        plotSynopsis = (TextView) findViewById(R.id.plotsynopsis);
        userRating = (TextView) findViewById(R.id.userrating);
        releaseDate = (TextView) findViewById(R.id.releasedate);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

            movie = getIntent().getParcelableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();

            String poster = "https://image.tmdb.org/t/p/w500" + thumbnail;


            // load ảnh gif (poster)
            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.load)
                    .into(imageView);

            nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);

        }

        else{
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }


//        SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(getApplicationContext());

        /**
         *
         * nút like
         * */
        MaterialFavoriteButton materialFavoriteButtonNice =
                (MaterialFavoriteButton) findViewById(R.id.favorite_button);
        materialFavoriteButtonNice.setOnFavoriteChangeListener(
                new MaterialFavoriteButton.OnFavoriteChangeListener(){
                    @Override
                    public void onFavoriteChanged( MaterialFavoriteButton buttonView,
                                                   boolean _favorite ){

                        favorite = new Movie();
                        favorite.setOriginalTitle(movieName);
                        if (_favorite){

                            SharedPreferences.Editor editor = getSharedPreferences(
                                    "com.dungpham.movieapp.DetailActivity", MODE_PRIVATE
                            ).edit();

                            editor.putBoolean("Favorite Added", true);
                            editor.commit();
                            saveFavorite();
                            Snackbar.make(
                                    buttonView,
                                    "Added to Favorite ",
                                    Snackbar.LENGTH_SHORT
                            ).show();
                        }

                        else{

                            int movie_id = getIntent().getExtras().getInt("id");
                            favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                            favoriteDbHelper.deleteFavorite(movie_id);

                            SharedPreferences.Editor editor = getSharedPreferences(
                                    "com.dungpham.movieapp.DetailActivity", MODE_PRIVATE
                            ).edit();

                            editor.putBoolean("Favorite Removed", true);
                            editor.commit();
                            delete_Favorite();
                            Snackbar.make(
                                    buttonView,
                                    "Removed from Favorite",
                                    Snackbar.LENGTH_SHORT
                            ).show();

                        }

                    }
                }
        );

        initViews();

    }


    /**
     *
     * hàm Collapsing Toolbar
     *
     * */
    private void initCollapsingToolbar(){
        final CollapsingToolbarLayout collapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener(){
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset){
                if (scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0){
                    collapsingToolbarLayout.setTitle(getString(R.string.movie_details));
                    isShow = true;
                }else if (isShow){
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }


    /**
     *
     * cài đặt view
     * */
    private void initViews(){

        trailerList = new ArrayList<>();
        adapter = new TrailerAdapter(this, trailerList);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadJSON();

    }



    /**
     *  lấy json
     *
     **/
    private void loadJSON(){

        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){

                Toast.makeText(
                        getApplicationContext(),
                        "Please obtain your API Key from themoviedb.org",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

//            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);

            Call<TrailerResponse> call = apiService.getMovieTrailer(
                    movie_id,
                    BuildConfig.THE_MOVIE_DB_API_TOKEN
            );

            call.enqueue(new Callback<TrailerResponse>() {
                @Override
                public void onResponse(Call<TrailerResponse> call,
                                       Response<TrailerResponse> response) {
                    List<Trailer> trailer = response.body().getResults();
                    recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailer));
                    recyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<TrailerResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(DetailActivity.this
                            , "Error fetching trailer data"
                            , Toast.LENGTH_SHORT)
                            .show();

                }
            });

        }
        catch (Exception e){
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     *
     * lưu phim đã thích
     * **/
    public void saveFavorite(){
        favoriteDbHelper = new FavoriteDbHelper(activity);
        favorite = new Movie();

        Double rate = movie.getVoteAverage();

        favorite.setId(movie_id);
        favorite.setOriginalTitle(movieName);
        favorite.setPosterPath(thumbnail);
        favorite.setVoteAverage(rate);
        favorite.setOverview(synopsis);

        favoriteDbHelper.addFavorite(favorite);
    }

    /**
     *
     * xóa phim đã thích
     * **/
    public void delete_Favorite(){
        favoriteDbHelper = new FavoriteDbHelper(activity);
        favorite = new Movie();

        Double rate = movie.getVoteAverage();

        favorite.setId(movie_id);
        favorite.setOriginalTitle(movieName);
        favorite.setPosterPath(thumbnail);
        favorite.setVoteAverage(rate);
        favorite.setOverview(synopsis);

        favoriteDbHelper.deleteFavorite(favorite.getId());
    }


}
