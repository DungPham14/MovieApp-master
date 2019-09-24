package com.dungpham.movieapp.sort;

import com.dungpham.movieapp.model.Movie;

import java.util.Comparator;

/**
 * Created by cpu0131 on 07/09/2017.
 */

public class SortByAlphabet {

    public static final Comparator<Movie> BY_NAME_ALPHABETICAL = new Comparator<Movie>() {
        @Override
        public int compare(Movie movie, Movie t1) {

            return movie.originalTitle.compareTo(t1.originalTitle);

        }
    };
}
