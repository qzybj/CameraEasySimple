/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Zillow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.custom.cameralibrary.ui.activity;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;
import com.custom.cameralibrary.module.camera.config.CameraConstant;
import com.custom.cameralibrary.R;
import com.custom.cameralibrary.module.camera.interfaces.CameraParamsChangedListener;
import com.custom.cameralibrary.module.camera.interfaces.KeyEventsListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoSavedListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoTakenCallback;
import com.custom.cameralibrary.module.camera.interfaces.RawPhotoTakenCallback;
import com.custom.cameralibrary.module.common.manager.SharedPrefManager;
import com.custom.cameralibrary.ui.activity.base.BaseActivity;
import com.custom.cameralibrary.ui.activity.base.BasePhotoActivity;
import com.custom.cameralibrary.ui.fragment.CameraFragment;
import com.custom.cameralibrary.module.camera.util.PhotoUtil;
import com.custom.cameralibrary.module.camera.util.SavingPhotoTask;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;


/**
 * 打开拍摄界面,并根据传参来做对应初始化<br/>
 *
 *  intent.putExtra();可选参数 ：<br/>
 *  1.CameraConstant.KEY_PATH 图片保存地址 <br/>
 *  2.CameraConstant.KEY_OPEN_PHOTO_PREVIEW 拍照完毕，是否打开预览界面<br/>
 *  3.CameraConstant.KEY_USE_FRONT_CAMERA  是否调用前置摄像头(暂未支持，只可用默认)<br/>
 */
public class CameraActivity extends BaseActivity implements PhotoTakenCallback, PhotoSavedListener, RawPhotoTakenCallback,CameraParamsChangedListener {



    private KeyEventsListener keyEventsListener;
    private PhotoSavedListener photoSavedListener;

    private String path;
    /**是否开户图片预览*/
    private boolean openPreview;
    /**当前是否正在保存图片*/
    private boolean saving;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_fragment);
        hideActionBar();
        //如果传递过来地址，则直接使用，否则用SD卡根目录
        if (TextUtils.isEmpty(path = getIntent().getStringExtra(CameraConstant.KEY_PATH))) {
            path = Environment.getExternalStorageDirectory().getPath()+"/"+CameraConstant.IMAGEPATH;
        }
        //拍照完毕，是否打开预览界面
        openPreview = getIntent().getBooleanExtra(CameraConstant.KEY_OPEN_PHOTO_PREVIEW, SharedPrefManager.i.isOpenPhotoPreview());
        if (openPreview != SharedPrefManager.i.isOpenPhotoPreview()) {
            SharedPrefManager.i.setOpenPhotoPreview(openPreview);
        }

        //是否调用前置摄像头
        boolean useFrontCamera = getIntent().getBooleanExtra(CameraConstant.KEY_USE_FRONT_CAMERA, SharedPrefManager.i.useFrontCamera());
        if (useFrontCamera != SharedPrefManager.i.useFrontCamera()) {
            SharedPrefManager.i.setUseFrontCamera(useFrontCamera);
        }
        init();
    }

    private void init() {
        CameraFragment fragment;
        int layoutId = getIntent().getIntExtra(CameraConstant.KEY_LAYOUT_ID, -1);
        if (layoutId > 0) {
            fragment = CameraFragment.newInstance(layoutId, this, createCameraParams());
        } else {
            fragment = CameraFragment.newInstance(this, createCameraParams());
        }
        fragment.setParamsChangedListener(this);
        keyEventsListener = fragment;
        photoSavedListener = fragment;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
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
        if (CameraConstant.DEBUG) {
            printExifOrientation(path);
        }
        if (openPreview) {
            openPreview(path, name);
        }
        if (photoSavedListener != null) {
            photoSavedListener.photoSaved(path, name);
        }
    }

    /**
     * 打开预览界面
     * @param path
     * @param name
     */
    private void openPreview(String path, String name) {
        Intent intent = new Intent(this, PhotoPreviewActivity.class);
        intent.putExtra(BasePhotoActivity.EXTRAS.PATH, path);
        intent.putExtra(BasePhotoActivity.EXTRAS.NAME, name);
        intent.putExtra(BasePhotoActivity.EXTRAS.FROM_CAMERA, true);
        startActivityForResult(intent, BasePhotoActivity.EXTRAS.REQUEST_PHOTO_EDIT);
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

    private void printExifOrientation(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Timber.d("Orientation: " + orientation);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                keyEventsListener.zoomIn();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                keyEventsListener.zoomOut();
                return true;
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
