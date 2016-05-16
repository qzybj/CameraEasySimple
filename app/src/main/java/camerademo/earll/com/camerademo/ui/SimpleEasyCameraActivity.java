package camerademo.earll.com.camerademo.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import java.io.IOException;
import camerademo.earll.com.camerademo.R;
import camerademo.earll.com.camerademo.module.aosp.easycamera.net.bozho.easycamera.DefaultEasyCamera;
import camerademo.earll.com.camerademo.module.aosp.easycamera.net.bozho.easycamera.EasyCamera;
import camerademo.earll.com.camerademo.module.camera.interfaces.impl.CameraInterfaceIMPL;
import camerademo.earll.com.camerademo.module.camera.view.CameraSurfaceView;
import camerademo.earll.com.camerademo.ui.base.BaseActivity;

/**
 * EasyCamera 调用示例
 */
public class SimpleEasyCameraActivity extends BaseActivity{
    @ViewInject(R.id.toolbar)
    Toolbar toolbar;

    @ViewInject(R.id.camera_surfaceview)
    CameraSurfaceView surfaceView;

    /** 焦距调节 **/
    @ViewInject(R.id.sb_zoombar)
    SeekBar sb_zoombar;

    EasyCamera mCamera;
    EasyCamera.CameraActions mActions;

    @ViewInject(R.id.btn_shoot)
    ImageButton btn_shoot;
    @Event(value = R.id.btn_shoot, type = View.OnClickListener.class)
    private void clickBtnShoot(View view) {
        EasyCamera.PictureCallback callback = new EasyCamera.PictureCallback() {
            public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
                // store picture
                Toast.makeText(SimpleEasyCameraActivity.this,"onPictureTaken",Toast.LENGTH_SHORT).show();
            }
        };
        mActions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(callback));
    }
    @ViewInject(R.id.btn_whitebalance)
    Button btn_whitebalance;
    @Event(value = R.id.btn_whitebalance, type = View.OnClickListener.class)
    private void clickWhiteBalance(View view) {
        Camera.Parameters parameters = mCamera.getParameters();
        String whiteBalance = parameters.getWhiteBalance();
        parameters.setWhiteBalance(whiteBalance);
        mCamera.setParameters(parameters);
        btn_whitebalance.setText("白平衡(whiteBalance)");
    }
    @ViewInject(R.id.btn_whitebalanceauto)
    Button btn_whitebalanceauto;
    @Event(value = R.id.btn_whitebalanceauto, type = View.OnClickListener.class)
    private void clickWhiteBalanceAuto(View view) {
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


    @Override
    protected int getMainResourceId() {
        return R.layout.activity_simple_easycamera;
    }

    @Override
    protected void initUI() {
        setSupportActionBar(toolbar);
        surfaceView.initSurfaceView();
        mCamera = DefaultEasyCamera.open();
        try {
            mActions = mCamera.startPreview(surfaceView.getSurfaceHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    protected void onDestroy() {
        CameraInterfaceIMPL.getInstance().doStopCamera();
        super.onDestroy();
    }

    /** 设置相机焦距  **/
    private void setZoom(int mValue){
        Camera.Parameters mParams=mCamera.getParameters();
        mParams.setZoom(mValue);
        mCamera.setParameters(mParams);
    }

    /** 设置相机焦距SeekBar的UI  **/
    private void initCameraZoom(){
        /** 设置焦距调节的最大值  **/
        sb_zoombar.setMax(mCamera.getParameters().getMaxZoom());
        sb_zoombar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2){
                setZoom(sb_zoombar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar arg0){}
            @Override
            public void onStopTrackingTouch(SeekBar arg0){}
        });
    }

}