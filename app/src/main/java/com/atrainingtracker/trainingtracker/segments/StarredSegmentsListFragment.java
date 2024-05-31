

package com.atrainingtracker.trainingtracker.segments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.database.SportTypeDatabaseManager;
import com.atrainingtracker.trainingtracker.activities.SegmentDetailsActivity;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaSegmentsHelper;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaSegmentsIntentService;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager.Segments;
import com.google.android.gms.maps.GoogleMap;


public class StarredSegmentsListFragment extends SwipeRefreshListFragment {

    private static final String TAG = StarredSegmentsListFragment.class.getSimpleName();
    private static final boolean DEBUG = TrainingApplication.DEBUG && false;

    private static final String SPORT_TYPE_ID = "SPORT_TYPE_ID";

    protected SQLiteDatabase mDb;
    protected Cursor mStarredSegmentsCursor;
    protected CursorAdapter mStarredSegmentsCursorAdapter;
    protected ListView mListView;
    protected long mSportTypeId;
    protected StravaSegmentsHelper mStravaSegmentsHelper;
    protected StartSegmentDetailsActivityInterface startSegmentDetailsActivityInterface;
    // actions will be added later
    protected IntentFilter mSegmentUpdateStartedFilter = new IntentFilter();
    protected IntentFilter mUpdateSegmentsListFilter = new IntentFilter();
    protected IntentFilter mUpdatingSegmentsCompleteFilter = new IntentFilter();
    BroadcastReceiver mSegmentUpdateStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(StravaSegmentsIntentService.SPORT_TYPE_ID)
                    && mSportTypeId == intent.getLongExtra(StravaSegmentsIntentService.SPORT_TYPE_ID, -1)) {
                setRefreshing(true);
            }
        }
    };
    BroadcastReceiver mUpdateSegmentsListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.i(TAG, "update segment list");

            if (intent.hasExtra(StravaSegmentsIntentService.SPORT_TYPE_ID)
                    && mSportTypeId == intent.getLongExtra(StravaSegmentsIntentService.SPORT_TYPE_ID, -1)) {
                updateCursor();
            }
        }
    };
    BroadcastReceiver mUpdatingSegmentsCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.i(TAG, "updating segments list completed");

            long sportTypeId = intent.getLongExtra(StravaSegmentsIntentService.SPORT_TYPE_ID, -1);
            if (mSportTypeId == sportTypeId) {

                if (isRefreshing()
                        && intent.hasExtra(StravaSegmentsIntentService.RESULT_MESSAGE)
                        && intent.getStringExtra(StravaSegmentsIntentService.RESULT_MESSAGE) != null) {
                    Toast.makeText(context, context.getString(R.string.updating_starred_segments_failed) + intent.getStringExtra(StravaSegmentsIntentService.RESULT_MESSAGE), Toast.LENGTH_LONG).show();
                }

                onRefreshComplete();
            }
        }
    };
    private AbsListView.RecyclerListener mRecycleListener = new AbsListView.RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            StarredSegmentsCursorAdapter.ViewHolder holder = (StarredSegmentsCursorAdapter.ViewHolder) view.getTag();
            if (holder != null && holder.map != null) {
                // Clear the map and free up resources by changing the map type to none
                holder.map.clear();
                holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }

        }
    };

    public static StarredSegmentsListFragment newInstance(long sportTypeId) {
        if (DEBUG) Log.i(TAG, "newInstance()");

        StarredSegmentsListFragment starredSegmentsListFragment = new StarredSegmentsListFragment();

        Bundle args = new Bundle();
        args.putLong(SPORT_TYPE_ID, sportTypeId);
        starredSegmentsListFragment.setArguments(args);

        return starredSegmentsListFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (DEBUG) Log.i(TAG, "onAttach");

        try {
            startSegmentDetailsActivityInterface = (StartSegmentDetailsActivityInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement StartSegmentDetailsActivityInterface");
        }
    }

    /**
     * Called when the fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.i(TAG, "onCreate");

        mStravaSegmentsHelper = new StravaSegmentsHelper(getContext());

        mSegmentUpdateStartedFilter.addAction(StravaSegmentsIntentService.SEGMENT_UPDATE_STARTED_INTENT);

        mUpdateSegmentsListFilter.addAction(StravaSegmentsIntentService.NEW_STARRED_SEGMENT_INTENT);
        mUpdateSegmentsListFilter.addAction(StravaSegmentsIntentService.LEADERBOARD_UPDATE_COMPLETE_INTENT);

        mUpdatingSegmentsCompleteFilter.addAction(StravaSegmentsIntentService.SEGMENTS_UPDATE_COMPLETE_INTENT);

        mSportTypeId = getArguments().getLong(SPORT_TYPE_ID);

        // setHasOptionsMenu(true);
    }

    // BEGIN_INCLUDE (setup_views)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStarredSegmentsCursorAdapter = new StarredSegmentsCursorAdapter(getActivity(), mStarredSegmentsCursor, mStravaSegmentsHelper, new StarredSegmentsCursorAdapter.ShowSegmentDetailsInterface() {
            @Override
            public void startSegmentDetailsActivity(long segmentId, SegmentDetailsActivity.SelectedFragment selectedFragment) {
                if (DEBUG) Log.i(TAG, "startSegmentDetailsActivity(" + segmentId + ")");

                Bundle bundle = new Bundle();
                bundle.putLong(Segments.SEGMENT_ID, segmentId);
                bundle.putString(SegmentDetailsActivity.SELECTED_FRAGMENT, selectedFragment.name());
                Intent segmentDetailsIntent = new Intent(getContext(), SegmentDetailsActivity.class);
                segmentDetailsIntent.putExtras(bundle);
                startActivity(segmentDetailsIntent);
            }
        });
        setListAdapter(mStarredSegmentsCursorAdapter);

        mListView = getListView();
        mListView.setRecyclerListener(mRecycleListener);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DEBUG) Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");

        // seems to not work???
        if (mStravaSegmentsHelper.isSegmentListUpdating(mSportTypeId)) {
            if (DEBUG) Log.d(TAG, "we have to setRefreshing(true)");
            setRefreshing(true);
        }

        mDb = SegmentsDatabaseManager.getInstance().getOpenDatabase();
        updateCursor();

        getContext().registerReceiver(mSegmentUpdateStartedReceiver, mSegmentUpdateStartedFilter);
        getContext().registerReceiver(mUpdateSegmentsListReceiver, mUpdateSegmentsListFilter);
        getContext().registerReceiver(mUpdatingSegmentsCompleteReceiver, mUpdatingSegmentsCompleteFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.i(TAG, "onPause()");

        SegmentsDatabaseManager.getInstance().closeDatabase();

        getContext().unregisterReceiver(mSegmentUpdateStartedReceiver);
        getContext().unregisterReceiver(mUpdateSegmentsListReceiver);
        getContext().unregisterReceiver(mUpdatingSegmentsCompleteReceiver);
    }

    /**
     * Called first time user clicks on the menu button
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu");

        inflater.inflate(R.menu.segments_list_menu, menu);
    }

    /* Called when an options item is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.i(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.itemDeleteAllSegments:
                Log.i(TAG, "option delete all segments");
                SegmentsDatabaseManager.deleteAllTables(getContext());

                updateCursor();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (DEBUG) Log.i(TAG, "onListItemClick, position=" + position + ", id=" + id);

        mStarredSegmentsCursor.moveToPosition(position);
        int segmentId = mStarredSegmentsCursor.getInt(mStarredSegmentsCursor.getColumnIndex(Segments.SEGMENT_ID));
        if (DEBUG) Log.i(TAG, "segmentId=" + segmentId);

        startSegmentDetailsActivityInterface.startSegmentDetailsActivity(segmentId, SegmentDetailsActivity.SelectedFragment.LEADERBOARD);
    }

    protected void updateCursor() {
        if (DEBUG)
            Log.i(TAG, "updateCursor, activity_type=" + SportTypeDatabaseManager.getStravaName(mSportTypeId));

        mStarredSegmentsCursor = mDb.query(Segments.TABLE_STARRED_SEGMENTS,
                StarredSegmentsCursorAdapter.FROM,           // columns
                Segments.ACTIVITY_TYPE + "=?",               // selection
                new String[]{SportTypeDatabaseManager.getStravaName(mSportTypeId)},  // selectionArgs
                null, null,                                  // groupBy, having
                Segments.OWN_RANK + " ASC");                 // orderBy  TODO: what if you never rode/run this segment? => they are at the very top

        if (DEBUG)
            Log.i(TAG, "got new cursor with " + mStarredSegmentsCursor.getCount() + " entries");

        mStarredSegmentsCursorAdapter.changeCursor(mStarredSegmentsCursor);
        mStarredSegmentsCursorAdapter.notifyDataSetChanged();
    }


    private void initiateRefresh() {
        if (DEBUG) Log.i(TAG, "initiateRefresh");

        new StravaSegmentsHelper(getContext()).getStarredStravaSegments(mSportTypeId);
    }

    private void onRefreshComplete() {
        if (DEBUG) Log.i(TAG, "onRefreshComplete");

        updateCursor();

        // Stop the refreshing indicator
        setRefreshing(false);
    }

    public interface StartSegmentDetailsActivityInterface {
        void startSegmentDetailsActivity(int segmentId, SegmentDetailsActivity.SelectedFragment selectedFragment);
    }
}
