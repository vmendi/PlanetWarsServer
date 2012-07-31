package unusual;

import java.awt.Rectangle;
import java.awt.Point;
import java.util.HashMap;
import java.util.Vector;

import net.sf.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

class TerrainCell
{
	public char TerrainType;
	
	public TerrainCell(char terrainType)
	{
		TerrainType = terrainType;
	}
}

public class Terrain
{
	public static int CONTINENT_LENGTH = 20;
	
	private Point mOrder[] = { new Point(0, 0), new Point(-1, 0), new Point(1, 0), new Point(0, -1), new Point(0, 1), 
							   new Point(-1, -1), new Point(1, 1), new Point(-1, 1), new Point(1, -1) };
	
	private HashMap<Integer, TerrainCell> mTerrainCells = new HashMap<Integer, TerrainCell>();	
	private Vector<TerrainCell[][]> mContinents = new Vector<TerrainCell[][]>();
	
	private int mUUID = -1;
	
	public Terrain(Planet thePlanet)
	{
		mUUID = thePlanet.RegisterNewUUID(this);
	}
	
	public int GetUUID() { return mUUID; }
	
	public void GenerateTerrainCompleteJSON(JSONArray updateCommandQueue) throws Exception
	{
		JSONObject ret = new JSONObject();
		
		ret.put("Method", "New");
		ret.put("Type", "Terrain");
		ret.put("MovieClip", "TerrainGenerator");
		ret.put("UUID", GetUUID());
		
		ret.put("ContinentLength", CONTINENT_LENGTH);

		JSONArray order = new JSONArray();
		ret.put("Order", order);
		
		for (Point p : mOrder)
		{
			JSONObject newP = new JSONObject();
			newP.put("X", p.x);
			newP.put("Y", p.y);
			order.add(newP);
		}

		JSONArray continents = new JSONArray();
		ret.put("Continents", continents);
		
		for (int c=0; c < mContinents.size(); c++)
			continents.add(GetContinentJSON(c));
		
		updateCommandQueue.add(ret);
	}
	
	public void GenerateContinentJSON(int continentID, JSONArray updateCommandQueue) throws Exception
	{
		TerrainCell[][] continent = mContinents.get(continentID);
		
		JSONArray cells = new JSONArray();
		
		for (int c=0; c < CONTINENT_LENGTH; c++)
			for (int d=0; d < CONTINENT_LENGTH; d++)
				cells.add(continent[c][d].TerrainType);
		
		JSONObject ret = new JSONObject();
		
		ret.put("Method", "ServerMergeContinent");
		ret.put("UUID", GetUUID());
		
		ret.put("Cells", cells);
		ret.put("ContinentID", continentID);
		
		updateCommandQueue.add(ret);
	}
		
	public JSONObject GetContinentJSON(int continentID) throws JSONException
	{
		TerrainCell[][] continent = mContinents.get(continentID);
		
		JSONArray cells = new JSONArray();
		
		for (int c=0; c < CONTINENT_LENGTH; c++)
			for (int d=0; d < CONTINENT_LENGTH; d++)
				cells.add(continent[c][d].TerrainType);
		
		JSONObject ret = new JSONObject();
		ret.put("Cells", cells);
		ret.put("ContinentID", continentID);
		return ret;
	}
	
	public Rectangle GetContinentRectangle(int continentID)
	{
		return new Rectangle(mOrder[continentID].x*CONTINENT_LENGTH, 
				  			 mOrder[continentID].y*CONTINENT_LENGTH, 
				  			 CONTINENT_LENGTH, CONTINENT_LENGTH);
	}
	
	/**
	 * 
	 * @return ID del nuevo continente. La (x,y) del rectángulo es arriba-izquierda!
	 */
	public int CreateNewContinentAutomatic() throws Exception
	{
		if (mContinents.size() == mOrder.length)
			throw new Exception("Terrain full!! You should check it before.");
		
		int continentID = mContinents.size();
		
		TerrainCell[][] continentTerrain = CreateContinent();	
		mContinents.add(continentTerrain);
		
		// La (x, y) del rectangulo es esquina sup-izq
		Rectangle ret = new Rectangle(mOrder[continentID].x*CONTINENT_LENGTH, 
									  mOrder[continentID].y*CONTINENT_LENGTH, 
					  		 		  CONTINENT_LENGTH, CONTINENT_LENGTH);
		
		int maxX = (int)ret.getMaxX();
		int maxY = (int)ret.getMaxY();
		
		for (int c=ret.y; c < maxY; c++)
		{
			for (int d=ret.x; d < maxX; d++)
			{
				// La coordenada x va arriba en el hash
				mTerrainCells.put((d << 16) | (c & 0x0000FFFF), continentTerrain[c-ret.y][d-ret.x]);
			}
		}
		
		return continentID;
	}
	
	private TerrainCell[][] CreateContinent()
	{
		TerrainCell cCell = new TerrainCell('c');
		TerrainCell gCell = new TerrainCell('g');
		
		TerrainCell[][] continentTerrain = new TerrainCell[CONTINENT_LENGTH][CONTINENT_LENGTH];
		
		for (int c=0; c < CONTINENT_LENGTH; c++)
			for (int d=0; d < CONTINENT_LENGTH; d++)
				continentTerrain[c][d] = cCell;
		
		for (int c=3; c < 17; c++)
			for (int d=3; d < 17; d++)
				continentTerrain[c][d] = gCell;
		
		continentTerrain[16][16] = cCell;
		continentTerrain[16][15] = cCell;
		continentTerrain[15][15] = cCell;
		continentTerrain[15][14] = cCell;
		
		continentTerrain[3][16] = cCell;
		continentTerrain[3][15] = cCell;
		continentTerrain[3][14] = cCell;
		continentTerrain[4][15] = cCell;
		continentTerrain[4][14] = cCell;
		continentTerrain[4][13] = cCell;
		continentTerrain[5][14] = cCell;
		
		continentTerrain[13][6] = cCell;
		continentTerrain[13][7] = cCell;
				
		return continentTerrain;
	}
}
