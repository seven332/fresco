package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 6/2/2017.
 */

import android.graphics.Matrix;

import com.facebook.drawee.drawable.Scaled;

public interface Transformed extends Scaled {

  Matrix getMatrix();

  void translate(float dx, float dy);

  void scale(float sx, float sy, float px, float py);

  void rotate(float degrees, float px, float py);
}
