package org.tequilacat.tcatris;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Scoreboard;
import org.tequilacat.tcatris.core.VisualResources;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String SCOREBOARD_PACKEDPREFS_KEY = "scoreboard_parcel_key";
  private static final String SCORE_PREFBANK_NAME = "scores";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Debug.print("Activity.onCreate");

    // set preferences from XML
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    // the sound vol buttons will control sound of FX
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    VisualResources.Defaults = new VisualResources(getResources());

    setContentView(R.layout.activity_main);

    Toolbar myToolbar = (Toolbar) findViewById(R.id.gameselector_toolbar);
    setSupportActionBar(myToolbar);

    // fill game list

    SharedPreferences prefs = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE);
    Scoreboard.setState(prefs.getString(SCOREBOARD_PACKEDPREFS_KEY, null));

    if(getData().getGameDescriptors() == null) {
      getData().setGameDescriptors(GameList.readAvailableGameTypes(getResources()));
    }

    if(savedInstanceState == null) {
      showFragment(GameSelectorFragment.Id);
    }// else consider it's same - already shown
  }


  /**
   * shows specified fragment
   *
   * @param fragmentId fragment ID to show
   */
  private MainActivityFragment showFragment(MainActivityFragment.FragmentId fragmentId) {

  //  Debug.print("showFragment(" + fragmentId.getId() + ")");
    FragmentTransaction transaction = getFragmentManager().beginTransaction();

    MainActivityFragment fragment = (MainActivityFragment) getFragmentManager().findFragmentByTag(fragmentId.getId());

    if(fragment == null) {
      fragment = fragmentId.create();
    }

    transaction.replace(R.id.main_layout, fragment, fragmentId.getId());
    transaction.commit();
    return fragment;
  }

  /**
   *
   * @return persistent fragment containing activity data, retained during configuration changes
   */
  private PersistentFragment getData() {
    FragmentManager fm = getFragmentManager();
    PersistentFragment fragment = (PersistentFragment) fm.findFragmentByTag(
        PersistentFragment.Id.getId());

    if (fragment == null) {
      fragment = new PersistentFragment();
      fragment.setRetainInstance(true);
      fm.beginTransaction().add(fragment, PersistentFragment.Id.getId()).commit();
    }

    return fragment;
  }

  public abstract static class MainActivityFragment extends Fragment {
    private final boolean _manageToolbar;
    private final boolean _showToolbar;

    public enum FragmentId {
      GameSelector(GameSelectorFragment.class.getName()),
      ScoreList(ScoreFragment.class.getName()),
      GameView(GameViewFragment.class.getName()),
      Persistent(PersistentFragment.class.getName());

      private String _className;

      FragmentId(String className) {
        _className = className;
      }

      public MainActivityFragment create() {
        try {
          return (MainActivityFragment)Class.forName(_className).newInstance();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }

      public String getId() {
        return name();
      }
    }

    public MainActivityFragment() {
      _showToolbar = false;
      _manageToolbar = false;
    }

    public MainActivityFragment(boolean showToolbar) {
      _showToolbar = showToolbar;
      _manageToolbar = true;
    }

    public boolean isShowToolbar() {
      return _showToolbar;
    }

    protected MainActivity getMainActivity() {
      return (MainActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      if(_manageToolbar) {
        ActionBar actionBar = getMainActivity().getSupportActionBar();

        if(actionBar != null) {
          Debug.print("Fragment " + getClass().getName() + ".toobar = " + (isShowToolbar() ? "show" : "hide"));

          if (isShowToolbar()) {
            actionBar.show();
          } else {
            actionBar.hide();
          }
        }
      }
    }
  }

  public static class PersistentFragment extends MainActivityFragment {
    public static final FragmentId Id = FragmentId.Persistent;

    private List<GameDescriptor> _gameDescriptors;
    private ABrickGame _game;

    public ABrickGame getCurrentGame() {
      return _game;
    }

    public void setCurrentGame(ABrickGame game) {
      _game = game;
    }

    public List<GameDescriptor> getGameDescriptors() {
      return _gameDescriptors;
    }

    public void setGameDescriptors(List<GameDescriptor> gameDescriptors) {
      _gameDescriptors = gameDescriptors;
    }
  }

  /**
   * fragment starting game of a chosen type
   */
  public static class GameSelectorFragment extends MainActivityFragment {
    public static final FragmentId Id = FragmentId.GameSelector;

    public GameSelectorFragment() {
      super(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      //Debug.print("GameSelectorFragment.onCreate");
      setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
     // Debug.print("GameSelectorFragment.onCreateOptionsMenu");
      inflater.inflate(R.menu.gameselector_menu, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      // update activity header
      ActionBar actionBar = getMainActivity().getSupportActionBar();

      if (actionBar != null) {
        actionBar.setTitle(R.string.app_name);
      }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.view_gameselector, container, false);

      final List<GameDescriptor> gameTypes = getMainActivity().getData().getGameDescriptors();
      final ArrayAdapter<GameDescriptor> adapter = new ArrayAdapter<>(getMainActivity(),
          android.R.layout.simple_list_item_1, gameTypes);

      ListView gameListView = (ListView) view.findViewById(R.id.lvMainOptionList);
      gameListView.setAdapter(adapter);
      gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          getMainActivity().runGame(gameTypes.get(position));
        }
      });
      return view;
    }
  }

  /**
   * main gaming area fragment
   */
  public static class GameViewFragment extends MainActivityFragment {
    public static final FragmentId Id = FragmentId.GameView;

    public GameViewFragment() {
      super(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
     // Debug.print("GameViewFragment.onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      GameView view = (GameView) inflater.inflate(R.layout.view_gamesurface, container, false);
      view.setGame(getMainActivity().getData().getCurrentGame());
      return view;
    }
  }

  /**
   * Fragment displaying scores in the app
   */
  public static class ScoreFragment extends MainActivityFragment {
    public static final FragmentId Id = FragmentId.ScoreList;
    private static java.text.DateFormat TIMESTAMP_FORMAT =
        DateFormat.getDateTimeInstance();

    public ScoreFragment() {
      super(true);
      //Debug.print("ScoreFragment::ScoreFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      //Debug.print("ScoreFragment.onCreate : " + this);
      setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      // update activity header
      ActionBar actionBar = getMainActivity().getSupportActionBar();

      if (actionBar != null) {
        actionBar.setTitle(String.format("%s: %s",
            getString(R.string.app_name),
            String.format(getActivity().getString(R.string.top_scores),
                getMainActivity().getData().getCurrentGame().getDescriptor().getLabel())
        ));
      }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
//      Debug.print("ScoreFragment.onCreateOptionsMenu : " + this);
      inflater.inflate(R.menu.score_menu, menu);

      ABrickGame game = getMainActivity().getData().getCurrentGame();

      if (game != null) { // fix crash when waking up without a game
        boolean isGameActive = game.getState() != ABrickGame.LOST;
        menu.findItem(R.id.mnu_back_to_game).setVisible(isGameActive);
        menu.findItem(R.id.mnu_play_again).setVisible(!isGameActive);
        ((TextView) getView().findViewById(R.id.gameStatusText)).setText(getActivity().getText(
                isGameActive ? R.string.msg_gamestatus_paused : R.string.msg_gamestatus_failed)
        );
      }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//      Debug.print("ScoreFragment.onCreateView : " + this);
      View view = inflater.inflate(R.layout.view_scores, container, false);
      loadScores(view);
      return view;
    }

    private void loadScores(View view) {
      //View view = getView();
      ABrickGame game = getMainActivity().getData().getCurrentGame();
      final Scoreboard.GameScores gs = Scoreboard.instance().getGameScores(game.getDescriptor().getId());

      ListView scoreListView = (ListView) view.findViewById(R.id.lvScoreList);

      ArrayAdapter<Scoreboard.ScoreEntry> adapter = new ArrayAdapter<Scoreboard.ScoreEntry>(
          getActivity(),
          //android.R.layout.simple_list_item_2,
          android.R.layout.simple_list_item_activated_2,
          android.R.id.text1, gs.getEntries()) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          View view = super.getView(position, convertView, parent);
          TextView text1 = (TextView) view.findViewById(android.R.id.text1);
          TextView text2 = (TextView) view.findViewById(android.R.id.text2);

          Scoreboard.ScoreEntry item = getItem(position);
          text1.setText(Integer.valueOf(item.getScore()).toString());
          text2.setText(gs.isCurrent(item) ? getContext().getString(R.string.current_score)
              : TIMESTAMP_FORMAT.format(new Date(item.getTime())));

          TwoLineListItem listItem = (TwoLineListItem) view;
          listItem.setActivated(gs.isCurrent(item));

          return view;
        }
      };
      scoreListView.setAdapter(adapter);

      // if current score did not make into roaster show message
      TextView scoreTooLowText = (TextView) view.findViewById(R.id.scoreOutOfRoasterText);
      scoreTooLowText.setText(gs.containsCurrentScore() ? null :
          String.format(getString(R.string.current_score_too_low), game.getScore()));
    }
  }

  /**
   * stores game scores for all games
   */
  @Override
  protected void onPause() {
    super.onPause();

    SharedPreferences.Editor prefEditor = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE).edit();
    prefEditor.putString(SCOREBOARD_PACKEDPREFS_KEY, Scoreboard.getState());
    prefEditor.apply();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean res = true;

    int itemId = item.getItemId();

    if (itemId == R.id.mnu_back_to_game || itemId == R.id.mnu_play_again) {
      backFromScores();

    } else if (itemId == R.id.mnu_selectgame) {
      showFragment(GameSelectorFragment.Id);

    } else if (itemId == R.id.mnu_settings) {
      showSettings();

    } else if (itemId == R.id.mnu_about) {
      Toast toast = Toast.makeText(this,
          String.format(getText(R.string.txt_about).toString(), BuildConfig.VERSION_NAME),
          Toast.LENGTH_LONG);

      toast.show();

    } else {
      res = super.onOptionsItemSelected(item);
    }

    return res;
  }

  private void runGame(GameDescriptor gameDescriptor) {
    ABrickGame game = gameDescriptor.createGame();// GameList.instance().createGame(gameDescriptor);
    game.initGame();
    getData().setCurrentGame(game);

    showFragment(GameViewFragment.Id);
  }

  /**
   * on back button if current is gamelist we finish it by calling base,
   * if current is game we set paused mode
   */
  @Override
  public void onBackPressed() {
    Debug.print("Back is pressed");
    FragmentManager fm = getFragmentManager();
    Fragment fragment;

    if ((fragment = fm.findFragmentByTag(GameSelectorFragment.Id.getId())) != null && fragment.isVisible()) {
      super.onBackPressed();
    } else if ((fragment = fm.findFragmentByTag(GameViewFragment.Id.getId())) != null && fragment.isVisible()) {
      showScores();

    } else if ((fragment = fm.findFragmentByTag(ScoreFragment.Id.getId())) != null && fragment.isVisible()) {
      backFromScores();
    }
  }

  /**
   * Called on pause, or when game is lost.
   * Shows scores screen.
   */
  public void showScores() {
    showFragment(ScoreFragment.Id);
  }

  private void backFromScores() {
//    Debug.print("restart/continue after scores");
    ABrickGame game = getData().getCurrentGame();

    if (game.getState() == ABrickGame.LOST) {
      game.initGame();
    }
    showFragment(GameViewFragment.Id);
  }

  private void showSettings() {
    Intent intent = new Intent();
    intent.setClass(MainActivity.this, SettingsActivity.class);
    startActivityForResult(intent, 0);
  }
}
