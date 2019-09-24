package com.dungpham.movieapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.dungpham.movieapp.R;
import com.dungpham.movieapp.adapter.ViewPagerAdapter;
import com.dungpham.movieapp.fragment.Favorite;
import com.dungpham.movieapp.fragment.HighestRate;
import com.dungpham.movieapp.fragment.MostPopular;


public class ScreenTab extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_tab);
        findViews();
        initViewPager();
    }

    private void findViews() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs_layout);
    }

    private void initViewPager() {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MostPopular(), "Home");
        adapter.addFragment(new HighestRate(), "Top Rate");
        adapter.addFragment(new Favorite(), "Favorite");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) { finish(); }

        return false;
    }
}
