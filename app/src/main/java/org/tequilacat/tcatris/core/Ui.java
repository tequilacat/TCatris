package org.tequilacat.tcatris.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.EnumMap;
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

  /**
   * Draws Vertically centered text
   * @param g
   * @param text
   * @param x
   * @param y
   * @param fontSize
   * @param textColor
   */
  public static void drawText(Canvas g, String text, int x, int y, int fontSize, int textColor) {
    _textPainter.setTextSize(fontSize);
    _textPainter.setColor(textColor);
    Paint.FontMetrics fm = _textPainter.getFontMetrics();
    float textH = fm.descent - fm.ascent;
    g.drawText(text, x, y - textH / 2 - fm.ascent, _textPainter);
    //g.drawText(text, x, y - fm.ascent, _textPainter);
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

  private static EnumMap<ABrickGame.ImpulseSemantics, Path> _gameImpulseGlyph
      = new EnumMap<>(ABrickGame.ImpulseSemantics.class);

  private static List<Path> GLYPH_PATHS;
  private static final Paint _buttonGlyphFillPainter = new Paint();
  private static final Paint _buttonGlyphStrokePainter = new Paint();
  private static final Paint _scoreTextPainter;

  /**
   * rotatas path around center which is considered 0.5, 0.5
   * @param srcPath
   * @param degree
   * @return
   */
  private static Path rotated(Path srcPath, int degree) {
    Matrix mtx = new Matrix();
    //mtx.postTranslate(-0.5f, -0.5f);
    mtx.postRotate(degree);
    //mtx.postTranslate(0.5f, 0.5f);
    Path path = new Path();
    srcPath.transform(mtx, path);
    return path;
  }

  private static Path mirror(Path srcPath) {
    Matrix mtx = new Matrix();
    //mtx.postTranslate(-0.5f, -0.5f);
    mtx.preScale(-1, 1);
    //mtx.postTranslate(1, 0);
    //mtx.postRotate(degree);
    //mtx.postTranslate(0.5f, 0.5f);
    Path path = new Path();
    srcPath.transform(mtx, path);
    return path;
  }

  static {
    _scoreTextPainter = new Paint();
    _scoreTextPainter.setStyle(Paint.Style.STROKE);

    _buttonGlyphStrokePainter.setStyle(Paint.Style.STROKE);
    _buttonGlyphStrokePainter.setStrokeWidth(0.02f);
    _buttonGlyphStrokePainter.setColor(VisualResources.Defaults.GLYPH_STROKE_COLOR);
    _buttonGlyphStrokePainter.setStrokeJoin(Paint.Join.ROUND);

    _buttonGlyphFillPainter.setStyle(Paint.Style.FILL);
    _buttonGlyphFillPainter.setColor(VisualResources.Defaults.GLYPH_FILL_COLOR);

    // create rotate arrow
    RectF oval = new RectF(0.3f - 0.5f, 0.3f - 0.5f, 0.7f - 0.5f, 0.7f - 0.5f);
    Path ccwArrow = new Path();

    ccwArrow.moveTo(0.5f - 0.5f, 0f - 0.5f);
    ccwArrow.lineTo(0.3f - 0.5f, 0.2f - 0.5f);
    ccwArrow.lineTo(0.5f - 0.5f, 0.4f - 0.5f);
    ccwArrow.lineTo(0.5f - 0.5f, 0.3f - 0.5f);

    ccwArrow.arcTo(oval, -90, 270);
    ccwArrow.lineTo(0.1f - 0.5f, 0.5f - 0.5f);

    oval.set(0.1f - 0.5f, 0.1f - 0.5f, 0.9f - 0.5f, 0.9f - 0.5f);
    ccwArrow.arcTo(oval, 180, -270);

    ccwArrow.lineTo(0.5f - 0.5f, 0f - 0.5f);

    // create straight arrow
    // create path
    Path arrowRightPath = new Path();
    arrowRightPath.moveTo(0.2f - 0.5f, 0.4f - 0.5f);
    arrowRightPath.lineTo(0.4f - 0.5f, 0.4f - 0.5f);
    arrowRightPath.lineTo(0.4f - 0.5f, 0.2f - 0.5f);
    arrowRightPath.lineTo(0.8f - 0.5f, 0.5f - 0.5f);
    arrowRightPath.lineTo(0.4f - 0.5f, 0.8f - 0.5f);
    arrowRightPath.lineTo(0.4f - 0.5f, 0.6f - 0.5f);
    arrowRightPath.lineTo(0.2f - 0.5f, 0.6f - 0.5f);
    arrowRightPath.close();

    
    Path shiftRightPath = new Path();
    shiftRightPath.moveTo(0.4f - 0.5f, 0.3f - 0.5f);
    shiftRightPath.lineTo(0.5f - 0.5f, 0.3f - 0.5f);
    shiftRightPath.lineTo(0.5f - 0.5f, 0f - 0.5f);
    shiftRightPath.lineTo(1f - 0.5f, 0.5f - 0.5f);
    shiftRightPath.lineTo(0.5f - 0.5f, 1f - 0.5f);
    shiftRightPath.lineTo(0.5f - 0.5f, 0.7f - 0.5f);
    shiftRightPath.lineTo(0.4f - 0.5f, 0.7f - 0.5f);
    shiftRightPath.close();

    shiftRightPath.addRect(-0.5f, 0.3f - 0.5f, 0.15f - 0.5f, 0.7f - 0.5f, Path.Direction.CW);
    shiftRightPath.addRect(0.2f - 0.5f, 0.3f - 0.5f, 0.35f - 0.5f, 0.7f - 0.5f, Path.Direction.CW);

    // create buttons
    GLYPH_PATHS = new ArrayList<>();

    GLYPH_PATHS.add(rotated(arrowRightPath, 180)); // left

    GLYPH_PATHS.add(arrowRightPath); // right
    //GLYPH_PATHS.add(shiftRightPath); // right

//    GLYPH_PATHS.add(rotated(arrowRightPath, 45)); // CW
//    GLYPH_PATHS.add(rotated(arrowRightPath, 135)); // CCW
    GLYPH_PATHS.add(mirror(ccwArrow)); // CW
    GLYPH_PATHS.add(ccwArrow); // CCW

    GLYPH_PATHS.add(rotated(arrowRightPath, 90)); // DROP


    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.MOVE_LEFT, rotated(arrowRightPath, 180));
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.MOVE_RIGHT, arrowRightPath);
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.MOVE_UP, rotated(arrowRightPath, -90));
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.MOVE_DOWN, rotated(arrowRightPath, 90));

    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.ROTATE_CCW, ccwArrow);
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.ROTATE_CW, mirror(ccwArrow));

    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.SHIFT_RIGHT, shiftRightPath);
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.SHIFT_LEFT, mirror(shiftRightPath));
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.SHIFT_UP, rotated(shiftRightPath, -90));
    _gameImpulseGlyph.put(ABrickGame.ImpulseSemantics.SHIFT_DOWN, rotated(shiftRightPath, 90));

    //ccwArrow.rMoveTo(0.3f, -0.2f);
  }
/*
  public static void drawGlyph(Canvas c, Rect bounds, ButtonGlyph glyph){
    drawGlyph(c, bounds.left, bounds.top, bounds.width(), bounds.height(), glyph);
  }

  public static void drawGlyph(Canvas c, int x, int y, int w, int h, ButtonGlyph glyph){*/
  public static void drawGlyph(Canvas c, int x, int y, int w, int h, ABrickGame.ImpulseSemantics semantics) {
    if (semantics != null) {
      Path path = _gameImpulseGlyph.get(semantics);

      // test case: fill rect with green
    /*final int index = glyph.ordinal();

    if (index < GLYPH_PATHS.size()) {
      Path path = GLYPH_PATHS.get(index);*/
      if (path != null) {
        c.save();
        c.translate(x + (w >> 1), y + (h >> 1));
        c.scale(w, h);

        c.drawPath(path, _buttonGlyphFillPainter);
        c.drawPath(path, _buttonGlyphStrokePainter);
        c.restore();
      }
    }
  }

  public static float getTextWidth(int textSize, String text) {
    _textPainter.setTextSize(textSize);
    return _textPainter.measureText(text);
  }

  public static void paintScores(Canvas c, int currentScore, int oldScore, int left, int top, int width, int height, float margin) {

    Ui.fillRect(c, left, top, width, height, VisualResources.Defaults.SCORE_BG_COLOR);
    // compute
    int textHeight = VisualResources.Defaults.HEADER_FONT_SIZE;
    _scoreTextPainter.setTextSize(textHeight);
    Paint.FontMetrics fm = _scoreTextPainter.getFontMetrics();

    //float margin = height/8f;
    int maxBarWidth = (int)(width - margin * 3);
    int barHeight = (int)(height - margin * 3);

    int curScoreColor;
    int curScoreCenterY;
    int curScoreTextLeft;
    String strCurScore = Integer.toString(currentScore);

    if(oldScore == 0) {
      // just paint current score as text
      curScoreColor = VisualResources.Defaults.SCORE_CUR_TEXTCOLOR_ALONE;
      curScoreCenterY = top + height / 2;
      curScoreTextLeft = left + (int)margin;

    }else {
      int oldScoreLeft = (int) (left + margin * 2);
      int oldScoreTop = (int) (top + margin);
      int curScoreLeft = (int) (left + margin);
      int curScoreTop = (int) (top + margin * 2);

      int curScoreBarWidth, oldScoreBarWidth;
      boolean displayOld = true;

      if (currentScore == 0) {
        // draw old score bar, old score as top score at right, cur score at left
        //oldScoreBarWidth = maxBarWidth;

        barHeight = height - (int) (2 * margin);
        oldScoreBarWidth = width - (int) (2 * margin);
        oldScoreLeft = left + (int) margin;

        curScoreBarWidth = 0;
        curScoreColor = VisualResources.Defaults.SCORE_CUR_TEXTCOLOR;
        curScoreCenterY = oldScoreTop + barHeight / 2;// center old score
        curScoreTextLeft = oldScoreLeft + (int) margin;

      } else if (oldScore <= currentScore) {
        // cur score same or bigger - don't draw old score
        // draw cur score leftmost
        curScoreColor = VisualResources.Defaults.SCORE_CUR_TEXTCOLOR;
        displayOld = false;
        curScoreBarWidth = maxBarWidth;
        oldScoreBarWidth = oldScore == currentScore ? maxBarWidth : (maxBarWidth * oldScore / currentScore);

        curScoreCenterY = curScoreTop + barHeight / 2;// center old score
        curScoreTextLeft = curScoreLeft + (int) margin; // draw over bar, have margin

      } else { // old score bigger, display it, display current
        curScoreColor = VisualResources.Defaults.SCORE_CUR_TEXTCOLOR;
        oldScoreBarWidth = maxBarWidth;
        curScoreBarWidth = maxBarWidth * currentScore / oldScore;

        // check if width fits
        float w = _scoreTextPainter.measureText(strCurScore);

        if (w + margin * 2 <= curScoreBarWidth) {
          // fits cur score bar, draw inside
          curScoreTextLeft = curScoreLeft + (int) margin;
          curScoreCenterY = curScoreTop + barHeight / 2;
        } else {
          // draw in topscore bar
          curScoreTextLeft = curScoreLeft + curScoreBarWidth + (int) margin;
          curScoreCenterY = oldScoreTop + barHeight / 2;
        }
      }

      if (oldScoreBarWidth > 0) {
        Drawable d = VisualResources.Defaults.SCOREBAR_TOPSCORE_DRAWABLE;
        d.setBounds(oldScoreLeft, oldScoreTop, oldScoreLeft + oldScoreBarWidth, oldScoreTop + barHeight);
        d.draw(c);

        // draw text
        if (displayOld) {
          // aligned to right
          String strScore = Integer.toString(oldScore);
          float w = _scoreTextPainter.measureText(strScore);
          _scoreTextPainter.setColor(VisualResources.Defaults.SCORE_TOP_TEXTCOLOR);
          c.drawText(strScore, oldScoreLeft + oldScoreBarWidth - w - margin,
              oldScoreTop + (barHeight - textHeight) / 2 - fm.ascent,
              _scoreTextPainter);
        }
      }

      if (curScoreBarWidth > 0) {
        // have
        if (curScoreBarWidth < VisualResources.Defaults.SCOREBAR_MIN_CURRENT_WIDTH) {
          Ui.fillRect(c, curScoreLeft, curScoreTop, curScoreBarWidth, barHeight,
              VisualResources.Defaults.SCOREBAR_CUR_COLOR);
        } else {
          VisualResources.Defaults.SCOREBAR_CURRENT_DRAWABLE.setBounds(
              curScoreLeft, curScoreTop, curScoreLeft + curScoreBarWidth, curScoreTop + barHeight);
          VisualResources.Defaults.SCOREBAR_CURRENT_DRAWABLE.draw(c);
        }
      }
    }

    _scoreTextPainter.setColor(curScoreColor);
    c.drawText(strCurScore, curScoreTextLeft, curScoreCenterY - textHeight / 2 - fm.ascent,
        _scoreTextPainter);
  }

  private static final RectF _rbOval = new RectF();

  private static void fillCircle(Canvas c, float cx, float cy, float radius, int color) {
    _fillPainter.setColor(color);
    _rbOval.set(cx-radius, cy-radius, cx+ radius, cy+ radius);
    c.drawOval(_rbOval, _fillPainter);
  }

  static float dx = -0.08f, radRatio = 0.8f;
  static float dx2 = 0.1f, radRatio2 = 0.9f;

  public static void drawRoundButton(Canvas c, float cx, float cy, float radius, int mainColor, int flashColor) {
    fillCircle(c, cx, cy, radius, mainColor);
    fillCircle(c, cx + radius * dx, cy + radius * dx, radius * radRatio, flashColor);
    fillCircle(c, cx + radius * dx2, cy + radius * dx2, radius * radRatio2, mainColor);
  }

  public static void drawRoundedButton(Canvas c, float left, float top, float width, float height,
                                       int mainColor, int flashColor) {
    float r = Math.min(width, height) / 4;
    _rbOval.set(left, top, left + width, top + height);
    _fillPainter.setColor(mainColor);
    c.drawRoundRect(_rbOval, r, r, _fillPainter);

    // modify to draw glimpse
/*
    _fillPainter.setColor(flashColor);
    float len = width / 3;
    float flashR = r/5;
    _rbOval.set(left + r, top + r, left + len, top + flashR * 2);
    c.drawRoundRect(_rbOval, flashR, flashR, _fillPainter);
*/
    // horz glimpse
    top += height * 0.05;
    left += r;
    width -= (r + r);

    height = (int) (height / 3.5);
    r = height / 2;

    _rbOval.set(left, top, left + width, top + height);
    _fillPainter.setColor(flashColor);
    c.drawRoundRect(_rbOval, r, r, _fillPainter);
  }

  private static final RectF _roundedAreaRect = new RectF();

  /**
   * Expands all dimensions by delta.
   * If delta is positive the rect becomes bigger otherwise it becomes smaller.
   * No sanity checks are made on resulting bounds
   * @param rect the rect to modify
   * @param delta the delta applied to each bound
   */
  public static void expand(Rect rect, int delta) {
    rect.left -= delta;
    rect.right += delta;
    rect.top -= delta;
    rect.bottom += delta;
  }

  enum FramePosition {EXACT, AROUND, INSIDE}

  public static void drawRoundedArea(Canvas c, Rect rect, FramePosition framePosition) {
    int left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom;

    if (framePosition == FramePosition.AROUND) {
      final int framePadding = (int) VisualResources.Defaults.ROUNDED_FRAME_PADDING;
      left -= framePadding;
      top -= framePadding;
      right += framePadding;
      bottom += framePadding;

    } else if (framePosition == FramePosition.INSIDE) {
      final int frameMargin = (int) VisualResources.Defaults.ROUNDED_FRAME_MARGIN;
      left += frameMargin;
      top += frameMargin;
      right -= frameMargin;
      bottom -= frameMargin;
    }

    float radius = VisualResources.Defaults.ROUNDED_FRAME_RADIUS;
    float shadowDelta = VisualResources.Defaults.ROUNDED_FRAME_STROKE_WIDTH;
    _framePainter.setStrokeWidth(shadowDelta);


    _framePainter.setColor(VisualResources.Defaults.LIGHTSHADOW_COLOR);
    _roundedAreaRect.set(left, top, right, bottom);
    c.drawRoundRect(_roundedAreaRect, radius, radius, _framePainter);

    left -= shadowDelta;
    top -= shadowDelta;
    right -= shadowDelta;
    bottom -= shadowDelta;

    _framePainter.setColor(VisualResources.Defaults.DARKSHADOW_COLOR);
    _roundedAreaRect.set(left, top, right, bottom);
    c.drawRoundRect(_roundedAreaRect, radius, radius, _framePainter);
  }

}

