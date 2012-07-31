package unusual;

//import it.gotoandplay.smartfoxserver.db.*;
//import it.gotoandplay.smartfoxserver.exceptions.*;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import it.gotoandplay.smartfoxserver.data.*;
import it.gotoandplay.smartfoxserver.extensions.*;
import it.gotoandplay.smartfoxserver.lib.ActionscriptObject;
import it.gotoandplay.smartfoxserver.util.scheduling.ITaskHandler;
import it.gotoandplay.smartfoxserver.util.scheduling.Scheduler;
import it.gotoandplay.smartfoxserver.util.scheduling.Task;
import it.gotoandplay.smartfoxserver.events.InternalEventObject;

import net.sf.json.JSONArray;

import org.json.JSONObject; 

// This is the class that will handle the callbacks from the Scheduler
class CivUpdater implements ITaskHandler
{
    public void doTask(Task task) throws Exception
    {
        String id = (String) task.id;
        
        if (id.equals("GlobalUpdate"))
        	mPlanetWars.UpdateGlobal();
    }
    
    public CivUpdater(PlanetWars planetWars)
    {
    	mPlanetWars = planetWars;
    }
    
    private PlanetWars mPlanetWars;
}


public class PlanetWars extends AbstractExtension
{
	private Scheduler mScheduler;
	private CivUpdater mCivUpdater;
	private Planet mPlanet;
	
	private ExtensionHelper mExtensionHelper;
	private Zone mCurrentZone;
	private Room mCurrentRoom;
	
	synchronized public void init()
	{		
		trace("Extension PlanetWars initialized");
		
		mExtensionHelper = ExtensionHelper.instance();
		mCurrentZone = mExtensionHelper.getZone(this.getOwnerZone()); 
		mCurrentRoom = mCurrentZone.getRoomByName(this.getOwnerRoom());
		
		try {
			GameParams.Init();
		}
		catch (Exception e) {
			trace("!!!!!! EXCEPTION !!!!!!");
		}
		
        mPlanet = new Planet();
        mCivUpdater = new CivUpdater(this);

        mScheduler = new Scheduler();
        Task globalUpdateTask = new Task("GlobalUpdate");        
        mScheduler.addScheduledTask(globalUpdateTask, 2, true, mCivUpdater);
        mScheduler.startService();
	}
	
	
	/**
	* Always make sure to release resources like setInterval(s)
	* open files etc in this method.
	*/
	synchronized public void destroy()
	{
		mScheduler.stopService();
		
		trace("Extension PlanetWars destroyed");
	}
	
	public void handleRequest(String cmd, ActionscriptObject ao, User u, int fromRoom) {}
	public void handleRequest(String cmd, String params[], User u, int fromRoom) {}
	
	
	synchronized public void handleRequest(String cmd, JSONObject jso, User user, int fromRoom)
	{
		try
		{
			if (cmd.equals("MethodCall"))
				ProcessMethodCall(jso, user);
			else
				throw new Exception("Unrecognized request");
		}
		catch (Exception e) {
			trace("!!!!!! EXCEPTION !!!!!!\n"+e.getMessage());
		}
	}
	
	private void ProcessMethodCall(JSONObject jso, User user) throws Exception
	{
		JSONArray returnCommandQueue = new JSONArray();
		
		// TODO: Habría que comprobar que el UUID pertenece al User. Esta y todas las demás comprobaciones de seguridad lo más
		//       limpio sería hacerlo dentro de la propia función a la que llamamos con el invoke(). Entonces hará falta añadir 
		//       aquí el user.getUserID al jso
		Object target = mPlanet.FindByUUID(jso.getInt("UUID"));
		Class<? extends Object> targetClass = target.getClass();
		String methodName = jso.getString("Method"); 
		
		Method targetMethod = targetClass.getMethod(methodName, new Class[] {JSONArray.class, JSONObject.class});
		targetMethod.invoke(target, returnCommandQueue, jso);
		
		// TODO: La respuesta la enviamos a todo el mundo, aquí habrá que distinguir alianzas, o si simplemente a los demás no les 
		//       interesa algo interno mio (cola de producción de mi ciudad, por ejemplo...)
		SendCommandQueueToAll(returnCommandQueue);
	}

	synchronized public void handleInternalEvent(InternalEventObject evt)
	{		
		try {
			String evtName = evt.getEventName();
			
			if (evtName.equals(InternalEventObject.EVENT_JOIN))
			{
				HandleJoin((User)evt.getObject("user"));	
			}
			else
			if (evtName.equals(InternalEventObject.EVENT_USER_LOST) || evtName.equals(InternalEventObject.EVENT_USER_EXIT))
			{
				HandleDisconnection(Integer.valueOf(evt.getParam("uid")));
			}
		}
		catch (Exception e) {
			trace("!!!!!! EXCEPTION !!!!!!");
		}
	}
	
	synchronized public void UpdateGlobal() throws Exception
    {
		// TODO: Todos conocen todo: De momento mandamos la actualización a todos los players, sin distinguir si son aliados...
		JSONArray updateCommandQueue = new JSONArray();    		
		mPlanet.OnTurn(updateCommandQueue);
		SendCommandQueueToAll(updateCommandQueue);		
    }
	
	private void HandleDisconnection(int userID)
	{
		// Adios, dejamos de actualizar en tiempo real
		mPlanet.GetPlayerByConnectionID(userID).Disconnect();
	}
	
	private void HandleJoin(User user) throws Exception
	{		
		Player theNewPlayer = mPlanet.GetPlayerByName(user.getName());
		
		if (theNewPlayer == null)
		{
			theNewPlayer = mPlanet.CreatePlayer(user.getName());

			// Este player está online en tiempo real a partir de ahora
			theNewPlayer.Connect(user.getUserId());
			
			// Mandamos a todos los que están conectados menos al nuevo player
			BroadcastNewPlayerCreated(theNewPlayer);
		}
		else
		{
			theNewPlayer.Connect(user.getUserId());
		}
		
		// Al nuevo player le mandamos el planeta completo
		SendNewPlanet(user);
		
		trace("Player " + user.getName() + " connected at planet " + this.getOwnerRoom());	 
	}
		
	private void SendNewPlanet(User user) throws Exception
	{
		JSONArray updateCommandQueue = new JSONArray();			
		mPlanet.GeneratePlanetCreationJSON(updateCommandQueue);
		SendCommandQueueTo(updateCommandQueue, user);
	}
	
	private void BroadcastNewPlayerCreated(Player theNewPlayer) throws Exception
	{
		JSONArray commandQueue = new JSONArray();			
		theNewPlayer.GeneratePlayerCreationJSON(commandQueue);
		mPlanet.GetTerrain().GenerateContinentJSON(theNewPlayer.GetContinentID(), commandQueue);		
		SendCommandQueueToAllExcept(commandQueue, mExtensionHelper.getUserById(theNewPlayer.GetSmartFoxUserID()));			
	}
	
	
	private void SendCommandQueueTo(JSONArray commandQueue, User user) throws Exception
	{
		if (commandQueue.size() == 0)
				return;
		
		JSONObject commandPackage = new JSONObject();
		commandPackage.put("_cmd", "BatchUpdate");
		commandPackage.put("Commands", commandQueue);
		
		LinkedList<SocketChannel> recipientList = new LinkedList<SocketChannel>();
		recipientList.add(user.getChannel());

		sendResponse(commandPackage, -1, null, recipientList);
	}
	
	private void SendCommandQueueToAll(JSONArray commandQueue) throws Exception
	{
		if (commandQueue.size() == 0)
			return;
		
		JSONObject commandPackage = new JSONObject();
		commandPackage.put("_cmd", "BatchUpdate");
		commandPackage.put("Commands", commandQueue);
		
		LinkedList<SocketChannel> recipientList = new LinkedList<SocketChannel>();
		
		for (User u : mCurrentRoom.getAllUsers())
			recipientList.add(u.getChannel());
		
		sendResponse(commandPackage, -1, null, recipientList);
	}
	
	private void SendCommandQueueToAllExcept(JSONArray commandQueue, User user) throws Exception
	{
		if (commandQueue.size() == 0)
			return;
		
		JSONObject commandPackage = new JSONObject();
		commandPackage.put("_cmd", "BatchUpdate");
		commandPackage.put("Commands", commandQueue);
		
		LinkedList<SocketChannel> recipientList = new LinkedList<SocketChannel>();
	
		for (User u : mCurrentRoom.getAllUsersButOne(user))
			recipientList.add(u.getChannel());
		
		sendResponse(commandPackage, -1, null, recipientList);
	}
}