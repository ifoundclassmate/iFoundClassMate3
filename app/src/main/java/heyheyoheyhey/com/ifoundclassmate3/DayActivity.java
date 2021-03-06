package heyheyoheyhey.com.ifoundclassmate3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import heyheyoheyhey.com.ifoundclassmate3.support.ProjectUtils;

//Represents a day schedule view
public class DayActivity extends ActionBarActivity {

    private static int day;
    private static int month;
    private static int year;
    protected static User user;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        Intent intent = getIntent();
        day = intent.getIntExtra(HomeActivity.CalendarFragment.SCHEDULE_DAY, 0);
        month = intent.getIntExtra(HomeActivity.CalendarFragment.SCHEDULE_MONTH, 0);
        year = intent.getIntExtra(HomeActivity.CalendarFragment.SCHEDULE_YEAR, 0);
        user = intent.getParcelableExtra(MainActivity.USER_MESSAGE);
        System.out.println("Received date: " + day + month + year);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getDateString() {
        String date;
        String monthString;
        switch(month) {
            case 0:
                monthString = "January";
                break;
            case 1:
                monthString = "Febuary";
                break;
            case 2:
                monthString = "March";
                break;
            case 3:
                monthString = "April";
                break;
            case 4:
                monthString = "May";
                break;
            case 5:
                monthString = "June";
                break;
            case 6:
                monthString = "July";
                break;
            case 7:
                monthString = "August";
                break;
            case 8:
                monthString = "September";
                break;
            case 9:
                monthString = "October";
                break;
            case 10:
                monthString = "November";
                break;
            case 11:
                monthString = "December";
                break;
            default:
                monthString = "WTF";
                break;
        }
        date = monthString + " " + day + ", " + year;
        return date;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return new DayScheduleFragment(getDateString());

            return DayScheduleFragment.newInstance(position + 1, getDateString());
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DayScheduleFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_DATE = "DATE";
        private static String test;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DayScheduleFragment newInstance(int sectionNumber, String date) {
            DayScheduleFragment fragment = new DayScheduleFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_DATE, date);
            fragment.setArguments(args);
            test = date;
            return fragment;
        }

        public DayScheduleFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_day, container, false);
            TextView dayText = (TextView) rootView.findViewById(R.id.txtDay);

            dayText.setText(test);
            ArrayList<ScheduleItem.ScheduleTime> scheduleTimes = new ArrayList<>();
            for (ScheduleItem scheduleItem : user.getScheduleItems()) {
                scheduleTimes.addAll(scheduleItem.getScheduleForDay(day, month, year));
            }
            Collections.sort(scheduleTimes);
            RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.MondayRL1);
            // final int WIDTH_BETWEEN_HOURS = R.dimen.activity_day_hour_distance;
            final int WIDTH_BETWEEN_HOURS = ProjectUtils.dpToPx(getActivity().getApplicationContext(), 60);
            int dimen = R.dimen.activity_day_hour_distance;
            System.out.println("dimen " + dimen);
            int test = 100; // id buffer
            for (ScheduleItem.ScheduleTime scheduleTime : scheduleTimes) {
                System.out.println("Adding course to schedule: " + scheduleTime.eventName);
                LinearLayout linearLayout = new LinearLayout(getActivity());

                TextView currentScheduleView = render(scheduleTime);
                currentScheduleView.setId(currentScheduleView.getId() + test);
                currentScheduleView.setWidth(400);
                currentScheduleView.setBackgroundResource(R.color.light_blue);
                currentScheduleView.setHeight(scheduleTime.length * WIDTH_BETWEEN_HOURS / 60);
                //LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int scale = scheduleTime.startMins;
                int margin = scale * WIDTH_BETWEEN_HOURS / 60;
                //llp.setMargins(0, margin, 0, 0);
                //currentScheduleView.setLayoutParams(llp);
                linearLayout.addView(currentScheduleView);
                test++;
                TextView thisTimeText = (TextView) relativeLayout.getChildAt((int) scheduleTime.startHours - 5);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                lp.addRule(RelativeLayout.ALIGN_TOP, thisTimeText.getId());
                lp.setMargins(0, margin, 0, 0);
                linearLayout.setLayoutParams(lp);
                relativeLayout.addView(linearLayout);
            }
            return rootView;
        }

        private TextView render(ScheduleItem.ScheduleTime scheduleTime) {
            TextView retVal = new TextView(getActivity().getApplicationContext());
            String textViewText = "";
            if (scheduleTime.eventName != null) textViewText += scheduleTime.eventName + "\n";
            if (scheduleTime.description != null) textViewText += scheduleTime.description;
            int startTimeHours = scheduleTime.startHours;
            String startTimeMins;
            if (scheduleTime.startMins < 10) startTimeMins = "0" + Integer.toString(scheduleTime.startMins);
            else startTimeMins = Integer.toString(scheduleTime.startMins);
            int endTimeHours = scheduleTime.endHours;
            String endTimeMins;
            if (scheduleTime.endMins < 10) endTimeMins = "0" + Integer.toString(scheduleTime.endMins);
            else endTimeMins = Integer.toString(scheduleTime.endMins);

            String startTimeString;
            String endTimeString;
            if (startTimeHours == 12) {
                startTimeString = startTimeHours + ":" + startTimeMins + " pm";
            } else if (startTimeHours > 12) {
                startTimeHours -= 12;
                startTimeString = startTimeHours + ":" + startTimeMins + " pm";
            }
            else startTimeString = startTimeHours + ":" + startTimeMins + " am";

            if (endTimeHours == 12) {
                endTimeString = endTimeHours + ":" + endTimeMins + " pm";
            } else if (endTimeHours > 12) {
                endTimeHours -= 12;
                endTimeString = endTimeHours + ":" + endTimeMins + " pm";
            }
            else endTimeString = endTimeHours + ":" + endTimeMins + " am";

            retVal.setText(textViewText + startTimeString + " - " + endTimeString);
            return retVal;
        }

    }

}
