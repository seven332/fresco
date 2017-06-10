package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.facebook.common.references.CloseableReference;
import com.facebook.drawable.base.DrawableWithCaches;
import com.facebook.drawee.drawable.ScalingUtils;

import com.hippo.fresco.large.decoder.ImageRegionDecoder;

public class SubsamplingDrawable extends Drawable implements Transformed, DrawableWithCaches {

  private static final boolean DEBUG = false;

  private static final int MAX_TILE_SIZE = 512;

  private static final int INVALID_SAMPLE = -1;

  private ImageRegionDecoder decoder;
  private DecoderReleaser releaser;
  private Executor executor;

  private final int width;
  private final int height;
  private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

  private ScalingUtils.ScaleType scaleType = ScalingUtils.ScaleType.FIT_CENTER;
  private PointF focusPoint;

  private final Matrix matrix = new Matrix();
  private final float[] matrixValue = new float[9];
  private boolean matrixValueDirty = true;
  private final Matrix invertedMatrix = new Matrix();
  private boolean invertedMatrixDirty = true;
  private final Matrix tempMatrix = new Matrix();

  private int windowWidth;
  private int windowHeight;
  private int windowOffsetX;
  private int windowOffsetY;

  // The visible rect of the image
  private final RectF visibleRectF = new RectF();
  private final Rect visibleRect = new Rect();

  private final PreviewTile preview;
  private final int previewSample;

  private int currentSample = INVALID_SAMPLE;
  private final SparseArray<List<Tile>> tilesMap = new SparseArray<>();

  private float[] debugPoints;
  private float[] debugLines;
  private Paint debugPaint;

  public SubsamplingDrawable(CloseableReference<ImageRegionDecoder> decoderReference,
      Executor executor) {
    this.decoder = decoderReference.get();
    this.executor = executor;

    releaser = new DecoderReleaser(decoderReference);
    releaser.obtain();

    width = decoder.getWidth();
    height = decoder.getHeight();

    previewSample = decoder.getPreviewSample();
    preview = new PreviewTile(decoder.getPreview(), previewSample);

    if (DEBUG) {
      debugPoints = new float[8];
      debugLines = new float[16];
      debugPaint = new Paint();
      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setStrokeWidth(3);
      debugPaint.setColor(Color.RED);
    }
  }

  @Override
  public void translate(float dx, float dy) {
    matrix.postTranslate(dx, dy);
    matrixValueDirty = true;
    invertedMatrixDirty = true;
    invalidateSelf();
  }

  @Override
  public void scale(float sx, float sy, float px, float py) {
    matrix.postScale(sx, sy, px, py);
    matrixValueDirty = true;
    invertedMatrixDirty = true;
    invalidateSelf();
  }

  @Override
  public void rotate(float degrees, float px, float py) {
    matrix.postRotate(degrees, px, py);
    matrixValueDirty = true;
    invertedMatrixDirty = true;
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
        width,
        height,
        (focusPoint != null) ? focusPoint.x : 0.5f,
        (focusPoint != null) ? focusPoint.y : 0.5f);
    invalidateSelf();
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    windowWidth = bounds.width();
    windowHeight = bounds.height();
    windowOffsetX = bounds.left;
    windowOffsetY = bounds.top;
  }

  private static int calculateSample(int scale) {
    int sample = Math.max(1, scale);
    return prevPow2(sample);
  }

  private void gc() {
    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      final int sample = tilesMap.keyAt(i);
      final List<Tile> list = tilesMap.valueAt(i);
      if (list == null) {
        continue;
      }

      if (sample == currentSample) {
        // Only recycle invisible tiles for current sample
        for (Tile tile : list) {
          if (!tile.isVisible()) {
            tile.recycle();
          }
        }
      } else {
        // Recycle all tiles for all the other samples
        for (Tile tile : list) {
          tile.recycle();
        }
      }
    }
  }

  // Creates a tile list for the sample, the rect of each tile is filled
  private List<Tile> createTileList(int sample) {
    int step = MAX_TILE_SIZE * sample;
    List<Tile> list = new ArrayList<>(ceilDiv(width, step) * ceilDiv(height, step));

    for (int y = 0; y < height; y += step) {
      for (int x = 0; x < width; x += step) {
        int w = Math.min(step, width - x);
        int h = Math.min(step, height - y);
        Rect rect = new Rect(x, y, x + w, y + h);
        Tile tile = new Tile(sample, rect);
        list.add(tile);
      }
    }

    return list;
  }

  private float[] getMatrixValue() {
    if (matrixValueDirty) {
      matrixValueDirty = false;
      matrix.getValues(matrixValue);
    }
    return matrixValue;
  }

  private Matrix getInvertedMatrix() {
    if (invertedMatrixDirty) {
      invertedMatrixDirty = false;
      matrix.invert(invertedMatrix);
    }
    return invertedMatrix;
  }

  private static int getMatrixScale(float[] matrix) {
    return (int) Math.round(1 / Math.sqrt(matrix[Matrix.MSCALE_X] * matrix[Matrix.MSCALE_X] +
        matrix[Matrix.MSKEW_X] * matrix[Matrix.MSKEW_X]));
  }

  private int getCurrentSample() {
    if (previewSample == 1) {
      // The preview is the most clear, no need tiles
      currentSample = INVALID_SAMPLE;
    } else {
      float[] matrixValue = getMatrixValue();
      // Get scale from matrix
      int scale = getMatrixScale(matrixValue);
      // Get the sample
      int sample = calculateSample(scale);

      if (sample >= previewSample) {
        // No need to create tiles which is
        currentSample = INVALID_SAMPLE;
      } else {
        currentSample = sample;
      }
    }
    return currentSample;
  }

  private Rect getVisibleRect() {
    visibleRectF.set(0, 0, windowWidth, windowHeight);
    Matrix matrix = getInvertedMatrix();
    matrix.mapRect(visibleRectF);

    visibleRectF.roundOut(visibleRect);
    if (!visibleRect.intersect(0, 0, width, height)) {
      visibleRect.setEmpty();
    }
    return visibleRect;
  }

  private void drawTiles(Canvas canvas) {
    // Get current sample
    int currentSample = getCurrentSample();

    if (currentSample == INVALID_SAMPLE) {
      // No current sample, just draw preview
      preview.draw(canvas, paint, matrix, tempMatrix);
    } else {
      // Get tile list for current sample
      List<Tile> currentTileList = tilesMap.get(currentSample);
      if (currentTileList == null) {
        currentTileList = createTileList(currentSample);
        tilesMap.put(currentSample, currentTileList);
      }

      // Get visible rect in the image
      Rect visibleRect = getVisibleRect();

      boolean hasDrawnPreview = false;
      for (Tile tile : currentTileList) {
        if (tile.updateVisibility(visibleRect) && !tile.isLoaded()) {
          tile.load();
          // Use the visible to mark drew tile to avoid draw it twice
          if (!hasDrawnPreview) {
            hasDrawnPreview = true;
            preview.draw(canvas, paint, matrix, tempMatrix);
          }
        }
      }

      for (Tile tile : currentTileList) {
        if (tile.isVisible()) {
          tile.draw(canvas, paint, matrix, tempMatrix);
        }
      }
    }

    gc();
  }

  @Override
  public void draw(@Nonnull Canvas canvas) {
    if (windowWidth > 0 && windowHeight > 0) {
      if (windowOffsetX != 0 || windowOffsetY != 0) {
        canvas.translate(windowOffsetX, windowOffsetY);
      }
      drawTiles(canvas);
      if (windowOffsetX != 0 || windowOffsetY != 0) {
        canvas.translate(-windowOffsetX, -windowOffsetY);
      }
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }

  @Override
  public void setAlpha(int alpha) {
    // Fade effect makes the image flickering when new tiles decoded
    // paint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    // Always return PixelFormat.TRANSLUCENT
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void dropCaches() {
    close();
  }

  public void close() {
    releaser.release();

    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      final List<Tile> list = tilesMap.valueAt(i);
      if (list == null) {
        continue;
      }

      // Close all tiles
      for (Tile tile : list) {
        tile.close();
      }
    }
  }

  private static class DecoderReleaser {

    private CloseableReference<ImageRegionDecoder> decoderReference;
    private int reference;

    private DecoderReleaser(CloseableReference<ImageRegionDecoder> decoderReference) {
      this.decoderReference = decoderReference;
    }

    private void obtain() {
      ++reference;
    }

    private void release() {
      if (--reference == 0) {
        decoderReference.close();
      }
    }
  }

  private class PreviewTile {

    private Bitmap bitmap;
    private int sample;

    public PreviewTile(Bitmap bitmap, int sample) {
      this.bitmap = bitmap;
      this.sample = sample;
    }

    public void draw(Canvas canvas, Paint paint, Matrix matrix, Matrix temp) {
      if (bitmap != null) {
        temp.set(matrix);
        temp.preScale(sample, sample);
        canvas.drawBitmap(bitmap, temp, paint);
      }
    }
  }

  private class Tile {
    public int sample;
    public Rect rect;
    public Bitmap bitmap;
    // The task to decode image
    private LoadingTask task;
    //
    private boolean visible;
    // True if can't decode the source
    // Check this flag to avoid infinity loading
    private boolean failed;

    public Tile(int sample, Rect rect) {
      this.sample = sample;
      this.rect = rect;

      releaser.obtain();
    }

    /**
     * Returns {@code true} if the rect contain that rect.
     */
    public boolean contains(Tile tile) {
      return rect.contains(tile.rect);
    }

    /**
     * Update the visibility according to the visible rect in the image.
     * Returns {@code true} if it's visible.
     */
    public boolean updateVisibility(Rect visibleRect) {
      visible = Rect.intersects(visibleRect, rect);
      return visible;
    }

    /**
     * Set the visibility.
     */
    public boolean setVisibility(boolean visible) {
      this.visible = visible;
      return visible;
    }

    /**
     * Returns {@code true} if it's visible.
     */
    public boolean isVisible() {
      return visible;
    }

    /**
     * Starts a task to decode the image.
     */
    public void load() {
      if (bitmap == null && task == null && !failed && releaser != null) {
        task = new LoadingTask();
        task.executeOnExecutor(executor);
      }
    }

    /**
     * Returns {@code true} if the tile has a bitmap.
     */
    public boolean isLoaded() {
      return bitmap != null;
    }

    /**
     * Draws the tile.
     */
    public void draw(Canvas canvas, Paint paint, Matrix matrix, Matrix temp) {
      if (bitmap != null) {
        temp.set(matrix);
        temp.preTranslate(rect.left, rect.top);
        temp.preScale(sample, sample);
        canvas.drawBitmap(bitmap, temp, paint);

        if (DEBUG) {
          debugPoints[0] = rect.left;
          debugPoints[1] = rect.top;
          debugPoints[2] = rect.right;
          debugPoints[3] = rect.top;
          debugPoints[4] = rect.right;
          debugPoints[5] = rect.bottom;
          debugPoints[6] = rect.left;
          debugPoints[7] = rect.bottom;

          matrix.mapPoints(debugPoints);

          debugLines[0] = debugPoints[0];
          debugLines[1] = debugPoints[1];
          debugLines[2] = debugPoints[2];
          debugLines[3] = debugPoints[3];

          debugLines[4] = debugPoints[2];
          debugLines[5] = debugPoints[3];
          debugLines[6] = debugPoints[4];
          debugLines[7] = debugPoints[5];

          debugLines[8] = debugPoints[4];
          debugLines[9] = debugPoints[5];
          debugLines[10] = debugPoints[6];
          debugLines[11] = debugPoints[7];

          debugLines[12] = debugPoints[6];
          debugLines[13] = debugPoints[7];
          debugLines[14] = debugPoints[0];
          debugLines[15] = debugPoints[1];

          canvas.drawLines(debugLines, debugPaint);
        }
      }
    }

    /**
     * Cancels loading task, recycles the bitmap.
     */
    public void recycle() {
      if (task != null) {
        task.cancel(false);
        task = null;
      }
      if (bitmap != null) {
        bitmap.recycle();
        bitmap = null;
      }
    }

    /**
     * Calls {@link #recycle()}. {@link #load()} will not work anymore.
     */
    public void close() {
      recycle();
      releaser.release();
    }

    private void onLoaded(Bitmap bitmap) {
      this.bitmap = bitmap;
      this.task = null;
      this.failed = bitmap == null;

      if (bitmap != null && visible && sample == currentSample) {
        invalidateSelf();
      }
    }

    private class LoadingTask extends AsyncTask<Void, Void, Bitmap> {
      @Override
      protected void onPreExecute() {
        releaser.obtain();
      }

      @Override
      protected Bitmap doInBackground(Void... params) {
        if (!isCancelled()) {
          return decoder.decode(rect, sample);
        } else {
          return null;
        }
      }

      @Override
      protected void onPostExecute(Bitmap bitmap) {
        releaser.release();
        onLoaded(bitmap);
      }

      @Override
      protected void onCancelled(Bitmap bitmap) {
        releaser.release();
        // The cleanup task is done in recycle(), just recycle the bitmap
        if (bitmap != null) {
          bitmap.recycle();
        }
      }
    }
  }

  private static int ceilDiv(int a, int b) {
    return (a + b - 1) / b;
  }

  private static int prevPow2(int n) {
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n - (n >> 1);
  }
}
