package camerademo.earll.com.camerademo.module.common.util;


import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by ZhangYuanBo on 2016/5/12.
 */
public class BitmapUtil {
    /**
     * 旋转Bitmap
     * @param b
     * @param rotateDegree
     * @return
     */
    public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotateDegree);
        Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
        return rotaBitmap;
    }
}