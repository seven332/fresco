package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/2/2017.
 */

import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.drawable.ForwardingDrawable;

public class StandardizedTransformedDrawable extends ForwardingDrawable {

  private RectF rectF = new RectF();

  public StandardizedTransformedDrawable(Drawable drawable) {
    super(drawable);
  }

  public boolean translate(float dx, float dy) {
    Transformed transformed = (Transformed) getDrawable();

    rectF.set(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
    transformed.getMatrix().mapRect(rectF);

    if (le(rectF.left, 0.0f) && dx > 0) {
      dx = Math.min(dx, -rectF.left);
    } else if (ge(rectF.right, getBounds().width()) && dx < 0) {
      dx = Math.max(dx, getBounds().width() - rectF.right);
    } else {
      dx = 0.0f;
    }

    if (le(rectF.top, 0.0f) && dy > 0) {
      dy = Math.min(dy, -rectF.top);
    } else if (ge(rectF.bottom, getBounds().height()) && dy < 0) {
      dy = Math.max(dy, getBounds().height() - rectF.bottom);
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

  // Greater than or equal
  private static boolean ge(float a, float b) {
    return a >= b - 0.25f;
  }

  // less than or equal
  private static boolean le(float a, float b) {
    return a <= b + 0.25f;
  }
}
