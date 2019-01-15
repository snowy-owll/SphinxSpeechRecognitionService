package ru.snowy_owl.testservicesphinx.helpers;

import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionsHelper {
    public static final int REQUEST_WRITE_STORAGE = 0;
    public static final int REQUEST_RECORD_AUDIO = 1;
    public static final int REQUEST_ALL_NEEDED_PERMISSIONS = 2;

    public static boolean checkExternalStoragePermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED);
    }

    public static void requestExternalStoragePermission(Fragment fragment) {
        requestPermissions(fragment, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
    }

    public static void requestExternalStoragePermission(Activity activity) {
        requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
    }

    public static boolean checkRecordAudioPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) == PERMISSION_GRANTED);
    }

    public static void requestRecordAudioPermission(Activity activity) {
        requestPermissions(activity, new String[]{RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
    }

    public static void requestAllNeededPermissions(Activity activity) {
        requestPermissions(activity,
                new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO},
                REQUEST_ALL_NEEDED_PERMISSIONS);
    }

    private static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private static void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }
}
