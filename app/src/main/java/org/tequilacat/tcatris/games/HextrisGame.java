package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

/**
 * Hextris game
 */
public class HextrisGame extends AbstractRotationGame {
  private static final FlatShape _StaticShapes[] = {
      new HextrisShape(new int[]{
          0, 0, 1, 1, 0, 1, 1, -1, 1, 0, 1, 1
      })
  };

  static {
    autoAssignColors(_StaticShapes);
  }

  protected HextrisGame(GameDescriptor descriptor) {
    super(descriptor, new HextrisPainter(), _StaticShapes);
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
