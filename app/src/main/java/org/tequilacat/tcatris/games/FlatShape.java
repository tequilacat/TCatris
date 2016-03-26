package org.tequilacat.tcatris.games;

import android.graphics.Rect;

import org.tequilacat.tcatris.core.GameImpulse;

public abstract class FlatShape {
  private int myCenterX;
  private int myCenterY;
  protected int[] myData;


  /**************************************************
   **************************************************/
  public FlatShape(int[] data) {
    myData = data;
    myCenterX = 0;
    myCenterY = 0;
  }

  /**
   * creates copy of the specified shape
   * @return deep copy of the shape
   */
  public abstract FlatShape createCopy();

  /**
   * copies data from other shape to this
   * @param fromOther source shape
   */
  protected void copyFields(FlatShape fromOther) {
    myCenterX = fromOther.myCenterX;
    myCenterY = fromOther.myCenterY;
    myData = new int[fromOther.myData.length];
    System.arraycopy(fromOther.myData, 0, myData, 0, fromOther.myData.length);
  }

  public int size() {
    return myData.length / 3;
  }

  /**
   * Computes the shape bounds in cell units and puts to bounds rect
   * @param bounds target rect whose values to be set
   */
  public abstract void getBounds(Rect bounds);

  /**
   * Sets center of the shape to specified coordinates
   * @param x new center X
   * @param y new center Y
   */
  public void moveTo(int x, int y) {
    myCenterX = x;
    myCenterY = y;
  }

  /**
   * modifies center of the shape by given offset
   *
   * @param deltaX x offset
   * @param deltaY y offset
   */
  public void moveBy(int deltaX, int deltaY) {
    myCenterX += deltaX;
    myCenterY += deltaY;
  }

  /**
   * rotates according to coordinate system of the shape implementation
   * @param rotationDir whether rotation is Clockwise
   */
  protected abstract void rotate(int rotationDir);

  /**
   * returns transformed copy of this shape
   * @param impulse action impulse modifying the shape
   * @return transformed shape or null if transformation is not possible
   */
  public FlatShape transformed(GameImpulse impulse) {
    FlatShape shape = this.createCopy();
    return shape.transform(impulse) ? shape : null;
  }

  /**
   * transforms current shape
   * @param impulse action impulse modifying the shape
   * @return whether transformation has succeeded
   */
  public boolean transform(GameImpulse impulse) {
    boolean modified = true;

    if (impulse == GameImpulse.ROTATE_CW) {
      rotate(1);
    } else if (impulse == GameImpulse.ROTATE_CCW) {
      rotate(-1);
    } else if (impulse == GameImpulse.MOVE_LEFT) {
      moveBy(-1, 0);
      //transformed.rotate(-1);
    } else if (impulse == GameImpulse.MOVE_RIGHT) {
      moveBy(1, 0);
    } else {
      modified = false;
    }

    return modified;
  }

  /**
   * @return center X of the shape
   */
  public int getCenterX() {
    return myCenterX;
  }

  /**
   * @return center Y of the shape
   */
  public int getCenterY() {
    return myCenterY;
  }


  /**
   * Gets X coord of i-th cell for shape centered at its current position
   *
   * @param i index of cell
   * @return absolute cell X coord of i'th cell
   */
  public int getX(int i) {
    return getX(i, myCenterX, myCenterY);
  }

  /**
   * Gets Y coord of i-th cell for shape centered at its current position
   * @param i index of cell
   * * @return absolute cell Y coord of i'th cell
   */
  public int getY(int i) {
    // quick *3
    return getY(i, myCenterX, myCenterY);
  }

  /**
   * gets X coord of i-th cell assuming shape is centered at centerX, centerY
   * @param i cell index
   * @param centerX center X
   * @param centerY center Y
   * @return x coordinate of specified cell
   */
  public abstract int getX(int i, int centerX, int centerY);
  /**
   * gets Y coord of i-th cell assuming shape is centered at centerX, centerY
   * @param i cell index
   * @param centerX center X
   * @param centerY center Y
   * @return y coordinate of specified cell
   */
  public abstract int getY(int i, int centerX, int centerY);

  /**
   * changes cell type
   * @param i cell index
   * @param cellType new value for cell type
   */
  public void setCellType(int i, int cellType) {
    // quick *3
    myData[(i << 1) + i + 2] = cellType;
  }

  /**************************************************
   **************************************************/
  public int getCellType(int i) {
    return myData[(i << 1) + i + 2];
  }

  public boolean signatureEquals(Object shapeSignature) {
    boolean equals = false;

    if (shapeSignature != null && shapeSignature instanceof int[] &&
        ((int[]) shapeSignature).length == myData.length) {
      final int[] sigArray = (int[]) shapeSignature;
      equals = true;

      for (int i = 0, len = sigArray.length; i < len; i++) {
        if (sigArray[i] != myData[i]) {
          equals = false;
          break;
        }
        i++;
        if (sigArray[i] != myData[i]) {
          equals = false;
          break;
        }
        i++; // skip color - does not matter for shape
      }
    }

    return equals;
  }

  public Object generateSignature() {
    return myData.clone();
  }
}
