package cn.hikyson.godeye.core.internal.modules.imagecanary;

import android.graphics.Bitmap;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.lang.ref.WeakReference;

@Keep
public class BitmapInfo implements Serializable {
    public int bitmapWidth;
    public int bitmapHeight;
    public WeakReference<Bitmap> bitmap;

    public boolean isValid() {
        return bitmapHeight > 0 && bitmapWidth > 0;
    }

    public int getSize() {
        return bitmapHeight * bitmapWidth;
    }

    @Override
    public String toString() {
        return "BitmapInfo{" +
                "bitmapWidth=" + bitmapWidth +
                ", bitmapHeight=" + bitmapHeight +
                ", bitmapSize=" + getSize() +
                '}';
    }
}
