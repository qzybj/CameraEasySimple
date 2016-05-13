package camerademo.earll.com.camerademo.ui;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import camerademo.earll.com.camerademo.R;
import camerademo.earll.com.camerademo.module.camera.interfaces.CameraInterface;
import camerademo.earll.com.camerademo.module.camera.utils.CameraDisplayUtil;
import camerademo.earll.com.camerademo.module.camera.view.CameraSurfaceView;

/**
 * Created by ZhangYuanBo on 2016/5/12.
 */
public class SimpleCamera extends Activity {
    private static final String TAG = "yanzi";
    CameraSurfaceView surfaceView = null;
    ImageButton shutterBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initUI();
        initViewParams();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    private void initUI(){
        surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
        shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
        shutterBtn.setOnClickListener(new BtnListeners());
    }
    private void initViewParams(){
        LayoutParams params = surfaceView.getLayoutParams();
        Point p = CameraDisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        surfaceView.setLayoutParams(params);

        //手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
        LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = CameraDisplayUtil.dip2px(this, 80);
        p2.height = CameraDisplayUtil.dip2px(this, 80);;
        shutterBtn.setLayoutParams(p2);
    }


    private class BtnListeners implements OnClickListener{
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_shutter:
                    CameraInterface.getInstance().doTakePicture();
                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        CameraInterface.getInstance().doStopCamera();
        super.onDestroy();
    }
}