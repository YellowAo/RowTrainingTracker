

package com.atrainingtracker.trainingtracker.onlinecommunities.strava;

import android.net.Uri;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.onlinecommunities.BaseGetAccessTokenActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class StravaGetAccessTokenActivity
        extends BaseGetAccessTokenActivity {
    protected static final String STRAVA_AUTHORITY = "www.strava.com";
    protected static final String MY_CLIENT_ID = "344";
    protected static final String MY_CLIENT_SECRET = "272b5ca4ba09a932e73ef2574162f04d7f41a643";
    private static final String TAG = "StravaGetAccessTokenActivity";
    private static final boolean DEBUG = true;

    @Override
    protected String getAuthorizationUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(STRAVA_AUTHORITY)
                .appendPath(OAUTH)
                .appendPath(MOBILE)
                .appendPath(AUTHORIZE)
                .appendQueryParameter(CLIENT_ID, MY_CLIENT_ID)
                .appendQueryParameter(REDIRECT_URI, MY_REDIRECT_URI)
                .appendQueryParameter(RESPONSE_TYPE, CODE)
                .appendQueryParameter(APPROVAL_PROMPT, AUTO)
                .appendQueryParameter(SCOPE, READ + ',' + ACTIVITY_WRITE + ',' + ACTIVITY_READ_ALL + ',' + PROFILE_READ_ALL);
        return builder.build().toString();
    }

    @Override
    protected String getAccessUrl(String code) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(STRAVA_AUTHORITY)
                .appendPath(OAUTH)
                .appendPath(TOKEN)
                .appendQueryParameter(CLIENT_ID, MY_CLIENT_ID)
                .appendQueryParameter(CLIENT_SECRET, MY_CLIENT_SECRET)
                .appendQueryParameter(CODE, code);
        return builder.build().toString();
    }

    @Override
    protected String getAcceptApplicationUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(STRAVA_AUTHORITY)
                .appendPath(OAUTH)
                .appendPath(ACCEPT_APPLICATION);
        return builder.build().toString();
    }

    @Override
    protected String getName() {
        return getString(R.string.Strava);
    }

    @Override
    protected void onJsonResponse(JSONObject jsonObject) {
        StravaHelper.storeJSONData(jsonObject);
    }

}
