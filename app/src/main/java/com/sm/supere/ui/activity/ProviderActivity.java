package com.sm.supere.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sm.supere.R;

public class ProviderActivity extends AppCompatActivity {

    private static final String TAG = "ProviderActivity";
    private static final int REQUEST_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        //检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            //进入到这里代表没有权限.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE);
        } else {
            getMsgs();
        }
    }

    private void getMsgs() {
        int i = 0;
        Uri uri = Uri.parse("content://sms/");
        ContentResolver resolver = getContentResolver();
        //获取的是哪些列的信息
        Cursor cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
        while (cursor != null && cursor.moveToNext() && i <= 10) {
            String address = cursor.getString(0);
            String date = cursor.getString(1);
            String type = cursor.getString(2);
            String body = cursor.getString(3);
            Log.d(TAG, "地址:" + address);
            Log.d(TAG, "时间:" + date);
            Log.d(TAG, "类型:" + type);
            Log.d(TAG, "内容:" + body);
            Log.d(TAG, "======================");
            i++;
        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权
                getMsgs();
            } else {
                //用户拒绝授权
                Toast.makeText(this, "有个傻瓜拒绝了我,好伤心哦！", Toast.LENGTH_SHORT).show();
            }

        }
    }
}