

package com.atrainingtracker.trainingtracker.segments;

import android.content.Context;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.sensor.formater.SpeedFormatter;
import com.atrainingtracker.banalservice.sensor.formater.TimeFormatter;
import com.atrainingtracker.trainingtracker.MyHelper;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaHelper;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager.Segments;
import com.squareup.picasso.Picasso;



public class SegmentLeaderboardCursorAdapter extends CursorAdapter {
    protected static final String[] FROM = {Segments.C_ID, Segments.ATHLETE_ID, Segments.ATHLETE_PROFILE_URL, Segments.RANK, Segments.ATHLETE_NAME, Segments.ELAPSED_TIME, Segments.DISTANCE};
    protected static final int[] TO = {R.id.tvSegmentName, R.id.tvSegmentName, R.id.ivSegmentAthletePicture, R.id.tvSegmentAthleteName, R.id.tvSegmentAthleteName, R.id.tvSegmentTime, R.id.tvSegmentAverageSpeed};
    private static final int SEPARATOR = 0;
    private static final int THIS_ATHLETE = 1;
    private static final int OTHER_ATHLETE = 2;
    private static int VIEW_TYPE_COUNT = 3;
    private final String TAG = SegmentLeaderboardCursorAdapter.class.getSimpleName();
    private final boolean DEBUG = TrainingApplication.DEBUG && false;
    protected Context mContext;
    TimeFormatter timeFormatter = new TimeFormatter();
    SpeedFormatter speedFormatter = new SpeedFormatter();

    private LayoutInflater mInflater;


    public SegmentLeaderboardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private int getItemViewType(Cursor cursor) {
        long athleteId = cursor.getLong(cursor.getColumnIndex(Segments.ATHLETE_ID));

        if (athleteId == -1) {
            return SEPARATOR;
        } else if (athleteId == new StravaHelper().getAthleteId(mContext)) {
            return THIS_ATHLETE;
        } else {
            return OTHER_ATHLETE;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.i(TAG, "newView");

        ViewHolder viewHolder = new ViewHolder();
        View view = null;
        int type = getItemViewType(cursor);
        viewHolder.type = type;
        switch (type) {
            case SEPARATOR:
                view = mInflater.inflate(R.layout.segment_leaderboard_seperator, parent, false);
                break;

            case THIS_ATHLETE:
            case OTHER_ATHLETE:
                view = mInflater.inflate(R.layout.segment_leaderboard_row, parent, false);
                viewHolder.ivAthleteProfile = view.findViewById(R.id.ivSegmentAthletePicture);
                viewHolder.tvAthleteName = view.findViewById(R.id.tvSegmentAthleteName);
                viewHolder.tvSegmentTime = view.findViewById(R.id.tvSegmentTime);
                viewHolder.tvAverageSpeed = view.findViewById(R.id.tvSegmentAverageSpeed);

                if (type == THIS_ATHLETE) {
                    int myBlue = context.getResources().getColor(R.color.my_blue);
                    viewHolder.tvAverageSpeed.setTextColor(myBlue);
                    viewHolder.tvAthleteName.setTextColor(myBlue);
                    viewHolder.tvSegmentTime.setTextColor(myBlue);
                }
                break;
        }

        view.setTag(viewHolder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (DEBUG) Log.i(TAG, "bindView");

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int type = viewHolder.type;
        switch (type) {
            case SEPARATOR:
                // nothing to do
                break;

            case THIS_ATHLETE:
            case OTHER_ATHLETE:
                Picasso.with(context).load(cursor.getString(cursor.getColumnIndex(Segments.ATHLETE_PROFILE_URL))).into(viewHolder.ivAthleteProfile);

                String rank = MyHelper.formatRank(cursor.getInt(cursor.getColumnIndex(Segments.RANK)));
                viewHolder.tvAthleteName.setText(rank + ": " + cursor.getString(cursor.getColumnIndex(Segments.ATHLETE_NAME)));

                int time_s = cursor.getInt(cursor.getColumnIndex(Segments.ELAPSED_TIME));
                viewHolder.tvSegmentTime.setText(timeFormatter.format_with_units(time_s));

                double distance_m = cursor.getDouble(cursor.getColumnIndex(Segments.DISTANCE));
                double speed_mps = distance_m / time_s;
                String speed_string = speedFormatter.format_with_units(speed_mps);
                viewHolder.tvAverageSpeed.setText(speed_string);
                break;
        }

    }

    private static class ViewHolder {
        int type;

        ImageView ivAthleteProfile;
        TextView tvAthleteName, tvSegmentTime, tvAverageSpeed;
    }
}