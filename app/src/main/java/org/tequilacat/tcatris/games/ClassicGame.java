// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Rect;

import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

import java.util.EnumSet;

public class ClassicGame extends FlatGame {

  boolean mySqueezable[];

  private static final FlatShape shapes[] = {
    new FlatShape(new int[]{
      0, 0, 1, 1, 0, 1, 1, -1, 1, 0, 1, 1
    }), new FlatShape(new int[]{
    0, 0, 6, 0, -1, 6, 1, 0, 6, 1, 1, 6
  }), new FlatShape(new int[]{
    0, 0, 2, 1, -1, 2, 0, -1, 2, 0, 1, 2
  }), new FlatShape(new int[]{
    0, 0, 7, 0, -1, 7, 0, 1, 7, 1, 1, 7
  }), new FlatShape(new int[]{
    0, 0, 3, 0, -1, 3, 0, 1, 3, 0, 2, 3
  }), new FlatShape(new int[]{
    0, 0, 4, 1, 0, 4, 1, 1, 4, 0, 1, 4
  }), new FlatShape(new int[]{
    0, 0, 5, 0, -1, 5, 0, 1, 5, 1, 0, 5
  })
  };

  private static int _maxNextWidth;
  private static int _maxNextHeight;

  static {
    Rect bounds = new Rect();
    _maxNextWidth = 0;
    _maxNextHeight = 0;

    for (FlatShape shape : shapes) {
      shape.getBounds(bounds);
      int w = bounds.width(), h = bounds.height();

      if (_maxNextWidth < w) {
        _maxNextWidth = w;
      }

      if (_maxNextHeight < h) {
        _maxNextHeight = h;
      }
    }
  }

  public ClassicGame(GameDescriptor descriptor) {
    super(descriptor, new FlatRectGamePainter());
    mySqueezable = new boolean[getHeight()];
  }

  @Override
  protected int getMaxNextWidth() {
    return _maxNextWidth;
  }

  @Override
  protected int getMaxNextHeight() {
    return _maxNextHeight;
  }

  @Override
  protected boolean isSqueezable(int x, int y) {
    return (mySqueezable != null) && canSqueeze() && mySqueezable[y];
  }

  @Override
  protected FlatShape createNext() {
    return new FlatShape(shapes[getRandomInt(shapes.length)]);
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
