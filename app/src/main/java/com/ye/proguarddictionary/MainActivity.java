package com.ye.proguarddictionary;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity ++++ 开始写入";
    Button btnCreat;
    EditText etResouceDic;
    ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCreat = findViewById(R.id.btn_create_dic);
        etResouceDic = findViewById(R.id.et_source_dic);

        initView();
        initHandler();
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    Toast.makeText(MainActivity.this, "字典创建成功", Toast.LENGTH_LONG).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "字典创建失败", Toast.LENGTH_LONG).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        };
    }

    private void initView() {
        btnCreat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    createDic();
                }
            }
        });
    }

    private void createDic() {
        String resouceDic = etResouceDic.getText().toString().trim();
        String resourceDicFilter = resouceDic.replaceAll("[^(A-Za-z)]", "").toLowerCase();
        Log.e(TAG, "原始字典处理后的值" + resourceDicFilter);
        if (resouceDic == null) {
            Toast.makeText(this, "原始字典不能为null", Toast.LENGTH_LONG).show();
            return;
        }
        if (resouceDic.length() < 36) {
            Toast.makeText(this, "原始字典长度不够", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog = ProgressDialog.show(MainActivity.this, "正在处理中", "Loading. Please wait...", true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        startCreate(resourceDicFilter);
    }

    private void startCreate(final String resourceDicFilter) {
        final Random random = new Random();
        final char[] resoursChars = resourceDicFilter.toCharArray();
        new Thread() {
            @Override
            public void run() {
                String llLocationPath = MainActivity.this.getCacheDir().getAbsolutePath() + File.separator;
                // File llLocationPath = Environment.getExternalStoragePublicDirectory("");
                File llLocationPathFile = new File(llLocationPath, "Proguard_Dictionary.txt");
                try {
                    llLocationPathFile.createNewFile();
                    FileWriter fileWriter = new FileWriter(llLocationPathFile, true);
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0; i <= 65535; i++) {
                        int index = random.nextInt(resourceDicFilter.length() - 10) + 1;
                        for (int b = 0; b < 6; b++) {
                            stringBuffer.append(String.valueOf(resoursChars[index + b]));
                        }
                        Log.e(TAG, "写入文件的地址" + stringBuffer.toString());
                        fileWriter.write(stringBuffer.toString() + "\r\n");
                        stringBuffer.delete(0, stringBuffer.length());
                    }
                    fileWriter.flush();
                    fileWriter.close();
                    handler.sendEmptyMessage(1);
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            createDic();
        }
    }
}
