package org.tequilacat.tcatris.core;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;

/**
 * Base class to display game state on screen
 */
public class AbstractGamePainter {

  public class ColorPalette {
    public int DYN_SHAPE_STROKE_VALID = VisualResources.Defaults.DYN_SHAPE_STROKE_VALID;
    public int DYN_SHAPE_STROKE_INVALID = VisualResources.Defaults.DYN_SHAPE_STROKE_INVALID;
    public int FIELD_BG_COLOR = VisualResources.Defaults.FIELD_BG_COLOR;
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
    return getTypeColor(cellType, false);
  }

  public static int getTypeColor(int cellType, boolean contrast) {
    return ColorCodes.getDistinctColor(cellType - 1,
        contrast ? ColorCodes.Lightness.Contrast : ColorCodes.Lightness.Normal );
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

}
