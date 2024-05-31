

package com.atrainingtracker.trainingtracker.segments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.sensor.formater.DistanceFormatter;
import com.atrainingtracker.banalservice.sensor.formater.TimeFormatter;
import com.atrainingtracker.trainingtracker.activities.SegmentDetailsActivity;
import com.atrainingtracker.trainingtracker.MyHelper;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.MyMapViewHolder;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.Roughness;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaHelper;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaSegmentsHelper;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager.Segments;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class StarredSegmentsCursorAdapter extends CursorAdapter {
    protected static final String[] FROM = {Segments.SEGMENT_ID, Segments.C_ID, Segments.SEGMENT_NAME, Segments.DISTANCE, Segments.AVERAGE_GRADE, Segments.CLIMB_CATEGORY, Segments.PR_TIME, Segments.OWN_RANK, Segments.PR_DATE, Segments.LAST_UPDATED};
    private final String TAG = StarredSegmentsCursorAdapter.class.getSimpleName();
    private final boolean DEBUG = TrainingApplication.DEBUG && false;
    protected Activity mActivity;
    protected Context mContext;
    // protected static final int[]    TO   = {R.id.tvSegmentName,  R.id.tvSegmentName, R.id.tvSegmentName,    R.id.tvSegmentDistance, R.id.tvSegmentAverageGrade, R.id.tvSegmentClimbCategory, R.id.tvSegmentPRTime, R.id.tvSegmentRank, R.id.tvSegmentPRDate, R.id.tvSegmentLastUpdated};
    ShowSegmentDetailsInterface mShowSegmentDetailsListener = null;
    DistanceFormatter distanceFormatter = new DistanceFormatter();
    TimeFormatter timeFormatter = new TimeFormatter();
    SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // 2013-03-29T13:49:35Z
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");               // 2013-03-29
    StravaSegmentsHelper mStravaSegmentsHelper;
    private boolean isPlayServiceAvailable = true;

    public StarredSegmentsCursorAdapter(Activity activity, Cursor cursor, StravaSegmentsHelper stravaSegmentsHelper, ShowSegmentDetailsInterface showSegmentDetailsInterface) {
        super(activity, cursor, 0);

        mContext = activity;
        mActivity = activity;
        mShowSegmentDetailsListener = showSegmentDetailsInterface;

        mStravaSegmentsHelper = stravaSegmentsHelper;

        isPlayServiceAvailable = checkPlayServices();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.i(TAG, "newView");

        View row = LayoutInflater.from(context).inflate(R.layout.segment_list_row, null);

        ViewHolder viewHolder = new ViewHolder(null, null);

        // set all the views of the view holder
        viewHolder.tvName = row.findViewById(R.id.tvSegmentName);
        viewHolder.tvDistance = row.findViewById(R.id.tvSegmentDistance);
        viewHolder.tvAverageGrade = row.findViewById(R.id.tvSegmentAverageGrade);
        viewHolder.tvClimbCategory = row.findViewById(R.id.tvSegmentClimbCategory);
        viewHolder.tvPRTime = row.findViewById(R.id.tvSegmentPRTime);
        viewHolder.tvRank = row.findViewById(R.id.tvSegmentRank);
        viewHolder.tvPRDate = row.findViewById(R.id.tvSegmentPRDate);
        viewHolder.mapView = row.findViewById(R.id.starred_segments_mapView);
        viewHolder.bUpdate = row.findViewById(R.id.bUpdate);

        viewHolder.llSegmentsHeader = row.findViewById(R.id.llSegmentsHeader);

        viewHolder.initializeMapView();

        row.setTag(viewHolder);

        return row;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        final long segmentId = cursor.getLong(cursor.getColumnIndex(Segments.SEGMENT_ID));
        viewHolder.segmentId = segmentId;

        viewHolder.tvName.setText(cursor.getString(cursor.getColumnIndex(Segments.SEGMENT_NAME)));
        viewHolder.tvDistance.setText(distanceFormatter.format_with_units(cursor.getDouble(cursor.getColumnIndex(Segments.DISTANCE))));
        viewHolder.tvAverageGrade.setText(String.format("%.1f %%", cursor.getDouble(cursor.getColumnIndex(Segments.AVERAGE_GRADE))));
        viewHolder.tvClimbCategory.setText(StravaHelper.translateClimbCategory(cursor.getInt(cursor.getColumnIndex(Segments.CLIMB_CATEGORY))));
        if (!cursor.isNull(cursor.getColumnIndex(Segments.PR_TIME))) {
            viewHolder.tvPRTime.setText(timeFormatter.format_with_units(cursor.getInt(cursor.getColumnIndex(Segments.PR_TIME))));
        } else {
            viewHolder.tvPRTime.setText("");
        }
        if (!cursor.isNull(cursor.getColumnIndex(Segments.OWN_RANK))) {
            viewHolder.tvRank.setText(MyHelper.formatRank(cursor.getInt(cursor.getColumnIndex(Segments.OWN_RANK))));
        } else {
            viewHolder.tvRank.setText(R.string.TODO);
        }
        String prDate = cursor.getString(cursor.getColumnIndex(Segments.PR_DATE));
        if (prDate != null) {
            try {
                Date date = dateAndTimeFormat.parse(prDate);
                prDate = dateFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            prDate = context.getString(R.string.tomorrow);
        }
        viewHolder.tvPRDate.setText(prDate);
        if (mStravaSegmentsHelper.isLeaderboardUpdating(segmentId)) {
            viewHolder.bUpdate.setText("updating");
        } else {
            viewHolder.bUpdate.setText("updated: " + cursor.getString(cursor.getColumnIndex(Segments.LAST_UPDATED)));  // TODO: more detailed???
        }
        viewHolder.bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewHolder.bUpdate.setText("updating");
                mStravaSegmentsHelper.getSegmentLeaderboard(segmentId);
            }
        });

        if (isPlayServiceAvailable) {
            viewHolder.mapView.setVisibility(View.VISIBLE);
            if (viewHolder.map != null) {
                viewHolder.showSegmentOnMap(segmentId);
            }
        } else {
            viewHolder.mapView.setVisibility(View.GONE);
        }

        viewHolder.llSegmentsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowSegmentDetailsListener.startSegmentDetailsActivity(segmentId, SegmentDetailsActivity.SelectedFragment.LEADERBOARD);
            }
        });

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return (apiAvailability.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS);
    }


    public interface ShowSegmentDetailsInterface {
        void startSegmentDetailsActivity(long segmentId, SegmentDetailsActivity.SelectedFragment selectedFragment);
    }

    public class ViewHolder
            extends MyMapViewHolder
            implements OnMapReadyCallback {

        long segmentId;
        TextView tvName;
        TextView tvDistance;
        TextView tvAverageGrade;
        TextView tvClimbCategory;
        TextView tvPRTime;
        TextView tvRank;
        TextView tvPRDate;
        Button bUpdate;
        LinearLayout llSegmentsHeader;

        public ViewHolder(GoogleMap map, MapView mapView) {
            super(map, mapView);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            map = googleMap;
            showSegmentOnMap(segmentId);
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }

        public void showSegmentOnMap(final long segmentId) {
            if (DEBUG) Log.i(TAG, "showSegmentOnMap: segmentId=" + segmentId);

            if (map == null) {
                mapView.setVisibility(View.GONE);
            } else {
                mapView.setVisibility(View.VISIBLE);

                // first, configure the map
                map.getUiSettings().setMapToolbarEnabled(false);
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mShowSegmentDetailsListener.startSegmentDetailsActivity(segmentId, SegmentDetailsActivity.SelectedFragment.MAP);
                    }
                });

                ((TrainingApplication) mActivity.getApplication()).segmentOnMapHelper.showSegmentOnMap(mContext, this, segmentId, Roughness.ALL, true, false);

                if (DEBUG) Log.i(TAG, "end of showSegmentOnMap()");
            }
        }
    }
}