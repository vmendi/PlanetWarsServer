package unusual;

import java.util.ArrayList;

public final class ProductionQueue
{
	public ProductionQueue()
	{
	}
	
	public void Enqueu(IBuildable item)
	{
		mQueue.add(item);
	}
	
	public IBuildable Produce(int productionPoints)
	{
		if (mQueue.size() == 0)
			return null;
		
		mProductionPoints += productionPoints;
		
		IBuildable ret  = null;
		int currItemCost = mQueue.get(0).GetCost();
				
		if (mProductionPoints >= currItemCost)
		{
			// Los puntos excedentes se pierden por diseño.
			mProductionPoints = 0;
			
			// Este elemento ya está producido: lo retornamos
			ret = mQueue.remove(0);
		}
		
		return ret;
	}
	
	public ArrayList<IBuildable> GetQueue() { return mQueue; }
	public int GetProductionPoints() { return mProductionPoints; }
	
	private int mProductionPoints = 0;
	private ArrayList<IBuildable> mQueue = new ArrayList<IBuildable>();
}
