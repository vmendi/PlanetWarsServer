package unusual;


public class Unit implements IBuildable 
{	
	public String Name = "NULL";
	public String Type = "NULL";		// "Ground", "Air"....
	public String ShortDesc = "NULL";
	public String LongDesc = "NULL";
	public int 	  Cost = -1;			// In labor points
		
	public Unit(String name)
	{
		Name = name;
	}
	
	public int GetCost() {	return Cost; }	
	public String GetName()	{ return Name;	}
	
	public int  GetUnitCount() { return mUnitCount; }
	public void SetUnitCount(int n) { mUnitCount = n; }
	
	public Unit Add(Unit other) throws Exception
	{ 
		if (!other.Name.equals(this.Name))
			throw new Exception("Adding oranges and bananas");
		
		mUnitCount += other.mUnitCount;
		
		return this;
	}
	
	public String GetCountAndName() 
	{
		// TODO: Mejor versión del plural
		String plural = (mUnitCount > 1)? "s" : "";
		return String.valueOf(mUnitCount) + " " + Name + plural; 
	}
	
	@Override
	public int hashCode() { return Name.hashCode(); }
	@Override
	public boolean equals(Object other) {	return ((Unit)other).Name.equals(this.Name); }
				
	static public Unit CreateUnitFromGameParams(String name)
	{
		Unit ret = null;
		
		for (Unit b : GameParams.Units)
		{
			if (b.GetName().equals(name))
			{
				ret = new Unit(name);
				ret.Type = b.Type;
				ret.ShortDesc = b.ShortDesc;
				ret.LongDesc = b.LongDesc;
				ret.Cost = b.Cost;
				break;
			}
		}
		
		return ret;
	}
	
	private int mUnitCount = 1;
}
