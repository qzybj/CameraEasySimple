package com.custom.cameralibrary.ui.activity;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.Toast;
import com.custom.cameralibrary.R;
import com.custom.cameralibrary.module.camera.config.CameraConstant;
import com.custom.cameralibrary.module.camera.interfaces.CameraParamsChangedListener;
import com.custom.cameralibrary.module.camera.interfaces.KeyEventsListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoSavedListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoTakenCallback;
import com.custom.cameralibrary.module.camera.interfaces.RawPhotoTakenCallback;
import com.custom.cameralibrary.module.camera.util.PhotoUtil;
import com.custom.cameralibrary.module.camera.util.SavingPhotoTask;
import com.custom.cameralibrary.module.common.manager.SharedPrefManager;
import com.custom.cameralibrary.ui.activity.base.BaseActivity;
import com.custom.cameralibrary.ui.activity.base.BasePhotoActivity;
import com.custom.cameralibrary.ui.fragment.CameraCustomFragment;
import com.custom.cameralibrary.ui.fragment.CameraFragment;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;


/**
 * 自定义样式：打开拍摄界面,并根据传参来做对应初始化<br/>
 *
 *  intent.putExtra();可选参数 ：<br/>
 *  1.CameraConstant.KEY_PATH 图片保存地址 <br/>
 *  2.CameraConstant.KEY_OPEN_PHOTO_PREVIEW 拍照完毕，是否打开预览界面<br/>
 */
public class CameraCustomActivity extends BaseActivity implements PhotoTakenCallback, PhotoSavedListener, RawPhotoTakenCallback,CameraParamsChangedListener {

    private KeyEventsListener keyEventsListener;
    private PhotoSavedListener photoSavedListener;

    private String path = Environment.getExternalStorageDirectory().getPath()+"/"+CameraConstant.IMAGEPATH;
    /**是否开户图片预览*/
    private boolean openPreview = false;
    /**当前是否正在保存图片*/
    private boolean saving;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customcamera);
        hideActionBar();
        initUI();
    }

    private void initUI() {
        CameraCustomFragment fragment;
        fragment = CameraCustomFragment.newInstance(this, createCameraParams());
        fragment.setParamsChangedListener(this);
        keyEventsListener = fragment;
        photoSavedListener = fragment;
        getFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }

    /**
     * 初始化Camera参数
     * @return
     */
    private Bundle createCameraParams() {
        Bundle bundle = new Bundle();

        bundle.putInt(CameraConstant.KEY_RATIO, SharedPrefManager.i.getCameraRatio());
        bundle.putInt(CameraConstant.KEY_FLASH_MODE, SharedPrefManager.i.getCameraFlashMode());
        bundle.putInt(CameraConstant.KEY_HDR_MODE, SharedPrefManager.i.isHDR());
        bundle.putInt(CameraConstant.KEY_QUALITY, SharedPrefManager.i.getCameraQuality());
        bundle.putInt(CameraConstant.KEY_FOCUS_MODE, SharedPrefManager.i.getCameraFocusMode());
        bundle.putBoolean(CameraConstant.KEY_FRONT_CAMERA, SharedPrefManager.i.useFrontCamera());
        return bundle;
    }

    /**
     * 初始化图片默认名称
     * @return
     */
    private String createImageName() {
        String timeStamp = new SimpleDateFormat(CameraConstant.TIME_FORMAT).format(new Date());
        return CameraConstant.IMG_PREFIX + timeStamp + CameraConstant.IMG_POSTFIX;
    }

    @Override
    public void photoTaken(byte[] data, int orientation) {
        savePhoto(data, createImageName(), path, orientation);
    }

    @Override
    public void rawPhotoTaken(byte[] data) {
        Timber.d("rawPhotoTaken: data[%1d]", data.length);
    }

    /**
     * 保存图片
     * @param data
     * @param name
     * @param path
     * @param orientation
     */
    private void savePhoto(byte[] data, String name, String path, int orientation) {
        saving = true;
        new SavingPhotoTask(data, name, path, orientation, this).execute();
    }

    @Override
    public void photoSaved(String path, String name) {
        saving = false;
        Toast.makeText(this, "Photo " + name + " saved", Toast.LENGTH_SHORT).show();
        Timber.d("Photo " + name + " saved");
        if (photoSavedListener != null) {
            photoSavedListener.photoSaved(path, name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BasePhotoActivity.EXTRAS.REQUEST_PHOTO_EDIT) {
            switch (resultCode) {
                case BasePhotoActivity.EXTRAS.RESULT_DELETED:
                    String path = data.getStringExtra(BasePhotoActivity.EXTRAS.PATH);
                    PhotoUtil.deletePhoto(path);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                keyEventsListener.takePhoto();
                return true;
        }
        return false;
    }

    @Override
    public void onQualityChanged(int id) {
        SharedPrefManager.i.setCameraQuality(id);
    }

    @Override
    public void onRatioChanged(int id) {
        SharedPrefManager.i.setCameraRatio(id);
    }

    @Override
    public void onFlashModeChanged(int id) {
        SharedPrefManager.i.setCameraFlashMode(id);
    }

    @Override
    public void onHDRChanged(int id) {
        SharedPrefManager.i.setHDRMode(id);
    }

    @Override
    public void onFocusModeChanged(int id) {
        SharedPrefManager.i.setCameraFocusMode(id);
    }
    @Override
    public void onWhiteBalanceModeChanged(int id) {SharedPrefManager.i.setCameraWhiteBalanceMode(id);}

    @Override
    public void onBackPressed() {
        if (!saving) {
            super.onBackPressed();
        }
    }
}
