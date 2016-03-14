// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Rect;

import org.tequilacat.tcatris.core.GameImpulse;

public class FlatShape {
  private int myCenterX;
  private int myCenterY;
  private int[] myData;


  /**************************************************
   **************************************************/
  public FlatShape(int[] data) {
    myData = data;
    myCenterX = 0;
    myCenterY = 0;
  }

  /**************************************************
   **************************************************/
//    public FlatShape(int cellCount){
//        myData = new int[cellCount*3];
//        myCenterX = 0;
//        myCenterY = 0;
//    }

  /**************************************************
   **************************************************/
  public FlatShape(FlatShape other) {
    myCenterX = other.myCenterX;
    myCenterY = other.myCenterY;
    myData = new int[other.myData.length];
    System.arraycopy(other.myData, 0, myData, 0, other.myData.length);
  }

  public int size() {
    return myData.length / 3;
  }

  public void moveTo(int x, int y) {
    myCenterX = x;
    myCenterY = y;
  }

  /**
   * Computes the shape bounds in cell units and puts to bounds rect
   * @param bounds
   */
  public void getBounds(Rect bounds) {
    int minCol = 0, maxCol = 0, minRow = 0, maxRow = 0;

    for (int i = 0, len = this.size(); i < len; i++) {
      int col = this.getX(i), row = this.getY(i);

      if (i == 0) {
        minCol = maxCol = col;
        minRow = maxRow = row;
      }else {
        if (minCol > col) {
          minCol = col;
        }else if (maxCol < col) {
          maxCol = col;
        }

        if (minRow > row) {
          minRow = row;
        }else if(maxRow < row) {
          maxRow = row;
        }
      }
    }
    bounds.set(minCol, minRow, maxCol + 1, maxRow + 1);
  }

  /**
   * modifies center of the shape by given offset
   *
   * @param deltaX
   * @param deltaY
   */
  public void moveBy(int deltaX, int deltaY) {
    myCenterX += deltaX;
    myCenterY += deltaY;
  }

  private void rotate(int rotationDir) {
    if (rotationDir != 0) {

      for (int i = 0; i < myData.length; i += 3) {
        int x = myData[i], y = myData[i + 1];
        if (x != 0 || y != 0) {
          if (rotationDir > 0) {
            // clockwise
            myData[i] = -y;
            myData[i + 1] = x;
          } else {
            myData[i] = y;
            myData[i + 1] = -x;
          }
        }
      }
    }
  }

  /**
   * returns transformed copy of this shape
   * @param impulse
   * @return
   */
  public FlatShape transformed(GameImpulse impulse) {
    FlatShape shape = new FlatShape(this);
    return shape.transform(impulse) ? shape : null;
  }

  /**
   * transforms current shape
   * @param impulse
   * @return
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
   *
   * @param i
   * @return absolute cell X coord of i'th cell
   */
  public int getX(int i) {
    return myData[(i << 1) + i] + myCenterX;
  }

  /**
   *
   * @param i
   * * @return absolute cell Y coord of i'th cell
   */
  public int getY(int i) {
    // quick *3
    return myData[(i << 1) + i + 1] + myCenterY;
  }

  /**************************************************
   **************************************************/
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
