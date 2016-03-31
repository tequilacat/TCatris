package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Paints hexagonal hextris
 */
public class HextrisPainter extends AbstractFlatGamePainter {

  private Paint _hexPaint = new Paint();
  private float _hexHalfHeight;
  private float _hexHalfWidth;
  private float _dx;

  private final Path _scaledHexaPath = new Path();
  private final Path _scaledHexaPathEmpty = new Path();

  private static final float sin60 = (float) Math.sin(Math.PI / 3);
  private static final Path _staticHexaPath;

  private static final float _emptyRatio = 0.9f;

  static {
    _staticHexaPath = new Path();
    float h = 1f;
    float halfSide = (float) (h / Math.tan(Math.PI / 3));
    float halfWidth = h / sin60;

    _staticHexaPath.moveTo(-halfSide, -h);
    _staticHexaPath.lineTo(halfSide, -h);
    _staticHexaPath.lineTo(halfWidth, 0);
    _staticHexaPath.lineTo(halfSide, h);
    _staticHexaPath.lineTo(-halfSide, h);
    _staticHexaPath.lineTo(-halfWidth, 0);
    _staticHexaPath.close();
  }

  public HextrisPainter() {
    _hexPaint.setStyle(Paint.Style.STROKE);
  }

  @Override
  public void init(GameScreenLayout gameScreenLayout, FlatGame game) {
    super.init(gameScreenLayout, game);
    // compute hex size
    //_hexHalfHeight = gameScreenLayout.getCellSize() / 3f;
    _hexHalfHeight = _cachedFieldRect.height() / (2 * game.getHeight() + 1);
    _hexHalfWidth = (_hexHalfHeight / sin60);
    _dx = _hexHalfWidth * 1.5f;

    _scaledHexaPath.reset();
    Matrix mtx = new Matrix();
    mtx.preScale(_hexHalfHeight, _hexHalfHeight);
    _staticHexaPath.transform(mtx, _scaledHexaPath);

    mtx = new Matrix();
    mtx.preScale(_hexHalfHeight * _emptyRatio, _hexHalfHeight * _emptyRatio);
    _staticHexaPath.transform(mtx, _scaledHexaPathEmpty);
  }

  @Override
  public int getFieldBgColor() {
    return ColorCodes.black;
  }

  private static final int EMPTY_CELL_COLOR = ColorCodes.darkCyan;
  private static final int FALLEN_SHADOW_COLOR = ColorCodes.cyan;

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {
    Path path = null;

    //int color = (state == FlatGame.EMPTY) ? ColorCodes.cyan : ColorCodes.

    if(state == FlatGame.EMPTY) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(EMPTY_CELL_COLOR);
      _hexPaint.setStyle(Paint.Style.STROKE);

    }else if(cellState == ABrickGame.CellState.FALLING) {
      path = _scaledHexaPath;
      _hexPaint.setColor(getTypeColor(state));
      _hexPaint.setStyle(Paint.Style.FILL);

    }else if(cellState == ABrickGame.CellState.FALLEN_SHADOW) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(FALLEN_SHADOW_COLOR);
      _hexPaint.setStyle(Paint.Style.STROKE);

    }else if(cellState == ABrickGame.CellState.SETTLED) {
      path = _scaledHexaPath;
      _hexPaint.setColor(ColorCodes.getDistinctColor(state - 1, ColorCodes.Lightness.Contrast));
      _hexPaint.setStyle(Paint.Style.FILL);
    }

    if(path != null) {
      c.save();
      c.translate(x, y);
      c.drawPath(path, _hexPaint);

      c.restore();
    }
  }


  public int getCenterX(int col, int row, FieldId cellField) {
    final int x;

    if(cellField == FieldId.NextField) {
      x = (int)(_cachedNextFieldCenterX + _dx * col);
    }else {
      x = (int) (_cachedFieldRect.left + _hexHalfWidth + _dx * col);
    }

    return x;
  }

  public int getCenterY(int col, int row, FieldId cellField) {
    int y = (int) ((_hexHalfHeight + _hexHalfHeight) * row + _hexHalfHeight);

    if ((col & 1) == 1) {
      y += (int) _hexHalfHeight;
    }

    if(cellField == FieldId.NextField) {
      y += _cachedNextFieldCenterY;
    }else {
      y += _cachedFieldRect.top;
    }

    return y;
  }

  @Override
  public void paintField(Canvas c, FlatGame game) {
    int gameRows = game.getHeight(), gameCols = game.getWidth();
    final Rect fieldRect = _cachedFieldRect;

    int fieldBgColor = getFieldBgColor();
    Ui.fillRect(c, fieldRect, VisualResources.Defaults.SCREEN_BG_COLOR);

    _hexPaint.setStyle(Paint.Style.FILL);
    _hexPaint.setColor(fieldBgColor);
    c.drawRect(fieldRect.left + _hexHalfWidth, fieldRect.top,
        fieldRect.right - _hexHalfWidth, fieldRect.bottom - _hexHalfHeight,
        _hexPaint);

    int cx = getCenterX(0, 0, FieldId.GameField);
    int dx = getCenterX(gameCols - 1, 0, FieldId.GameField) - cx;

    for(int y = 0; y < gameRows; y++) {
      int cy = getCenterY(0, y, FieldId.GameField);
      c.save();
      c.translate(cx, cy);
      c.drawPath(_scaledHexaPath, _hexPaint);
      c.translate(dx, 0);
      c.drawPath(_scaledHexaPath, _hexPaint);
      c.restore();
    }

    int cy = getCenterY(1, gameRows - 1, FieldId.GameField);

    for (int x = 1; x < gameCols; x += 2) {
      cx = getCenterX(x, gameRows - 1, FieldId.GameField);
      c.save();
      c.translate(cx, cy);
      c.drawPath(_scaledHexaPath, _hexPaint);
      c.restore();
    }

    //Ui.fillRect(c, fieldRect, fieldBgColor);
//
//    _hexPaint.setStyle(Paint.Style.STROKE);
//    _hexPaint.setColor(ColorCodes.red);
//
//    for(int row = 1; row < game.getHeight(); row++) {
//      int y = fieldRect.top + row * _cachedCellSize;
//      c.drawLine(fieldRect.left, y, fieldRect.right, y, _hexPaint);
//    }

    //_hexPaint.setColor(ColorCodes.cyan);

    // TODO consider ints instead of floats in drawing hex field

    float rowHeight = _hexHalfHeight * 2;
    float centerY = fieldRect.top + _hexHalfHeight;

    for (int row = 0; row < gameRows; row++) {
      float centerX = fieldRect.left + _hexHalfWidth;
      boolean isEven = true;

      for (int col = 0; col < gameCols; col++) {
        paintCellPix(c, (int) centerX, (int) (isEven ? centerY : (centerY + _hexHalfHeight)),
            game.getCellValue(col, row),
            game.isSqueezable(col, row) ? ABrickGame.CellState.SQUEEZED : ABrickGame.CellState.SETTLED);

        isEven = !isEven;
        centerX += _dx;
      }

      centerY += rowHeight;
    }


  }

  @Override
  protected void updateCurrentShapeContour(FlatShape shape, Path shapeContourPath) {
    // TODO update hex falling shape contour
  }

  @Override
  public void drawShapeContour(Canvas c, FlatGame game, boolean isValid, float dx, float rotateByDegrees) {
    // TODO draw shape contour when it's computed
  }

}
