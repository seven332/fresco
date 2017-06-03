package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/3/2017.
 */

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

public class AnimatableTransformedDrawable extends TransformedDrawable implements Animatable {

  public AnimatableTransformedDrawable(Drawable drawable) {
    super(drawable);
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
