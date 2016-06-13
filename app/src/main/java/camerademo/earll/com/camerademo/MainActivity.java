package camerademo.earll.com.camerademo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


import com.custom.cameralibrary.ui.activity.CameraActivity;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import camerademo.earll.com.camerademo.ui.SimpleCameraActivity;
import camerademo.earll.com.camerademo.ui.base.BaseActivity;


public class MainActivity extends BaseActivity {

    @ViewInject(R.id.toolbar)
    Toolbar toolbar;

    @ViewInject(R.id.btn_gosimple1)
    Button btn_gosimple1;
    @Event(value = R.id.btn_gosimple1, type = View.OnClickListener.class)
    private void clickBtn1(View view) {
        goActivity(SimpleCameraActivity.class,null);
    }

    @ViewInject(R.id.btn_gosimple2)
    Button btn_gosimple2;
    @Event(value = R.id.btn_gosimple2, type = View.OnClickListener.class)
    private void clickBtn2(View view) {
        openCameraCustom();
    }

    @ViewInject(R.id.btn_gosimple3)
    Button btn_gosimple3;
    @Event(value = R.id.btn_gosimple3, type = View.OnClickListener.class)
    private void clickBtn3(View view) {
        openCamera();
    }


    @Override
    protected int getMainResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initUI(){
        setSupportActionBar(toolbar);
        //btn_gosimple2.setVisibility(View.GONE);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    public void goActivity(Class<?> cls,Bundle extras){
        Intent intent = new Intent(this,cls);
        if(extras!=null&&extras.size()>0){
            intent.putExtras(extras);
        }
        startActivity(intent);
    }

    public void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.PATH, Environment.getExternalStorageDirectory().getPath());
        intent.putExtra(CameraActivity.OPEN_PHOTO_PREVIEW, true);
        intent.putExtra(CameraActivity.USE_FRONT_CAMERA, false);
        startActivity(intent);
    }
    public void openCameraCustom() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.PATH, Environment.getExternalStorageDirectory().getPath());
        intent.putExtra(CameraActivity.OPEN_PHOTO_PREVIEW, true);
        intent.putExtra(CameraActivity.USE_FRONT_CAMERA, false);
        startActivity(intent);
    }
}