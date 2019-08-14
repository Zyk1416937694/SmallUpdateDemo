package com.small.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory() + File.separator;
    public static final String PATCH_FILE = "old-to-new.patch";
    public static final String NEW_APK_FILE = "download" + File.separator + "new.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log("onCreate");

        ((TextView) findViewById(R.id.tv_main)).setText("更新完成！\n当前版本号为 v" + getVersionName(MainActivity.this));

        findViewById(R.id.btn_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //并行任务
                Log("setOnClickListener");
                new ApkUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        requestPermission(11111, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 请求权限
     */
    protected void requestPermission(int code, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, code);
    }

    public void checkIsAndroidO(String apkPath) {
        mApkPath = apkPath;
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                installApk();//安装应用的逻辑(写自己的就可以)
            } else {
                Log("没有权限，申请权限");
                //请求安装未知应用来源的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                startActivityForResult(intent,GOTO_SET_INSTALL_PERMISSION);
            }
        } else {
            installApk();
        }
    }

    private void installApk() {
        Intent intent = getIntentToInstall();
        startActivity(intent);
    }

    private Intent getIntentToInstall() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(this, "com.small.app.fileprovider", new File(mApkPath));
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(mApkPath)),
                    "application/vnd.android.package-archive");
        }
        return intent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                installApk();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                startActivityForResult(intent, GOTO_SET_INSTALL_PERMISSION);
            }
        }
    }

    private final int GOTO_SET_INSTALL_PERMISSION = 11, INSTALL_PACKAGES_REQUESTCODE = 12;
    private String mApkPath = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case GOTO_SET_INSTALL_PERMISSION:
                checkIsAndroidO(mApkPath);
                break;
            default:
                break;
        }

    }

    /**
     * 获取当前版本号
     *
     * @param context
     * @return
     */
    private String getVersionName(Context context) {
        String versionName = "";
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 合并增量文件任务
     */
    private class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Log("doInBackground");
            String oldApkPath = ApkUtils.getCurApkPath(MainActivity.this);
            File oldApkFile = new File(oldApkPath);
            File patchFile = new File(getPatchFilePath());
            if (oldApkFile.exists() && patchFile.exists()) {
                Log("正在合并增量文件...");
                String newApkPath = getNewApkFilePath();
                BsPatchJNI.patch(oldApkPath, newApkPath, getPatchFilePath());
//                //检验文件MD5值
//                return Signtils.checkMd5(oldApkFile, MD5);

                Log("增量文件的MD5值为：" + SignUtils.getMd5ByFile(oldApkFile));
                Log("新文件的MD5值为：" + SignUtils.getMd5ByFile(new File(newApkPath)));

                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Log("合并成功，开始安装");
                checkIsAndroidO(getNewApkFilePath());
            } else {
                Log("合并失败");
            }
        }
    }

    private String getPatchFilePath() {
        return SDCARD_PATH + PATCH_FILE;
    }

    private String getNewApkFilePath() {
        return SDCARD_PATH + NEW_APK_FILE;
    }

    /**
     * 打印日志
     *
     * @param log
     */
    private void Log(String log) {
        Log.d("aaaaa", log);
    }

}
