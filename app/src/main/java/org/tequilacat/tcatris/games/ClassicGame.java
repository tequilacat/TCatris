// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.Dimensions;
import org.tequilacat.tcatris.core.GameDescriptor;

public class ClassicGame extends AbstractRotationGame {

  boolean mySqueezable[];

  private static final FlatShape _StaticShapes[] = {
    new FlatRectShape(new int[]{
      0, 0, 1, 1, 0, 1, 1, -1, 1, 0, 1, 1
    }), new FlatRectShape(new int[]{
    0, 0, 6, 0, -1, 6, 1, 0, 6, 1, 1, 6
  }), new FlatRectShape(new int[]{
    0, 0, 2, 1, -1, 2, 0, -1, 2, 0, 1, 2
  }), new FlatRectShape(new int[]{
    0, 0, 7, 0, -1, 7, 0, 1, 7, 1, 1, 7
  }), new FlatRectShape(new int[]{
    0, 0, 3, 0, -1, 3, 0, 1, 3, 0, 2, 3
  }), new FlatRectShape(new int[]{
    0, 0, 4, 1, 0, 4, 1, 1, 4, 0, 1, 4
  }), new FlatRectShape(new int[]{
    0, 0, 5, 0, -1, 5, 0, 1, 5, 1, 0, 5
  })
  };

  public ClassicGame(GameDescriptor descriptor) {
    super(descriptor, new FlatRectGamePainter(), _StaticShapes);
    mySqueezable = new boolean[getHeight()];
  }


//  static {
//    autoAssignColors(_StaticShapes);
//  }
//
//  private static void autoAssignColors(FlatShape[] shapes) {
//    for (int i = 0; i < shapes.length; i++) {
//      FlatShape shape = shapes[i];
//
//      for (int nCell = 0, n = shape.size(); nCell < n; nCell++) {
//        ((FlatRectShape)shape).setCellType(nCell, i + 1);
//      }
//    }
//  }

  @Override
  protected boolean isSqueezable(int x, int y) {
    return (mySqueezable != null) && canSqueeze() && mySqueezable[y];
  }

  @Override
  protected void estimateSides(float[] threeSides) {
    Dimensions maxSizes = computeMaxNextShapeDimensions(_StaticShapes);
    threeSides[0] = getHeight() / (float) getWidth();
    float oneCell = 1f / getWidth();
    threeSides[1] = oneCell * maxSizes.width;
    threeSides[2] = oneCell * maxSizes.height;
  }

  @Override
  public boolean computeCanSqueeze() {
    boolean canDo = false;

    for (int y = 0; y < getHeight(); y++) {
      mySqueezable[y] = true;
      for (int x = 0; x < getWidth(); x++) {
        if (field[y][x] == EMPTY) {
          mySqueezable[y] = false;
          break;
        }
      }
      canDo |= mySqueezable[y];
    }

    //    System.out.println("  squeeze? "+ canDo +"");
    return canDo;
  }

  @Override
  public boolean squeeze() {
    int curRow = getHeight() - 1, targetRow = curRow;

    while (curRow >= 0) {
      if(mySqueezable[curRow]) { // have contents
        curRow--; // move forward, keep target pointed to same
        setScore(getScore() + 1);
        
      }else {
        if(curRow < targetRow) {
          System.arraycopy(field[curRow], 0, field[targetRow], 0, getWidth());
        }

        targetRow--;
        curRow--;
      }
    }

    while (targetRow >= 0) {
      for (int col = 0; col < getWidth(); col++) {
        field[targetRow][col] = EMPTY;
      }
      targetRow--;
    }

    return false;
  }


}
