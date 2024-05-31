

package com.atrainingtracker.trainingtracker.exporter;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import android.util.Log;

import com.atrainingtracker.R;
import com.atrainingtracker.trainingtracker.activities.MainActivityWithNavigation;
import com.atrainingtracker.trainingtracker.MyHelper;
import com.atrainingtracker.trainingtracker.TrainingApplication;

import java.io.File;
import java.util.ArrayList;

import static com.atrainingtracker.trainingtracker.TrainingApplication.NOTIFICATION_CHANNEL__EXPORT;

/**
 *
 */
public class ExportWorkoutIntentService extends IntentService {
    private static final String TAG = "ExportWorkoutIntentService";
    private static final boolean DEBUG = TrainingApplication.DEBUG & true;


    boolean exported = false;

    public ExportWorkoutIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onHandleIntent");

        ExportManager exportManager = new ExportManager(this, TAG);

        ArrayList<Uri> emailUris = new ArrayList<>();

        boolean tryExporting = true;  // TODO: what are we exactly doing with this variable?? 
        while (tryExporting) {
            tryExporting = false;
            for (ExportInfo exportInfo : exportManager.getExportQueue()) {

                if (DEBUG) Log.d(TAG, "ExportType: " + exportInfo.getExportType().toString()
                        + ", FileFormat: " + exportInfo.getFileFormat());

                if ((exportInfo.getExportType() == ExportType.FILE | exportInfo.getExportType() == ExportType.COMMUNITY)
                        && !TrainingApplication.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    continue;
                }

                BaseExporter exporter = null;
                switch (exportInfo.getExportType()) {
                    case FILE:
                        tryExporting = true;
                        switch (exportInfo.getFileFormat()) {
                            // TODO: change Uri.fromFile to the stuff explained at https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
                            case CSV:
                                exporter = new CSVFileExporter(this);
                                if (TrainingApplication.sendCSVEmail()) {
                                    emailUris.add(FileProvider.getUriForFile(this,
                                            this.getApplicationContext().getPackageName() + ".com.atrainingtracker.file.provider",
                                            new File(BaseExporter.getDir(this, FileFormat.CSV.getDirName()), exportInfo.getFileBaseName() + FileFormat.CSV.getFileEnding())));
                                }
                                break;
                            case GC:
                                exporter = new GCFileExporter(this);
                                if (TrainingApplication.sendGCEmail()) {
                                    emailUris.add(FileProvider.getUriForFile(this,
                                            this.getApplicationContext().getPackageName() + ".com.atrainingtracker.file.provider",
                                            new File(BaseExporter.getDir(this, FileFormat.GC.getDirName()), exportInfo.getFileBaseName() + FileFormat.GC.getFileEnding())));
                                }
                                break;
                            case TCX:
                                exporter = new TCXFileExporter(this);
                                if (TrainingApplication.sendTCXEmail()) {
                                    emailUris.add(FileProvider.getUriForFile(this,
                                            this.getApplicationContext().getPackageName() + ".com.atrainingtracker.file.provider",
                                            new File(BaseExporter.getDir(this, FileFormat.TCX.getDirName()), exportInfo.getFileBaseName() + FileFormat.TCX.getFileEnding())));
                                }
                                break;
                            case GPX:
                                exporter = new GPXFileExporter(this);
                                if (TrainingApplication.sendGPXEmail()) {
                                    emailUris.add(FileProvider.getUriForFile(this,
                                            this.getApplicationContext().getPackageName() + ".com.atrainingtracker.file.provider",
                                            new File(BaseExporter.getDir(this, FileFormat.GPX.getDirName()), exportInfo.getFileBaseName() + FileFormat.GPX.getFileEnding())));
                                }
                                break;
                            case STRAVA:
                                exporter = new TCXFileExporter(this);
                                break;
                            case RUNKEEPER:
                                exporter = new RunkeeperFileExporter(this);
                                break;
                            case TRAINING_PEAKS:
                                exporter = new TCXFileExporter(this);
                                // exporter = new TrainingPeaksFileExporter(this);
                                break;
                        }
                        break;

                    case DROPBOX:
                        if (MyHelper.isOnline()) {
                            exporter = new DropboxUploader(this);
                        }
                        break;

                    case COMMUNITY:
                        if (MyHelper.isOnline()) {
                            switch (exportInfo.getFileFormat()) {
                                case STRAVA:
                                    exporter = new StravaUploader(this);
                                    break;
                                case RUNKEEPER:
                                    exporter = new RunkeeperUploader(this);
                                    break;
                                case TRAINING_PEAKS:
                                    exporter = new TrainingPeaksUploader(this);
                                    break;
                                default:
                                    // exporter remains null
                                    break;
                            }
                        }
                        break;
                }
                if (exporter != null) {
                    exported = true;
                    this.startForeground(TrainingApplication.EXPORT_PROGRESS_NOTIFICATION_ID, exporter.getExportProgressNotification(exportInfo));
                    exporter.export(exportInfo);
                    exporter.onFinished();
                }
            }
        }
        exportManager.onFinished(TAG);

        if (exported) {

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancelAll();

            // show notification with a summary of the result
            Bundle bundle = new Bundle();
            bundle.putString(MainActivityWithNavigation.SELECTED_FRAGMENT, MainActivityWithNavigation.SelectedFragment.WORKOUT_LIST.name());
            Intent notificationIntent = new Intent(this, MainActivityWithNavigation.class);
            notificationIntent.putExtras(bundle);
            // notificationIntent.setAction("barAction");
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            // PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL__EXPORT)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_save_black_48dp))
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(getString(R.string.TrainingTracker))
                    .setContentText(getString(R.string.notification_exporting_finished))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            notificationManager.notify(TrainingApplication.EXPORT_RESULT_NOTIFICATION_ID, notificationBuilder.build());


            // send email stuff
            if (TrainingApplication.sendEmail()) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{TrainingApplication.getSpEmailAddress()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, TrainingApplication.getSpEmailSubject());
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, emailUris);
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // emailIntent.setAction(Long.toString(System.currentTimeMillis()));

                PendingIntent pendingShareIntent = PendingIntent.getActivity(this, 0, emailIntent, PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder notificationBuilder2 = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL__EXPORT)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_mail_outline_black_48dp))
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(getString(R.string.TrainingTracker))
                        .setContentText(getString(R.string.ready_to_send_email))
                        .setContentIntent(pendingShareIntent)
                        .setAutoCancel(true);

                notificationManager.notify(TrainingApplication.SEND_EMAIL_NOTIFICATION_ID, notificationBuilder2.build());
            }
        }
    }
}
