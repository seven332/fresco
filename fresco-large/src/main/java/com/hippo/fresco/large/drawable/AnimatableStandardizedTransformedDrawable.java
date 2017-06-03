package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/3/2017.
 */

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

public class AnimatableStandardizedTransformedDrawable extends StandardizedTransformedDrawable
    implements Animatable {

  public AnimatableStandardizedTransformedDrawable(
      Context context,
      Drawable drawable) {
    super(context, drawable);
  }

  @Override
  public void start() {
    Drawable drawable = getCurrent();
    if (drawable instanceof Animatable) {
      ((Animatable) drawable).start();
    }
  }

  @Override
  public void stop() {
    Drawable drawable = getCurrent();
    if (drawable instanceof Animatable) {
      ((Animatable) drawable).stop();
    }
  }

  @Override
  public boolean isRunning() {
    Drawable drawable = getCurrent();
    if (drawable instanceof Animatable) {
      return ((Animatable) drawable).isRunning();
    } else {
      return false;
    }
  }
}
