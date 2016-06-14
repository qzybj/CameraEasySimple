package com.custom.cameralibrary.module.camera.util;

import android.hardware.Camera;
import com.custom.cameralibrary.module.camera.model.Quality;
import com.custom.cameralibrary.module.camera.model.Ratio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhangYuanBo on 2016/6/13.
 * Camera参数处理辅助工具类
 */
public class CameraArgUtils {

    public static Map<Ratio, Map<Quality, Camera.Size>> buildPictureSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Map<Quality, Camera.Size>> map = new HashMap<>();

        Map<Ratio, List<Camera.Size>> ratioListMap = new HashMap<>();
        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                List<Camera.Size> sizeList = ratioListMap.get(ratio);
                if (sizeList == null) {
                    sizeList = new ArrayList<>();
                    ratioListMap.put(ratio, sizeList);
                }
                sizeList.add(size);
            }
        }
        for (Ratio r : ratioListMap.keySet()) {
            List<Camera.Size> list = ratioListMap.get(r);
            ratioListMap.put(r, sortSizes(list));
            Map<Quality, Camera.Size> sizeMap = new HashMap<>();
            int i = 0;
            for (Quality q : Quality.values()) {
                Camera.Size size = null;
                if (i < list.size()) {
                    size = list.get(i++);
                }
                sizeMap.put(q, size);
            }
            map.put(r, sizeMap);
        }

        return map;
    }

    public static List<Camera.Size> sortSizes(List<Camera.Size> sizes) {
        int count = sizes.size();

        while (count > 2) {
            for (int i = 0; i < count - 1; i++) {
                Camera.Size current = sizes.get(i);
                Camera.Size next = sizes.get(i + 1);

                if (current.width < next.width || current.height < next.height) {
                    sizes.set(i, next);
                    sizes.set(i + 1, current);
                }
            }
            count--;
        }
        return sizes;
    }

    /**生成预览比例集合*/
    public static Map<Ratio, Camera.Size> buildPreviewSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Camera.Size> map = new HashMap<>();
        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                Camera.Size oldSize = map.get(ratio);
                if (oldSize == null || (oldSize.width < size.width || oldSize.height < size.height)) {
                    map.put(ratio, size);
                }
            }
        }
        return map;
    }

    public static int getCameraId(boolean useFrontCamera) {
        int count = Camera.getNumberOfCameras();
        int result = -1;
        if (count > 0) {
            result = 0;
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK&& !useFrontCamera) {
                    result = i;
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT&& useFrontCamera) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     *
     * 获取图片旋转角度
     * @param orientation
     * @param cameraId
     * @return
     */
    public static int getCameraPictureRotation(int orientation,int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation;
        orientation = (orientation + 45) / 90 * 90;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else { // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        return (rotation);
    }

}
