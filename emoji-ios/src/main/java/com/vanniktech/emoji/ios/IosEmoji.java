package com.vanniktech.emoji.ios;

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

public class IosEmoji extends Emoji {
  private static final Object LOCK = new Object();
  private static final int NUM_STRIPS = 51;
  private static final SoftReference[] STRIP_REFS =
      new SoftReference[NUM_STRIPS];
  private static final int CACHE_SIZE = 100;
  private static final LruCache<CacheKey, Bitmap> BITMAP_CACHE =
      new LruCache<>(CACHE_SIZE);
  private static final int SPRITE_SIZE = 64;
  private static final int SPRITE_SIZE_INC_BORDER = 66;

  static {
    for (int i = 0; i < NUM_STRIPS; i++) {
      STRIP_REFS[i] = new SoftReference<Bitmap>(null);
    }
  }

  private final int x;
  private final int y;

  public IosEmoji(@NonNull final int[] codePoints, final int x, final int y) {
    super(codePoints, -1);

    this.x = x;
    this.y = y;
  }

  public IosEmoji(final int codePoint, final int x, final int y) {
    super(codePoint, -1);

    this.x = x;
    this.y = y;
  }

  public IosEmoji(final int codePoint, final int x, final int y, final Emoji... variants) {
    super(codePoint, -1, variants);

    this.x = x;
    this.y = y;
  }

  public IosEmoji(@NonNull final int[] codePoints, final int x, final int y, final Emoji... variants) {
    super(codePoints, -1, variants);

    this.x = x;
    this.y = y;
  }

  @NonNull @Override public Drawable getDrawable(final Context context) {
    final CacheKey key = new CacheKey(x, y);
    final Bitmap bitmap = BITMAP_CACHE.get(key);
    if (bitmap != null) {
      return new BitmapDrawable(context.getResources(), bitmap);
    }
    final Bitmap strip = loadStrip(context);
    final Bitmap cut = Bitmap.createBitmap(strip, 1,
        y * SPRITE_SIZE_INC_BORDER + 1, SPRITE_SIZE, SPRITE_SIZE);
    BITMAP_CACHE.put(key, cut);
    return new BitmapDrawable(context.getResources(), cut);
  }

  private Bitmap loadStrip(final Context context) {
    Bitmap strip = (Bitmap) STRIP_REFS[x].get();
    if (strip == null) {
      synchronized (LOCK) {
        strip = (Bitmap) STRIP_REFS[x].get();
        if (strip == null) {
          Log.i(getClass().getSimpleName(), "Loading strip " + x);
          final Resources resources = context.getResources();
          final int resId = resources.getIdentifier("emoji_ios_sheet_" + x,
              "drawable", context.getPackageName());
          strip = BitmapFactory.decodeResource(resources, resId);
          STRIP_REFS[x] = new SoftReference<>(strip);
        }
      }
    }
    return strip;
  }

  private static final class CacheKey {
    private final int x;
    private final int y;

    private CacheKey(final int x, final int y) {
      this.x = x;
      this.y = y;
    }

    @Override public boolean equals(final Object o) {
      return o instanceof CacheKey
          && x == ((CacheKey) o).x
          && y == ((CacheKey) o).y;
    }

    @Override public int hashCode() {
      return (x << 16) ^ y;
    }
  }
}
