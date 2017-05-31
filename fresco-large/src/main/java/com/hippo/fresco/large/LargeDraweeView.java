package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/31/2017.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import com.hippo.fresco.large.drawable.SubsamplingDrawable;
import com.hippo.fresco.large.gesture.GestureRecognizer;

public class LargeDraweeView extends SimpleDraweeView implements GestureRecognizer.Listener {

  private GestureRecognizer gestureRecognizer;

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
  public boolean onTouchEvent(MotionEvent event) {
    super.onTouchEvent(event);
    gestureRecognizer.onTouchEvent(event);
    return true;
  }

  private SubsamplingDrawable getSubsamplingDrawable() {
    DraweeController controller = getController();
    if (controller != null) {
      Drawable drawable = controller.getDrawable();
      if (drawable instanceof SubsamplingDrawable) {
        return (SubsamplingDrawable) drawable;
      }
    }
    return null;
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
    SubsamplingDrawable drawable = getSubsamplingDrawable();
    if (drawable != null) {
      drawable.translate(dx, dy);
    }
  }

  @Override
  public void onFling(float velocityX, float velocityY) {

  }

  @Override
  public void onScale(float factor, float x, float y) {
    SubsamplingDrawable drawable = getSubsamplingDrawable();
    if (drawable != null) {
      drawable.scale(factor, factor, x, y);
    }
  }

  @Override
  public void onRotate(float angle, float x, float y) {
    SubsamplingDrawable drawable = getSubsamplingDrawable();
    if (drawable != null) {
      drawable.rotate(angle, x, y);
    }
  }
}
