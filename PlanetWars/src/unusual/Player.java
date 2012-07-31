package unusual;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import net.sf.json.JSONArray;

import org.json.JSONObject;

public class Player
{
	private int mUUID;
	
	private int mGold;
	private int mScience;
	
	private Vector<City> mCities;
	private int mContinentID;
	
	private String mUserName;
	private int mSmartFoxUserID;		// -1 cuando no el player no esta conectado
	
	private Planet mPlanet;
	
	public Player(Planet thePlanet, String name, int continentID)
	{
		mPlanet = thePlanet;
		mUUID = mPlanet.RegisterNewUUID(this);
		mUserName = name;
		mSmartFoxUserID = -1;
		mGold = 0;
		mScience = 0;
		mCities = new Vector<City>();
		mContinentID = continentID;
		
		Rectangle playerContinent = mPlanet.GetTerrain().GetContinentRectangle(continentID);
		
		int capitalX = (int)playerContinent.getCenterX();
		int capitalY = (int)playerContinent.getCenterY();
		
		City capital = new City(this, new Point(capitalX, capitalY), true);
		mCities.add(capital);
	}
	
	public int GetSmartFoxUserID() { return mSmartFoxUserID; }
	public Planet GetPlanet() { return mPlanet; }
	public int GetUUID() { return mUUID; }
	public String GetUserName() { return mUserName; }

	// Continente inicial en el que sale el player
	public int GetContinentID() { return mContinentID; }
	
	public void Connect(int userID)
	{
		mSmartFoxUserID = userID;
	}
	
	public void Disconnect()
	{
		mSmartFoxUserID = -1;
	}
	
	
	public void GeneratePlayerCreationJSON(JSONArray updateCommandQueue) throws Exception
	{
		JSONObject ret = new JSONObject();
		ret.put("Method", "New");
		ret.put("Type", "Player");
		ret.put("MovieClip", "Player");
		ret.put("UUID", GetUUID());
		
		ret.put("UserID", mSmartFoxUserID);
		ret.put("ContinentID", mContinentID);
		
		ret.put("Gold", mGold);
		ret.put("Science", mScience);
				
		ret.put("GoldPerTurn", GetGoldPerTurn());
		ret.put("SciencePerTurn", GetSciencePerTurn());
		ret.put("LaborPerTurn", GetLaborPerTurn());
		
		updateCommandQueue.add(ret);
								
		for (int c=0; c < mCities.size(); c++)
			mCities.get(c).GenerateCityCreationJSON(updateCommandQueue);
	}
	
	public void OnTurn(JSONArray responseCommandQueue) throws Exception
	{
		mGold += GetGoldPerTurn();
		mScience += GetSciencePerTurn();
		
		if (mScience >= GetScienceToNextDiscovery())
		{
			mScience = mScience - GetScienceToNextDiscovery();
			Discover();
		}
		
		responseCommandQueue.add(GetJSONTurnUpdate());

		for (int c=0; c < mCities.size(); c++)
			mCities.get(c).OnTurn(responseCommandQueue);
	}
	
	private JSONObject GetJSONTurnUpdate() throws Exception
	{
		JSONObject retJSON = new JSONObject();
		
		retJSON.put("Method", "FromServerTurnUpdate");
		retJSON.put("UUID", mUUID);
		
		retJSON.put("Gold", mGold);
		retJSON.put("Science", mScience);
		
		retJSON.put("GoldPerTurn", GetGoldPerTurn());
		retJSON.put("SciencePerTurn", GetSciencePerTurn());
		retJSON.put("LaborPerTurn", GetLaborPerTurn());
		
		return retJSON;
	}
	
	public int GetScienceToNextDiscovery()
	{
		return 1000000;
	}
	
	private void Discover() throws Exception
	{
		throw new Exception("Not ready");
	}
	
	public int GetGoldPerTurn()
	{
		int totalGold = 0;
		for (City city : mCities)
			totalGold += city.GetGoldPerTurn();
		return totalGold;
	}
	
	public int GetSciencePerTurn()
	{
		int totalScience = 0;
		for (City city : mCities)
			totalScience += city.GetSciencePerTurn();
		return totalScience;
	}
	
	public int GetLaborPerTurn()
	{
		int totalLabor = 0;
		for (City city : mCities)
			totalLabor += city.GetLaborPerTurn();
		return totalLabor;
	}
	
	public void CreateCity(JSONArray responseCommandQueue, JSONObject fromClient) throws Exception
	{
		int worldPosX = fromClient.getInt("WorldPosX");
		int worldPosY = fromClient.getInt("WorldPosY");
		
		City newCity = new City(this, new Point(worldPosX, worldPosY), false);
		mCities.add(newCity);
		
		newCity.GenerateCityCreationJSON(responseCommandQueue);
	}
}