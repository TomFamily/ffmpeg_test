package com.example.ffmpeg_test.uitls;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class PermissionUtil {

    /**
     * 请求内存卡权限
     *
     * @param activity    Activity
     * @param requestCode 权限请求码
     * @param success     已有权限的回调
     */
    public static void requestSDCardPermission(Activity activity, int requestCode, Runnable success) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                success.run();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, requestCode);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            boolean hasPermission = true;

            for (String permission : permissions) {
                hasPermission = ActivityCompat.checkSelfPermission(activity, permission)
                        == PackageManager.PERMISSION_GRANTED;
                if (!hasPermission) {
                    break;
                }
            }

            if (hasPermission) {
                success.run();
            } else {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }
        } else {
            success.run();
        }
    }
}
