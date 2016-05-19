package camerademo.earll.com.camerademo.module.camera.interfaces;

import android.view.SurfaceHolder;

/**
 * 摄像头功能调用需要实现的接口
 */
public interface ICameraInterface {

    /**
     * 打开Camera
     *
     * @param callback
     */
    void doOpenCamera(ICameraOpenOverCallback callback);

    /**
     * 开启预览
     *
     * @param holder
     * @param previewRate
     */
    void doStartPreview(SurfaceHolder holder, float previewRate);

    /**
     * 停止预览，释放Camera
     */
    void doStopCamera();

    /**
     * 拍照
     */
    void doTakePicture();


    /**
     * Camera是否支持缩放焦距
     */
    boolean isSupportZoom();

    /**
     * 设置自动对焦
     * @return
     */
    void autoFocus();

    /**
     * 是否可以调用Camera
     * @return
     */
     boolean isSupportCallCamera();


    /**
     * 设置相机焦距
     */
     void setZoom(int mValue);
}