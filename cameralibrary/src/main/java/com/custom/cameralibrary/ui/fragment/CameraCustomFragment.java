package com.custom.cameralibrary.ui.fragment;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.custom.cameralibrary.R;
import com.custom.cameralibrary.module.camera.config.CameraConstant;
import com.custom.cameralibrary.module.camera.interfaces.CameraParamsChangedListener;
import com.custom.cameralibrary.module.camera.interfaces.FocusCallback;
import com.custom.cameralibrary.module.camera.interfaces.KeyEventsListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoSavedListener;
import com.custom.cameralibrary.module.camera.interfaces.PhotoTakenCallback;
import com.custom.cameralibrary.module.camera.interfaces.RawPhotoTakenCallback;
import com.custom.cameralibrary.module.camera.model.FlashMode;
import com.custom.cameralibrary.module.camera.model.FocusMode;
import com.custom.cameralibrary.module.camera.model.HDRMode;
import com.custom.cameralibrary.module.camera.model.Quality;
import com.custom.cameralibrary.module.camera.model.Ratio;
import com.custom.cameralibrary.module.camera.util.CameraArgUtils;
import com.custom.cameralibrary.module.camera.util.ResourcesUtils;
import com.custom.cameralibrary.module.camera.view.CameraPreview;
import com.custom.cameralibrary.ui.fragment.base.BaseFragment;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import timber.log.Timber;


/**自定义样式：拍照UI及逻辑实现*/
public class CameraCustomFragment extends BaseFragment implements PhotoSavedListener, KeyEventsListener, CameraParamsChangedListener, FocusCallback {

    private PhotoTakenCallback callback;
    private RawPhotoTakenCallback rawCallback;
    private CameraParamsChangedListener paramsChangedListener;
    private OrientationEventListener orientationListener;

    private Quality quality;
    private Ratio ratio;
    private FlashMode flashMode;
    private FocusMode focusMode;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNavigationBarHeight;
    private int mStatusBarHeight;
    private List<Integer> zoomRatios;
    private int zoomIndex;
    private int minZoomIndex;
    private int maxZoomIndex;

    private Map<Ratio, Camera.Size> previewSizes;
    private Map<Ratio, Map<Quality, Camera.Size>> pictureSizes;

    private int layoutId;
    private Camera camera;
    private Camera.Parameters parameters;
    private CameraPreview cameraPreview;
    private ViewGroup previewContainer;

    /**处理中进度条*/
    private ProgressBar progressBar;

    /**焦距显示文本*/
    private TextView mZoomRatioTextView;
    /**闪光灯模式*/
    private ImageButton flashModeButton;
    /**拍照*/
    private View mCapture;
    /**Camera附加设置*/
    private View cameraSettings;

    private HDRMode hdrMode;
    private boolean supportedHDR = false;
    private boolean supportedFlash = false;

    private int cameraId;
    private int outputOrientation;

    public static CameraCustomFragment newInstance(PhotoTakenCallback callback, Bundle params) {
        CameraCustomFragment fragment = new CameraCustomFragment();
        fragment.callback = callback;
        fragment.layoutId = R.layout.fragment_customcamera;
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCamera();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (camera == null) {
            return inflater.inflate(R.layout.fragment_no_camera, container, false);
        }
        View view = inflater.inflate(layoutId, container, false);
        try {
            previewContainer = (ViewGroup) view.findViewById(R.id.camera_preview);
        } catch (NullPointerException e) {
            throw new RuntimeException("You should add container that extends ViewGroup for CameraPreview.");
        }

        ImageView canvasFrame = new ImageView(activity);//对象中心点图标
        cameraPreview = new CameraPreview(activity, camera, canvasFrame, this, this);
        previewContainer.addView(cameraPreview);
        previewContainer.addView(canvasFrame);
        cameraPreview.setFocusMode(focusMode);

        progressBar = (ProgressBar) view.findViewById(R.id.progress);

        mCapture = view.findViewById(R.id.ibtn_capture);
        if (mCapture != null) {
            mCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto();
                }
            });
        }
        flashModeButton = (ImageButton) view.findViewById(R.id.flash_mode);
        flashModeButton.setVisibility(View.GONE);//不需要显示
        if (flashModeButton != null) {
            if (supportedFlash) {
                flashModeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchFlashMode();
                        onFlashModeChanged(flashMode.getId());
                    }
                });
                setFlashModeImage(flashMode);
            } else {
                flashModeButton.setVisibility(Button.GONE);
            }
        }

        setPreviewContainerSize(mScreenWidth, mScreenHeight, ratio);

        mZoomRatioTextView = (TextView) view.findViewById(R.id.zoom_ratio);
        mZoomRatioTextView.setVisibility(View.GONE);//不需要显示
        if (mZoomRatioTextView != null) {
            setZoomRatioText(zoomIndex);
        }

        cameraSettings = view.findViewById(R.id.camera_settings);
        cameraSettings.setVisibility(View.GONE);//不需要显示
        if (cameraSettings != null) {
            view.findViewById(R.id.camera_settings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CameraSettingsDialogFragment.newInstance(packSettings(), CameraCustomFragment.this).show(getFragmentManager());
                }
            });
        }

        View controls = view.findViewById(R.id.controls_layout);
        if (controls != null) {
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,150);
            params.topMargin = mStatusBarHeight;
            params.bottomMargin = mNavigationBarHeight;
            controls.setLayoutParams(params);
        }
        return view;
    }


    /**初始化Camera*/
    private void initCamera() {
        boolean useFrontCamera = getArguments().getBoolean(CameraConstant.KEY_FRONT_CAMERA, false);
        camera = getCameraInstance(useFrontCamera);//获取Camera 实例
        if (camera == null) {
            return;
        }
        initScreenParams();
        parameters = camera.getParameters();//获取Camera参数集合
        zoomRatios = parameters.getZoomRatios();
        zoomIndex = minZoomIndex = 0;
        maxZoomIndex = parameters.getMaxZoom();
        previewSizes = CameraArgUtils.buildPreviewSizesRatioMap(parameters.getSupportedPreviewSizes());
        pictureSizes = CameraArgUtils.buildPictureSizesRatioMap(parameters.getSupportedPictureSizes());
        List<String> sceneModes = parameters.getSupportedSceneModes();
        if (sceneModes != null) {
            for (String mode : sceneModes) {
                if (mode.equals(Camera.Parameters.SCENE_MODE_HDR)) {
                    supportedHDR = true;
                    break;
                }
            }
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null || flashModes.size() <= 1) {
            supportedFlash = false;
        } else {
            supportedFlash = true;
        }
        if (CameraConstant.DEBUG) {
            Timber.d("PictureSizesRatioMap:");
            for (Ratio r : pictureSizes.keySet()) {
                Timber.d(r.toString() + ":");
                for (Quality q : pictureSizes.get(r).keySet()) {
                    Camera.Size size = pictureSizes.get(r).get(q);
                    if (size != null) {
                        Timber.d(q.toString() + ": " + size.width + "x" + size.height);
                    }
                }
            }
        }
        expandParams(getArguments());
        initCameraParams();
    }
    /**初始化Camera参数*/
    private void initCameraParams() {
        setFlashMode(parameters, flashMode);
        setPreviewSize(parameters, ratio);
        setHDRMode(parameters, hdrMode);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(boolean useFrontCamera) {
        Camera c = null;
        try {
            cameraId = CameraArgUtils.getCameraId(useFrontCamera);
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Timber.e(e, getString(R.string.lbl_camera_unavailable));
        }
        return c;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera != null) {
            try {
                camera.reconnect();
            } catch (IOException e) {
                Timber.e(e,e.getLocalizedMessage());
            }
        }
        if (orientationListener == null) {
            initOrientationListener();
        }
        orientationListener.enable();
    }

    /**初始化屏幕参数*/
    private void initScreenParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mNavigationBarHeight = getNavigationBarHeight();
        mStatusBarHeight = getStatusBarHeight();
    }

    private int getNavigationBarHeight() {
        return ResourcesUtils.getPixelSizeByName(getActivity(),"navigation_bar_height");
    }

    private int getStatusBarHeight() {
        return ResourcesUtils.getPixelSizeByName(getActivity(),"status_bar_height");
    }

    public void setCallback(PhotoTakenCallback callback) {
        this.callback = callback;
    }

    public void setRawCallback(RawPhotoTakenCallback rawCallback) {
        this.rawCallback = rawCallback;
    }

    public void setParamsChangedListener(CameraParamsChangedListener paramsChangedListener) {
        this.paramsChangedListener = paramsChangedListener;
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (callback != null) {
                callback.photoTaken(data.clone(), outputOrientation);
            }
            camera.startPreview();
            cameraPreview.onPictureTaken();
        }

    };

    @Override
    public void onFocused(Camera camera) {
        camera.takePicture(null, rawPictureCallback, pictureCallback);
    }

    private Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (rawCallback != null && data != null) {
                rawCallback.rawPhotoTaken(data.clone());
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (orientationListener != null) {
            orientationListener.disable();
            orientationListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**初始化参数变量*/
    private void expandParams(Bundle params) {
        if (params == null) {
            params = new Bundle();
        }
        int id = 0;
        if (params.containsKey(CameraConstant.KEY_RATIO)) {
            id = params.getInt(CameraConstant.KEY_RATIO, 1);
        }
        ratio = Ratio.getRatioById(id);
        id = 0;
        if (params.containsKey(CameraConstant.KEY_QUALITY)) {
            id = params.getInt(CameraConstant.KEY_QUALITY, 0);
        }
        quality = Quality.getQualityById(id);
        id = 0;
        if (params.containsKey(CameraConstant.KEY_FOCUS_MODE)) {
            id = params.getInt(CameraConstant.KEY_FOCUS_MODE);
        }
        focusMode = FocusMode.getFocusModeById(id);
        id = 0;
        if (params.containsKey(CameraConstant.KEY_FLASH_MODE)) {
            id = params.getInt(CameraConstant.KEY_FLASH_MODE);
        }
        flashMode = FlashMode.getFlashModeById(id);
        id = 0;
        if (params.containsKey(CameraConstant.KEY_HDR_MODE)) {
            id = params.getInt(CameraConstant.KEY_HDR_MODE);
        }
        hdrMode = HDRMode.getHDRModeById(id);
    }

    /**生成参数集合*/
    private Bundle packSettings() {
        Bundle params = new Bundle();
        params.putInt(CameraConstant.KEY_QUALITY, quality.getId());
        //params.putInt(CameraConstant.KEY_RATIO, ratio.getId());//比例
        params.putInt(CameraConstant.KEY_FOCUS_MODE, focusMode.getId());
        params.putInt(CameraConstant.KEY_HDR_MODE, hdrMode.getId());
        return params;
    }

    @Override
    public void onQualityChanged(int id) {
        quality = Quality.getQualityById(id);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
        if (paramsChangedListener != null) {
            paramsChangedListener.onQualityChanged(id);
        }
    }

    @Override
    public void onRatioChanged(int id) {
        ratio = Ratio.getRatioById(id);
        setPreviewSize(parameters, ratio);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
        setPreviewContainerSize(mScreenWidth, mScreenHeight, ratio);
        if (paramsChangedListener != null) {
            paramsChangedListener.onRatioChanged(id);
        }
    }

    @Override
    public void onHDRChanged(int id) {
        hdrMode = HDRMode.getHDRModeById(id);
        setHDRMode(parameters, hdrMode);
        camera.setParameters(parameters);
        if (paramsChangedListener != null) {
            paramsChangedListener.onHDRChanged(id);
        }
    }

    @Override
    public void onFlashModeChanged(int id) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onFlashModeChanged(id);
        }
    }

    @Override
    public void onFocusModeChanged(int id) {
        focusMode = FocusMode.getFocusModeById(id);
        cameraPreview.setFocusMode(focusMode);
        if (paramsChangedListener != null) {
            paramsChangedListener.onFocusModeChanged(id);
        }
    }

    @Override
    public void onWhiteBalanceModeChanged(int value) {
        if (paramsChangedListener != null) {
            paramsChangedListener.onFocusModeChanged(value);
        }
    }

    @Override
    public void zoomIn() {
//        if (++zoomIndex > maxZoomIndex) {
//            zoomIndex = maxZoomIndex;
//        }
//        setZoom(zoomIndex);
    }

    @Override
    public void zoomOut() {
//        if (--zoomIndex < minZoomIndex) {
//            zoomIndex = minZoomIndex;
//        }
//        setZoom(zoomIndex);
    }

    @Override
    public void takePhoto() {
        mCapture.setEnabled(false);
        mCapture.setVisibility(View.INVISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        cameraPreview.takePicture();
    }

    private void setZoom(int index) {
        parameters.setZoom(index);
        camera.setParameters(parameters);
        setZoomRatioText(index);
    }

    private void setZoomRatioText(int index) {
        if (mZoomRatioTextView != null) {
            float value = zoomRatios.get(index) / 100.0f;
            mZoomRatioTextView.setText(getString(R.string.lbl_zoom_ratio_value, value));
        }
    }

    private void switchFlashMode() {
        switch (flashMode) {
            case AUTO:
                flashMode = FlashMode.ON;
                break;
            case ON:
                flashMode = FlashMode.OFF;
                break;
            case OFF:
                flashMode = FlashMode.AUTO;
                break;
        }
        setFlashMode(parameters, flashMode);
        setFlashModeImage(flashMode);
        camera.setParameters(parameters);
    }

    private void setHDRMode(Camera.Parameters parameters, HDRMode hdrMode) {
        if (supportedHDR && hdrMode == HDRMode.NONE) {
            hdrMode = HDRMode.OFF;
        }
        switch (hdrMode) {
            case ON:
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                break;
            case OFF:
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                break;
        }
    }

    private void setFlashMode(Camera.Parameters parameters, FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            case OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
    }

    private void setFlashModeImage(FlashMode flashMode) {
        switch (flashMode) {
            case ON:
                flashModeButton.setImageResource(R.drawable.cam_flash_fill_flash_icn);
                break;
            case OFF:
                flashModeButton.setImageResource(R.drawable.cam_flash_off_icn);
                break;
            case AUTO:
                flashModeButton.setImageResource(R.drawable.cam_flash_auto_icn);
                break;
        }
    }

    private void setPictureSize(Camera.Parameters parameters, Quality quality, Ratio ratio) {
        Camera.Size size = pictureSizes.get(ratio).get(quality);
        if (size != null) {
            parameters.setPictureSize(size.width, size.height);
        }
    }

    private void setPreviewSize(Camera.Parameters parameters, Ratio ratio) {
        Camera.Size size = previewSizes.get(ratio);
        parameters.setPreviewSize(size.width, size.height);
    }

    /**
     * @param width  Screen width
     * @param height Screen height
     * @param ratio  Required ratio
     */
    private void setPreviewContainerSize(int width, int height, Ratio ratio) {
        height = (width / ratio.h) * ratio.w;
        previewContainer.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
    }

    @Override
    public void photoSaved(String path, String name) {
        mCapture.setEnabled(true);
        mCapture.setVisibility(View.VISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /** 初始化图片旋转角度监听*/
    private void initOrientationListener() {
        orientationListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (camera != null && orientation != ORIENTATION_UNKNOWN) {
                    int newOutputOrientation = CameraArgUtils.getCameraPictureRotation(orientation,cameraId);
                    if (newOutputOrientation != outputOrientation) {
                        outputOrientation = newOutputOrientation;
                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(outputOrientation);
                        try {
                            camera.setParameters(params);
                        } catch (Exception e) {
                            Timber.e(e, "Exception updating camera parameters in orientation change");
                        }
                    }
                }
            }
        };
    }

}
