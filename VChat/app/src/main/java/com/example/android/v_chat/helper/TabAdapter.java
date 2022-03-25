package com.example.android.v_chat.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.v_chat.fragments.Chat_Fragment;
import com.example.android.v_chat.fragments.Request_Fragment;
import com.example.android.v_chat.fragments.Status_Fragment;

public class TabAdapter extends FragmentPagerAdapter {
    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position)
        {
            case 0:
                Chat_Fragment chat_fragment= new Chat_Fragment();
                return chat_fragment;
            case 1:
                Status_Fragment status_fragment= new Status_Fragment();
                return status_fragment;
            case 2:
                Request_Fragment request_fragment=new Request_Fragment();
                return request_fragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Chats";
            case 1:
                return "Status";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
