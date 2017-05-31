package com.facebook.drawee.drawable;

/*
 * Created by Hippo on 5/30/2017.
 */

import android.graphics.PointF;

public interface Scaled {

  void setScaleType(ScalingUtils.ScaleType scaleType);

  void setFocusPoint(PointF focusPoint);

  void applyScaleType();
}
