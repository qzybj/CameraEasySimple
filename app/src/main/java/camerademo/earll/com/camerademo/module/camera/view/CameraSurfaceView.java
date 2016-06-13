package camerademo.earll.com.camerademo.module.camera.view;

import android.graphics.Point;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import camerademo.earll.com.camerademo.module.camera.config.CCameraConfig;
import camerademo.earll.com.camerademo.module.camera.interfaces.ICameraOpenOverCallback;
import camerademo.earll.com.camerademo.module.camera.interfaces.impl.CameraInterfaceIMPL;
import camerademo.earll.com.camerademo.module.common.util.ScreenDensityUtil;

/**
 *  用来处理Camera显示的SurfaceView
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback ,ICameraOpenOverCallback {
    private static final String TAG = CCameraConfig.TAG;
    Context mContext;
    SurfaceHolder mSurfaceHolder;
    float previewRate = -1f;

    /**对外暴露实现打开摄像头回调*/
    ICameraOpenOverCallback mCameraOpenOverCallback;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        previewRate = ScreenDensityUtil.getScreenRate(mContext); //默认全屏的比例预览
    }

    /**要对SurfaceView进行宽高初始化，否则会有显示问题*/
    public void initSurfaceView(){
        ViewGroup.LayoutParams params = getLayoutParams();
        Point p = ScreenDensityUtil.getScreenMetrics(mContext);
        params.width = p.x;
        params.height = p.y;
        setLayoutParams(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        CameraInterfaceIMPL.getInstance().doOpenCamera(this);
        Log.i(TAG, "surfaceCreated...");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        // TODO Auto-generated method stub
        if(holder!=null){
            CameraInterfaceIMPL.getInstance().doStartPreview(holder, previewRate);
        }
        Log.i(TAG, "surfaceChanged...");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed...");
        CameraInterfaceIMPL.getInstance().doStopCamera();
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public void cameraHasOpened() {
//        SurfaceHolder holder = surfaceView.getSurfaceHolder();
//        ICameraInterface.getInstance().doStartPreview(holder, previewRate);
        if(mCameraOpenOverCallback!=null){
            mCameraOpenOverCallback.cameraHasOpened();
        }
    }

    /**设置对外暴露实现打开摄像头回调监听<br/>
     * 之所以在这里设置该监听，是用来处理 surfaceCreated 时才实际上开始打开camera */
    public void setCameraHasOpenedCallBack(ICameraOpenOverCallback callBack) {
        mCameraOpenOverCallback = callBack;
    }
}