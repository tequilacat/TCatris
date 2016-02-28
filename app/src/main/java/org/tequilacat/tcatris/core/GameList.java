package org.tequilacat.tcatris.core;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by avo on 24.02.2016.
 */
public class GameList {
  private static final String GameDefinitions =
    "game=ClassicGame:Tetris:10:15:2:4\n" +
    "game=Columns:Xixit:5:12:1:3:fig=vert\n" +
    "game=Columns:Columns:8:15:3:1:fig=horz\n" +
    "game=Columns:Trix:8:15:1:3:type=rot\n";
  private static GameList _instance;

  private List<GameData> _gameStateVector;
  private List<GameDescriptor> _descriptors = new ArrayList<>();

  /**
   * stores parameters of certain game type
   */
  public class GameDescriptor {
    private final String _gameClassName;
    private final String _label;
    private final String _gameParameters;

    public GameDescriptor(String gameClassName, String label, String gameParams) {
      _gameClassName = gameClassName;
      _label = label;
      _gameParameters = gameParams;
    }

    public String getGameClassName() {
      return _gameClassName;
    }

    public String getLabel() {
      return _label;
    }

    public String getGameParameters() {
      return _gameParameters;
    }

    public String getId() { return getGameClassName()+"/"+getLabel(); }

    @Override
    public String toString() {
      return getLabel();
    }
  }

  public static GameList instance() {
    return _instance;
  }

  public static void init() {
    _instance = new GameList();
  }

  private GameList() {
    //game=flassname:label:w:h:nw:nh:optionalParams

    try {
      Reader reader = new StringReader(GameDefinitions);

      //new InputStreamReader(getClass().getResourceAsStream("/games.txt"));
      StringBuilder stb = new StringBuilder();
      //char[] chunk = new char[1000];
      //int readCount;
      //while((readCount = reader.read(chunk)) > 0){
      int inChar;
      while ((inChar = reader.read()) != -1) {
        if (inChar == '\n') {
          String mayBeDesc = stb.toString();
          if (mayBeDesc.startsWith("game=")) {
            // in form:
            // game=flat.ClassicGame:Tetris:10:15:2:4

            // sep1 after classname
            int sep1 = mayBeDesc.indexOf(':', 5), sep2 = mayBeDesc.indexOf(':', sep1 + 1);
            _descriptors.add(new GameDescriptor("org.tequilacat.tcatris.games." + mayBeDesc.substring(5, sep1),
              mayBeDesc.substring(sep1 + 1, sep2), mayBeDesc.substring(sep2 + 1)));

            //Debug.print("ADD Game: '"+ _descriptors.elementAt() +"'");
          }
          stb.setLength(0);
        } else {
          stb.append((char) inChar);
        }
      }

      readStoredData();
    } catch (Exception e) {
      Debug.print("Error reading game definitions: " + e);
    }
  }

  public GameDescriptor findDescriptor(String gameId) {
    GameDescriptor found = null;

    for(GameDescriptor descriptor : _descriptors){
      if(descriptor.getId().equals(gameId)) {
        found = descriptor;
        break;
      }
    }

    return found;
  }

  /**
   *
   * @return available game types
   */
  public List<GameDescriptor> getGameDescriptors() {
    return _descriptors;
  }

  /**
   * Creates game of given type, inits from last saved props
   * */
  public Tetris createGame(GameDescriptor descriptor) {
    Tetris game = null;

    try {

      //Debug.print("Create game: class = "+gameClass+", '"+ gameName +"', -> "+gameDesc);
      game = (Tetris) Class.forName( descriptor.getGameClassName()).newInstance();
      game.init(descriptor.getLabel(), descriptor.getGameParameters(), getGameData(descriptor));

    } catch (Exception e) {
      Debug.print("Error creating game: " + e);
    }

    return game;
  }

  private class GameData {
    public String gameId;
    public byte[] data;
  }

  private byte[] getGameData(GameDescriptor descriptor) {
    String gameId = descriptor.getId();
    byte[] gameData = null;

    for(GameData gd : _gameStateVector) {
      if(gd.gameId.equals(gameId)) {
        gameData = gd.data;
        break;
      }
    }

    return gameData;
  }

  /**
   *
   * @param data
   * @param descriptor
   */
  public void storeGameData(byte[] data, GameDescriptor descriptor) {
    String gameId = descriptor.getId();
    Debug.print("Store game data '" + gameId + "': bytes are " + data.length);
    GameData foundData = null;

    for (GameData gd : _gameStateVector) {
      if (gd.gameId.equals(gameId)) {
        foundData = gd;
        break;
      }
    }

    if (foundData == null) {
      foundData = new GameData();
      foundData.gameId = gameId;
    }

    foundData.data = data;
  }

  /**************************************************
   **************************************************/
  private void readStoredData() {
    if (_gameStateVector == null) {
      _gameStateVector = new ArrayList<>();
/*


      try {
        if (myRS == null) {
          myRS = RecordStore.openRecordStore(RMS_NAME, true);
        }

//                Debug.print("Read RMS: #recs = "+myRS.getNumRecords()+" ["+myRS.getNextRecordID()+"]");
        // check record
        for (int i = 1; i <= myRS.getNumRecords(); i++) {
//                    Debug.print("Get record #" + i);

          byte[] data = myRS.getRecord(i);
//                    Debug.print("  done");
          if (i == 1) {
            if (data != null) {
              DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
              myState = dis.readUTF();
            }
          } else {
            //
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            _gameStateVector.addElement(dis.readUTF());
            //Debug.print("Read "+_gameStateVector.lastElement()+" game: "+dis.available()+" bytes");
            data = new byte[dis.available()];
            dis.read(data);
            _gameStateVector.addElement(data);

          }
        }


      } catch (Exception e) {
        Debug.print("Error reading RMS " + e);
        myRS = null;
        //_gameStateVector = new Vector();
      }
    */
    }
  }

  /**************************************************
   **************************************************/
  private static void saveStoredData() {
    //Debug.print("myState: "+myState + ".");
/*
    try {

      if (myRS != null) {

        byte[] data;

        int nRecord = 1;
        for (int i = -2; i < _gameStateVector.size(); i += 2) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

//                    Debug.print("Write @"+i);

          if (i == -2) { /// trick to save myState
            dos.writeUTF(myState);
          } else {
            dos.writeUTF((String) _gameStateVector.elementAt(i));
            dos.write((byte[]) _gameStateVector.elementAt(i + 1));
//                        Debug.print("Write game " + _gameStateVector.elementAt(i)
//                            +" : "+((byte[]) _gameStateVector.elementAt(i+1)).length+" bytes");
          }
          data = baos.toByteArray();

          if (nRecord <= myRS.getNumRecords()) {
            myRS.setRecord(nRecord, data, 0, data.length);
          } else {
            myRS.addRecord(data, 0, data.length);
          }
          nRecord++;
        }

        if (closeOnSave) {
          try {
            myRS.closeRecordStore();
          } finally {
            myRS = null;
          }
        }
      }
    } catch (Exception e) {
      Debug.print("Error writing RMS: " + e);
//            e.printStackTrace();
    }
    */
  }
}
