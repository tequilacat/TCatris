package org.tequilacat.tcatris.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**************************************************
 **************************************************/
public class Ui {

  private static final Paint _textPainter;
  private static final Paint _framePainter;
  private static final Paint _fillPainter;

  static {
    _textPainter = new Paint();
    _textPainter.setAntiAlias(true);
    _textPainter.setTextAlign(Paint.Align.LEFT);

    _framePainter = new Paint();
    _framePainter.setStyle(Paint.Style.STROKE);

    _fillPainter = new Paint();
    _fillPainter.setStyle(Paint.Style.FILL);
  }

  public static void drawText(Canvas g, String text, int x, int y, int fontSize, int textColor) {
    _textPainter.setTextSize(fontSize);
    _textPainter.setColor(textColor);
    Paint.FontMetrics fm = _textPainter.getFontMetrics();
    g.drawText(text, x, y - fm.ascent, _textPainter);
  }

  public static void drawRect(Canvas c, int x, int y, int w, int h, int fillColor) {
    _framePainter.setColor(fillColor);
    c.drawRect(x, y, x + w, y + h, _framePainter);
  }

  public static void fillRect(Canvas c, int x, int y, int w, int h, int fillColor) {
    _fillPainter.setColor(fillColor);
    c.drawRect(x, y, x + w, y + h, _fillPainter);
  }

  public static void fillRect(Canvas c, Rect fieldRect, int fillColor) {
    fillRect(c, fieldRect.left, fieldRect.top, fieldRect.width(), fieldRect.height(), fillColor);
  }

  public static void draw3dRect(Canvas g, Rect rect) {
    draw3dRect(g, rect.left, rect.top, rect.width(), rect.height());
  }

  public static void draw3dRect(Canvas g, int x, int y, int w, int h) {
    x += 2;
    w -= 4;
    y += 2;
    h -= 4;

    _framePainter.setColor(VisualResources.Defaults.DARKSHADOW_COLOR);
    g.drawLine(x - 1, y - 1, x - 1, y + h, _framePainter);
    g.drawLine(x - 1, y - 1, x + w, y - 1, _framePainter);

    _framePainter.setColor(VisualResources.Defaults.LIGHTSHADOW_COLOR);
    x += w;
    y += h;
    g.drawLine(x, y, x - w, y, _framePainter);
    g.drawLine(x, y, x, y - h, _framePainter);
  }


  public enum ButtonGlyph {
    LEFT, RIGHT, RCW, RCCW, DROP,
  }

  private static List<Path> GLYPH_PATHS;
  private static Paint _buttonGlyphFillPainter = new Paint();
  private static Paint _buttonGlyphStrokePainter = new Paint();

  /**
   * rotatas path around center which is considered 0.5, 0.5
   * @param srcPath
   * @param degree
   * @return
   */
  private static Path rotated(Path srcPath, int degree) {
    Matrix mtx = new Matrix();
    mtx.postTranslate(-0.5f, -0.5f);
    mtx.postRotate(degree);
    mtx.postTranslate(0.5f, 0.5f);
    Path path = new Path();
    srcPath.transform(mtx, path);
    return path;
  }

  static {
    _buttonGlyphStrokePainter.setStyle(Paint.Style.STROKE);
    _buttonGlyphStrokePainter.setStrokeWidth(0.02f);
    _buttonGlyphStrokePainter.setColor(VisualResources.Defaults.GLYPH_STROKE_COLOR);
    _buttonGlyphStrokePainter.setStrokeJoin(Paint.Join.ROUND);

    _buttonGlyphFillPainter.setStyle(Paint.Style.FILL);
    _buttonGlyphFillPainter.setColor(VisualResources.Defaults.GLYPH_FILL_COLOR);

    // create buttons
    GLYPH_PATHS = new ArrayList<>();

    float side = 1;
    // create path
    Path arrowRightPath = new Path();
    arrowRightPath.moveTo(side * 0.2f, side * 0.4f);
    arrowRightPath.lineTo(side * 0.4f, side * 0.4f);
    arrowRightPath.lineTo(side * 0.4f, side * 0.2f);
    arrowRightPath.lineTo(side * 0.8f, side * 0.5f);
    arrowRightPath.lineTo(side * 0.4f, side * 0.8f);
    arrowRightPath.lineTo(side * 0.4f, side * 0.6f);
    arrowRightPath.lineTo(side * 0.2f, side * 0.6f);
    arrowRightPath.close();

    GLYPH_PATHS.add(rotated(arrowRightPath, 180)); // left
    GLYPH_PATHS.add(arrowRightPath); // right
    GLYPH_PATHS.add(rotated(arrowRightPath, 45)); // CW
    GLYPH_PATHS.add(rotated(arrowRightPath, 135)); // CCW
    GLYPH_PATHS.add(rotated(arrowRightPath, 90)); // DROP
  }

  public static void drawGlyph(Canvas c, Rect bounds, ButtonGlyph glyph){
    drawGlyph(c, bounds.left, bounds.top, bounds.width(), bounds.height(), glyph);
  }

  public static void drawGlyph(Canvas c, int x, int y, int w, int h, ButtonGlyph glyph){
    // test case: fill rect with green
    final int index = glyph.ordinal();

    if(index < GLYPH_PATHS.size()) {
      Path path = GLYPH_PATHS.get(index);
      c.save();
      c.translate(x, y);
      c.scale(w, h);

      c.drawPath(path, _buttonGlyphFillPainter);
      c.drawPath(path, _buttonGlyphStrokePainter);
      c.restore();
    }
  }
}

