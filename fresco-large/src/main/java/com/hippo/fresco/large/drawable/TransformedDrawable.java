package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/2/2017.
 */

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.facebook.drawable.base.DrawableWithCaches;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.ScalingUtils;

public class TransformedDrawable extends ForwardingDrawable
    implements Transformed, DrawableWithCaches {

  private ScalingUtils.ScaleType scaleType = ScalingUtils.ScaleType.FIT_CENTER;
  private PointF focusPoint;

  private final Matrix matrix = new Matrix();

  public TransformedDrawable(Drawable drawable) {
    super(drawable);
    drawable.setBounds(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
  }

  @Override
  public void translate(float dx, float dy) {
    matrix.postTranslate(dx, dy);
    invalidateSelf();
  }

  @Override
  public void scale(float sx, float sy, float px, float py) {
    matrix.postScale(sx, sy, px, py);
    invalidateSelf();
  }

  @Override
  public void rotate(float degrees, float px, float py) {
    matrix.postRotate(degrees, px, py);
    invalidateSelf();
  }

  @Override
  public Matrix getMatrix() {
    return matrix;
  }

  @Override
  public void setScaleType(ScalingUtils.ScaleType scaleType) {
    this.scaleType = scaleType;
  }

  @Override
  public void setFocusPoint(PointF focusPoint) {
    this.focusPoint = focusPoint;
  }

  @Override
  public void applyScaleType() {
    scaleType.getTransform(
        matrix,
        getBounds(),
        getIntrinsicWidth(),
        getIntrinsicHeight(),
        (focusPoint != null) ? focusPoint.x : 0.5f,
        (focusPoint != null) ? focusPoint.y : 0.5f);
    invalidateSelf();
  }

  @Override
  public void dropCaches() {
    Drawable drawable = getCurrent();
    if (drawable instanceof DrawableWithCaches) {
      ((DrawableWithCaches) drawable).dropCaches();
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {}

  @Override
  public void draw(Canvas canvas) {
    int saved = canvas.save();
    canvas.concat(matrix);
    super.draw(canvas);
    canvas.restoreToCount(saved);
  }
}
