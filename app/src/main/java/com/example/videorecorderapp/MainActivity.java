package com.example.videorecorderapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private VideoView videoView;
    private ActivityResultLauncher<Intent> videoCaptureLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.videoView);
        Button btnRecord = findViewById(R.id.btnRecordVideo);
        videoCaptureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            videoView.setVideoURI(videoUri);
                            videoView.start();
                            Toast.makeText(this, "Видео записано и воспроизводится", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Запись отменена", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        //регистрация разрешений
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        Toast.makeText(this, "Разрешения получены! Запуск камеры...", Toast.LENGTH_SHORT).show();
                        dispatchTakeVideoIntent();
                    } else {
                        Toast.makeText(this, "Без доступа к камере и микрофону запись невозможна", Toast.LENGTH_LONG).show();
                    }
                }
        );

        btnRecord.setOnClickListener(v -> dispatchTakeVideoIntent());
        //кнопки управления
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        btnPlay.setOnClickListener(v -> videoView.start());
        btnPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) videoView.pause();
        });
        btnStop.setOnClickListener(v -> {
            videoView.pause();
            videoView.seekTo(0);
        });
    }

    private void dispatchTakeVideoIntent() {
        // проверка видимости разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            });
            return;
        }
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            videoCaptureLauncher.launch(takeVideoIntent);
        } else {
            Toast.makeText(this, "На устройстве нет приложения камеры", Toast.LENGTH_SHORT).show();
        }
    }
}