package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;

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
  public void init(GameScreenLayout gameScreenLayout) {
    super.init(gameScreenLayout);
    // compute hex size
    _hexHalfHeight = gameScreenLayout.getCellSize() / 3f;
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

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {
    Path path = null;

    //int color = (state == FlatGame.EMPTY) ? ColorCodes.cyan : ColorCodes.

    if(state == FlatGame.EMPTY) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(ColorCodes.cyan); // TODO define specific empty color
      _hexPaint.setStyle(Paint.Style.STROKE);

    }else if(cellState == ABrickGame.CellState.FALLING) {
      path = _scaledHexaPath;
      _hexPaint.setColor(getTypeColor(state));
      _hexPaint.setStyle(Paint.Style.FILL);

    }else if(cellState == ABrickGame.CellState.FALLEN_SHADOW) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(ColorCodes.getDistinctColor(state - 1, ColorCodes.Lightness.Contrast));
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

  // TODO override getCenterX, getCenterY

  protected int getCenterX(int col, int row, FieldId cellField) {
    final int x;

    if(cellField == FieldId.NextField) {
      x = _cachedNextFieldCenterX + _cachedCellSize * col; // TODO fix next X
    }else {
      x = (int) (_cachedFieldRect.left + _hexHalfWidth + _dx * col);
    }

    return x;
  }

  protected int getCenterY(int col, int row, FieldId cellField) {
    int y;

    if(cellField == FieldId.NextField) {
      y = _cachedNextFieldCenterY + _cachedCellSize * row; // TODO fix next Y
    }else {
      y = (int) (_cachedFieldRect.top + (_hexHalfHeight + _hexHalfHeight) * row + _hexHalfHeight);
      if ((col & 1) == 1) {
        y += (int) _hexHalfHeight;
      }
    }

    return y;
  }

  @Override
  public void paintField(Canvas c, FlatGame game) {
    final Rect fieldRect = _cachedFieldRect;
    // fill with bg and draw circles all over it
    Ui.fillRect(c, fieldRect, getFieldBgColor());


    _hexPaint.setStyle(Paint.Style.STROKE);
    _hexPaint.setColor(ColorCodes.red);

    for(int row = 1; row < game.getHeight(); row++) {
      int y = fieldRect.top + row * _cachedCellSize;
      c.drawLine(fieldRect.left, y, fieldRect.right, y, _hexPaint);
    }

    _hexPaint.setColor(ColorCodes.cyan);

    // TODO consider ints instead of floats in drawing hex field

    float rowHeight = _hexHalfHeight * 2;
    //float dx = _hexHalfWidth * 1.5f; // width + width*cos(60)
    float centerY = fieldRect.top + _hexHalfHeight;

    int gameRows = game.getHeight(), gameCols = game.getWidth();

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
}
