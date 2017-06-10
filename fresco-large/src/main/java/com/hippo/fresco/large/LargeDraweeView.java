package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/31/2017.
 */

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ArrayDrawable;
import com.facebook.drawee.drawable.DrawableParent;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;

import com.hippo.fresco.large.drawable.StandardizedTransformedDrawable;
import com.hippo.fresco.large.gesture.GestureRecognizer;

/**
 * {@code LargeDraweeView} shows a large image and supports gesture
 * to translate, scale and rotate the image.
 * <p>
 * It must be used with {@link FrescoLarge}.
 */
public class LargeDraweeView extends GenericDraweeView {

  private GestureRecognizer gestureRecognizer;
  private StandardizedTransformedDrawable transform;

  private boolean isFixAngleEnabled = true;

  private boolean hasValues;
  private float[] matrixValues = new float[9];

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
    gestureRecognizer = new GestureRecognizer(context, new Listener());
    gestureRecognizer.setIsDoubleTapEnabled(true);
    gestureRecognizer.setIsLongPressEnabled(false);
  }

  /**
   * Set whether fixing image rotating angle to meet right rect.
   * If {@code true}, a animation will be run to fix image rotating angle,
   * otherwise image rotating angle could be any value.
   */
  public void setIsFixAngleEnabled(boolean isFixAngleEnabled) {
    this.isFixAngleEnabled = isFixAngleEnabled;
  }

  /**
   * @return {@code true} if fixing image rotating angle is enabled, else {@code false}.
   */
  public boolean isFixAngleEnabled() {
    return isFixAngleEnabled;
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
      if (hasValues) {
        transform.setMatrixValues(matrixValues);
      }
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

  private class Listener extends GestureRecognizer.ListenerAdapter {

    @Override
    public void onDown(int count, float x, float y) {
      if (transform != null) {
        transform.cancelAnimator();
      }
    }

    @Override
    public void onUp(int count, float x, float y) {
      if (isFixAngleEnabled && count == 0 && transform != null) {
        transform.rotateToNextAngle(x, y);
      }
    }

    @Override
    public void onDoubleTap(float x, float y) {
      if (transform != null) {
        transform.scaleToNextLevel(x, y);
      }
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
      if (transform != null) {
        transform.fling(velocityX, velocityY);
      }
    }

    @Override
    public void onScale(float factor, float x, float y) {
      if (transform != null) {
        transform.scale(factor, x, y);
        requestDisallowInterceptTouchEvent();
      }
    }

    @Override
    public void onRotate(float angle, float x, float y) {
      if (transform != null) {
        transform.rotate(angle, x, y);
        requestDisallowInterceptTouchEvent();
      }
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    if (transform != null) {
      hasValues = true;
      savedState.hasValues = true;
      transform.getMatrixValues(matrixValues);
      System.arraycopy(matrixValues, 0, savedState.matrixValues, 0, 9);
    } else {
      hasValues = false;
      savedState.hasValues = false;
    }
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    hasValues = ss.hasValues;
    System.arraycopy(ss.matrixValues, 0, matrixValues, 0, 9);
    if (transform != null && hasValues) {
      transform.setMatrixValues(matrixValues);
    }
  }

  private static class SavedState extends BaseSavedState {

    boolean hasValues;
    float[] matrixValues = new float[9];

    SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(hasValues ? 1 : 0);
      out.writeFloatArray(matrixValues);
    }

    public static final Parcelable.Creator<SavedState> CREATOR
        = new Parcelable.Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };

    private SavedState(Parcel in) {
      super(in);
      hasValues = in.readInt() != 0;
      in.readFloatArray(matrixValues);
    }
  }

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
