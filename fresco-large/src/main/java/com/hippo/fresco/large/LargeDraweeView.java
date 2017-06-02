package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/31/2017.
 */

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ArrayDrawable;
import com.facebook.drawee.drawable.DrawableParent;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.Scaled;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import com.hippo.fresco.large.drawable.StandardizedTransformedDrawable;
import com.hippo.fresco.large.gesture.GestureRecognizer;

public class LargeDraweeView extends SimpleDraweeView implements GestureRecognizer.Listener {

  private GestureRecognizer gestureRecognizer;

  private StandardizedTransformedDrawable transform;

  private final ControllerListener controllerListener = new BaseControllerListener<Object>() {
    @Override
    public void onFinalImageSet(
        String id,
        @Nullable Object imageInfo,
        @Nullable Animatable animatable) {
      LargeDraweeView.this.onFinalImageSet();
    }

    @Override
    public void onRelease(String id) {
      LargeDraweeView.this.onRelease();
    }
  };

  public LargeDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
    super(context, hierarchy);
    init(context);
  }

  public LargeDraweeView(Context context) {
    super(context);
    init(context);
  }

  public LargeDraweeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public LargeDraweeView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  public LargeDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  private void init(Context context) {
    gestureRecognizer = new GestureRecognizer(context, this);
    gestureRecognizer.setIsDoubleTapEnabled(false);
    gestureRecognizer.setIsLongPressEnabled(false);
  }

  @Override
  public void setController(@Nullable DraweeController draweeController) {
    removeControllerListener(getController());
    super.setController(draweeController);
    addControllerListener(draweeController);
  }

  private void removeControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      //noinspection unchecked
      ((AbstractDraweeController) controller).removeControllerListener(controllerListener);
    }
  }

  private void addControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      //noinspection unchecked
      ((AbstractDraweeController) controller).addControllerListener(controllerListener);
    }
  }


  private Drawable getActualDrawable() {
    DraweeController controller = getController();
    if (controller != null) {
      return controller.getDrawable();
    }
    return null;
  }

  private void onFinalImageSet() {
    final Drawable drawable = getActualDrawable();

    // Auto start
    Animatable animatable = getActiveAnimatable(drawable);
    if (animatable != null) {
      animatable.start();
    }

    if (drawable instanceof StandardizedTransformedDrawable) {
      transform = (StandardizedTransformedDrawable) drawable;
    } else {
      transform = null;
    }
  }

  private void onRelease() {
    transform = null;
  }

  private void requestDisallowInterceptTouchEvent() {
    ViewParent parent = getParent();
    if (parent != null) {
      parent.requestDisallowInterceptTouchEvent(true);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    super.onTouchEvent(event);
    gestureRecognizer.onTouchEvent(event);
    return true;
  }

  @Override
  public void onSingleTap(float x, float y) {

  }

  @Override
  public void onDoubleTap(float x, float y) {

  }

  @Override
  public void onLongPress(float x, float y) {

  }

  @Override
  public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
    if (transform != null) {
      if (transform.translate(dx, dy)) {
        requestDisallowInterceptTouchEvent();
      }
    }
  }

  @Override
  public void onFling(float velocityX, float velocityY) {

  }

  @Override
  public void onScale(float factor, float x, float y) {

  }

  @Override
  public void onRotate(float angle, float x, float y) {}

  @Nullable
  private static Animatable getActiveAnimatable(Drawable drawable) {
    if (drawable == null) {
      return null;
    } else if (drawable instanceof Animatable) {
      return (Animatable) drawable;
    } else if (drawable instanceof DrawableParent) {
      final Drawable childDrawable = ((DrawableParent) drawable).getDrawable();
      return getActiveAnimatable(childDrawable);
    } else if (drawable instanceof ArrayDrawable) {
      final ArrayDrawable fadeDrawable = (ArrayDrawable) drawable;
      final int numLayers = fadeDrawable.getNumberOfLayers();

      for (int i = 0; i < numLayers; i++) {
        final Drawable childDrawable = fadeDrawable.getDrawable(i);
        final Animatable result = getActiveAnimatable(childDrawable);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
}
