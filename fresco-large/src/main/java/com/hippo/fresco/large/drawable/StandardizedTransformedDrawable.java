package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/2/2017.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.facebook.drawable.base.DrawableWithCaches;
import com.facebook.drawee.drawable.ForwardingDrawable;

public class StandardizedTransformedDrawable extends ForwardingDrawable
    implements DrawableWithCaches {

  private static final long DURATION = 200L;

  private static final int ANIMATOR_TRANSLATE = 0;
  private static final int ANIMATOR_SCALE = 1;
  private static final int ANIMATOR_ROTATE = 2;

  private Context context;

  private RectF rect = new RectF();
  private Matrix matrix = new Matrix();
  private float[] matrixValue = new float[9];

  private float widthScale;
  private float heightScale;
  private float fitScale;
  private float minScale;
  private float maxScale;
  private float[] scaleLevels;

  private final SparseArray<Animator> animators = new SparseArray<>();

  public StandardizedTransformedDrawable(Context context, Drawable drawable) {
    super(drawable);
    this.context = context;
  }

  @Override
  public void dropCaches() {
    Drawable drawable = getCurrent();
    if (drawable instanceof DrawableWithCaches) {
      ((DrawableWithCaches) drawable).dropCaches();
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    updateScales();
  }

  private void updateScales() {
    rect.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    Transformed transformed = (Transformed) getDrawable();
    float scale = getScale(transformed.getMatrix());
    matrix.set(transformed.getMatrix());
    matrix.postScale(1 / scale, 1 / scale);
    matrix.mapRect(rect);

    widthScale = (float) getBounds().width() / rect.width();
    heightScale = (float) getBounds().height() / rect.height();
    fitScale = Math.min(widthScale, heightScale);
    scaleLevels = mergeScaleLevels(widthScale, heightScale, 1.0f, 3.0f);
    minScale = scaleLevels[0];
    maxScale = scaleLevels[scaleLevels.length - 1];
  }

  private float[] mergeScaleLevels(float... scales) {
    Arrays.sort(scales);

    List<Float> scaleLevels = new ArrayList<>();
    float lastScale = Float.NaN;

    for (float scale : scales) {
      if (Float.isNaN(lastScale) || !eq(scale, lastScale)) {
        scaleLevels.add(scale);
      }
      lastScale = scale;
    }

    float[] result = new float[scaleLevels.size()];
    for (int i = 0, n = scaleLevels.size(); i < n; ++i) {
      result[i] = scaleLevels.get(i);
    }
    return result;
  }

  public void cancelAnimator() {
    int size = animators.size();
    if (size != 0) {
      Animator[] copy = new Animator[size];
      for (int i = 0; i < size; ++i) {
        copy[i] = animators.valueAt(i);
      }
      for (Animator animator : copy) {
        animator.cancel();
      }
      animators.clear();
    }
  }

  private void startAnimator(Animator animator, final int type) {
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        animators.remove(type);
      }
    });

    Animator oldAnimator = animators.get(type);
    if (oldAnimator != null) {
      oldAnimator.cancel();
    }
    animators.put(type, animator);

    animator.start();
  }

  public boolean translate(float dx, float dy) {
    Transformed transformed = (Transformed) getDrawable();

    rect.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    transformed.getMatrix().mapRect(rect);

    if (le(rect.left, 0.0f) && dx > 0) {
      dx = Math.min(dx, -rect.left);
    } else if (ge(rect.right, getBounds().width()) && dx < 0) {
      dx = Math.max(dx, getBounds().width() - rect.right);
    } else {
      dx = 0.0f;
    }

    if (le(rect.top, 0.0f) && dy > 0) {
      dy = Math.min(dy, -rect.top);
    } else if (ge(rect.bottom, getBounds().height()) && dy < 0) {
      dy = Math.max(dy, getBounds().height() - rect.bottom);
    } else {
      dy = 0.0f;
    }

    if (dx != 0.0f || dy != 0.0f) {
      transformed.translate(dx, dy);
      return true;
    } else {
      return false;
    }
  }

  // Make the image in center
  private void fixPosition() {
    Transformed transformed = (Transformed) getDrawable();

    rect.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    transformed.getMatrix().mapRect(rect);

    float dx = 0.0f, dy = 0.0f;
    int windowWidth = getBounds().width();
    int windowHeight = getBounds().height();

    if (rect.width() < windowWidth) {
      dx = (windowWidth / 2) - rect.centerX();
    } else {
      if (rect.left > 0.0f) {
        dx = -rect.left;
      } else if (rect.right < windowWidth) {
        dx = windowWidth - rect.right;
      }
    }

    if (rect.height() < windowHeight) {
      dy = (windowHeight / 2) - rect.centerY();
    } else {
      if (rect.top > 0.0f) {
        dy = -rect.top;
      } else if (rect.bottom < windowHeight) {
        dy = windowHeight - rect.bottom;
      }
    }

    if (!eq(dx, 0.0f) || !eq(dy, 0.0f)) {
      transformed.translate(dx, dy);
    }
  }

  public void scale(float factor, float x, float y) {
    Transformed transformed = (Transformed) getDrawable();

    final float scale = getScale(transformed.getMatrix());
    if (scale < maxScale && factor > 1.0f) {
      factor = Math.min(maxScale / scale, factor);
    } else if (scale > minScale && factor < 1.0f) {
      factor = Math.max(minScale / scale, factor);
    } else {
      factor = 1.0f;
    }

    if (factor != 1.0f) {
      transformed.scale(factor, factor, x, y);
      fixPosition();
    }
  }

  public void rotate(float angle, float x, float y) {
    Transformed transformed = (Transformed) getDrawable();

    transformed.rotate(angle, x, y);
    fixPosition();
    updateScales();
  }

  public void rotateToNextAngle(final float x, final float y) {
    Transformed transformed = (Transformed) getDrawable();

    float angle = getAngleToRect(transformed.getMatrix());
    ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0.0f, angle);
    rotateAnimator.setDuration(DURATION);
    rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      private float previousAngle = 0.0f;
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float angle = (float) animation.getAnimatedValue();
        rotate(angle - previousAngle, x, y);
        previousAngle = angle;
      }
    });
    startAnimator(rotateAnimator, ANIMATOR_ROTATE);
  }

  private float getNextScale(float currentScale) {
    float result = Float.NaN;

    for (float scale : scaleLevels) {
      if (!eq(currentScale, scale) && scale > currentScale) {
        result = scale;
        break;
      }
    }

    if (Float.isNaN(result)) {
      result = scaleLevels[0];
    }

    return result;
  }

  public void scaleToNextLevel(final float x, final float y) {
    Transformed transformed = (Transformed) getDrawable();

    final float currentScale = getScale(transformed.getMatrix());
    final float nextScale = getNextScale(currentScale);

    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(currentScale, nextScale);
    scaleAnimator.setDuration(DURATION);
    scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      private float previousScale = currentScale;
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float scale = (float) animation.getAnimatedValue();
        scale(scale / previousScale, x, y);
        previousScale = scale;
      }
    });
    startAnimator(scaleAnimator, ANIMATOR_SCALE);
  }

  public void fling(float velocityX, float velocityY) {
    Transformed transformed = (Transformed) getDrawable();

    rect.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    transformed.getMatrix().mapRect(rect);

    Fling fling = new Fling(context);
    float dx =
        (float) (fling.getSplineFlingDistance((int) velocityX) * Math.signum(velocityX));
    float dy =
        (float) (fling.getSplineFlingDistance((int) velocityY) * Math.signum(velocityY));
    int durationX = fling.getSplineFlingDuration((int) velocityX);
    int durationY = fling.getSplineFlingDuration((int) velocityY);

    int windowWidth = getBounds().width();
    int windowHeight = getBounds().height();
    // Fix dx and durationX
    if (rect.width() < windowWidth) {
      dx = 0.0f;
      durationX = 0;
    } else if (dx > -rect.left) {
      durationX = fling.adjustDuration(0, (int) dx, (int) -rect.left, durationX);
      dx = -rect.left;
    } else if (dx < windowWidth - rect.right) {
      durationX = fling.adjustDuration(0, (int) dx, (int) (windowWidth - rect.right), durationX);
      dx = windowWidth - rect.right;
    }
    // Fix dy and durationY
    if (rect.height() < windowHeight) {
      dy = 0.0f;
      durationY = 0;
    } else if (dy > -rect.top) {
      durationY = fling.adjustDuration(0, (int) dy, (int) -rect.top, durationY);
      dy = -rect.top;
    } else if (dy < windowHeight - rect.bottom) {
      durationY = fling.adjustDuration(0, (int) dy, (int) (windowHeight - rect.bottom), durationY);
      dy = windowHeight - rect.bottom;
    }

    final float finalDX = dx;
    final float finalDY = dy;
    final int finalDuration = Math.max(durationX, durationY);
    if (finalDuration == 0 || (eq(finalDX, 0.0f) && eq(finalDY, 0.0f))) {
      // Can't scroll
      return;
    }

    fling.setDuration(finalDuration);
    fling.setFloatValues(0.0f, 1.0f);
    fling.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      float previousDX = 0.0f;
      float previousDY = 0.0f;
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float factor = (float) animation.getAnimatedValue();
        float dx = factor * finalDX;
        float dy = factor * finalDY;
        translate(dx - previousDX, dy - previousDY);
        previousDX = dx;
        previousDY = dy;
      }
    });
    startAnimator(fling, ANIMATOR_TRANSLATE);
  }

  private float getScale(Matrix matrix) {
    matrix.getValues(matrixValue);
    float x = matrixValue[Matrix.MSCALE_X];
    float y = matrixValue[Matrix.MSKEW_X];
    return (float) Math.sqrt(x * x + y * y);
  }

  private float getAngleToRect(Matrix matrix) {
    matrix.getValues(matrixValue);
    float x = matrixValue[Matrix.MSCALE_X];
    float y = matrixValue[Matrix.MSKEW_X];
    float angle = (float) Math.toDegrees(Math.atan(- y / x));
    if (Float.isNaN(angle)) {
      angle = 90.0f;
    } else if (angle < 0.0f) {
      angle += 90.0f;
    }
    if (angle < 45.0f) {
      return -angle;
    } else {
      return 90.0f - angle;
    }
  }

  // Greater than or equal
  private static boolean ge(float a, float b) {
    return a >= b - 0.1f;
  }

  // less than or equal
  private static boolean le(float a, float b) {
    return a <= b + 0.1f;
  }

  // equal
  private static boolean eq(float a, float b) {
    return a <= b + 0.05f && a >= b - 0.05f;
  }
}
