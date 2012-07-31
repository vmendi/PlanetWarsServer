package unusual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GameParams 
{
	static public ArrayList<City.CityParams> CityParams;
	static public ArrayList<Building> Buildings;
	static public ArrayList<Unit> Units;
	
	static public void Init() throws Exception
	{
		CityParams = ReadCityFile();
		Buildings = ReadBuildingsFile();
		Units = ReadUnitsFile();
	}
	
	static private ArrayList<City.CityParams> ReadCityFile() throws Exception
	{
		ArrayList<City.CityParams> ret = new ArrayList<City.CityParams>();
		
		ArrayList<ArrayList<String>> contents = ReadCSVFile("_PlanetWarsCFG/City.csv");
		contents.remove(0);
		
		for (int c=0; c < contents.size(); c++)
		{
			if (c+1 != Integer.parseInt(contents.get(c).get(0)))
				throw new Exception("Params table error");
			
			City.CityParams params = new City.CityParams();
			params.Size = Integer.parseInt(contents.get(c).get(0));
			params.FoodToGrow = Integer.parseInt(contents.get(c).get(1));
			params.SciencePerTurn = Integer.parseInt(contents.get(c).get(2));
			params.GoldPerTurn = Integer.parseInt(contents.get(c).get(3));			
			
			ret.add(params);
		}
		
		return ret;
	}
	
	static private ArrayList<Unit> ReadUnitsFile() throws Exception
	{
		ArrayList<ArrayList<String>> contents = ReadCSVFile("_PlanetWarsCFG/Units.csv");
		contents.remove(0);
		
		ArrayList<Unit> ret = new ArrayList<Unit>();
		
		for (int c=0; c < contents.size(); c++)
		{
			Unit newUnit = new Unit(contents.get(c).get(0));
			newUnit.ShortDesc = contents.get(c).get(1);
			newUnit.LongDesc = contents.get(c).get(2);
			newUnit.Type = contents.get(c).get(3);
			newUnit.Cost = Integer.parseInt(contents.get(c).get(10));
			ret.add(newUnit);
		}
		
		return ret;
	}
	
	static private ArrayList<Building> ReadBuildingsFile() throws Exception
	{
		ArrayList<ArrayList<String>> contents = ReadCSVFile("_PlanetWarsCFG/Buildings.csv");
		contents.remove(0);
		
		ArrayList<Building> ret = new ArrayList<Building>();
		
		for (int c=0; c < contents.size(); c++)
		{
			Building newBuilding = new Building(contents.get(c).get(0));
			newBuilding.LongDesc = contents.get(c).get(1);
			newBuilding.Cost = Integer.parseInt(contents.get(c).get(6));
			ret.add(newBuilding);
		}
		
		return ret;
	}
	
	static private ArrayList<ArrayList<String>> ReadCSVFile(String fileName) throws Exception
	{
		File theFile = new File(fileName);
		
		BufferedReader bufRdr  = new BufferedReader(new FileReader(theFile));
		String line = null;
				
		ArrayList<ArrayList <String>> contents = new ArrayList<ArrayList<String>>();
		
		while((line = bufRdr.readLine()) != null)
		{
			contents.add(new ArrayList<String>());
			
			StringTokenizer st = new StringTokenizer(line, ",");
			while (st.hasMoreTokens())
			{
				contents.get(contents.size()-1).add(st.nextToken());
			}
		}
		bufRdr.close();
		
		return contents;
	}
}
