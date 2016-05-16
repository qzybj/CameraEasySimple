package camerademo.earll.com.camerademo;

import android.app.Application;

import com.yalantis.cameramodule.ManagerInitializer;

/**
 * Created by ZhangYuanBo on 2016/5/13.
 */
public class FApplication  extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ManagerInitializer.i.init(getApplicationContext());
    }
}
