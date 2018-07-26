package com.vanniktech.emoji.<%= package %>;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;

import com.vanniktech.emoji.emoji.Emoji;

import java.lang.ref.SoftReference;

import static android.util.DisplayMetrics.DENSITY_XHIGH;

public class <%= name %> extends Emoji {
  private static final Object LOCK = new Object();
  private static final int NUM_STRIPS = <%= strips %>;
  private static final SoftReference[] STRIP_REFS =
      new SoftReference[NUM_STRIPS];
  private static final int CACHE_SIZE = 100;
  private static final LruCache<CacheKey, Bitmap> BITMAP_CACHE =
      new LruCache<>(CACHE_SIZE);

  static {
    for (int i = 0; i < NUM_STRIPS; i++)
      STRIP_REFS[i] = new SoftReference<Bitmap>(null);
  }

  private final int x;
  private final int y;

  private volatile int scale = -1;

  public <%= name %>(@NonNull final int[] codePoints, final int x, final int y) {
    super(codePoints, -1);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(final int codePoint, final int x, final int y) {
    super(codePoint, -1);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(final int codePoint, final int x, final int y, final Emoji... variants) {
    super(codePoint, -1, variants);

    this.x = x;
    this.y = y;
  }

  public <%= name %>(@NonNull final int[] codePoints, final int x, final int y, final Emoji... variants) {
    super(codePoints, -1, variants);

    this.x = x;
    this.y = y;
  }

  @NonNull @Override public Drawable getDrawable(final Context context) {
    CacheKey key = new CacheKey(x, y);
    Bitmap bitmap = BITMAP_CACHE.get(key);
    if (bitmap != null)
      return new BitmapDrawable(context.getResources(), bitmap);
    if (scale == -1) scale = getScale(context.getResources());
    Bitmap strip = loadStrip(context);
    int size = 66 / scale;
    Bitmap cut = Bitmap.createBitmap(strip, 0, y * size, size, size);
    BITMAP_CACHE.put(key, cut);
    return new BitmapDrawable(context.getResources(), cut);
  }

  private int getScale(Resources resources) {
    int density = resources.getDisplayMetrics().densityDpi;
    return density < DENSITY_XHIGH ? 2 : 1;
  }

  private Bitmap loadStrip(final Context context) {
    Bitmap strip = (Bitmap) STRIP_REFS[x].get();
    if (strip == null) {
      synchronized (LOCK) {
        strip = (Bitmap) STRIP_REFS[x].get();
        if (strip == null) {
          Log.i(getClass().getSimpleName(), "Loading strip " + x);
          Resources resources = context.getResources();
          int resId = resources.getIdentifier("emoji_<%= package %>_sheet_" + x,
              "drawable", context.getPackageName());
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inSampleSize = scale;
          strip = BitmapFactory.decodeResource(resources, resId, options);
          STRIP_REFS[x] = new SoftReference<>(strip);
        }
      }
    }
    return strip;
  }

  private static class CacheKey {
    private final int x, y;

    private CacheKey(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof CacheKey
          && x == ((CacheKey) o).x
          && y == ((CacheKey) o).y;
    }

    @Override
    public int hashCode() {
      return (x << 16) ^ y;
    }
  }
}
