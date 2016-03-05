package org.tequilacat.tcatris.core;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by user1 on 05.03.2016.
 */
public class GameListTest {

  @Test
  public void testScoreBoard() {
    GameList.init();
    GameList.GameScores gs = GameList.instance().getGameScores("1");
    assertThat(gs, nullValue());
  }
}