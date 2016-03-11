// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

import java.util.EnumSet;

public class ClassicGame extends FlatGame {

  boolean mySqueezable[];

  private final FlatShape shapes[] = {
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

  public ClassicGame(GameDescriptor descriptor) {
    super(descriptor, new FlatRectGamePainter());
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
    if (mySqueezable == null) {
      mySqueezable = new boolean[getHeight()];
    }

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
    if (mySqueezable == null || mySqueezable.length != getHeight()) return false;

    int increment = 1;

    for (int i = 0; i < mySqueezable.length; ) {
      if (mySqueezable[i]) {
        setScore(getScore() + increment);
        increment++;
        mySqueezable[i] = false;

        for (int y = i; y < getHeight(); y++) {
          mySqueezable[y] = (y == getHeight() - 1) ? false : mySqueezable[y + 1];
          for (int x = 0; x < getWidth(); x++) {
            field[y][x] = (y == getHeight() - 1) ? EMPTY : field[y + 1][x];
          }
        }
      } else {
        i++;
      }
    }
    return false;
  }


}
