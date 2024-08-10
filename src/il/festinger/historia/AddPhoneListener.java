package il.festinger.historia;

import net.rim.blackberry.api.phone.AbstractPhoneListener;
import net.rim.device.api.ui.component.Dialog;

public class AddPhoneListener  extends AbstractPhoneListener
{
    public AddPhoneListener()
    {
    
    }
    
    private void checkCall(String ehandler, int callid)
    {
    	try	
    	{
    		if (ehandler.compareTo("callFailed") == 0)	// deal with oncall true when call is failed, in getCalls()
    			Main.callFailed = true;
    		else if (Main.callFailed == true)
    			Main.callFailed = false;
    			

    	}
    	catch(Exception e)
    	{
    		Dialog.alert("Error checking call , if error persists contact support.\n" + e.getMessage());
    		
    	}	
    }
    
    //IMPLEMENTED LISTENER METHODS
    
    // A call has been added to a conference call
    public void callAdded(int callId)
    { checkCall("callAdded", callId); }
    
    // User answered a call
    public void callAnswered(int callId)
    {checkCall("callAnswered", callId); }
    
    // Conference call established
    public void callConferenceCallEstablished(int callId)
    { checkCall("callConferenceCallEstablished", callId); }
    
    // Network indicates a connected event
    public void callConnected(int callId)
    { checkCall("callConnected", callId); }
    
    // Direct-connect call connected
    public void callDirectConnectConnected(int callId)
    { checkCall("callDirectConnectConnected", callId); }
    
    // Direct-connect call disconnected
    public void callDirectConnectDisconnected(int callId)
    { checkCall("callDirectConnectDisconnected", callId); }
    
    // Call disconnected
    public void callDisconnected(int callId)
    { checkCall("callDisconnected", callId); }
    
    // User ended call
    public void callEndedByUser(int callId)
    { checkCall("callEndedByUser", callId); }
    
    // Call has been placed on "hold"
    public void callHeld(int callId)
    { checkCall("callHeld", callId); }
    
    // New call has arrived
    public void callIncoming(int callId)
    { checkCall("callIncoming", callId); }
    
    // Outbound call initiated by the handheld
    public void callInitiated(int callid)
    { checkCall("callInitiated", callid); }
    
    // Call removed from a conference call
    public void callRemoved(int callId)
    { checkCall("callRemoved", callId); }
    
    // Call taken off of "hold"
    public void callResumed(int callId)
    { checkCall("callResumed", callId); }
    
    // Call is waiting
    public void callWaiting(int callid)
    { checkCall("callWaiting", callid); }
    
    // Conference call has been terminated
    // (all members disconnected)
    public void conferenceCallDisconnected(int callId)
    { checkCall("conferenceCallDisconnected", callId); }
    
    // Call failed
    public void callFailed(int callId, int reason)
    { checkCall("callFailed", callId);}
}