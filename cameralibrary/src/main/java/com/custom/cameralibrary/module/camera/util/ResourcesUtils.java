package com.custom.cameralibrary.module.camera.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by ZhangYuanBo on 2016/6/13.
 */
public class ResourcesUtils {
    public static int getPixelSizeByName(Context con,String name) {
        if(con!=null){
            Resources resources = con.getResources();
            int resourceId = resources.getIdentifier(name, "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }
}
