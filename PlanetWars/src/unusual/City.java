package unusual;

import java.awt.Point;
import java.util.ArrayList;

import net.sf.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

public class City
{
	static public class CityParams {
		public int Size=-1;
		public int FoodToGrow=-1;
		public int SciencePerTurn=-1;
		public int GoldPerTurn=-1;
	}

	private int mUUID;
	private int mSize;
	private int mFoodOnStore;
	private Point mPos;
	private boolean mIsCapital;
	
	private ProductionQueue mProductionQueue;
	private ArrayList<Unit> mUnits;
	private ArrayList<Building> mBuildings;
	
	private Player mPlayer;
	
	public int GetUUID() { return mUUID; }
	
	public boolean IsCapital() { return mIsCapital; }
	
	public Point GetPos() { return mPos; }	
	public int GetSize() { return mSize; }
	
	public int GetFoodPerTurn() { return mSize; }
	public int GetLaborPerTurn() { return mSize; }
	public int GetCurrentFoodOnStore() { return mFoodOnStore; }
	
	public int GetFoodToGrowToNextSize() { return GameParams.CityParams.get(CapSizeAndZeroIndex()).FoodToGrow; }	
	public int GetSciencePerTurn() { return GameParams.CityParams.get(CapSizeAndZeroIndex()).SciencePerTurn; }
	public int GetGoldPerTurn() { return GameParams.CityParams.get(CapSizeAndZeroIndex()).GoldPerTurn; }
	
	
	private int CapSizeAndZeroIndex() 
	{ 
		if (mSize-1 < GameParams.CityParams.size())
			return mSize-1;
		else
			return GameParams.CityParams.size()-1; 
	}
		
	
	public City(Player player, Point worldPos, boolean isCapital)
	{
		mUUID = player.GetPlanet().RegisterNewUUID(this);
		mSize = 1;
		mPos = worldPos;
		mIsCapital = isCapital;
		
		mProductionQueue = new ProductionQueue();
		mBuildings = new ArrayList<Building>();
		mUnits = new ArrayList<Unit>();
		
		mPlayer = player;
	}
	
	public void GenerateCityCreationJSON(JSONArray responseCommandQueue) throws Exception
	{
		JSONObject ret = new JSONObject();
		
		ret.put("Method", "New");
		ret.put("Type", "City");
		ret.put("MovieClip", "AncientCity");
		ret.put("UUID", GetUUID());
		
		ret.put("PlayerOwnerUUID", mPlayer.GetUUID());
		ret.put("Size", mSize);
		ret.put("FoodOnStore", mFoodOnStore);
		ret.put("PosX", mPos.x);
		ret.put("PosY", mPos.y);
		ret.put("IsCapital", mIsCapital);
		
		JSONArray units = new JSONArray();
		for (Unit unit : mUnits)
			units.add(unit.GetCountAndName());
		ret.put("Units", units);
		
		JSONArray buildings = new JSONArray();
		for (Building building : mBuildings)
			buildings.add(building.GetName());
		ret.put("Buildings", buildings);
		
		responseCommandQueue.add(ret);
		responseCommandQueue.add(GetProductionQueueJSON());
	}
	
	public void OnTurn(JSONArray responseCommandQueue) throws Exception
	{
		mFoodOnStore += GetFoodPerTurn();
		
		if (mFoodOnStore >= GetFoodToGrowToNextSize())
		{
			mFoodOnStore = mFoodOnStore - GetFoodToGrowToNextSize();
			mSize++;
			System.out.println("Crecemos:" + String.valueOf(mSize));
		}
		
		IBuildable newBuildable = mProductionQueue.Produce(GetLaborPerTurn());
		
		if (newBuildable != null)
		{
			System.out.println("Construido:" + newBuildable.GetName());
			
			if (newBuildable.getClass().equals(Building.class))
			{
				mBuildings.add((Building)newBuildable);
			}
			else
			{
				Unit theUnit = (Unit)newBuildable;
				int alreadyThereIndex = mUnits.indexOf(theUnit);
				if (alreadyThereIndex != -1)
					newBuildable = mUnits.get(alreadyThereIndex).Add(theUnit);
				else
					mUnits.add(theUnit);
			}
			
			responseCommandQueue.add(GetNewBuildedJSON(newBuildable));
		}
		
		responseCommandQueue.add(GetTurnUpdateJSON());
		responseCommandQueue.add(GetProductionQueueJSON());
	}
	
	private JSONObject GetNewBuildedJSON(IBuildable newBuildable) throws Exception
	{
		JSONObject ret = new JSONObject();
		ret.put("Method", "FromServerNewBuilded");
		ret.put("UUID", mUUID);
		
		ret.put("Name", newBuildable.GetName());
		
		if (newBuildable.getClass().equals(Unit.class))
			ret.put("CountAndName", ((Unit)newBuildable).GetCountAndName());
	
		return ret;
	}
	
	private JSONObject GetProductionQueueJSON() throws Exception
	{
		JSONObject ret = new JSONObject();
		
		ret.put("Method", "FromServerProductionQueueUpdated");
		ret.put("UUID", mUUID);
		
		JSONArray namesInQueue = new JSONArray();
		
		for (IBuildable b : mProductionQueue.GetQueue())
			namesInQueue.add(b.GetName());
		
		ret.put("Names", namesInQueue);
		ret.put("Points", mProductionQueue.GetProductionPoints());
		
		return ret;
	}
	
	private JSONObject GetTurnUpdateJSON() throws JSONException
	{
		JSONObject ret = new JSONObject();
		ret.put("Method", "FromServerTurnUpdate");
		ret.put("UUID", mUUID);
		
		ret.put("Size", mSize);
		ret.put("FoodOnStore", mFoodOnStore);
		
		return ret;
	}
	
	public void AddBuildableToQueue(JSONArray responseCommandQueue, JSONObject fromClient) throws Exception
	{
		String productionItemName = fromClient.getString("Name");
		
		IBuildable daBuildable = CreateBuildable(productionItemName);
		mProductionQueue.Enqueu(daBuildable);
		
		responseCommandQueue.add(GetProductionQueueJSON());
	}
	
	private IBuildable CreateBuildable(String name) throws Exception
	{
		IBuildable ret = Building.CreateBuilding(name);
		
		if (ret == null)
			ret = Unit.CreateUnitFromGameParams(name);
				
		if (ret == null)
			throw new Exception("Buildable not known");
		
		return ret;		
	}
}
