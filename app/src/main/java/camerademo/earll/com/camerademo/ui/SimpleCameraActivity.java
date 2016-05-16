package camerademo.earll.com.camerademo.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import camerademo.earll.com.camerademo.R;
import camerademo.earll.com.camerademo.module.camera.interfaces.impl.CameraInterfaceIMPL;
import camerademo.earll.com.camerademo.module.camera.view.CameraSurfaceView;
import camerademo.earll.com.camerademo.ui.base.BaseActivity;


/**
 * 自定实现处理
 */
public class SimpleCameraActivity extends BaseActivity implements CameraInterfaceIMPL.CameraOpenOverCallback{

    @ViewInject(R.id.camera_surfaceview)
    CameraSurfaceView surfaceView;

    @ViewInject(R.id.toolbar)
    Toolbar toolbar;
    Camera mCamera;

    /** 焦距调节 **/
    @ViewInject(R.id.sb_zoombar)
    SeekBar sb_zoombar;


    @ViewInject(R.id.fab_shoot)
    FloatingActionButton fab_shoot;
    @Event(value = R.id.fab_shoot, type = View.OnClickListener.class)
    private void clickBtnShoot(View view) {
        CameraInterfaceIMPL.getInstance().doTakePicture();//拍照
        //弹出显示文本
//        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @ViewInject(R.id.btn_autofocus)
    Button btn_autofocus;
    @Event(value = R.id.btn_autofocus, type = View.OnClickListener.class)
    private void clickBtnAutoFocus(View view) {
        CameraInterfaceIMPL.getInstance().setAutoFocus();
    }

    @ViewInject(R.id.btn_whitebalance)
    Button btn_whitebalance;
    @Event(value = R.id.btn_whitebalance, type = View.OnClickListener.class)
    private void clickWhiteBalance(View view) {
        if(mCamera!=null){
            Camera.Parameters parameters = mCamera.getParameters();
            String whiteBalance = parameters.getWhiteBalance();
            parameters.setWhiteBalance(whiteBalance);
            mCamera.setParameters(parameters);
            btn_whitebalance.setText("白平衡("+whiteBalance+")");
        }
    }
    @ViewInject(R.id.btn_whitebalanceauto)
    Button btn_whitebalanceauto;
    @Event(value = R.id.btn_whitebalanceauto, type = View.OnClickListener.class)
    private void clickWhiteBalanceAuto(View view) {
        if(mCamera!=null){
            Camera.Parameters parameters = mCamera.getParameters();
            boolean autoWhiteBalanceLock = parameters.getAutoWhiteBalanceLock();
            if(autoWhiteBalanceLock){
                btn_whitebalanceauto.setText("自动白平衡(关)");
            }else{
                btn_whitebalanceauto.setText("自动白平衡(开)");
            }
            autoWhiteBalanceLock=!autoWhiteBalanceLock;
            parameters.setAutoWhiteBalanceLock(autoWhiteBalanceLock);
            mCamera.setParameters(parameters);
        }
    }

    @Override
    protected int getMainResourceId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void initUI(){
        setSupportActionBar(toolbar);
        surfaceView.initSurfaceView();
        surfaceView.setCameraHasOpenedCallBack(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    /** 设置相机焦距SeekBar的UI  **/
    private void initCameraZoomSeejBar(){
        /** 设置焦距调节的最大值  **/
        sb_zoombar.setMax(mCamera.getParameters().getMaxZoom());
        sb_zoombar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener(){
                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2){
                        CameraInterfaceIMPL.getInstance().setZoom(sb_zoombar.getProgress());
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar arg0){}
                    @Override
                    public void onStopTrackingTouch(SeekBar arg0){}
                });
    }

    @Override
    protected void onDestroy() {
        CameraInterfaceIMPL.getInstance().doStopCamera();
        super.onDestroy();
    }

    @Override
    public void cameraHasOpened() {
        mCamera = CameraInterfaceIMPL.getInstance().getCamera();
        initCameraZoomSeejBar();
        showToast("Camera 初始化完毕 ");
    }
}
