package unusual;


public class Building implements IBuildable
{	
	public String Name = "NULL";
	public String LongDesc = "NULL";
	public int    Cost = -1;
	
	public Building(String name)
	{
		Name = name;
	}
	public int GetCost() 
	{
		return Cost;
	}

	public String GetName() 
	{
		return Name;
	}
	
	static public Building CreateBuilding(String name)
	{
		Building ret = null;
		
		for (Building b : GameParams.Buildings)
		{
			if (b.GetName().equals(name))
			{
				ret = new Building(name);
				ret.LongDesc = b.LongDesc;
				ret.Cost = b.Cost;
				break;
			}
		}
		
		return ret;
	}
}
