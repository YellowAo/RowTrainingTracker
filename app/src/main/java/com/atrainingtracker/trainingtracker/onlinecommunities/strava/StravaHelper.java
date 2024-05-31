

package com.atrainingtracker.trainingtracker.onlinecommunities.strava;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.atrainingtracker.banalservice.BSportType;
import com.atrainingtracker.banalservice.database.SportTypeDatabaseManager;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.onlinecommunities.BaseGetAccessTokenActivity;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class StravaHelper {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer";
    protected static final String ID = "id";
    private static final String TAG = StravaHelper.class.getSimpleName();
    private static final boolean DEBUG = true; // TrainingApplication.DEBUG & true;

    public static String translateClimbCategory(int climbCategory) {
        switch (climbCategory) {
            case 1:
                return "cat. 4";

            case 2:
                return "cat. 3";

            case 3:
                return "cat. 2";

            case 4:
                return "cat. 1";

            case 5:
                return "HC";

            default:
                return "";
        }
    }

    protected static void storeJSONData(JSONObject jsonObject) {
        if (DEBUG) {Log.i(TAG, "string JSON response: " + jsonObject);}
        try {
            if (jsonObject.has("athlete")) {
                JSONObject athlete = jsonObject.getJSONObject("athlete");
                int athleteId = athlete.getInt("id");
                TrainingApplication.setStravaAthleteId(athleteId);
            }
            TrainingApplication.setStravaAccessToken(jsonObject.getString(BaseGetAccessTokenActivity.ACCESS_TOKEN));
            TrainingApplication.setStravaRefreshToken(jsonObject.getString(BaseGetAccessTokenActivity.REFRESH_TOKEN));
            TrainingApplication.setStravaTokenExpiresAt(jsonObject.getInt(BaseGetAccessTokenActivity.EXPIRES_AT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected static String getRefreshUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(BaseGetAccessTokenActivity.HTTPS)
                .authority(StravaGetAccessTokenActivity.STRAVA_AUTHORITY)
                .appendPath(BaseGetAccessTokenActivity.OAUTH)
                .appendPath(BaseGetAccessTokenActivity.TOKEN)
                .appendQueryParameter(BaseGetAccessTokenActivity.CLIENT_ID, StravaGetAccessTokenActivity.MY_CLIENT_ID)
                .appendQueryParameter(BaseGetAccessTokenActivity.CLIENT_SECRET, StravaGetAccessTokenActivity.MY_CLIENT_SECRET)
                .appendQueryParameter(BaseGetAccessTokenActivity.GRANT_TYPE, BaseGetAccessTokenActivity.REFRESH_TOKEN)
                .appendQueryParameter(BaseGetAccessTokenActivity.REFRESH_TOKEN, TrainingApplication.getStravaRefreshToken());
        return builder.build().toString();
    }

    public static String getRefreshedAccessToken() {
        if (DEBUG) Log.i(TAG, "getRefreshedAccessToken()");
        // first, check if we really need a new access token
        if (System.currentTimeMillis()/1000 < TrainingApplication.getStravaTokenExpiresAt()) {
            return TrainingApplication.getStravaAccessToken();
        }

        String refreshUrl = getRefreshUrl();
        if (refreshUrl == null) {
            return null;
        }

        HttpPost httpPost = new HttpPost(refreshUrl);
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (DEBUG) Log.i(TAG, "HTTP status: " + httpResponse.getStatusLine());

            String response = EntityUtils.toString(httpResponse.getEntity());
            if (DEBUG) Log.d(TAG, "response: " + response);
            // Uri uri = Uri.parse(response);
            JSONObject responseJson = new JSONObject(response);
            storeJSONData(responseJson);

            if (responseJson.has(BaseGetAccessTokenActivity.ACCESS_TOKEN)) {
                // String tokenType   = responseJson.getString(TOKEN_TYPE);
                return responseJson.getString(BaseGetAccessTokenActivity.ACCESS_TOKEN);
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return null;
    }

    /* returns the athleteId stored in the shared preferences.  If this is not available (== 0), get the id from strava in the background and also get the segments
     * But this method still returns 0. */
    public int getAthleteId(Context context) {
        int athleteId = TrainingApplication.getStravaAthleteId();

        if (athleteId == 0) {
            new GetAthleteIdFromStravaAsyncTask(context).execute();
        }

        return athleteId;
    }

    class GetAthleteIdFromStravaAsyncTask extends AsyncTask<Void, Void, Void> {
        Context mContext;

        GetAthleteIdFromStravaAsyncTask(Context context) {
            mContext = context;
        }

        protected Void doInBackground(Void... foo) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("https://www.strava.com/api/v3/athlete");
            httpGet.addHeader(AUTHORIZATION, BEARER + " " + getRefreshedAccessToken());

            HttpResponse httpResponse;
            try {
                httpResponse = httpClient.execute(httpGet);

                String response = EntityUtils.toString(httpResponse.getEntity());
                if (DEBUG) Log.d(TAG, "getAthleteId response: " + response);

                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.has(ID)) {
                    if (DEBUG) Log.i(TAG, "got own athleteId: " + jsonObject.getInt(ID));
                    TrainingApplication.setStravaAthleteId(jsonObject.getInt(ID));

                    // ok, we got a new strava id, so we update the segments database
                    if (!SegmentsDatabaseManager.doesDatabaseExist(mContext)) {  // but there is not yet a database for the segments
                        StravaSegmentsHelper stravaSegmentsHelper = new StravaSegmentsHelper(mContext);
                        stravaSegmentsHelper.getStarredStravaSegments(SportTypeDatabaseManager.getSportTypeId(BSportType.ROWING));
                        stravaSegmentsHelper.getStarredStravaSegments(SportTypeDatabaseManager.getSportTypeId(BSportType.RUN));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
