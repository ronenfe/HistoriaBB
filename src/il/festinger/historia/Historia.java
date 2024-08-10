package il.festinger.historia;


import java.util.Date;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.system.RuntimeStore;

public class Historia extends UiApplication implements ScreenClosedListener 
{
	private static PersistentObject configObject;	// for working with persistence
	public static Vector keys;	// saved settings keys
    public static int daysPassed;
	public static int FIRSTTIME = 0;
	public static int TIMEFORMAT = 1;
	public static int KEYNUMBER = 2;
	public static int LASTCALL = 3;
	public static int NUMBEROFCALLS = 4;
	public static int DATEFORMAT = 5;
	public static int MONITOR = 6;
	public static int LAYOUT = 7;
	public static int FONTSIZE = 8;
	public static int LOCATION = 9;
	public static int REGISTERED = 10;
	public static int CALENDAR = 11;
	public static int CALLTYPETOLOG = 12;
	public static int NUMBERSTOLOG = 13;
	public static int LOGINCOMING = 14;
	public static int LOGOUTGOING = 15;
	public static int LOGMISSED = 16;
	public static int LOGUNANSWERED = 17;
	
	private static int NUMBEROFKEYS = 18;
	
	private static final long MY_VECTOR_ID = 0xa6310b065830922eL;

	public static Vector getVector()
	{
		Vector _vector;
		RuntimeStore rs = RuntimeStore.getRuntimeStore();
		synchronized( rs )
		{
			_vector = (Vector) rs.get( MY_VECTOR_ID );

			if( _vector == null )
			{
				_vector = new Vector();
				rs.put( MY_VECTOR_ID, _vector );
			}
		}
		return _vector;
	}
//	public static Vector vectorCellID = new Vector();
	public static Vector vectorNumbers = new Vector();
	
	public static String targetFolder;	// folder path
	public static CallLogListener callLogListener;	// monitor calls
	public static AddPhoneListener phone_handler;	// monitor calls
	private Main main;	// main window
	private LicenseScreen license; //license window
	public static void main(String[] args)	//starting point of program
	{
		try
		{
			daysPassed = (int)((new Date().getTime() - Long.parseLong(Historia.keys.elementAt(Historia.FIRSTTIME).toString()))/86400000);	// time software was ran for the first time
			 if ( args != null && args.length > 0 && args[0].equals("service") )	// start the background service
			 {
				 GPRSInfo.getCellInfo().getCellId();	// to make security warning appear at first launch
				 callLogListener = new CallLogListener();	// monitor calls
				 	phone_handler = new AddPhoneListener();
					Phone.addPhoneListener(phone_handler);
				 if (Integer.parseInt((String)keys.elementAt(MONITOR)) == 1)
				 {
						callLogListener.addListener();
				 }
		     } 
			 else
			 {
				// code to initialize the gui app
			 	Historia historia = new Historia();	// create instance of historia class
		 		historia.enterEventDispatcher();	// needed by blackberry
		     }
		}
		catch(Exception e)
		{
			System.exit(1);
		}

	};
	
	public Historia()
	{
		try
		{
	        if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)  // if software is not registered display license form
	        {
	        	license = new LicenseScreen(this);
	    		pushScreen(license);
	        }
	        else
	        {
	        	runMain();
	        }
		}
		catch(final Exception e)
		{
	    	UiApplication.getUiApplication().invokeLater (new Runnable() {
			    public void run()
			    {
			    	Dialog.alert("Error in Historia. if error persists contact support.\n" + e.getMessage());
			    }
			});
			System.exit(1);
		}
	};
	
	public static void saveConfiguration()	// save configuration
	{
		try
		{
			synchronized(configObject)
			{
				configObject.setContents(keys);
				configObject.commit();
			}
		}
		catch(final Exception e)
		{
	    	UiApplication.getUiApplication().invokeLater (new Runnable() {
			    public void run()
			    {
			    	Dialog.alert("Error saving settings, if error persists contact support.\n" + e.getMessage());
			    }
			});
			System.exit(1);		
		}
	};

	static
	{
		try
		{
			configObject = PersistentStore.getPersistentObject(0xa6310b065830922eL); // generated from "il.festinger.historia"
			synchronized(configObject)
			{
					Vector prevKeys = (Vector) configObject.getContents();	// get keys from persistence
					// prevKeys = null; // COMMENT THIS LINE!!!!
					if (prevKeys == null)	// if not exists, create new keys
					{
						keys = new Vector(NUMBEROFKEYS);
						keys.insertElementAt(Long.toString(new Date().getTime()),FIRSTTIME);
						keys.insertElementAt("0", TIMEFORMAT); // hour format
						keys.insertElementAt("0", KEYNUMBER);
						keys.insertElementAt("0", LASTCALL);
						keys.insertElementAt("1000", NUMBEROFCALLS);
						keys.insertElementAt("0", DATEFORMAT); // US date Format
						keys.insertElementAt("1", MONITOR); // run in background
						keys.insertElementAt("0", LAYOUT); // normal layout
						keys.insertElementAt("0", FONTSIZE); // normal layout
						keys.insertElementAt("0", LOCATION); // normal layout
						keys.insertElementAt("n-r", REGISTERED); // normal layout
						keys.insertElementAt("1", CALENDAR); // normal layout
						keys.insertElementAt("All", CALLTYPETOLOG); // normal layout
						keys.insertElementAt("All", NUMBERSTOLOG); // normal layout
						
						keys.insertElementAt("1", LOGINCOMING); // normal layout
						keys.insertElementAt("1", LOGOUTGOING); // normal layout
						keys.insertElementAt("1", LOGMISSED); // normal layout
						keys.insertElementAt("1", LOGUNANSWERED); // normal layout

						
						configObject.setContents(keys);
						configObject.commit();
					}
					else if (prevKeys.size() == NUMBEROFKEYS)
					{
						keys = prevKeys;
					}
					else
					{
						int i;
						keys = new Vector(NUMBEROFKEYS);
						for (i = 0 ; i < prevKeys.size() ; i ++)
						{
							keys.insertElementAt(prevKeys.elementAt(i),i);
						}
						// here will come more additions for future versions
						
						if(keys.size() == 11)// version 1.0 rc1
						{
							keys.insertElementAt("1", CALENDAR); // log to calendar
						}
						if(keys.size() == 12)// version 1.0 rc1
						{
							keys.insertElementAt("All", CALLTYPETOLOG); // normal layout
							keys.insertElementAt("All", NUMBERSTOLOG); // normal layout
						}
						if(keys.size() == 14)// version 1.0 rc1
						{
							keys.insertElementAt("1", LOGINCOMING); // normal layout
							keys.insertElementAt("1", LOGOUTGOING); // normal layout
							keys.insertElementAt("1", LOGMISSED); // normal layout
							keys.insertElementAt("1", LOGUNANSWERED); // normal layout
						}
						if (keys.size() != NUMBEROFKEYS)	// if still there are missing keys, reset all
						{
							keys.removeAllElements();
							keys.insertElementAt(Long.toString(new Date().getTime()),FIRSTTIME);
							keys.insertElementAt("0", TIMEFORMAT); // hour format
							keys.insertElementAt("0", KEYNUMBER);
							keys.insertElementAt("0", LASTCALL);
							keys.insertElementAt("1000", NUMBEROFCALLS);
							keys.insertElementAt("0", DATEFORMAT); // US date Format
							keys.insertElementAt("1", MONITOR); // run in background
							keys.insertElementAt("0", LAYOUT); // normal layout
							keys.insertElementAt("0", FONTSIZE); // normal layout
							keys.insertElementAt("0", LOCATION); // normal layout
							keys.insertElementAt("n-r", REGISTERED); // normal layout
							keys.insertElementAt("1", CALENDAR); // normal layout
							keys.insertElementAt("All", CALLTYPETOLOG); // normal layout
							keys.insertElementAt("All", NUMBERSTOLOG); // normal layout
							
							keys.insertElementAt("1", LOGINCOMING); // normal layout
							keys.insertElementAt("1", LOGOUTGOING); // normal layout
							keys.insertElementAt("1", LOGMISSED); // normal layout
							keys.insertElementAt("1", LOGUNANSWERED); // normal layout
						}
						configObject.setContents(keys);
						configObject.commit();
					}
			}
		}
		catch(final Exception e)
		{
			UiApplication.getUiApplication().invokeLater (new Runnable() {
			    public void run()
			    {
			    	Dialog.alert("Error getting settings, if error persists contact support.\n" + e.getMessage());
			    }
			});		
			System.exit(1);
		}
	};
	public void notifyScreenClosed(MainScreen screen)	// when license is closed, open main window
	{
		try
		{
			// check the event source object
			if (screen.equals(license)) 
			{
				runMain();
			}
		}
		catch(final Exception e)
		{
			UiApplication.getUiApplication().invokeLater (new Runnable() {
			    public void run()
			    {
			    	Dialog.alert("Error Closing License window, if error persists contact support.\n" + e.getMessage());
			    }
			});	
			
			System.exit(1);
		}
	}
	private void runMain()
	{
		UiApplication.getUiApplication().invokeLater (new Runnable() {
		    public void run()
		    {
		    	Status.show("Loading...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
		    }
		});
    	UiApplication.getUiApplication().invokeLater (new Runnable() {
		    public void run()
		    {
		    	main = new Main();
		    	pushScreen(main);
		    	if (Main.storageStatus <= 0)
		    		Main.displayStorageError();
		    }
		});
	}
}

