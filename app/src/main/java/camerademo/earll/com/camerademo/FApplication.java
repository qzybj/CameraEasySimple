package camerademo.earll.com.camerademo;

import android.app.Application;

import com.custom.cameralibrary.module.common.manager.ManagerInitializer;


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
