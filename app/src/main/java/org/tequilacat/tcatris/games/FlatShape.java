// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

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
   * modifies center of the shape by given offset
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
          // rotate:
          int rotation = (rotationDir < 0) ? 3 : 1;
          while (rotation > 0) {
            int tmp = x;
            x = y;
            y = -tmp;
            rotation--;
          }

          myData[i] = x;
          myData[i + 1] = y;
        }
      }
    }
  }


  public FlatShape transformed(GameImpulse impulse) {
    FlatShape shape = new FlatShape(this);
    return shape.transform(impulse) ? shape : null;
  }

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

  /**************************************************
   **************************************************/
  public int getX(int i) {
    return myData[i * 3] + myCenterX;
  }

  /**************************************************
   **************************************************/
  public int getY(int i) {
    return myData[i * 3 + 1] + myCenterY;
  }

  /**************************************************
   **************************************************/
  public void setCellType(int i, int cellType) {
    myData[i * 3 + 2] = cellType;
  }

  /**************************************************
   **************************************************/
  public int getCellType(int i) {
    return myData[i * 3 + 2];
  }
}
