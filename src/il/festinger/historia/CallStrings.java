package il.festinger.historia;

import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.GPRSInfo;
import net.rim.device.api.ui.component.Dialog;

public class CallStrings	// class for each call strings
{
	public String[] strings = new String[8];
	public final static int NUMBER = 0;
	public final static int NAME = 1;
	public final static int TIME = 2;
	public final static int DURATION = 3;
	public final static int TYPE = 4;
	public final static int ERROR = 5;
	public final static int CELLID = 6;
	public final static int SN = 7;
	CallStrings(String number, String name, String time, String duration, String type, String error,String cellID,String sn)	// constructor to store data in object
	{
		strings[NUMBER] = number;
		strings[NAME] = name;
		strings[TIME] = time;
		strings[DURATION] = duration;
		strings[TYPE] = type;
		strings[ERROR] = error;
		strings[CELLID] = cellID;
		strings[SN] = sn;
	};
	CallStrings()	// default constructor
	{
	};

	public void setStrings(PhoneCallLog call, boolean isCellIDValid)	// create a new record from call log
	{
		try
		{		
			int intDuration = call.getDuration();
			int hours;
			int minutes;
			strings[NAME] = call.getParticipant().getName() == null? "Unknown" : call.getParticipant().getName();
			int i =0;
			while(i != -1)	// replace ," with / 
			{
				i = strings[NAME].indexOf(",\"");
				if ( i != -1)
				{
					strings[NAME] = (new StringBuffer(strings[NAME]).insert(i + 1, " ")).toString(); 
				}
			}
			strings[NUMBER] = (call.getParticipant().getNumber() == null || call.getParticipant().getNumber().length() == 0 )? "Unknown" : call.getParticipant().getNumber();
			switch(call.getType())	//set type
			{
				case PhoneCallLog.TYPE_MISSED_CALL_OPENED:
				case PhoneCallLog.TYPE_MISSED_CALL_UNOPENED:
					strings[TYPE] = "Missed";
					break;
				case PhoneCallLog.TYPE_RECEIVED_CALL:
					strings[TYPE] = "Incoming";
					break;
				default:
					strings[TYPE] = "Outgoing";	
			}
			if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 0 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 0)
			{
				strings[TIME] = new SimpleDateFormat("M/d/yy, h:mm aa").formatLocal(call.getDate().getTime());	//US date format
			}
			else if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 1 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 1)
				strings[TIME] = new SimpleDateFormat("d/M/yy, H:mm").formatLocal(call.getDate().getTime());	//European date format
			else if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 0 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 1)
				strings[TIME] = new SimpleDateFormat("M/d/yy, H:mm").formatLocal(call.getDate().getTime());	
			else
				strings[TIME] = new SimpleDateFormat("d/M/yy, h:mm aa").formatLocal(call.getDate().getTime());	
			//---- set duration, converting seconds to 0:00:00 format----
			strings[DURATION] = "";
			hours = intDuration/3600;
			strings[DURATION] += hours == 0 ? "" : (Integer.toString(hours) +":");
			intDuration %= 3600;
			minutes = intDuration/60;
			strings[DURATION] += (Integer.toString(minutes).length() == 2 ? Integer.toString(minutes) : ("0" + Integer.toString(minutes))) + ":";
			intDuration %= 60 ;
			strings[DURATION] +=  Integer.toString(intDuration).length() == 2 ? Integer.toString(intDuration) : ("0" + Integer.toString(intDuration));
			//------------------------
			strings[SN] = Long.toString(call.getDate().getTime());
			//----------- set error code----
				if (call.getStatus() == PhoneCallLog.STATUS_BUSY)
					strings[ERROR] = "Subscriber Busy";
				else if (call.getStatus() == PhoneCallLog.STATUS_NUMBER_UNOBTAINABLE)
					strings[ERROR] = "Number unobtainable";
				else if (call.getStatus() == PhoneCallLog.STATUS_AUTHENTICATION_FAILURE)
					strings[ERROR] = "Authorization failure";
				else if (call.getStatus() == PhoneCallLog.STATUS_SERVICE_NOT_AVAILABLE)
					strings[ERROR] = "Service not available";
				else if (call.getStatus() == PhoneCallLog.STATUS_CONNECTION_DENIED)
					strings[ERROR] = "Connection denied by network";
				else if (call.getStatus() == PhoneCallLog.STATUS_PATH_UNAVAILABLE)
					strings[ERROR] = "Radio path unavailable";
				else if (call.getStatus() == 30)
					strings[ERROR] = "No user responding";
				else if (call.getStatus() == 31)
					strings[ERROR] = "User alerting but no answer";
				else if (call.getStatus() == PhoneCallLog.STATUS_GENERAL_ERROR)
					strings[ERROR] = "General call error";
				else
					strings[ERROR] = "No Error";
			if (isCellIDValid == true)
				strings[CELLID] = Integer.toString(GPRSInfo.getCellInfo().getCellId());
			if (isCellIDValid == false || strings[CELLID].compareTo("0") == 0)
				strings[CELLID] =  "Not Available";
		}
		catch(Exception e)
		{
			Dialog.inform("Error setting call strings. if error persists contact support.\n");
			System.exit(1);
		}
	}
	public String toString()	// used in writing to csv
	{
		if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 0 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 0)
		{
			strings[TIME] = new SimpleDateFormat("M/d/yy, h:mm aa").formatLocal(Long.parseLong(strings[SN]));	//US date format
		}
		else if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 1 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 1)
			strings[TIME] = new SimpleDateFormat("d/M/yy, H:mm").formatLocal(Long.parseLong(strings[SN]));	//European date format
		else if (Integer.parseInt(Historia.keys.elementAt(Historia.DATEFORMAT).toString()) == 0 && Integer.parseInt(Historia.keys.elementAt(Historia.TIMEFORMAT).toString()) == 1)
			strings[TIME] = new SimpleDateFormat("M/d/yy, H:mm").formatLocal(Long.parseLong(strings[SN]));	
		else
			strings[TIME] = new SimpleDateFormat("d/M/yy, h:mm aa").formatLocal(Long.parseLong(strings[SN]));	
		return '"' + strings[NUMBER] + "\",\"" + strings[NAME] + "\",\""
		+ strings[TIME]+ "\",\"" + strings[DURATION] + "\",\"" + strings[TYPE] + "\",\"" + strings[ERROR] + "\",\"" + strings[CELLID] + "\",\"" + strings[SN] + '"' + '\r' + '\n';
	}
}