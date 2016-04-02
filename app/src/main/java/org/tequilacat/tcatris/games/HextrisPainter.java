package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.GameConstants;
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
  private final Path _scaledHexaPathNormal = new Path();
  private final Path _scaledHexaPathEmpty = new Path();
  private final Path _scaledHexaPathContour = new Path();
  private final Path _scaledCollapsingPath = new Path();
  private final Path _scaledHexaPathSettledCore = new Path();

  private static final float sin60 = (float) Math.sin(Math.PI / 3);
  private static final Path _rawHexaPath;
  private static final Path _rawCollapsingPath = new Path();

  private static final float _emptyRatio = 0.9f;
  private static final float SETTLED_CORE_RATIO = 0.7f;
  private static final float SQUEEZE_RATIO = 0.5f;

  static {
    _rawHexaPath = new Path();
    float h = 1f;
    float halfSide = (float) (h / Math.tan(Math.PI / 3));
    float halfWidth = h / sin60;

    _rawHexaPath.moveTo(-halfSide, -h);
    _rawHexaPath.lineTo(halfSide, -h);
    _rawHexaPath.lineTo(halfWidth, 0);
    _rawHexaPath.lineTo(halfSide, h);
    _rawHexaPath.lineTo(-halfSide, h);
    _rawHexaPath.lineTo(-halfWidth, 0);
    _rawHexaPath.close();

    float collapseFactor = 0.2f;
    _rawCollapsingPath.moveTo(-halfWidth * 0.9f, 0);
    _rawCollapsingPath.lineTo(0, -halfWidth * collapseFactor);
    _rawCollapsingPath.lineTo(halfWidth * 0.9f, 0);
    _rawCollapsingPath.lineTo(0, halfWidth * collapseFactor);
    _rawCollapsingPath.close();
  }

  public HextrisPainter() {
    _hexPaint.setStyle(Paint.Style.STROKE);
    _colorPalette.FIELD_BG_COLOR = ColorCodes.black;
    _colorPalette.DYN_SHAPE_STROKE_VALID = 0xFF4BFF0A;// acid green instead of blueish 0xFFA4E804;
    _colorPalette.DYN_SHAPE_STROKE_INVALID = 0xFFFF2F80;
  }

  private static final int EMPTY_CELL_COLOR = ColorCodes.darkCyan;
  private static final int FALLEN_SHADOW_COLOR = ColorCodes.cyan;

  @Override
  public void init(GameScreenLayout gameScreenLayout, ABrickGame game) {
    super.init(gameScreenLayout, game);
    // compute hex size
    //_hexHalfHeight = gameScreenLayout.getCellSize() / 3f;
    _hexHalfHeight = _cachedFieldRect.height() / (2 * ((FlatGame)game).getHeight() + 1);
    _hexHalfWidth = (_hexHalfHeight / sin60);
    _dx = _hexHalfWidth * 1.5f;

    scale(_rawHexaPath, _scaledHexaPath, _hexHalfHeight);
    scale(_rawHexaPath, _scaledHexaPathNormal, _hexHalfHeight * 0.95f);// little less to have thin margins
    scale(_rawHexaPath, _scaledHexaPathContour, _hexHalfHeight * GameConstants.CONTOUR_FACTOR);
    scale(_rawHexaPath, _scaledHexaPathEmpty, _hexHalfHeight * _emptyRatio);
    scale(_rawHexaPath, _scaledHexaPathSettledCore, _hexHalfHeight * SETTLED_CORE_RATIO);
    //scale(_rawCollapsingPath, _scaledCollapsingPath, _hexHalfHeight);
    scale(_rawHexaPath, _scaledCollapsingPath, _hexHalfHeight * SQUEEZE_RATIO);
  }

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {
    Path path = null;
    int cellColor = getTypeColor(state);

    if (state == ABrickGame.EMPTY_CELL_TYPE) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(EMPTY_CELL_COLOR);
      _hexPaint.setStyle(Paint.Style.STROKE);

    } else if (cellState == ABrickGame.CellState.FALLING) {
      path = _scaledHexaPathNormal;
      _hexPaint.setColor(cellColor);
      _hexPaint.setStyle(Paint.Style.FILL);

    } else if (cellState == ABrickGame.CellState.FALLEN_SHADOW) {
      path = _scaledHexaPathEmpty;
      _hexPaint.setColor(FALLEN_SHADOW_COLOR);
      _hexPaint.setStyle(Paint.Style.STROKE);

    } else if (cellState == ABrickGame.CellState.SETTLED) {
      path = _scaledHexaPathNormal;
      _hexPaint.setColor(cellColor);
      _hexPaint.setStyle(Paint.Style.FILL);

    } else { // SQUEEZED
      path = _scaledCollapsingPath;
      _hexPaint.setStyle(Paint.Style.FILL);
      _hexPaint.setColor(cellColor);
      //_hexPaint.setColor(ColorCodes.white); // TODO improve display of hex cells squeezed
    }

    if (path != null) {
      c.save();
      c.translate(x, y);
      c.drawPath(path, _hexPaint);

      if (cellState == ABrickGame.CellState.SETTLED && state != ABrickGame.EMPTY_CELL_TYPE) {
        // draw another inside, with contrast color
        _hexPaint.setColor(getTypeColor(state, ColorCodes.Lightness.Contrast));
        _hexPaint.setStyle(Paint.Style.FILL);
        c.drawPath(_scaledHexaPathSettledCore, _hexPaint);
      }

      c.restore();
    }
  }


  public int getCenterX(int col, int row, FieldId cellField) {
    final int x;

    if (cellField == FieldId.NextField) {
      x = (int) (_cachedNextFieldCenterX + _dx * col);
    } else {
      x = (int) (_cachedFieldRect.left + _hexHalfWidth + _dx * col);
    }

    return x;
  }

  public int getCenterY(int col, int row, FieldId cellField) {
    int y = (int) ((_hexHalfHeight + _hexHalfHeight) * row + _hexHalfHeight);

    if ((col & 1) == 1) {
      y += (int) _hexHalfHeight;
    }

    if (cellField == FieldId.NextField) {
      y += _cachedNextFieldCenterY;
    } else {
      y += _cachedFieldRect.top;
    }

    return y;
  }

  @Override
  public void paintField(Canvas c, ABrickGame brickGame) {
    FlatGame game = (FlatGame) brickGame;
    int gameRows = game.getHeight(), gameCols = game.getWidth();
    final Rect fieldRect = _cachedFieldRect;

    int fieldBgColor = _colorPalette.FIELD_BG_COLOR;
    Ui.fillRect(c, fieldRect, VisualResources.Defaults.SCREEN_BG_COLOR);

    _hexPaint.setStyle(Paint.Style.FILL);
    _hexPaint.setColor(fieldBgColor);
    c.drawRect(fieldRect.left + _hexHalfWidth, fieldRect.top,
        fieldRect.right - _hexHalfWidth, fieldRect.bottom - _hexHalfHeight,
        _hexPaint);

    int cx = getCenterX(0, 0, FieldId.GameField);
    int dx = getCenterX(gameCols - 1, 0, FieldId.GameField) - cx;

    for (int y = 0; y < gameRows; y++) {
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
    shapeContourPath.reset();

    // get center for [0,0] cell
    int x0 = getCenterX(0, 0, FieldId.NextField);
    int y0 = getCenterY(0, 0, FieldId.NextField);

    // add hexagons relative to center
    for (int i = 0; i < HextrisShape.CELLCOUNT; i++) {
      int col = shape.getX(i, 0, 0), row = shape.getY(i, 0, 0);
      int x = getCenterX(col, row, FieldId.NextField) - x0;
      int y = getCenterY(col, row, FieldId.NextField) - y0;
      shapeContourPath.addPath(_scaledHexaPathContour, x, y);
    }
  }

  @Override
  public void drawShapeContour(Canvas c, FlatGame game, boolean isValid, float dxFactor, float rotateFactor) {
    drawTransformedShapeContour(c, game, isValid, dxFactor * _dx, rotateFactor * 30);
  }

}
