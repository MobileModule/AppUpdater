package com.king.app.updater.constant;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public final class Constants {

    public static final String TAG = "AppUpdater";

    public static final String KEY_UPDATE_CONFIG = "app_update_config";

    public static final int DEFAULT_NOTIFICATION_ID = 0x66;

    public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "0x66";

    public static final String DEFAULT_NOTIFICATION_CHANNEL_NAME = "AppUpdater";

    public static final String KEY_STOP_DOWNLOAD_SERVICE = "stop_download_service";

    public static final String KEY_RE_DOWNLOAD = "app_update_re_download";

    public static String getDefaultPath(Context context){
        String root_path = "";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            root_path = context.getExternalFilesDir("").getPath();
        } else {
            root_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return root_path + File.separator + ".AppUpdater";
    }

    public static final int RE_CODE_STORAGE_PERMISSION = 0x66;

    public static final int NONE = -1;
}
