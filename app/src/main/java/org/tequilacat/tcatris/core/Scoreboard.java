package org.tequilacat.tcatris.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores scores for all games played in this installation.
 * Games are distinguished by string ID.
 * For each game, top MAX_SCORE_LENGTH are stored.
 * For each score entry, its timestamp and scores are stored.
 *
 * Created by user1 on 07.03.2016.
 */
public class Scoreboard {

  public static final int MAX_SCORE_LENGTH = 5;
  private static Scoreboard _instance = new Scoreboard();

  private Map<String, GameScores> _gameScoresMap = new HashMap<>();

  private Scoreboard() { }

  public static void setState(Scoreboard board) {
    _instance = board == null ? new Scoreboard() : board;
  }

  public static Scoreboard instance() {
    return _instance;
  }

  public static class ScoreEntry {
    private int _score;
    private long _time;

    public int getScore() {
      return _score;
    }

    public void setScore(int score) {
      _score = score;
    }

    public long getTime() {
      return _time;
    }

    public void setTime(long time) {
      _time = time;
    }
  }

  /**
   * finds game scores object for certain game, or creates new one
   *
   * @param gameId
   * @return
   */
  public GameScores getGameScores(String gameId) {
    final GameScores gameScores;

    if (_gameScoresMap.containsKey(gameId)) {
      gameScores = _gameScoresMap.get(gameId);
    } else {
      gameScores = new GameScores();
      _gameScoresMap.put(gameId, gameScores);
    }

    return gameScores;
  }


  public static class GameScores {
    private List<ScoreEntry> _entries = new ArrayList<>();
    private ScoreEntry _currentEntry = null;

    public int getMaxScore() {
      return _entries.isEmpty() ? 0 : _entries.get(0).getScore();
    }

    public boolean isCurrent(ScoreEntry entry) {
      return entry == _currentEntry;
    }

    public List<ScoreEntry> getEntries() {
      return _entries;
    }

    /**
     * creates new entry for current player (if score = 0)
     * or updates already existing current entry.
     * If score is too small it's not appended.
     * If score is 0 the game is considered just started
     *
     * @param score
     */
    public void setScore(int score) {
      if (score == 0 || _currentEntry == null) {
        // avoid entry recreation for the same score parameter
        if (_currentEntry == null || _currentEntry.getScore() != score) {
          _currentEntry = new ScoreEntry();
          _currentEntry.setScore(score);
        }
      } else if (score > _currentEntry.getScore()) { //foolproof - usually must be greater than last invocation
        // current entry is non null, see if it fits scoreboard

        // if less than last, and not 0, we add it to last if we have slots
        int scorePos = _entries.size() - 1;
        // find scorePos with score >= added one

        while (scorePos >= 0) {
          if (score <= _entries.get(scorePos).getScore()) {
            break;
          }
          scorePos--;
        }
//        while (scorePos >= 0 && score > _entries.get(scorePos).getScore()) {
//          scorePos--;
//        }

        // now scorePos is next higher score.
        // add if new pos is within LE maxcount
        if (scorePos + 1 < MAX_SCORE_LENGTH) {
          _entries.remove(_currentEntry);
          _entries.add(scorePos + 1, _currentEntry);
        }

        while (_entries.size() > MAX_SCORE_LENGTH) {
          _entries.remove(_entries.size() - 1);
        }

        _currentEntry.setScore(score);
        _currentEntry.setTime(System.currentTimeMillis());
      }
    }
  }
}
