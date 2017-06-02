package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/2/2017.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.drawable.ForwardingDrawable;

public class StandardizedTransformedDrawable extends ForwardingDrawable {

  private RectF rect = new RectF();
  private float[] matrixValue = new float[9];

  private float widthScale;
  private float heightScale;
  private float fitScale;
  private float minScale;
  private float maxScale;
  private float[] scaleLevels;

  public StandardizedTransformedDrawable(Drawable drawable) {
    super(drawable);
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    updateScales();
  }

  private void updateScales() {
    Transformed transformed = (Transformed) getDrawable();
    rect.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    transformed.getMatrix().mapRect(rect);

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

  public boolean scale(float factor, float x, float y) {
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

    // Always return true
    return true;
  }

  public boolean rotate(float angle, float x, float y) {
    Transformed transformed = (Transformed) getDrawable();

    transformed.rotate(angle, x, y);
    fixPosition();
    updateScales();

    // Always return true
    return true;
  }

  private float getScale(Matrix matrix) {
    matrix.getValues(matrixValue);
    return (float) Math.sqrt(matrixValue[Matrix.MSCALE_X] * matrixValue[Matrix.MSCALE_X] +
        matrixValue[Matrix.MSKEW_X] * matrixValue[Matrix.MSKEW_X]);
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
