package camerademo.earll.com.camerademo.module.camera.view;


import android.view.SurfaceView;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import camerademo.earll.com.camerademo.module.camera.interfaces.CameraInterface;
import camerademo.earll.com.camerademo.module.camera.utils.CameraDisplayUtil;
import camerademo.earll.com.camerademo.ui.SimpleCamera;

/**
 * Created by ZhangYuanBo on 2016/5/12.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback ,CameraInterface.CamOpenOverCallback {
    private static final String TAG = CameraSurfaceView.class.getSimpleName();
    CameraInterface mCameraInterface;
    Context mContext;
    SurfaceHolder mSurfaceHolder;
    float previewRate = -1f;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        previewRate = CameraDisplayUtil.getScreenRate(mContext); //默认全屏的比例预览
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        CameraInterface.getInstance().doOpenCamera(this);
        Log.i(TAG, "surfaceCreated...");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        // TODO Auto-generated method stub
        if(holder!=null){
            CameraInterface.getInstance().doStartPreview(holder, previewRate);
        }
        Log.i(TAG, "surfaceChanged...");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceDestroyed...");
        CameraInterface.getInstance().doStopCamera();
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }


    @Override
    public void cameraHasOpened() {
        // TODO Auto-generated method stub
//        SurfaceHolder holder = surfaceView.getSurfaceHolder();
//        CameraInterface.getInstance().doStartPreview(holder, previewRate);
    }
}