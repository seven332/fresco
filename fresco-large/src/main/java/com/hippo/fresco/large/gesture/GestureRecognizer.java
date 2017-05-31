/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.fresco.large.gesture;

/*
 * Created by Hippo on 5/26/2017.
 */

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GestureRecognizer implements GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener,
    RotationGestureDetector.OnRotateGestureListener {

  private static final float SCALE_SLOP = 0.015f;
  private static final float ROTATE_SLOP = 0.5f;

  private Listener listener;

  private GestureDetector gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;
  private RotationGestureDetector rotationGestureDetector;

  private boolean isDoubleTapEnabled = true;
  private boolean isLongPressEnabled = true;
  private boolean isScaleEnabled = true;
  private boolean isRotateEnabled = true;

  private boolean isScaling;
  private boolean isRotating;

  private float scaling;
  private float rotating;

  public GestureRecognizer(Context context, Listener listener) {
    this.listener = listener;

    gestureDetector = new GestureDetector(context, this);
    scaleGestureDetector = new ScaleGestureDetector(context, this);
    rotationGestureDetector = new RotationGestureDetector(this);
  }

  public void setIsDoubleTapEnabled(boolean isDoubleTapEnabled) {
    if (this.isDoubleTapEnabled != isDoubleTapEnabled) {
      this.isDoubleTapEnabled = isDoubleTapEnabled;
      if (isDoubleTapEnabled) {
        gestureDetector.setOnDoubleTapListener(this);
      } else {
        gestureDetector.setOnDoubleTapListener(null);
      }
    }
  }

  public boolean isDoubleTapEnabled() {
    return isDoubleTapEnabled;
  }

  public void setIsLongPressEnabled(boolean isLongPressEnabled) {
    if (this.isLongPressEnabled != isLongPressEnabled) {
      this.isLongPressEnabled = isLongPressEnabled;
      gestureDetector.setIsLongpressEnabled(isLongPressEnabled);
    }
  }

  public boolean isLongPressEnabled() {
    return isLongPressEnabled;
  }

  public void setIsScaleEnabled(boolean isScaleEnabled) {
    this.isScaleEnabled = isScaleEnabled;
  }

  public boolean isScaleEnabled() {
    return isScaleEnabled;
  }

  public void setIsRotateEnabled(boolean isRotateEnabled) {
    this.isRotateEnabled = isRotateEnabled;
  }

  public boolean isRotateEnabled() {
    return isRotateEnabled;
  }

  public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    scaleGestureDetector.onTouchEvent(event);
    rotationGestureDetector.onTouchEvent(event);
    return true;
  }

  @Override
  public boolean onDown(MotionEvent e) {
    return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {}

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    if (!isDoubleTapEnabled) {
      listener.onSingleTap(e.getX(), e.getY());
    }
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (!isScaling && !isRotating) {
      listener.onScroll(-distanceX, -distanceY,
          e2.getX() - e1.getX(), e2.getY() - e1.getY(),
          e2.getX(), e2.getY());
    }
    return true;
  }

  @Override
  public void onLongPress(MotionEvent e) {
    listener.onLongPress(e.getX(), e.getY());
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    listener.onFling(velocityX, velocityY);
    return true;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    if (isDoubleTapEnabled) {
      listener.onSingleTap(e.getX(), e.getY());
    }
    return true;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
    listener.onDoubleTap(e.getX(), e.getY());
    return true;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Scale
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    scaling = detector.getScaleFactor();
    if (scaling < 1.0f) {
      scaling = 1.0f / scaling;
    }
    scaling -= 1.0f;

    if (isRotating) {
      if (rotating < ROTATE_SLOP && scaling > SCALE_SLOP) {
        // Switch from rotating to scaling
        isRotating = false;
        isScaling = true;
      }
    } else if (!isScaling) {
      isScaling = true;
    }

    if (isScaling) {
      listener.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
    }

    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    if (isScaleEnabled) {
      if (!isRotating) {
        isScaling = true;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    isScaling = false;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Rotation
  ///////////////////////////////////////////////////////////////////////////

  @Override
  public void onRotate(float angle, float x, float y) {
    rotating = Math.abs(angle);

    if (isScaling) {
      if (scaling < SCALE_SLOP && rotating > ROTATE_SLOP) {
        // Switch from scaling to rotating
        isScaling = false;
        isRotating = true;
      }
    } else if (!isRotating) {
      isRotating = true;
    }

    if (isRotating) {
      listener.onRotate(angle, x, y);
    }
  }

  @Override
  public boolean onRotateBegin() {
    if (isRotateEnabled) {
      if (!isScaling) {
        isRotating = true;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void onRotateEnd() {
    isRotating = false;
  }

  public interface Listener {

    void onSingleTap(float x, float y);

    void onDoubleTap(float x, float y);

    void onLongPress(float x, float y);

    void onScroll(float dx, float dy, float totalX, float totalY, float x, float y);

    void onFling(float velocityX, float velocityY);

    void onScale(float factor, float x, float y);

    void onRotate(float angle, float x, float y);
  }
}
