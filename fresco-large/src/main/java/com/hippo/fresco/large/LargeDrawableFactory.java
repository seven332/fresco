package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.util.concurrent.Executor;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;

import com.hippo.fresco.large.drawable.StandardizedTransformedDrawable;
import com.hippo.fresco.large.drawable.Transformed;
import com.hippo.fresco.large.drawable.TransformedDrawable;
import com.hippo.fresco.large.drawable.SubsamplingDrawable;

public class LargeDrawableFactory implements DrawableFactory {

  private final Context context;

  private boolean hasDecodeExecutor;
  private final Object lockDecodeExecutor = new Object();
  private Executor decodeExecutor;

  private boolean hasAnimatedDrawableFactory;
  private final Object lockAnimatedDrawableFactory = new Object();
  private AnimatedDrawableFactory animatedDrawableFactory;

  public LargeDrawableFactory(Context context) {
    this.context = context;
  }

  @Override
  public final boolean supportsImageType(CloseableImage image) {
    return true;
  }

  private Executor getDecodeExecutor() {
    if (!hasDecodeExecutor) {
      synchronized (lockDecodeExecutor) {
        if (!hasDecodeExecutor) {
          hasDecodeExecutor = true;
          decodeExecutor =
              Fresco.getImagePipelineFactory().getConfig().getExecutorSupplier().forDecode();
        }
      }
    }
    return decodeExecutor;
  }

  private AnimatedDrawableFactory getAnimatedDrawableFactory() {
    if (!hasAnimatedDrawableFactory) {
      synchronized (lockAnimatedDrawableFactory) {
        if (!hasAnimatedDrawableFactory) {
          hasAnimatedDrawableFactory = true;
          AnimatedFactory factory = Fresco.getImagePipelineFactory().getAnimatedFactory();
          if (factory != null) {
            animatedDrawableFactory = factory.getAnimatedDrawableFactory(context);
          }
        }
      }
    }
    return animatedDrawableFactory;
  }

  @Nullable
  @Override
  public final Drawable createDrawable(CloseableImage image) {
    Drawable drawable = null;

    if (image instanceof CloseableLargeImage) {
      drawable = new SubsamplingDrawable(
          ((CloseableLargeImage) image).getDecoder(),
          getDecodeExecutor());
    } else {
      drawable = createNormalDrawable(image);
      if (drawable != null) {
        drawable = new TransformedDrawable(drawable);
      }
    }

    if (drawable instanceof Transformed) {
      drawable = new StandardizedTransformedDrawable(context, drawable);
    }

    return drawable;
  }

  private Drawable createNormalDrawable(CloseableImage image) {
    if (image instanceof CloseableStaticBitmap) {
      CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
      Drawable bitmapDrawable = new BitmapDrawable(
          context.getResources(),
          closeableStaticBitmap.getUnderlyingBitmap());
      if (closeableStaticBitmap.getRotationAngle() == 0 ||
          closeableStaticBitmap.getRotationAngle() == EncodedImage.UNKNOWN_ROTATION_ANGLE) {
        return bitmapDrawable;
      } else {
        return new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle());
      }
    } else {
      AnimatedDrawableFactory factory = getAnimatedDrawableFactory();
      if (factory != null) {
        return factory.create(image);
      }
    }
    return null;
  }
}
