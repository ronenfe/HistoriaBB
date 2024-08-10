package il.festinger.historia;

import java.util.Date;

import javax.microedition.io.Connector;

import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.ui.component.Dialog;


public class CallLogListener implements PhoneLogListener
{
	CallLogListener()
	{
   	 	
	}
	//---------- CALL LOG LISTENER ---------------
    public void callLogRemoved(CallLog cl)
    {
    	
    }
    public void callLogUpdated(CallLog cl,CallLog oldCl)
    {
    	
    }
    public void reset()
    {
    	
    }

    public void callLogAdded(CallLog cl) 
	{
    	
		
		
	    	try
	    	{

				    	Historia.daysPassed = (int)((new Date().getTime() - Long.parseLong(Historia.keys.elementAt(Historia.FIRSTTIME).toString()))/86400000);	// time software was ran for the first time
				    	if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0 && (Historia.daysPassed >7 || Historia.daysPassed < 0))
				    		return;
						if (((String)Historia.keys.elementAt(Historia.LOCATION)).compareTo("0") == 0 )
							Historia.targetFolder = "file:///store/home/user/documents/"; //where to save and get the csv file
						else
							Historia.targetFolder = "file:///SDCard/documents/"; //where to save and get the csv file
						try 	// if can't access call file
						{
							Connector.open(Historia.targetFolder,Connector.READ_WRITE);
						}
						catch (Exception e)
						{
							return;
						}
						Main.getCSV();
						Main.getCalls(true);
						
						Main.saveCSV();	// store new calls in csv file
						
						Main.flagNewCall = true;

    		}
	    	catch(Exception e)
	    	{
	    		Dialog.alert("Error adding call. If error persists contact support.\n" + e.getMessage());
	    		PhoneLogs.removeListener(this); 	// remove call log listener
	    		System.exit(1);
	    	}			    	

	}
    public void addListener()
    {
        PhoneLogs.addListener(this); 	// register call log listener
    }
	

}
