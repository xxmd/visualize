package io.github.xxmd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;

import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.util.ArrayList;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private String videoFilePath = "/storage/emulated/0/Download/BigBuckBunny.mp4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityPermissionsDispatcher.chooseVideoWithPermissionCheck(this);
        bindEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    private void bindEvent() {
        MainActivityPermissionsDispatcher.chooseVideoWithPermissionCheck(this);
//        findViewById(R.id.tv_choose_video).setOnClickListener(v -> {
//            int i = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
//            System.out.println(i == PackageManager.PERMISSION_GRANTED);
//            chooseVideo();
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
//    public void showRationaleForCamera(PermissionRequest request) {
//        showRationaleDialog(R.string.permission_camera_rationale, request)
//    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void chooseVideo() {
        VideoTimeLine videoTimeLine = findViewById(R.id.video_time_line);
        videoTimeLine.setVideoFilePath(videoFilePath);
//        PictureSelector.create(this)
//                .openGallery(SelectMimeType.ofVideo())
//                .setSelectionMode(SelectModeConfig.SINGLE)
//                .setImageEngine(GlideEngine.createGlideEngine())
//                .forResult(new OnResultCallbackListener<LocalMedia>() {
//                    @Override
//                    public void onResult(ArrayList<LocalMedia> result) {
//                        VideoTimeLine videoTimeLine = findViewById(R.id.video_time_line);
//                        videoTimeLine.setVideoFilePath(result.get(0).getRealPath());
//                        System.out.println(result.get(0).getRealPath());
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        System.out.println();
//                    }
//                });
    }
}