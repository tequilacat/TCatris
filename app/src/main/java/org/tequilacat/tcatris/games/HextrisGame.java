package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.GameDescriptor;

/**
 * Hextris game
 */
public class HextrisGame extends AbstractRotationGame {

  public HextrisGame(GameDescriptor descriptor) {
    super(descriptor, new HextrisPainter(), HextrisShape.getStaticShapes());
  }

  @Override
  protected FlatShape createNext() {
    return HextrisShape.getStaticShapes()[0].createCopy();
  }

  @Override
  protected boolean isSqueezable(int i, int j) {
    // TODO implement isSqueezable
    return false;
  }

  @Override
  protected boolean computeCanSqueeze() {
    // TODO implement computeCanSqueeze
    return false;
  }

  @Override
  protected boolean squeeze() {
    // TODO implement squeeze
    return false;
  }
}
