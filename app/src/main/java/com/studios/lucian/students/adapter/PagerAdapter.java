package com.studios.lucian.students.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.studios.lucian.students.fragment.GradesFragment;
import com.studios.lucian.students.fragment.PresencesFragment;
import com.studios.lucian.students.model.Student;

/**
 * Created with Love by Lucian and Pi on 21.06.2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private final Student currentStudent;

    public PagerAdapter(FragmentManager fm, Student student) {
        super(fm);
        this.currentStudent = student;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frag = null;
        switch (position) {
            case 0:
                frag = GradesFragment.newInstance(currentStudent.getMatricol());
                break;
            case 1:
                frag = PresencesFragment.newInstance(currentStudent.getMatricol());
                break;
        }
        return frag;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = " ";
        switch (position) {
            case 0:
                title = "Grades";
                break;
            case 1:
                title = "Presences";
                break;
        }

        return title;
    }
}
