package unusual;

import java.util.Hashtable;
import java.util.Vector;

import net.sf.json.JSONArray;
import org.json.JSONObject;


public class Planet
{
	private Vector<Player> mPlayers = new Vector<Player>();
	private Terrain mTerrain;
	
	private int mUUIDGenerator = 1;
	private Hashtable<Integer, Object> mAllUUIDs = new Hashtable<Integer, Object>(100);
	
	
	public int GetUUID() { return 0; }	
	public Terrain GetTerrain() { return mTerrain; }
	
	
	public Planet()
	{
		mTerrain = new Terrain(this);
	}
	
	public int RegisterNewUUID(Object registered)
	{ 
		mUUIDGenerator++;		
		mAllUUIDs.put(mUUIDGenerator, registered);		
		return mUUIDGenerator;
	}
	
	public Object FindByUUID(int uuid)
	{
		return mAllUUIDs.get(uuid);
	}
			
	public Player GetPlayerByName(String name)
	{
		for (int c=0; c < mPlayers.size(); c++)
			if (mPlayers.get(c).GetUserName().equals(name))
				return mPlayers.get(c);
		return null;
	}
	
	public Player GetPlayerByConnectionID(int id)
	{
		for (int c=0; c < mPlayers.size(); c++)
			if (mPlayers.get(c).GetSmartFoxUserID() == id)
				return mPlayers.get(c);
		return null;
	}
	
	public Player CreatePlayer(String name) throws Exception
	{
		assert(GetPlayerByName(name) == null);
		
		System.out.println("Creating new player: " + name);
				
		int continentID = mTerrain.CreateNewContinentAutomatic();

		Player newPlayer = new Player(this, name, continentID);		
		mPlayers.add(newPlayer);
		
		return newPlayer;
	}
	
	public void GeneratePlanetCreationJSON(JSONArray updateCommandQueue) throws Exception
	{
		// Garantizamos que el terreno ya existe cuando creamos el planeta
		mTerrain.GenerateTerrainCompleteJSON(updateCommandQueue);
		
		JSONObject ret = new JSONObject();
		
		ret.put("Method", "New");
		ret.put("Type", "Planet");
		ret.put("MovieClip", "Planet");
		ret.put("UUID", GetUUID());
	
		updateCommandQueue.add(ret);
				
		for (int c=0; c < mPlayers.size(); c++)
			mPlayers.get(c).GeneratePlayerCreationJSON(updateCommandQueue);
		
		JSONObject ready = new JSONObject();
		ready.put("Method", "FromServerPlanetReady");
		ready.put("UUID", GetUUID());
		updateCommandQueue.add(ready);
	}
	
	public void OnTurn(JSONArray updateCommandQueue) throws Exception
	{		
		for (int c=0; c < mPlayers.size(); c++)
			mPlayers.get(c).OnTurn(updateCommandQueue);
		
		// El planeta manda End para indicar que no hay nada más este turno
		JSONObject retJSON = new JSONObject();
		retJSON.put("Method", "FromServerTurnUpdateEnd");
		retJSON.put("UUID", GetUUID());		
		updateCommandQueue.add(retJSON);
	}
}