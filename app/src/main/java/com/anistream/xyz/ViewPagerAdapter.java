package com.anistream.xyz;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class    ViewPagerAdapter extends FragmentPagerAdapter {
    ViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
    Fragment fragment=null;
    if(position==0)
    {
        //fragment=AnimeFragment.newInstance(Constants.url+"page-recent-release.html?page=1&type=2");
        fragment = AnimeFragment.newInstance("https://ajax.apimovie.xyz/ajax/page-recent-release.html?page=1&type=2");
    }
    else if(position==1)
        fragment=AnimeFragment.newInstance(Constants.url);
    else
    {
        fragment=new RecentFragment();
    }
    return  fragment;
    }
    @Override
    public int getCount() {
        return 3;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 1)
        {
            title = "SUB";
        }
        else if (position == 0)
        {
            title = "DUB";
        }
        else if(position == 2)
        {
            title= "RECENT";
        }


        return title;
    }
}
