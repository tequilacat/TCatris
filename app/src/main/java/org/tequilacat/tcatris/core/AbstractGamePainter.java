package org.tequilacat.tcatris.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.games.FlatGame;

/**
 * Base class to display game state on screen
 */
public abstract class AbstractGamePainter {

  public class ColorPalette {
    public int DYN_SHAPE_STROKE_VALID = VisualResources.Defaults.DYN_SHAPE_STROKE_VALID;
    public int DYN_SHAPE_STROKE_INVALID = VisualResources.Defaults.DYN_SHAPE_STROKE_INVALID;
    public int FIELD_BG_COLOR = VisualResources.Defaults.FIELD_BG_COLOR;

    public int EMPTY_CELL_FRAME_COLOR = VisualResources.Defaults.FIELD_LINE_COLOR;
  }

  protected ColorPalette _colorPalette = new ColorPalette();

  private GameScreenLayout _gameScreenLayout;

  // can be used for quick access from inheritors
  protected final Rect _cachedFieldRect = new Rect();
  protected int _cachedNextFieldCenterX;
  protected int _cachedNextFieldCenterY;

  public enum FieldId { GameField, NextField }

  private static final Matrix _scaleMatrix = new Matrix();

  public static int getTypeColor(int cellType) {
    return getTypeColor(cellType, ColorCodes.Lightness.Normal);
  }

  public static int getTypeColor(int cellType, ColorCodes.Lightness lightness) {
    return ColorCodes.getDistinctColor(cellType - 1, lightness);
  }

  public static void scale(Path source, Path target, float ratio) {
    _scaleMatrix.reset();
    _scaleMatrix.preScale(ratio, ratio);
    source.transform(_scaleMatrix, target);
  }

  /**
   * stores size and calculates all things dependent on cell size
   * @param gameScreenLayout screen view_scores
   */
  public void init(GameScreenLayout gameScreenLayout, ABrickGame game){
    _gameScreenLayout = gameScreenLayout;
    _cachedFieldRect.set(gameScreenLayout.getFieldRect());

    Rect nextShapeRect = gameScreenLayout.getNextShapeRect();
    _cachedNextFieldCenterX = (nextShapeRect.left + nextShapeRect.right) / 2;
    _cachedNextFieldCenterY = (nextShapeRect.top + nextShapeRect.bottom) / 2;
  }

  protected GameScreenLayout getGameScreenLayout() {
    return _gameScreenLayout;
  }

  /**
   * paints background of field if needed
   * @param c canvas
   * @param game game for which the field is painted
   */
  public abstract void paintField(Canvas c, ABrickGame game);

}
