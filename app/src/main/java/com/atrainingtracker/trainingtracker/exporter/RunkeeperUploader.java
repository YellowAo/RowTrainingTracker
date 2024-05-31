

package com.atrainingtracker.trainingtracker.exporter;

import android.content.Context;
import android.util.Log;

import com.atrainingtracker.trainingtracker.TrainingApplication;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class RunkeeperUploader extends BaseExporter {
    protected static final String MY_UPLOAD_URL = "http://api.runkeeper.com/fitnessActivities";
    protected static final String MY_CONTENT_TYPE = "application/vnd.com.runkeeper.NewFitnessActivity+json";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String BEARER = "Bearer";
    private static final String TAG = "RunkeeperUploader";
    private static final boolean DEBUG = false;

    public RunkeeperUploader(Context context) {
        super(context);
    }

    @Override
    protected ExportResult doExport(ExportInfo exportInfo)
            throws IOException, JSONException {
        if (DEBUG) Log.d(TAG, "doExport: " + exportInfo.getFileBaseName());

        File file = new File(getDir(mContext, FileFormat.RUNKEEPER.getDirName()), exportInfo.getFileBaseName() + FileFormat.RUNKEEPER.getFileEnding());

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(MY_UPLOAD_URL);
        // httpPost.addHeader(AUTHORIZATION, "Bearer " + TrainingApplication.getRunkeeperToken());
        httpPost.addHeader(AUTHORIZATION, BEARER + " " + TrainingApplication.getRunkeeperToken());
        httpPost.addHeader(CONTENT_TYPE, MY_CONTENT_TYPE);

        FileEntity fileEntity = new FileEntity(file, CONTENT_TYPE);
        fileEntity.setContentType(CONTENT_TYPE);
        // fileEntity.setChunked(true); 
        httpPost.setEntity(fileEntity);

        // TODO: do this in background!
        if (DEBUG) Log.d(TAG, "starting to upload to runkeeper");
        HttpResponse httpResponse = httpClient.execute(httpPost);
        String response = EntityUtils.toString(httpResponse.getEntity());
        if (DEBUG) Log.d(TAG, "uploadToRunkeeper response: " + response);
        if (response == null) {
            return new ExportResult(false, "no response");
        } else if (response.equals("")) {
            return new ExportResult(true, "successfully uploaded " + exportInfo.getFileBaseName() + " to RunKeeper");
        }

        return new ExportResult(true, response);
    }

    @Override
    protected Action getAction() {
        return Action.UPLOAD;
    }

}
