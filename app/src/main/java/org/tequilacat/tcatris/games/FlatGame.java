// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Canvas;

import com.google.gson.Gson;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.Dimensions;
import org.tequilacat.tcatris.core.DragAxis;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameConstants;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.LayoutParameters;
import org.tequilacat.tcatris.core.VisualResources;

import java.util.EnumSet;

public abstract class FlatGame extends ABrickGame {

  private final Dimensions _fieldDimensions;

  protected int field[][];
  int _shapesThrown;
  private FlatShape myFallingShape;
  private FlatShape myNextShape;

  //private AbstractFlatGamePainter _fieldPainter;
  private int _finalCurShapeY;

  protected FlatGame(GameDescriptor descriptor, AbstractFlatGamePainter fieldPainter) {
    super(descriptor, fieldPainter);

    //_fieldPainter = fieldPainter;
    _fieldDimensions = new Gson().fromJson(descriptor.getGameParameters().get(
        GameConstants.JSON_DIMENSIONS), Dimensions.class);
  }

  private AbstractFlatGamePainter getAbstractFlatGamePainter() {
    return (AbstractFlatGamePainter) getGamePainter();
  }

  /**
   * @return width of game field in cells
   */
  public final int getWidth() {
    return _fieldDimensions.width;
  }

  /**
   * @return height of game field in cells
   */
  public final int getHeight() {
    return _fieldDimensions.height;
  }

  @Override
  public int getMoveDimension() {
    return getWidth();
  }

  /**
   * resets game to initial step
   */
  public void init() {
    myFallingShape = null;
    myNextShape = null;
    _shapesThrown = 0;
    countStep();
    field = new int[getHeight()][getWidth()];

    for (int i = 0; i < getHeight(); i++) {
      for (int j = 0; j < getWidth(); j++) {
        field[i][j] = EMPTY_CELL_TYPE;
      }
    }
  }

  private static EnumSet<GameImpulse> _CheckedImpulses = EnumSet.of(GameImpulse.MOVE_LEFT, GameImpulse.MOVE_RIGHT,
    GameImpulse.ROTATE_CW, GameImpulse.ROTATE_CCW);

  @Override
  public EnumSet<GameImpulse> getSupportedImpulses() {
    return _CheckedImpulses;
  }

  @Override
  public void addEffectiveImpulses(EnumSet<GameImpulse> actionSet) {
    FlatShape curShape = getCurrentShape();

    for (GameImpulse impulse : getSupportedImpulses()) {
      if(!actionSet.contains(impulse)) {
        FlatShape transformed = curShape.transformed(impulse);
        if (transformed != null && isShapePlaceable(transformed)) {
          actionSet.add(impulse);
        }
      }
    }
  }

  @Override
  public boolean doAction(GameImpulse impulse) {
    boolean moved = isEffective(impulse) && getCurrentShape().transform(impulse);

    if(moved){
      checkEffectiveImpulses();
    }

    return moved;
  }

  @Override
  public void onShapeMoved() {
    // find fallen position of current shape
    int x = myFallingShape.getCenterX(), nextY = myFallingShape.getCenterY(), y = nextY + 1;

    while (isShapePlaceable(myFallingShape, x, y)) {
      nextY = y;
      y++;
    }
    // y is last position at which shape can be placed, is current shape Y if shape cannot be moved
    _finalCurShapeY = nextY;
  }

  public int getCellValue(int i, int j) {
    return field[j][i];
  }

  /**
   * checks whether the given shape fits the field
   * @param aShape a shape to test
   * @return whether aShape can be placed to its current coordinates (during fall)
   */
  private boolean isShapePlaceable(FlatShape aShape) {
    return isShapePlaceable(aShape, aShape.getCenterX(), aShape.getCenterY());
  }

  /**
   * Checks whether the given shape fits the field when centered at given coordinates
   * @param aShape shape to test
   * @param centerCol center of shape
   * @param centerRow center of shape
   * @return whether the shape can be moved to these coords
   */
  private boolean isShapePlaceable(FlatShape aShape, int centerCol, int centerRow) {
    // check for not out of bounds, not not over existing
    boolean canPlace = true;
    int rowCount = getHeight(), colCount = getWidth();

    for (int i = 0; i < aShape.size(); i++) {
      int x = aShape.getX(i, centerCol, centerRow), y = aShape.getY(i, centerCol, centerRow);

      if (x < 0 || x >= colCount || y >= rowCount || (y >= 0 && field[y][x] != EMPTY_CELL_TYPE)) {
        canPlace = false;
        break;
      }
    }

    return canPlace;
  }

  /********************************
   * drops cur shape 1 level , or till bottom.
   *
   * @return if successfully moved a shape
   ******************************/
  @Override
  protected boolean dropCurrent(boolean tillBottom) {
    // find y pos to settle
    int finalY = -1, x = myFallingShape.getCenterX(), y = myFallingShape.getCenterY() + 1;
    boolean shapeMoved = false;

    while(isShapePlaceable(myFallingShape, x, y)) {
      finalY = y;
      shapeMoved = true;
      y++;
      if (!tillBottom) {
        break;
      }
    }

    if(shapeMoved) {
      myFallingShape.moveTo(x, finalY);
    }

    return shapeMoved;
  }

  @Override
  protected boolean acquireFallenShape() {
    FlatShape shape = getCurrentShape();

    for (int i = 0; i < myFallingShape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
        return false;
      }
      field[y][x] = shape.getCellType(i);
    }

    return true;
  }


  public FlatShape getNextShape() {
    return myNextShape;
  }

  public FlatShape getCurrentShape() {
    return myFallingShape;
  }

  /**
   * Called on each thrown shape to update stats like level, speed etc
   */
  private void countStep() {
    setLevel(Math.min(1 + _shapesThrown / 20, 10));
  }

  @Override
  protected boolean throwInNewShape() {
    _shapesThrown++;
    countStep();

    myFallingShape = myNextShape == null ? createNext() : myNextShape;
    myNextShape = createNext();
    myFallingShape.moveTo(getWidth() / 2, 0);
    return isShapePlaceable(myFallingShape);
  }


  protected abstract boolean isSqueezable(int i, int j);

  protected abstract FlatShape createNext();

  @Override
  public void layout(LayoutParameters layoutParams) {

    //int MARGIN = VisualResources.Defaults.MARGIN_SIZE;
    //int VERT_SPACING = MARGIN;

    int screenWidth = layoutParams.GameArea.width(), screenHeight = layoutParams.GameArea.height();

    // padding around widest next figure but still within next shape area
    int nextFigMargin = VisualResources.Defaults.MARGIN_SIZE;

    //int width = screenWidth - MARGIN - MARGIN - VERT_SPACING - nextFigMargin * 2;
    //int height = screenHeight - MARGIN - MARGIN;

    int marginPadding = (int) (VisualResources.Defaults.ROUNDED_FRAME_MARGIN
            + VisualResources.Defaults.ROUNDED_FRAME_PADDING);
    // left right and between, + padding within next shape area
    int width = screenWidth - (marginPadding * 4 + nextFigMargin * 2);
    int height = screenHeight - marginPadding * 2;// top and bottom

    // consider left to right
    float[] sideRatios = new float[3];
    estimateSides(sideRatios);
    float fieldHwRatio = sideRatios[0], nextWRatio = sideRatios[1], nextHRatio = sideRatios[2];
    // never consider nextfig higher than the field
    float sumWidth = 1 + nextWRatio, sumHeight = fieldHwRatio; //Math.max(fieldHwRatio, nextHRatio);
    // now normalize to existing space of width, height
    float cellAreasWhRatio = sumWidth / sumHeight, screenWRatio = width / (float)height;

    // how much pixels per estimated sides unit (1.0)
    double pixelRatio;
    if (cellAreasWhRatio > screenWRatio) {
      // field wider the screen
      pixelRatio = width / (1f + nextWRatio);
    } else {
      // taller than the screen
      pixelRatio = height / fieldHwRatio;
    }

    int fieldWidth = (int) pixelRatio, fieldHeight = (int) (fieldHwRatio * pixelRatio),
        nextFigWidth = (int) (nextWRatio * pixelRatio),
        nextFigHeight = (int) (nextHRatio * pixelRatio);

    int fieldX0 = marginPadding;
    int fieldY0 = marginPadding;

//    int fieldWidth = cellSize * glassWidth;
//    int fieldHeight = cellSize * glassHeight;


    // lay out next fig
    ///layoutParams.GameArea.left
    int nextShapeX = layoutParams.GameArea.left + fieldWidth + marginPadding * 3;
    int nextShapeWidth = nextFigMargin * 2 + nextFigWidth;
    int nextShapeY = layoutParams.GameArea.top + marginPadding;
    int nextShapeHeight = nextFigMargin * 2 + nextFigHeight;

    setGameScreenLayout(new GameScreenLayout(
      layoutParams.GameArea.left + fieldX0, layoutParams.GameArea.top + fieldY0,
      fieldWidth, fieldHeight,
      nextShapeX, nextShapeY, nextShapeWidth, nextShapeHeight));

    getGamePainter().init(getGameScreenLayout(), this);
  }

  /**
   * Fills array with 3 values:
   * [0] = field height
   * [1] = next width
   * [2] = next height
   *
   * the field width is considered 1.0f
   *
   * @param threeSides array of at least 3 cells
   */
  protected abstract void estimateSides(float[] threeSides);

//  protected abstract int getMaxNextWidth();
//  protected abstract int getMaxNextHeight();

  /**
   * paints game field
   * @param g canvas to draw to
   * @param dynamicState props of current move state
   */
  @Override
  public void paintField(Canvas g, DynamicState dynamicState) {
    // paints field background and field cells
    getGamePainter().paintField(g, this);

    // display falling shape
    if (getState() == ACTIVE && !canSqueeze()) {

      float dxFactor = 0;
      float rotateFactor = 0; // 1 means 90', 0 means no rotate
      boolean isValid = true;
      boolean drawContour = false;

      DragAxis moveAxis = getMovementAxis();

      if(moveAxis!=null) {
        int pos = moveAxis.ordinal();

        if (dynamicState.isTracking(pos)) {
          dxFactor = dynamicState.getValue(pos);
          drawContour = true;

          if (!dynamicState.isValid(pos)) {
            isValid = false;
            dxFactor = 0;
          }
        }
      }

      DragAxis rotationAxis = getRotationAxis();

      if(rotationAxis!=null) {
        int pos = DragAxis.ROTATE.ordinal();

        if (dynamicState.isTracking(pos)) {
          rotateFactor = dynamicState.getValue(pos);
          drawContour = true;

          if (!dynamicState.isValid(pos)) {
            isValid = false;
            rotateFactor = 0;
          }
        }
      }

      if (drawContour) {
        // TODO only update shape contour when it's thrown in or rotated
        getAbstractFlatGamePainter().updateCurrentShapeContour(getCurrentShape());
        getAbstractFlatGamePainter().drawShapeContour(g, this, isValid, dxFactor, rotateFactor);
            //centerX0 + cellSize / 2 + (int) (dx * cellSize), centerY0 + cellSize / 2,
      }

      getAbstractFlatGamePainter().paintFallingShape(g, this, _finalCurShapeY, dynamicState);
    }
  }

  /**
   *
   * @return axis used to move shape along
   */
  public DragAxis getMovementAxis() {
    return DragAxis.HORIZONTAL;
  }

  /**
   *
   * @return rotation axis
   */
  public DragAxis getRotationAxis() {
    return DragAxis.ROTATE;
  }

  /**
   * paints next figure in given rect
   * @param g canwas to draw to
   */
  @Override
  public void paintNext(Canvas g) {
    getAbstractFlatGamePainter().paintNext(g, getNextShape());
  }

  @Override
  public GameImpulse getAxisImpulse(DragAxis axis, boolean positiveDirection) {
    GameImpulse impulse;

    if(axis == DragAxis.HORIZONTAL) {
      impulse = positiveDirection ? GameImpulse.MOVE_RIGHT : GameImpulse.MOVE_LEFT;
    }else if(axis == DragAxis.ROTATE) {
      impulse = positiveDirection ? GameImpulse.ROTATE_CW : GameImpulse.ROTATE_CCW;
    }else {
      impulse = null;
    }

    return impulse;
  }
}
