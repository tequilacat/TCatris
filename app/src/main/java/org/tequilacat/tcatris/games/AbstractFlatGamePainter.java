package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Draws cells on field.
 * Created by avo on 29.02.2016.
 */
public abstract class AbstractFlatGamePainter {

  private GameScreenLayout _gameScreenLayout;

  private final Path _shapeContourPath = new Path();
  private Object _shapeSignature = null;
  private final Paint _shapeContourPaint = new Paint();

  // can be used for quick access from inheritors
  protected final Rect _cachedFieldRect = new Rect();
  protected int _cachedNextFieldCenterX;
  protected int _cachedNextFieldCenterY;

  public AbstractFlatGamePainter(){
    float strokeWidth = VisualResources.Defaults.DYN_SHAPE_STROKE_WIDTH;

    _shapeContourPaint.setStrokeWidth(strokeWidth);
    _shapeContourPaint.setStyle(Paint.Style.STROKE);
    _shapeContourPaint.setPathEffect(new DashPathEffect(new float[]{
        strokeWidth * 7, strokeWidth * 3}, 0));
  }

  public static int getTypeColor(int cellType) {
    return ColorCodes.getDistinctColor(cellType - 1, ColorCodes.Lightness.Normal);
  }

  protected Paint getShapeContourPaint() {
    return _shapeContourPaint;
  }

  protected Path getShapeContourPath() {
    return _shapeContourPath;
  }

  /**
   * stores size and calculates all things dependent on cell size
   * @param gameScreenLayout screen view_scores
   */
  public void init(GameScreenLayout gameScreenLayout, FlatGame game){
    _gameScreenLayout = gameScreenLayout;

    _cachedFieldRect.set(gameScreenLayout.getFieldRect());
   // _cachedCellSize = gameScreenLayout.getCellSize();

    Rect nextShapeRect = gameScreenLayout.getNextShapeRect();

    _cachedNextFieldCenterX = (nextShapeRect.left + nextShapeRect.right) / 2;
    _cachedNextFieldCenterY = (nextShapeRect.top + nextShapeRect.bottom) / 2;
  }

  /**
   * @return background color, defaults to VisualResources.Defaults.FIELD_BG_COLOR
   */
  public int getFieldBgColor() {
    return VisualResources.Defaults.FIELD_BG_COLOR;
  }

  protected GameScreenLayout getGameScreenLayout() {
    return _gameScreenLayout;
  }

  /**
   * draws cell of state on canvas
   * @param c
   * @param cx center of cell
   * @param cy center of cell
   * @param state
   * @param cellState
   */
  public abstract void paintCellPix(Canvas c, int cx, int cy, int state, ABrickGame.CellState cellState);

  /**
   * paints background of field if needed
   * @param c canvas
   * @param game game for which the field is painted
   */
  public abstract void paintField(Canvas c, FlatGame game);

  public enum FieldId { GameField, NextField }

  public abstract int getCenterX(int col, int row, FieldId cellField);
  public abstract int getCenterY(int col, int row, FieldId cellField);
  /*
*/
  /**
   * Draws current shape contour with rotation
   * @param c canvas
   * @param isValid whether specified offset can be complete
   * @param dx from -1 to 1, 0 means no offset
   * @param rotateByDegrees degrees of rotation
   */
  public abstract void drawShapeContour(Canvas c, FlatGame game, boolean isValid, float dx, float rotateByDegrees);

  /**
   * Modifies shapeContourPath to represent given shape
   * @param shape shape for which the contour is generated
   * @param shapeContourPath the receiving path, will be reset and rebuilt
   */
  protected abstract void updateCurrentShapeContour(FlatShape shape, Path shapeContourPath);


  /**
   * creates shape contour if needed (if current shape geometry differs)
   */
  public void updateCurrentShapeContour(FlatShape fallingShape) {

    if (fallingShape == null) {
      //Debug.print("updateCurrentShapeContour: nullify");
      _shapeContourPath.reset();

    } else if(!fallingShape.signatureEquals(_shapeSignature)) {
      //Debug.print("updateCurrentShapeContour: recreate path");
      _shapeSignature = fallingShape.generateSignature();
      _shapeContourPath.reset();
      updateCurrentShapeContour(fallingShape, _shapeContourPath);
    }
  }


  /**
   * @param c canvas
   * @param game game for which shape is drawn
   * @param finalFallingShapeY row of current shape final position center cell
   */
  public void paintFallingShape(Canvas c, FlatGame game, int finalFallingShapeY, DynamicState dynamicState) {
    FlatShape shape = game.getCurrentShape();
    int cols = game.getWidth(), rows = game.getHeight();
    int shapeCenterY = shape.getCenterY();

    final boolean drawShadow = game.isPrefShowDropTarget() && finalFallingShapeY > shapeCenterY;

    for (int i = 0, len = shape.size(); i < len; i++) {
      int x = shape.getX(i), y = shape.getY(i);

      if (x >= 0 && x < cols && y >= 0 && y < rows) {
        paintCellPix(c, getCenterX(x, y, FieldId.GameField), getCenterY(x, y, FieldId.GameField),
            shape.getCellType(i), ABrickGame.CellState.FALLING);
      }

      if(drawShadow) {
        y += (finalFallingShapeY - shape.getCenterY());
        paintCellPix(c, getCenterX(x, y, FieldId.GameField), getCenterY(x, y, FieldId.GameField),
            shape.getCellType(i), ABrickGame.CellState.FALLEN_SHADOW);
      }
    }
  }

  public void paintNext(Canvas g, FlatShape shape) {
    Rect nextRect = getGameScreenLayout().getNextShapeRect();
    Ui.fillRect(g, nextRect, getFieldBgColor());

    // TODO center next figure
    //int cellSize = getGameScreenLayout().getCellSize();
    //Rect bounds = new Rect();
    //shape.getBounds(bounds);

    //int x0 = nextRect.left + (nextRect.width() - bounds.width() * _cachedCellSize) / 2 + (_cachedCellSize >> 1);
    //int y0 = nextRect.top + (nextRect.height() - bounds.height() * _cachedCellSize) / 2 + (_cachedCellSize >> 1);

    for (int i = 0; i < shape.size(); i++) {
      int col = shape.getX(i), row = shape.getY(i);
      paintCellPix(g, getCenterX(col, row, FieldId.NextField),
          getCenterY(col, row, FieldId.NextField),
          shape.getCellType(i), ABrickGame.CellState.FALLING);
    }

  }
}
