package com.ayzconsultores.diegoshouse.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapterActivity extends FragmentStateAdapter {
    public ViewPagerAdapterActivity(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 1) {
            return new SingupFragment();
        }
        return new LoginTabFragment() ;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
