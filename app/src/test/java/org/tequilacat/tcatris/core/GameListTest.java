package org.tequilacat.tcatris.core;

import org.junit.Test;

import org.tequilacat.tcatris.core.Scoreboard.*;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by user1 on 05.03.2016.
 */
public class GameListTest {

  private int getCurrentScoreIndex(GameScores gameScores) {
    int index = -1;
    List<ScoreEntry> entries = gameScores.getEntries();

    if(entries.size()> Scoreboard.MAX_SCORE_LENGTH) {
      fail("Too much entries");
    }

    for(int i = 0; i < entries.size(); i++) {
      ScoreEntry entry = entries.get(i);
      if (gameScores.isCurrent(entry)) {
        if(index != -1) {
          fail("met current entry twice");
        }
        index = i;
      }
    }

    return index;
  }

  /**
   * observe how adding becomes last, then ups the top
   * @throws Exception
   */
  @Test
  public void testScoreBoardFromHalfFilled() throws Exception {
    Scoreboard.setState(null);
    GameScores gs = Scoreboard.instance().getGameScores("1");

    // adding to
    ScoreEntry entry;
    entry = new ScoreEntry(); entry.setScore(50); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(40); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(30); gs.getEntries().add(entry);

    gs.setScore(0);
    assertThat(gs.getEntries(), hasSize(3));
    assertEquals(-1, getCurrentScoreIndex(gs));

    gs.setScore(10);
    assertThat(gs.getEntries(), hasSize(4));
    assertEquals(3, getCurrentScoreIndex(gs));

    gs.setScore(100);
    assertThat(gs.getEntries(), hasSize(4));
    assertEquals(0, getCurrentScoreIndex(gs));
  }

  @Test
  public void testScoreBoardFromFilled() throws Exception {
    Scoreboard.setState(null);
    GameScores gs = Scoreboard.instance().getGameScores("1");

    // adding to
    ScoreEntry entry;
    entry = new ScoreEntry(); entry.setScore(50); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(40); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(30); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(20); gs.getEntries().add(entry);
    entry = new ScoreEntry(); entry.setScore(10); gs.getEntries().add(entry);

    // now init , must be same
    gs.setScore(0);
    assertThat(gs.getEntries(), hasSize(5));
    assertEquals(-1, getCurrentScoreIndex(gs));

    // still less than lowest
    gs.setScore(5);
    assertEquals(-1, getCurrentScoreIndex(gs));

    // LE than lowest
    gs.setScore(10);
    assertThat(gs.getEntries(), hasSize(5));
    assertEquals(-1, getCurrentScoreIndex(gs));

    // add 35 - should jump to #3
    gs.setScore(35);
    assertThat(gs.getEntries(), hasSize(5));
    assertEquals(2, getCurrentScoreIndex(gs));
    // former #3 becomes #4
    assertEquals(20, gs.getEntries().get(4).getScore());
  }

  /**
   * fill from empty table
   * @throws Exception
   */
  @Test
  public void testScoreBoardFromEmpty() throws Exception {
    Scoreboard.setState(null);

    GameScores gs = Scoreboard.instance().getGameScores("1");
    gs.setScore(0);
    assertTrue(gs.getEntries().isEmpty());
    gs.setScore(0);
    assertTrue(gs.getEntries().isEmpty());

    // adds first one
    gs.setScore(10);
    assertThat(gs.getEntries(), hasSize(1));
    assertEquals(0, getCurrentScoreIndex(gs));
    assertEquals(10, gs.getEntries().get(0).getScore());


    long prevTime = gs.getEntries().get(0).getTime();
    // after wait add another same score, not added - time is the same as in prev
    Thread.sleep(50);
    gs.setScore(10);
    assertThat(gs.getEntries(), hasSize(1));
    assertEquals(0, getCurrentScoreIndex(gs));
    assertEquals(10, gs.getEntries().get(0).getScore());
    assertEquals(prevTime, gs.getEntries().get(0).getTime());


    gs.setScore(5);// not added since added score is less than last, nonsense
    assertThat(gs.getEntries(), hasSize(1));
    assertEquals(0, getCurrentScoreIndex(gs));
    assertEquals(10, gs.getEntries().get(0).getScore());

    gs.setScore(20); // increase score
    assertThat(gs.getEntries(), hasSize(1));
    assertEquals(0, getCurrentScoreIndex(gs));
    assertEquals(20, gs.getEntries().get(0).getScore());
  }

  @Test
  public void testScoreBoardReplaceCurrent() throws Exception {
    Scoreboard.setState(null);

    GameScores gs = Scoreboard.instance().getGameScores("1");
    assertThat(gs, not(is(nullValue())));

    long t0 = System.currentTimeMillis();
    // check empty scoreboard filling
    gs.setScore(0);
    assertTrue(gs.getEntries().isEmpty());

    Thread.sleep(10);
    gs.setScore(10);
    // must return one, is
    assertThat(gs.getEntries(), hasSize(1));
    assertTrue(gs.isCurrent(gs.getEntries().get(0)));
    assertThat(gs.getEntries().get(0).getScore(), is(10));
    assertThat(gs.getEntries().get(0).getTime(), is(greaterThan(t0)));

    // still the same player, should replace current score 10 with 20
    t0 = System.currentTimeMillis();
    Thread.sleep(10);
    gs.setScore(20);
    assertThat(gs.getEntries(), hasSize(1));
    assertTrue(gs.isCurrent(gs.getEntries().get(0)));
    assertThat(gs.getEntries().get(0).getScore(), is(20));
    assertThat(gs.getEntries().get(0).getTime(), is(greaterThan(t0)));

    
    // consider this game finished, and start another game, add another score
    gs.setScore(0);
    // now top is the same and current is -1
    assertThat(gs.getEntries(), hasSize(1));
    assertEquals(-1, getCurrentScoreIndex(gs));
    assertThat(gs.getEntries().get(0).getScore(), is(20));

    gs.setScore(10); // this one gets second
    assertThat(gs.getEntries(), hasSize(2));
    assertEquals(1, getCurrentScoreIndex(gs));
    assertThat(gs.getEntries().get(1).getScore(), is(10));

    gs.setScore(100); // this one gets second
    assertThat(gs.getEntries(), hasSize(2));
    assertEquals(0, getCurrentScoreIndex(gs));
    assertThat(gs.getEntries().get(0).getScore(), is(100));
  }
}