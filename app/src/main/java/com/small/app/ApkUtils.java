package com.small.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;

import java.io.File;

/**
 * Created by AA on 2016/10/20.
 */

public class ApkUtils {

    /**
     * 获取当前应用的Apk路径
     * @param context 上下文
     * @return
     */
    public static String getCurApkPath(Context context) {
        context = context.getApplicationContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String apkPath = applicationInfo.sourceDir;
        return apkPath;
    }

    /**
     * 安装Apk
     * @param context 上下文
     * @param apkPath Apk路径
     */
    public static void installApk(Context context, String apkPath) {
        File file = new File(apkPath);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }



}
