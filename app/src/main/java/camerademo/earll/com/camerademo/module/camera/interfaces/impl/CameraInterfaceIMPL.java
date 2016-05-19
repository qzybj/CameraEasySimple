package camerademo.earll.com.camerademo.module.camera.interfaces.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.List;
import camerademo.earll.com.camerademo.module.camera.interfaces.ICameraInterface;
import camerademo.earll.com.camerademo.module.camera.interfaces.ICameraOpenOverCallback;
import camerademo.earll.com.camerademo.module.camera.utils.CameraUtil;
import camerademo.earll.com.camerademo.module.camera.utils.CameraFileUtil;
import camerademo.earll.com.camerademo.module.camera.utils.CameraImageUtil;


/**
 * Created by ZhangYuanBo on 2016/5/12.
 */
public class CameraInterfaceIMPL implements ICameraInterface {
    private static final String TAG = "yanzi";
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private static CameraInterfaceIMPL mCameraInterface;

    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback mShutterCallback = new ShutterCallback() {
        public void onShutter() {
            // TODO 快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };
    PictureCallback mRawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO  拍摄的未压缩原数据的回调,可以为null
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };
    PictureCallback mJpegPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO 对jpeg图像数据的回调,最重要的一个回调
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != b) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
                Bitmap rotaBitmap = CameraImageUtil.getRotateBitmap(b, 90.0f);
                CameraFileUtil.saveBitmap(rotaBitmap);
            }
            //再次进入预览
            mCamera.startPreview();
            isPreviewing = true;
        }
    };

    private CameraInterfaceIMPL() {

    }

    public static synchronized CameraInterfaceIMPL getInstance() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterfaceIMPL();
        }
        return mCameraInterface;
    }

    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 打开Camera
     *
     * @param callback
     */
    public void doOpenCamera(ICameraOpenOverCallback callback) {
        Log.i(TAG, "Camera open....");
        mCamera = Camera.open();
        Log.i(TAG, "Camera open over....");
        if (callback != null) {
            callback.cameraHasOpened();
        }
    }

    /**
     * 开启预览
     *
     * @param holder
     * @param previewRate
     */
    public void doStartPreview(SurfaceHolder holder, float previewRate) {
        Log.i(TAG, "doStartPreview...");
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            CameraUtil.getInstance().printSupportPictureSize(mParams);
            CameraUtil.getInstance().printSupportPreviewSize(mParams);
            //设置PreviewSize和PictureSize
            Size pictureSize = CameraUtil.getInstance().getPropPictureSize(mParams.getSupportedPictureSizes(), previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Size previewSize = CameraUtil.getInstance().getPropPreviewSize(mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);

            CameraUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
            } else {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(mParams);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();//开启预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); //重新get一次
            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width + "Height = " + mParams.getPictureSize().height);
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }


    ///////////////////////Camera调用操作方法////////////////////

    /**
     * 获取支持的白平衡模式
     */
    public List<String> getWhiteBalanceValues() {
        if (isSupportCallCamera()) {
            try {
                List<String> whiteBalances = mCamera.getParameters().getSupportedWhiteBalance();
                return whiteBalances;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 拍照
     */
    public void doTakePicture() {
        if (isSupportCallCamera()) {
            mCamera.takePicture(mShutterCallback, mRawCallback, mJpegPictureCallback);
        }
    }

    /**
     * 设置自动对焦
     */
    public void setAutoFocus() {
        if (isSupportCallCamera()) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        //doStartPreview();//实现相机的参数初始化
                    }
                }
            });
        }
    }

    public void setZoom(int mValue) {
        if (isSupportCallCamera()) {
            Camera.Parameters mParams = mCamera.getParameters();
            mParams.setZoom(mValue);
            mCamera.setParameters(mParams);
        }
    }

    ///////////////////////Camera支持校验方法////////////////////
    public boolean isSupportCallCamera() {
        if (isPreviewing && (mCamera != null)) {
            return true;
        }
        return false;
    }

    public boolean isSupportZoom() {
        if (isSupportCallCamera()&&
                mCamera.getParameters().isSmoothZoomSupported()) {
            return true;
        }
        return false;
    }

    @Override
    public void autoFocus() {

    }

    /**Camera是否支持自动白平衡锁*/
    public boolean isSupportAutoWhiteBalanceLock() {
        if (isSupportCallCamera()&&
                mCamera.getParameters().isAutoWhiteBalanceLockSupported()) {
            return true;
        }
        return false;
    }
    /**
     * 获取支持的白平衡模式
     */
    public boolean isSupportWhiteBalanceMode(String mode) {
        if (isSupportCallCamera()&&(mode!=null&&"".equals(mode))) {
            List<String> whiteBalances =  getWhiteBalanceValues();
            if(whiteBalances!=null&&whiteBalances.size()>0){
                for (int i = 0; i <whiteBalances.size() ; i++) {
                   String whiteBalanceMode =  whiteBalances.get(i);
                    if(mode.equalsIgnoreCase(whiteBalanceMode)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}