package com.hippo.fresco.large;

/*
 * Created by Hippo on 6/10/2017.
 */

import javax.annotation.Nullable;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;

public class SimpleLargeDraweeView extends LargeDraweeView {

  private static Supplier<? extends SimpleDraweeControllerBuilder> sDraweeControllerBuilderSupplier;

  /** Initializes {@link SimpleLargeDraweeView} with supplier of Drawee controller builders. */
  public static void initialize(
      Supplier<? extends SimpleDraweeControllerBuilder> draweeControllerBuilderSupplier) {
    sDraweeControllerBuilderSupplier = draweeControllerBuilderSupplier;
  }

  /** Shuts {@link SimpleLargeDraweeView} down. */
  public static void shutDown() {
    sDraweeControllerBuilderSupplier = null;
  }

  private SimpleDraweeControllerBuilder simpleDraweeControllerBuilder;

  public SimpleLargeDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
    super(context, hierarchy);
  }

  public SimpleLargeDraweeView(Context context) {
    super(context);
    init();
  }

  public SimpleLargeDraweeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SimpleLargeDraweeView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public SimpleLargeDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    simpleDraweeControllerBuilder = sDraweeControllerBuilderSupplier.get();
  }

  protected SimpleDraweeControllerBuilder getControllerBuilder() {
    return simpleDraweeControllerBuilder;
  }

  /**
   * Displays an image given by the uri string.
   *
   * @param uriString uri string of the image
   */
  public void setImageURI(@Nullable String uriString) {
    setImageURI(uriString, null);
  }

  /**
   * Displays an image given by the uri.
   *
   * @param uri uri of the image
   * @param callerContext caller context
   */
  public void setImageURI(Uri uri, @Nullable Object callerContext) {
    DraweeController controller = simpleDraweeControllerBuilder
        .setCallerContext(callerContext)
        .setUri(uri)
        .setOldController(getController())
        .build();
    setController(controller);
  }

  /**
   * Displays an image given by the uri string.
   *
   * @param uriString uri string of the image
   * @param callerContext caller context
   */
  public void setImageURI(@Nullable String uriString, @Nullable Object callerContext) {
    Uri uri = (uriString != null) ? Uri.parse(uriString) : null;
    setImageURI(uri, callerContext);
  }

  /**
   * Sets the actual image resource to the given resource ID.
   *
   * Similar to {@link #setImageResource(int)}, this sets the displayed image to the given resource.
   * However, {@link #setImageResource(int)} bypasses all Drawee functionality and makes the view
   * act as a normal {@link android.widget.ImageView}, whereas this method keeps all of the
   * Drawee functionality, including the {@link com.facebook.drawee.interfaces.DraweeHierarchy}.
   *
   * @param resourceId the resource ID to use.
   */
  public void setActualImageResource(@DrawableRes int resourceId) {
    setActualImageResource(resourceId, null);
  }

  /**
   * Sets the actual image resource to the given resource ID.
   *
   * Similar to {@link #setImageResource(int)}, this sets the displayed image to the given resource.
   * However, {@link #setImageResource(int)} bypasses all Drawee functionality and makes the view
   * act as a normal {@link android.widget.ImageView}, whereas this method keeps all of the
   * Drawee functionality, including the {@link com.facebook.drawee.interfaces.DraweeHierarchy}.
   *
   * @param resourceId the resource ID to use.
   * @param callerContext caller context
   */
  public void setActualImageResource(@DrawableRes int resourceId, @Nullable Object callerContext) {
    setImageURI(UriUtil.getUriForResourceId(resourceId), callerContext);
  }
}
