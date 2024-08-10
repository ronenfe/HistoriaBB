package il.festinger.historia;
 

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.blackberry.api.invoke.AddressBookArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.pdap.BlackBerryEvent;
import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.blackberry.api.phone.phonelogs.*;
import net.rim.device.api.io.LineReader;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

final class Main extends MainScreen implements ScreenClosedListener//main screen of historia
{
	public static final int ROWCONST = 275;	// row scaling
	public static int storageStatus;	// storage status
	public static boolean flagNewCall = false;	// flag for new call happened
	private static boolean flagNewCallInCSV = false;	// flag for new call happened and it's in csv

	private Vector vectorFilteredByNumberCalls = new Vector();	// stores calls for display
	private Vector vectorFilteredByTypeCalls = new Vector();	// stores filtered by type calls for display
	public static long lastCallTime = Long.parseLong((String)Historia.keys.elementAt(Historia.LASTCALL)); // get it from file;	// stores last call time
	private SettingsScreen settings;	// create a settings window	
	private CallDetailsScreen callDetails;	
	// create a call details window
//	private CellIDLocations cellIDLocations;
	private Bitmap incomingBitmap = Bitmap.getBitmapResource("call_type_incoming.png");	// incoming icon
	private Bitmap outgoingBitmap = Bitmap.getBitmapResource("call_type_outgoing.png");	// outgoing icon
	private Bitmap missedBitmap = Bitmap.getBitmapResource("call_type_error.png");	// missed icon
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private HorizontalFieldManager horizontalTitle;	// stores the title
	private VerticalFieldManager verticalCalls ;	// stores the listfield of calls
	private ListField callList;	// list of calls
	private ListCallBack callBack;	// callBack of list of calls
	private boolean filteredByTypeFlag = false;	//flag raised if display is filtered
	private boolean filteredByNumberFlag = false; //flag raised if display is filtered by type
	private int selectedIndex = 0;
	public static boolean callFailed = false; //	indicate to getCalls() that the call is disconnected
	public static CallStrings callStrings;
	public Main()
	{
		super(MainScreen.NO_VERTICAL_SCROLL); 
		try
		{	
//			vectorCalls.ensureCapacity(Integer.parseInt((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS)));
//			vectorFilteredByTypeCalls.ensureCapacity(Integer.parseInt((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS))/3);
			titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" All calls");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(new LabelField(" "));
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
			storageStatus = checkStorage();
			Historia.getVector().ensureCapacity(Integer.parseInt((String)(Historia.keys.elementAt(Historia.NUMBEROFCALLS))));
			getCSV();	// get calls from csv file
//			getCellID();	// get cells and locations
			getNumbers();	//get numbers to add to calendar
			getCalls(false);	// get new calls from phone
			saveCSV();	// store new calls in csv file
			showCallList();	//display list on screen

		}
		catch(final Exception e)
		{
		    Dialog.alert("Error in main window, if error persists contact support.\n" + e.getMessage());		
			return;
		}
	}

	public void showCallList()	// show call list on screen
	{
		callList = new ListField();	// list of calls
		callBack = new ListCallBack();	//callback for list of calls
		callList.setCallback(callBack);
		Graphics g = this.getGraphics();	// for getting font height
		if (Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) != 0)
		{
			int size = Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) + 6;
			Font myFont = Font.getDefault().derive(Font.getDefault().getStyle(), size);	// for setting font size
			g.setFont(myFont);	// set font size
		}
		callList.setRowHeight(g.getFont().getHeight()*ROWCONST/100);	// set row height in list
		verticalCalls = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.USE_ALL_WIDTH); //manager for calls
		for (int i=0; i < Historia.getVector().size(); i++) // insert elements to the call row
		{
			callList.insert(i);
		}
		verticalCalls.add(callList);	//add list of call to calls manager
		add(verticalCalls);	//add main manager to screen

	}

	public static boolean getCalls(boolean onCall)	// get calls from call log file
	{
		lastCallTime = Long.parseLong((String)Historia.keys.elementAt(Historia.LASTCALL)); // get it from file
		PhoneCall call = Phone.getActiveCall();	// know if in call
		if (checkStorage() <= 0)	
			return false;
		if (call != null &&  callFailed == false ) // if in call return
			return false;
		PhoneLogs logs = PhoneLogs.getInstance(); // create a phone log to get calls from phone
		PhoneCallLog normalCall;	//store normal call
		PhoneCallLog missedCall;	// store missed call
		PhoneCallLog currentCall;	// store current call in iteration
		int indexMissed = logs.numberOfCalls(PhoneLogs.FOLDER_MISSED_CALLS) - 1;	// store current index of missed call
		int indexNormal = logs.numberOfCalls(PhoneLogs.FOLDER_NORMAL_CALLS) - 1;	// stores current index of normal call
		int size = Historia.getVector().size();	// stores total calls size
		int limit = Integer.parseInt((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS));
		int insertAt = 0;	// indicate where to insert new call
		int num =0;	// number of total elements in vector
		while((indexNormal >= 0 || indexMissed >= 0) && num < limit)	// as long as there are calls left in the call log and size is less than limit
		{
			
			try
			{
				normalCall  = (PhoneCallLog) (indexNormal >= 0 ? logs.callAt(indexNormal, PhoneLogs.FOLDER_NORMAL_CALLS) : null) ;
				missedCall  = (PhoneCallLog) (indexMissed >= 0 ? logs.callAt(indexMissed, PhoneLogs.FOLDER_MISSED_CALLS) : null) ;
				callStrings = new CallStrings();	// create an instance of class which stores details of call
			}
			catch(Exception e)
			{
				indexNormal--;
				continue;
			}
			if(indexMissed >= 0 && indexNormal >= 0)	//if both normal and missed exist
			{
				if(normalCall.getDate().getTime() > missedCall.getDate().getTime())	// if normal is newer than missed
				{
					indexNormal--;	// advance normal index
					currentCall = normalCall;	//normal will be processed
				}
				else	// if missed is newer than normal
				{
					indexMissed--;
					currentCall = missedCall;
				}
			}
			else if (indexMissed >= 0 && indexNormal < 0)	// if only missed left
			{
				indexMissed--;
				currentCall = missedCall;			
			}
			else	// if only normal left
			{
				indexNormal--;
				currentCall = normalCall;
			}
			long currentTime = currentCall.getDate().getTime();
			if ( lastCallTime >= currentTime )	//if call already exists in file
				break;
			if (Historia.getVector().size() > insertAt && Long.parseLong(((CallStrings) Historia.getVector().elementAt(insertAt)).strings[CallStrings.SN]) >= currentTime)
				break;
			callStrings.setStrings(currentCall,onCall);	// store call details in class
			Historia.getVector().insertElementAt(callStrings, insertAt++);	// insert new call details to vector
			num++;
			if (((String)Historia.keys.elementAt(Historia.CALENDAR)).compareTo("1") == 0) 
			{			
				addCalendarEvent(Main.callStrings);
			}
					
		}
		if (size < Historia.getVector().size())	// if calls were added
		{
			if (Historia.getVector().size() > 0 && lastCallTime < Long.parseLong(((CallStrings) Historia.getVector().elementAt(0)).strings[CallStrings.SN]))
			{
				lastCallTime = Long.parseLong(((CallStrings) Historia.getVector().elementAt(0)).strings[CallStrings.SN]);
				Historia.keys.setElementAt( Long.toString(lastCallTime), Historia.LASTCALL);	//save to persistence
				Historia.saveConfiguration();
			}
			callFailed = false;
			
			while (Historia.getVector().size() > limit) // delete calls from vector after 
			{
				Historia.getVector().removeElementAt(limit);
			}
			return true;
		}
		return false;

	};

	public static void getCSV()	// get calls from csv file
	{
		boolean csvExists = true; // flag to indicate if csv was existing
		if (((String)Historia.keys.elementAt(Historia.LOCATION)).compareTo("0") == 0 )
			Historia.targetFolder = "file:///store/home/user/documents/"; //where to save and get the csv file
		else
			Historia.targetFolder = "file:///SDCard/documents/"; //where to save and get the csv file
		String historiaFileName; // file name of calls
		

		FileConnection fc;
		storageStatus = checkStorage();
		try 
		{
			fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
			if(!fc.exists())  // if directory Documents does not exist, create it
			{
				fc.mkdir();
			}
			Historia.targetFolder += "Historia/";
			fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
			if(!fc.exists())  // if directory Historia does not exist, create it
			{
				fc.mkdir();
			}
			historiaFileName = Historia.targetFolder + "Historia.txt"; // file name of calls
			fc = (FileConnection) Connector.open(historiaFileName, Connector.READ_WRITE);
			if(!fc.exists()) // if csv file does not exists , create a new one
			{
				fc.create();
				OutputStream os = fc.openOutputStream();
			    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
				char[] theCharArray = ('"' + "Number" + "\",\"" + "Name" + "\",\"" + "Time" + "\",\"" + "Duration" + "\",\"" + "Type" +  "\",\"" + "Error" + "\",\"" + "Cell ID" + "\",\"" + "S/N" + '"' + '\r' + '\n').toCharArray();// header line
				writer.write(theCharArray);
				os.close();
				fc.close();
				csvExists = false;	// csv is new
			}
			csvToVector();	// get calls to vector
			
			if (((String)Historia.keys.elementAt(Historia.LASTCALL)).compareTo("0") == 0 ) // if it's the first time software is running
			{
				if (csvExists == true && Historia.getVector().isEmpty() == false) // if file csv was existing (from previous versions) and its's not empty
					lastCallTime = Long.parseLong(((CallStrings) Historia.getVector().elementAt(0)).strings[CallStrings.SN]); // make the first call in the file the lastcall so you can update the new calls from call log
				else // if csv wasn't existing or is empty
					lastCallTime = 0;   // set lastcalltime to early date so file will be updated with all calls from call log                
			}
			else    // if it's not the first time the program is running
			{
				if (csvExists == true)  // csv file was existing
					lastCallTime = Long.parseLong((String)Historia.keys.elementAt(Historia.LASTCALL)); // get it from file
				else
					lastCallTime = 0;   // set lastcalltime to early date so file will be updated with all calls from call log
			}
//			throw(new Exception());
		}
		catch (Exception e) // file exception 
		{
			if (storageStatus <= 0 )
			{
				return;
			}
			historiaFileName = Historia.targetFolder + "Historia.txt"; // file name of calls
			if (Dialog.ask(Dialog.D_YES_NO,"Error getting data from Historia.txt. Try restarting. If it doesn't help, you can delete the file by choosing Yes. The old file will be saved as HistoriaBackup.txt for backup purposes. If error persists please contact support or reinstall the software. Do you want to delete the file?\n") == Dialog.YES)
			{
				String historiaFileNameBackup = Historia.targetFolder + "HistoriaBackup.txt"; // file name of backup
				FileConnection fcException;
				try //	 create backup
				{
					fcException = (FileConnection) Connector.open(historiaFileName, Connector.READ_WRITE);
					if (fcException.exists() == true)
					{
						copyFile(historiaFileName, historiaFileNameBackup);
						fcException.delete();	// delete historia.txt
					}
				} 
				catch (Exception e1)
				{
					Dialog.alert("Error creating backup file.\n" + e.getMessage());
				}
			}
			return;
		}
	}
/*	public static void getCellID()	// get calls from csv file
	{
		String cellIDFileName; // file name of calls
		
		FileConnection fc;
		
		try 
		{
			fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);

			cellIDFileName = Historia.targetFolder + "CellID.txt"; // file name of calls
			fc = (FileConnection) Connector.open(cellIDFileName, Connector.READ_WRITE);
			if(!fc.exists()) // if csv file does not exists , create a new one
			{
				fc.create();
				OutputStream os = fc.openOutputStream();
			    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
				char[] theCharArray = ('"' + "Cell ID" + "\",\"" + "Location" + '"' + '\r' + '\n').toCharArray();// header line
				writer.write(theCharArray);
				os.close();
				fc.close();
			}
			cellIDToVector();	// get calls to vector
		}
		catch (Exception e) // file exception 
		{
			if (storageStatus <= 0 )
			{
				return;
			}
			cellIDFileName = Historia.targetFolder + "CellID.txt"; // file name of calls
			if (Dialog.ask(Dialog.D_YES_NO,"Error getting data from CellID.txt. Try restarting. If it doesn't help, you can delete the file by choosing Yes. The old file will be saved as CellIDBackup.txt for backup purposes. If error persists please contact support or reinstall the software. Do you want to delete the file?\n") == Dialog.YES)
			{
				String cellIDFileNameBackup = Historia.targetFolder + "CellIDBackup.txt"; // file name of backup
				FileConnection fcException;
				try //	 create backup
				{
					fcException = (FileConnection) Connector.open(cellIDFileName, Connector.READ_WRITE);
					if (fcException.exists() == true)
					{
						copyFile(cellIDFileName, cellIDFileNameBackup);
						fcException.delete();	// delete historia.txt
					}
				} 
				catch (Exception e1)
				{
					Dialog.alert("Error creating backup file.\n" + e.getMessage());
				}
			}
			return;
		}
	}*/
	public static void getNumbers()	// get calls from csv file
	{
		String numbersFileName; // file name of calls
		
		FileConnection fc;
		
		try 
		{
			fc = (FileConnection) Connector.open("file:///store/home/user/documents/",Connector.READ_WRITE);
			if(!fc.exists())  // if directory Documents does not exist, create it
			{
				fc.mkdir();
			}
			fc = (FileConnection) Connector.open("file:///store/home/user/documents/Historia/",Connector.READ_WRITE);
			if(!fc.exists())  // if directory Historia does not exist, create it
			{
				fc.mkdir();
			}

			numbersFileName = "file:///store/home/user/documents/Historia/" + "Numbers.txt"; // file name of calls
			fc = (FileConnection) Connector.open(numbersFileName, Connector.READ_WRITE);
			if(!fc.exists()) // if csv file does not exists , create a new one
			{
				fc.create();
				OutputStream os = fc.openOutputStream();
			    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
				char[] theCharArray = ('"' + "Number" + '"' + '\r' + '\n').toCharArray();// header line
				writer.write(theCharArray);
				os.close();
				fc.close();
			}
			numbersToVector();	// get calls to vector
		}
		catch (Exception e) // file exception 
		{
			if (storageStatus <= 0 )
			{
				return;
			}
			numbersFileName = "file:///store/home/user/documents/Historia/" + "Numbers.txt"; // file name of calls
			if (Dialog.ask(Dialog.D_YES_NO,"Error getting data from Numbers.txt. Try restarting. If it doesn't help, you can delete the file by choosing Yes. The old file will be saved as CellIDBackup.txt for backup purposes. If error persists please contact support or reinstall the software. Do you want to delete the file?\n") == Dialog.YES)
			{
				String numbersFileNameBackup = "file:///store/home/user/documents/Historia/" + "NumbersBackup.txt"; // file name of backup
				FileConnection fcException;
				try //	 create backup
				{
					fcException = (FileConnection) Connector.open(numbersFileName, Connector.READ_WRITE);
					if (fcException.exists() == true)
					{
						copyFile(numbersFileName, numbersFileNameBackup);
						fcException.delete();	// delete numbers.txt
					}
				} 
				catch (Exception e1)
				{
					Dialog.alert("Error creating backup file.\n" + e.getMessage());
				}
			}
			return;
		}
	}
	public static void copyFile(String srFile, String dtFile)	// copy file for backup
	{
		try
		{
			FileConnection f1;
			FileConnection f2;
			f1 = (FileConnection) Connector.open(srFile,Connector.READ_WRITE);
			f2 = (FileConnection) Connector.open(dtFile,Connector.READ_WRITE);
			if(!f2.exists()) // if  file does not exists , create a new one
			{
				f2.create();
			}
			InputStream is = f1.openInputStream();
			OutputStream os =f2.openOutputStream();
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			char[] buf = new char[1024];
			int len;
			while ((len = reader.read(buf)) > 0)
			{
				writer.write(buf, 0, len);
			}
			is.close();
			os.close();
			reader.close();
			writer.close();
		}
		catch(Exception e)
		{
			Dialog.alert("Error copying file.\n" + e.getMessage());
		}
	}

	public static void csvToVector()	// get calls from file to vector
	{
		FileConnection fc;
		int limit = Integer.parseInt((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS));	// limit of calls
		LineReader lineReader;
		String line;
		InputStream is;
		storageStatus = checkStorage();
		if (storageStatus <=0)
			return;
		try
		{
			fc = (FileConnection) Connector.open(Historia.targetFolder + "Historia.txt", Connector.READ);
			is = fc.openInputStream();
			line = null;
			lineReader = new LineReader(is);
		}
		catch (Exception e) 
		{
			Dialog.alert("Error reading CSV File.\n");
			return;
		}
		try
		{
			lineReader.readLine();	// skip header line
		}
		catch (EOFException eofe)	// if file is empty catch the exception and do nothing
		{
		}
        catch(IOException ioe)
        {
			Dialog.alert("Error reading CSV File.\n");
			return; // Error reading data from file        	
        }              
		for (int i = 0;i < limit;i++)	// run over the lines
		{
			try
			{
				line = new String(lineReader.readLine(), "UTF-8");
				if (line == null)
					break;
				String[] values = new String[8];	// stores values
				line = line.substring(1,line.length()-1);
				values = split(line,"\",\"");	// split by ","
				if (Historia.getVector().size() > i  && Long.parseLong(values[CallStrings.SN]) <= Long.parseLong(((CallStrings) Historia.getVector().elementAt(i)).strings[CallStrings.SN]))	// don't add old calls which already exists
					break;
				CallStrings callStrings = new CallStrings(values[CallStrings.NUMBER], values[CallStrings.NAME], values[CallStrings.TIME], values[CallStrings.DURATION], values[CallStrings.TYPE], values[CallStrings.ERROR], values[CallStrings.CELLID], values[CallStrings.SN]);	// stores in object
//				ObjectGroup.createGroup(callStrings);
				Historia.getVector().insertElementAt(callStrings, i);	//add object to vector
				flagNewCallInCSV = true;				
			}
	        catch(EOFException eof)
	        {
	            // We've reached the end of the file.
	            break;
	        }
	        catch(IOException ioe)
	        {
				Dialog.alert("Error reading CSV File.\n");
				return; // Error reading data from file        	
	        }
		}
		while (Historia.getVector().size() > limit)
			Historia.getVector().removeElementAt(limit);
		try
		{
			is.close();
			fc.close();
		}
		catch (Exception e) 
		{
			Dialog.alert("Error reading CSV File.\n");
			return;
		}
	}
/*	public static void cellIDToVector()	// get calls from file to vector
	{
		FileConnection fc;
		LineReader lineReader;
		String line;
		InputStream is;
		try
		{
			fc = (FileConnection) Connector.open(Historia.targetFolder + "CellID.txt", Connector.READ);
			is = fc.openInputStream();
			line = null;
			lineReader = new LineReader(is);
		}
		catch (Exception e) 
		{
			Dialog.alert("Error reading Cell ID File.\n");
			return;
		}
		try
		{
			lineReader.readLine();	// skip header line
		}
		catch (EOFException eofe)	// if file is empty catch the exception and do nothing
		{
		}
        catch(IOException ioe)
        {
			Dialog.alert("Error reading Cell ID File.\n");
			return; // Error reading data from file        	
        }              
		while(true)	// run over the lines
		{
			try
			{
				line = new String(lineReader.readLine(), "UTF-8");
				if (line == null)
					break;
				String[] values = new String[8];	// stores values
				line = line.substring(1,line.length()-1);
				values = split(line,"\",\"");	// split by ","
				
				Historia.vectorCellID.addElement(new CellIDStrings(Integer.parseInt(values[0]),values[1]));	//add object to vector			
			}
	        catch(EOFException eof)
	        {
	            // We've reached the end of the file.
	            break;
	        }
	        catch(IOException ioe)
	        {
				Dialog.alert("Error reading Cell ID File.\n");
				return; // Error reading data from file        	
	        }
		}
		try
		{
			is.close();
			fc.close();
		}
		catch (Exception e) 
		{
			Dialog.alert("Error reading Cell ID File.\n");
			return;
		}
	}*/
	public static void numbersToVector()	// getnumbers from file to vector
	{
		FileConnection fc;
		LineReader lineReader;
		String line;
		InputStream is;
		try
		{
			fc = (FileConnection) Connector.open(Historia.targetFolder + "Numbers.txt", Connector.READ);
			is = fc.openInputStream();
			line = null;
			lineReader = new LineReader(is);
		}
		catch (Exception e) 
		{
			return;
		}
		try
		{
			lineReader.readLine();	// skip header line
		}
		catch (EOFException eofe)	// if file is empty catch the exception and do nothing
		{
		}
        catch(IOException ioe)
        {
			Dialog.alert("Error reading Numbers File.\n");
			return; // Error reading data from file        	
        }              
		while(true)	// run over the lines
		{
			try
			{
				line = new String(lineReader.readLine(), "UTF-8");
				if (line == null)
					break;
				line = line.substring(1,line.length()-1);
				Historia.vectorNumbers.addElement(line);	//add object to vector			
			}
	        catch(EOFException eof)
	        {
	            // We've reached the end of the file.
	            break;
	        }
	        catch(IOException ioe)
	        {
				Dialog.alert("Error reading Numbers File.\n");
				return; // Error reading data from file        	
	        }
		}
		try
		{
			is.close();
			fc.close();
		}
		catch (Exception e) 
		{
			Dialog.alert("Error reading Numbers File.\n");
			return;
		}
	}
	public static String[] split(String inString, String delimeter) // split strings
	{
		String[] retAr = null;	//array of values
		try
		{
			Vector vec = new Vector();
			int indexA = 0;
			int indexB = inString.indexOf(delimeter);

			while (indexB != -1)
			{
				if (indexB > indexA)
					vec.addElement(new String(inString.substring(indexA, indexB)));
				indexA = indexB + delimeter.length();
				indexB = inString.indexOf(delimeter, indexA);
			}
			vec.addElement(new String(inString.substring(indexA, inString.length())));
			retAr = new String[vec.size()];
			for (int i = 0; i < vec.size(); i++)
			{
				retAr[i] = vec.elementAt(i).toString();
			}
			return retAr;
		} 
		catch (Exception e) 
		{
			Dialog.alert("Error proccesing CSV File.\n" + e.getMessage());
			System.exit(1);
			return retAr;
		}
		
	}

	public static void saveCSV()	// save to csv file
	{
		FileConnection fc;
		String historiaFileName = Historia.targetFolder + "Historia.txt"; // file name of calls
		
		storageStatus = checkStorage();
		if (storageStatus <= 0)
			return;
		
		try 
		{
			if (storageStatus <= 0 )
			{
				return;
			}
			fc = (FileConnection) Connector.open(historiaFileName, Connector.READ_WRITE);
			if (fc.exists())
			{
				fc.delete();
				fc.create();
			}
			else
				fc.create();
			OutputStream os =fc.openOutputStream();
		    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			char[] theCharArray = ('"' + "Number" + "\",\"" + "Name" + "\",\"" + "Time" + "\",\"" + "Duration" + "\",\"" + "Type" +  "\",\"" + "Error" + "\",\"" + "Cell ID" + "\",\"" + "S/N" + '"' + '\r' + '\n').toCharArray();// header line
			writer.write(theCharArray);

			for( int i = 0 ; i < Historia.getVector().size(); i++)
			{				
				theCharArray = ((CallStrings) Historia.getVector().elementAt(i)).toString().toCharArray();	// get string of data from vector
				writer.write(theCharArray);
			}
			os.close();
			writer.close();
			fc.close(); 
		}
		catch (Exception e) 
		{
			if (Dialog.ask(Dialog.D_YES_NO,"Error writing data to Historia.txt. Try restarting. If it doesn't help, you can delete the file by choosing Yes. The old file will be saved as HistoriaBackup.txt for backup purposes. If error persists please contact support or reinstall the software. Do you want to delete the file?\n" + e.getMessage()) == Dialog.YES)
			{
				String historiaFileNameBackup = Historia.targetFolder + "HistoriaBackup.txt"; // file name of calls
				FileConnection fcException;
				try 
				{
					fcException = (FileConnection) Connector.open(historiaFileName, Connector.READ_WRITE);
					if (fcException.exists() == true)
					{
						copyFile(historiaFileName, historiaFileNameBackup);
						fcException.delete();
					}
				} 
				catch (IOException e1)
				{
					Dialog.alert("Error creating backup file.\n" + e.getMessage());
				}

			}
			System.exit(1);
		}
	}
	public static void saveToFile(Vector vector, String fileName)	// save to csv file
	{
		FileConnection fc;
		String historiaFileName = "file:///store/home/user/documents/Historia/" + fileName; // file name of calls
		storageStatus = checkStorage();
		if (storageStatus <= 0)
			return;
		
		try 
		{
			fc = (FileConnection) Connector.open("file:///store/home/user/documents/",Connector.READ_WRITE);
			if(!fc.exists())  // if directory Documents does not exist, create it
			{
				fc.mkdir();
			}
			fc = (FileConnection) Connector.open("file:///store/home/user/documents/Historia/",Connector.READ_WRITE);
			if(!fc.exists())  // if directory Historia does not exist, create it
			{
				fc.mkdir();
			}
			
			
			fc = (FileConnection) Connector.open(historiaFileName, Connector.READ_WRITE);
			if (fc.exists())
			{
				fc.delete();
				fc.create();
			}
			else
				fc.create();
			OutputStream os =fc.openOutputStream();
		    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			char[] theCharArray = ('"' + "Number" + '"' + '\r' + '\n').toCharArray();// header line
			writer.write(theCharArray);

			for( int i = 0 ; i < vector.size(); i++)
			{				
				theCharArray = ('"' + ((String) vector.elementAt(i)).toString()+ '"' + '\r' + '\n').toCharArray();	// get string of data from vector
				writer.write(theCharArray);
			}
			os.close();
			writer.close();
			fc.close(); 
		}
		catch (Exception e) 
		{
			if (Dialog.ask(Dialog.D_YES_NO,"Error writing data to Historia.txt. Try restarting. If it doesn't help, you can delete the file by choosing Yes. The old file will be saved as HistoriaBackup.txt for backup purposes. If error persists please contact support or reinstall the software. Do you want to delete the file?\n" + e.getMessage()) == Dialog.YES)
			{
				String historiaFileNameBackup = Historia.targetFolder + "HistoriaBackup.txt"; // file name of calls
				FileConnection fcException;
				try 
				{
					fcException = (FileConnection) Connector.open("file:///store/home/user/documents/Historia/", Connector.READ_WRITE);
					if (fcException.exists() == true)
					{
						copyFile(historiaFileName, historiaFileNameBackup);
						fcException.delete();
					}
				} 
				catch (IOException e1)
				{
					Dialog.alert("Error creating backup file.\n" + e.getMessage());
				}

			}
			System.exit(1);
		}
	}
	private class ListCallBack implements ListFieldCallback // drawing call list
	{
		public void drawListRow(ListField list, Graphics g, int index, int y, int w)	// draw row 
		{
			String stringNumber;
			String stringName;
			String stringTime;
			String stringDuration;
			String stringType;
			if (filteredByNumberFlag == true)	// if display of calls filtered by type
			{
				stringNumber = ((CallStrings) vectorFilteredByNumberCalls.elementAt(index)).strings[CallStrings.NUMBER];
				stringName = ((CallStrings) vectorFilteredByNumberCalls.elementAt(index)).strings[CallStrings.NAME];
				stringTime = ((CallStrings) vectorFilteredByNumberCalls.elementAt(index)).strings[CallStrings.TIME];
				stringDuration = ((CallStrings) vectorFilteredByNumberCalls.elementAt(index)).strings[CallStrings.DURATION];
				stringType = ((CallStrings) vectorFilteredByNumberCalls.elementAt(index)).strings[CallStrings.TYPE];
			}
			else if (filteredByTypeFlag == true) // if display of calls  filtered
			{
				stringNumber = ((CallStrings) vectorFilteredByTypeCalls.elementAt(index)).strings[CallStrings.NUMBER];
				stringName = ((CallStrings) vectorFilteredByTypeCalls.elementAt(index)).strings[CallStrings.NAME];
				stringTime = ((CallStrings) vectorFilteredByTypeCalls.elementAt(index)).strings[CallStrings.TIME];
				stringDuration = ((CallStrings) vectorFilteredByTypeCalls.elementAt(index)).strings[CallStrings.DURATION];
				stringType = ((CallStrings) vectorFilteredByTypeCalls.elementAt(index)).strings[CallStrings.TYPE];
			}
			else 	// if display of calls not filtered
			{
				stringNumber = ((CallStrings) Historia.getVector().elementAt(index)).strings[CallStrings.NUMBER];
				stringName = ((CallStrings) Historia.getVector().elementAt(index)).strings[CallStrings.NAME];
				stringTime = ((CallStrings) Historia.getVector().elementAt(index)).strings[CallStrings.TIME];
				stringDuration = ((CallStrings) Historia.getVector().elementAt(index)).strings[CallStrings.DURATION];
				stringType = ((CallStrings) Historia.getVector().elementAt(index)).strings[CallStrings.TYPE];
			}
			int pos = 10;	// starting position of strings
			
			if (Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) != 0)
			{
				int size = Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) + 6;
				Font myFont = Font.getDefault().derive(Font.getDefault().getStyle(), size);	// for setting font size
				g.setFont(myFont);	// set font size
			}
				
			int height = g.getFont().getHeight();
			int width = Display.getWidth();
						
			//---- add icon---------
			if( stringType.compareTo("Incoming") == 0)
			{
				g.drawBitmap(pos, y + height*40/100, 30 , 30,incomingBitmap, 0, 0 );
			}
			else if (stringType.compareTo("Outgoing") == 0)
			{
				g.drawBitmap(pos, y+ height*40/100, 30,30,outgoingBitmap, 0, 0 );
			}
			else
				g.drawBitmap(pos, y + height*40/100, 30,30,missedBitmap, 0, 0 );

			pos += 20 ;
			if (Integer.parseInt((String)Historia.keys.elementAt(Historia.LAYOUT)) == 0)	// normal layout
			{
				g.drawText(stringNumber, pos , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - pos) *41/100 );
				g.drawText(stringName, pos  ,  y + height*150/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - pos) *75/100 );
				g.drawText(stringTime, pos + (width - pos) *41/100  + 5 , y + height*25/100, (DrawStyle.RIGHT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - pos) *59/100  - 10);
				g.drawText(stringDuration,  pos + (width - pos) *75/100  +5 ,  y + height*150/100, (DrawStyle.RIGHT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - pos) *25/100  - 10);
				g.setColor(Color.LIGHTGREY);
				g.drawLine(0 , y + height*ROWCONST/100 - 1 ,Display.getWidth() , y + height*ROWCONST/100 - 1);	// draw line under row
			}
			else	// compact layout
			{
				if (stringName.compareTo("Unknown") == 0)
				{
					g.drawText(stringNumber, pos , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - pos));
				}
				else
				{
					g.drawText(stringName, pos  ,  y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - pos));
				}
				g.drawText(stringTime, pos, y + height*150/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - pos) *70/100);
				g.drawText(stringDuration,  pos + (width - pos) *70/100  +5 ,  y + height*150/100, (DrawStyle.RIGHT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - pos) *30/100  - 10);
				g.setColor(Color.LIGHTGREY);
				g.drawLine(0 , y + height*ROWCONST/100 - 1 ,Display.getWidth() , y + height*ROWCONST/100 - 1);	// draw line under row*/
			}
		}
		public Object get(ListField list, int index)
		{
			if (filteredByNumberFlag == true)	// if display of calls filtered by type
			{
				return vectorFilteredByNumberCalls.elementAt(index);
			}
			else if (filteredByTypeFlag == true) // if display of calls  filtered
			{
				return vectorFilteredByTypeCalls.elementAt(index);
			}
			else 	// if display of calls not filtered
			{
				return Historia.getVector().elementAt(index);
			}
		}
		public int indexOfList(ListField list, String p, int s) 
		{
			if (filteredByNumberFlag == true)	// if display of calls filtered by type
			{
				return vectorFilteredByNumberCalls.indexOf(p, s);
			}
			else if (filteredByTypeFlag == true) // if display of calls  filtered
			{
				return vectorFilteredByTypeCalls.indexOf(p, s);
			}
			else 	// if display of calls not filtered
			{
				return Historia.getVector().indexOf(p, s);
			}
		}
		public int getPreferredWidth(ListField list)
		{
			return Display.getWidth();
		}
	}

	//---making a menu
	protected void makeMenu(Menu menu, int instance)	// make application menu
	{
		super.makeMenu(menu, instance);
		if (filteredByTypeFlag == false)
		{
			menuIncoming.setText("  Filter by Incoming");
			menuOutgoing.setText("  Filter by Outgoing");
			menuMissed.setText("  Filter by Missed");
		}		
		if (instance == Menu.INSTANCE_DEFAULT && filteredByNumberFlag == false)	// if not filtered by number
		{
			if (callList.getSize() != 0)	// if calls exist in list
			{
				selectedIndex = callList.getSelectedIndex();
				menu.add(menuDetails);
				if (((String)Historia.keys.elementAt(Historia.CALENDAR)).compareTo("1") == 0 && 
						((String)Historia.keys.elementAt(Historia.NUMBERSTOLOG)).compareTo("Custom") == 0)
					menu.add(menuLogToCalendar);
				menu.add(menuCall);
				menu.add(menuSMS);
				menu.add(menuContacts);
				menu.add(menuFilter);
				menu.add(menuCopy);
				menu.add(menuDelete);
				menu.setDefault(menuDetails);
			}
			menu.add(menuIncoming);
			menu.add(menuOutgoing);
			menu.add(menuMissed);
			
			menu.add(menuSummary);
			if (callList.getSize() != 0)	// if calls exist in list
				menu.add(menuDeleteAll);
			menu.add(menuSettings);
		}
		else if (instance == Menu.INSTANCE_DEFAULT && filteredByNumberFlag == true)	// if filtered by number
		{
			if (callList.getSize() != 0)	// if calls exist in list
			{
				selectedIndex = callList.getSelectedIndex();
				
				menu.add(menuDetails);
				if (((String)Historia.keys.elementAt(Historia.CALENDAR)).compareTo("1") == 0 && 
						((String)Historia.keys.elementAt(Historia.NUMBERSTOLOG)).compareTo("Custom") == 0)
					menu.add(menuLogToCalendar);
				menu.add(menuCall);
				menu.add(menuSMS);
				menu.add(menuContacts);
				menu.add(menuCopy);
				menu.add(menuDelete);
				menu.setDefault(menuDetails);
			}
			
			menu.add(menuBack);
			menu.add(menuSummary);
			if (callList.getSize() != 0)	// if calls exist in list
				menu.add(menuDeleteAll);
			menu.add(menuSettings);
		}
		else if (filteredByNumberFlag == false && callList.getSize() != 0)	// context menu if not filtered by number and list is not empty
		{
			selectedIndex = callList.getSelectedIndex();
			menu.add(menuDetails);
			if (((String)Historia.keys.elementAt(Historia.CALENDAR)).compareTo("1") == 0 && 
					((String)Historia.keys.elementAt(Historia.NUMBERSTOLOG)).compareTo("Custom") == 0)
				menu.add(menuLogToCalendar);
			menu.add(menuCall);
			menu.add(menuSMS);
			menu.add(menuContacts);
			menu.add(menuFilter);
			menu.add(menuCopy);
			menu.add(menuDelete);
			menu.setDefault(menuDetails);
		}
		else if (filteredByNumberFlag == true && callList.getSize() != 0) // context menu if  filtered by number and list is not empty
		{
			selectedIndex = callList.getSelectedIndex();
			menu.add(menuDetails);
			if (((String)Historia.keys.elementAt(Historia.CALENDAR)).compareTo("1") == 0 && 
					((String)Historia.keys.elementAt(Historia.NUMBERSTOLOG)).compareTo("Custom") == 0)
				menu.add(menuLogToCalendar);
			menu.add(menuCall);
			menu.add(menuSMS);
			menu.add(menuContacts);
			menu.add(menuCopy);
			menu.add(menuDelete);
			menu.setDefault(menuDetails);
		} 
	}
	public void filterByType(String type)
	{
		callList.setSize(0);
		filteredByTypeFlag = true;
		vectorFilteredByTypeCalls.removeAllElements();
		titleLabel.setText(" " + type + " calls");
		for (int i=0; i < Historia.getVector().size(); i++)  // get all Outgoing and insert to list
		{
			if(((CallStrings) Historia.getVector().elementAt(i)).strings[CallStrings.TYPE].equals(type))
			{
				vectorFilteredByTypeCalls.addElement((CallStrings) Historia.getVector().elementAt(i));
			}
		}

	}
	public void filterByNumber(String number)
	{
		filteredByNumberFlag = true;
		callList.setSize(0);
		vectorFilteredByNumberCalls.removeAllElements();
		for (int i=0; i < Historia.getVector().size(); i++) // get all calls from same number and insert to list
		{
			if(((CallStrings) Historia.getVector().elementAt(i)).strings[CallStrings.NUMBER].equals(number))
			{
				vectorFilteredByNumberCalls.addElement((CallStrings) Historia.getVector().elementAt(i));
			}
		}
		titleLabel.setText(" " + number);
	}
	//---filter by incoming
	private MenuItem menuIncoming = new MenuItem("  Filter by Incoming", 400000, 2000) 	
	{
		public void run() 
		{
			Status.show("Please Wait...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
			if (menuIncoming.toString().equals("  Filter by Incoming"))
			{
				menuIncoming.setText(Characters.CHECK_MARK + "Filter by Incoming");
				menuOutgoing.setText("  Filter by Outgoing");
				menuMissed.setText("  Filter by Missed");
				filterByType("Incoming");
				for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
					callList.insert(i);
			}
			else
			{
				callList.setSize(0);
				menuIncoming.setText("  Filter by Incoming");
				filteredByTypeFlag = false;
				for (int i=0; i < Historia.getVector().size(); i++) // insert elements to the call row
				{
					callList.insert(i);
				}
				titleLabel.setText(" All calls");
			}

		}
	};
	//---filter by outgoing
	private MenuItem menuOutgoing = new MenuItem("  Filter by Outgoing", 400001, 2000) 	// open settings window
	{
		public void run() 
		{
			Status.show("Please Wait...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
			if (menuOutgoing.toString().equals("  Filter by Outgoing"))
			{
				menuOutgoing.setText(Characters.CHECK_MARK + "Filter by Outgoing");
				menuIncoming.setText("  Filter by Incoming");
				menuMissed.setText("  Filter by Missed");
				filterByType("Outgoing");
				for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
					callList.insert(i);
			}		
			else
			{
				callList.setSize(0);
				menuOutgoing.setText("  Filter by Outgoing");
				filteredByTypeFlag = false;
				for (int i=0; i < Historia.getVector().size(); i++) // insert elements to the call row
				{
					callList.insert(i);
				}
				titleLabel.setText(" All calls");
			}
		}
	};
	//---filter by missed
	private MenuItem menuMissed = new MenuItem("  Filter by Missed", 400002, 2000) 	// open settings window
	{
		public void run() 
		{
			Status.show("Please Wait...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
			if (menuMissed.toString().equals("  Filter by Missed"))
			{
				menuMissed.setText(Characters.CHECK_MARK + "Filter by Missed");
				menuIncoming.setText("  Filter by Incoming");
				menuOutgoing.setText("  Filter by Outgoing");
				filterByType("Missed");
				for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
					callList.insert(i);
			}
			else
			{
				callList.setSize(0);
				menuMissed.setText("  Filter by Missed");
				filteredByTypeFlag = false;
				for (int i=0; i < Historia.getVector().size(); i++) // insert elements to the call row
				{
					callList.insert(i);
				}
				titleLabel.setText(" All calls");
			}

		}
	};
	private MenuItem menuSettings = new MenuItem("Settings", 800000, 2000) 	// open settings window
	{
		public void run() 
		{
			settings = new SettingsScreen(Main.this,Historia.vectorNumbers);	// create the settings window
			UiApplication.getUiApplication().pushScreen(settings);
		}
	};
	
	private MenuItem menuDetails = new MenuItem("Details", 19990, 0) 	// Show call details
	{
		public void run() 
		{
			if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
				callDetails = new CallDetailsScreen((CallStrings)Historia.getVector().elementAt(selectedIndex));	// create the details window
			else if (filteredByTypeFlag == true )	// if filtered by type
				callDetails = new CallDetailsScreen((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex));	// create the details window
			else // if filtered by number
				callDetails = new CallDetailsScreen((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex));	// create the details window

			UiApplication.getUiApplication().pushScreen(callDetails);
		}
	};
	private MenuItem menuLogToCalendar = new MenuItem("Log to Calendar", 19993, 0) 	// Show call details
	{
		public void run() 
		{
			String number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
			int index = NumbersListScreen.binarySearchNumber(Historia.vectorNumbers, number);
			if(Historia.vectorNumbers.size() <= index || ((String)(Historia.vectorNumbers.elementAt(index))).compareTo(number) != 0)
			{
				Historia.vectorNumbers.insertElementAt(number, index);
				saveToFile(Historia.vectorNumbers, "Numbers.txt");
			}
			else
			{
				Dialog.inform("Number already exists.");
			}
		}
	};
/*	private MenuItem menuLocation = new MenuItem("Edit Location", 19995, 0) 	// Show call details
	{
		public void run() 
		{
			String cellID;
			if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
				cellID = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.CELLID];
			else if (filteredByTypeFlag == true )	// if filtered by type
				cellID = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.CELLID];
			else // if filtered by number
				cellID = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.CELLID];
			if  (cellID.compareTo("Not Available") == 0)
				cellID = "-1";
			cellIDLocations = new CellIDLocations(vectorCellID,Integer.parseInt(cellID));	// create the cell id  window
			UiApplication.getUiApplication().pushScreen(cellIDLocations);
		}
	};*/
	private MenuItem menuBack = new MenuItem("Back", 400000, 2000) 	// back from filtered number
	{
		public void run() 
		{
			Status.show("Please Wait...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
			filteredByNumberFlag = false;
			if (filteredByTypeFlag == true)	// if display was filtered by type
			{
				callList.setSize(0);
				vectorFilteredByTypeCalls.removeAllElements();
				String type;

				if (menuMissed.toString().equals(Characters.CHECK_MARK + "Filter by Missed"))
					type = "Missed";
				else if (menuOutgoing.toString().equals(Characters.CHECK_MARK  + "Filter by Outgoing"))
					type = "Outgoing";
				else
					type = "Incoming";
				titleLabel.setText(" " + type + " calls");
				filterByType(type);
				callList.setSize(0);
				for (int i = 0; i < vectorFilteredByTypeCalls.size(); i++)
					callList.insert(i);
			}
			else
			{
				callList.setSize(0);
				filteredByTypeFlag = false;
				titleLabel.setText(" All calls");
				for (int i=0; i < Historia.getVector().size(); i++) // insert elements to the call row
				{
					callList.insert(i);
				}

			}
		}
	};

	private MenuItem menuCall = new MenuItem("Call", 100010, 2000) 	// call number
	{
		public void run() 
		{
			try
			{
				String number;
				if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
					number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else if (filteredByTypeFlag == true )	// if filtered by type
					number = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else // if filtered by number
					number = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				PhoneArguments call = new PhoneArguments (PhoneArguments.ARG_CALL,number);
				Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
			}
			catch(Exception e)
			{
				Dialog.alert("Error calling, if error persists contact support.\n" + e.getMessage());
			}
		}
	};
	private MenuItem menuSMS = new MenuItem("SMS", 100020, 2000) 	// send sms
	{
		public void run() 
		{
			try
			{
				String number;
				if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
					number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else if (filteredByTypeFlag == true )	// if filtered by type
					number = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else // if filtered by number
					number = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];

				MessageConnection mc = (MessageConnection)Connector.open("sms://");
				mc.close();
				TextMessage textMessage = (TextMessage)mc.newMessage(MessageConnection.TEXT_MESSAGE);
				textMessage.setAddress("sms://" + number);
				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(textMessage));
			}
			catch (Throwable e)
			{
				Dialog.alert("Error sending SMS, if error persists contact support.\n" + e.getMessage());
			}          


		}
	};
	private MenuItem menuContacts = new MenuItem("Save to Contacts", 100030, 2000) 	// save to contacts
	{
		public void run() 
		{
			try
			{          
				String number;
				if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
					number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else if (filteredByTypeFlag == true )	// if filtered by type
					number = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else // if filtered by number
					number = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];

				ContactList deviceAdrBook = null;
				deviceAdrBook = (ContactList) PIM.getInstance().openPIMList( PIM.CONTACT_LIST, PIM.READ_WRITE);
				Contact newBBContact = deviceAdrBook.createContact();


				newBBContact.addString(Contact.TEL, Contact.ATTR_MOBILE, number);               
				newBBContact.commit();

				Invoke.invokeApplication(Invoke.APP_TYPE_ADDRESSBOOK,new AddressBookArguments(AddressBookArguments.ARG_NEW, newBBContact));
			}
			catch(Exception e)
			{
				Dialog.alert("Error saving to contacts, if error persists contact support.\n" + e.getMessage());
			}

		}
	};
	private MenuItem menuCopy = new MenuItem("Copy", 100040, 2000) 	// Copy number to clipboard
	{
		public void run() 
		{
			try
			{
				String number;
				if (filteredByNumberFlag == false && filteredByTypeFlag == false)	// if not filtered
					number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else if (filteredByTypeFlag == true )	// if filtered by type
					number = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				else // if filtered by number
					number = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
				Clipboard.getClipboard().put(number);
			}
			catch(Exception e)
			{
				Dialog.alert("Error copying to clipboard, if error persists contact support.\n" + e.getMessage());
			}
		}
	};
	private MenuItem menuFilter = new MenuItem("Filter", 100050, 2000) 	// filter by number
	{
		public void run() 
		{
			String number;
			if (filteredByTypeFlag == true)	// if filtered by type
				number = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.NUMBER];
			else	// if not filtered
				number = ((CallStrings)Historia.getVector().elementAt(selectedIndex)).strings[CallStrings.NUMBER];
			filterByNumber(number);
			for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
				callList.insert(i);
		}
	};
	private MenuItem menuDelete = new MenuItem("Delete", 100060, 2000) 	// delete a number
	{
		public void run() 
		{
			int index;
			String sn;
			
			storageStatus = checkStorage();
			if(storageStatus <= 0)
				return;
			Status.show("Deleting...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
			if (filteredByNumberFlag == false && filteredByTypeFlag == false ) 	// if in non filtered display
			{
				callList.setSize(0);
				Historia.getVector().removeElementAt(selectedIndex);
				saveCSV();				
				for (int i =0 ; i < Historia.getVector().size(); i++)
					callList.insert(i);
			}
			else if (filteredByNumberFlag == true )		// if in filtered by number
			{
				sn = ((CallStrings)vectorFilteredByNumberCalls.elementAt(selectedIndex)).strings[CallStrings.SN];
				callList.setSize(0);		
				vectorFilteredByNumberCalls.removeElementAt(selectedIndex);
				index = binarySearch(Historia.getVector(),sn);	// remove from Historia.getVector()
				if (index!= -1)
				{
					Historia.getVector().removeElementAt(index);
					saveCSV();
				}
				if (filteredByTypeFlag == true)
				{
					index = binarySearch(vectorFilteredByTypeCalls,sn);	// remove from filtered by type vector
					if (index!= -1) 
						vectorFilteredByTypeCalls.removeElementAt(index);
				}
				for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
					callList.insert(i);
			}
			else if (filteredByNumberFlag == false && filteredByTypeFlag == true)	// if in filtered by type display
			{
				sn = ((CallStrings)vectorFilteredByTypeCalls.elementAt(selectedIndex)).strings[CallStrings.SN];
				callList.setSize(0);
				vectorFilteredByTypeCalls.removeElementAt(selectedIndex);

				index = binarySearch(Historia.getVector(),sn);	// remove from Historia.getVector()
				if (index!= -1)
				{
					Historia.getVector().removeElementAt(index);
					saveCSV();
				}
				for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
					callList.insert(i);
			}						
		}
	};
	private MenuItem menuDeleteAll = new MenuItem("Delete All", 700000, 2000) 	// delete all numbers
	{
		public void run() 
		{

				int index;
				String sn;
				storageStatus = checkStorage();
				if(storageStatus <= 0)
					return;
				if (Dialog.ask(Dialog.D_YES_NO,"All calls shown in current display will be deleted, are you sure?",Dialog.NO) == Dialog.YES)  // warn the user
				{
					Status.show("Deleting...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
					if (filteredByNumberFlag == false && filteredByTypeFlag == false ) 	// if in non filtered display
					{
						callList.setSize(0);
						Historia.getVector().removeAllElements();
						saveCSV();
					}
					else if (filteredByNumberFlag == true )		// if in filtered by number display 
					{
						for (int i =0 ; i < vectorFilteredByNumberCalls.size(); i++)
						{
							sn = ((CallStrings)vectorFilteredByNumberCalls.elementAt(i)).strings[CallStrings.SN];
							index = binarySearch(Historia.getVector(),sn);	// remove from Historia.getVector()
							if (index != -1)
							{
								Historia.getVector().removeElementAt(index);
							}
							if (filteredByTypeFlag == true)	// if display was also difiltered by type
							{
								index = binarySearch(vectorFilteredByTypeCalls,sn);
								if (index != -1)
								{
									vectorFilteredByTypeCalls.removeElementAt(index);
								}
							}
						}
						saveCSV();
						callList.setSize(0);
						vectorFilteredByNumberCalls.removeAllElements();
					}
					else if (filteredByNumberFlag == false && filteredByTypeFlag == true)	// if in filtered by type display 
					{
						for (int i =0 ; i < vectorFilteredByTypeCalls.size(); i++)
						{
							sn = ((CallStrings)vectorFilteredByTypeCalls.elementAt(i)).strings[CallStrings.SN];
							index = binarySearch(Historia.getVector(),sn);	// remove from Historia.getVector()
							if (index != -1)
							{
								Historia.getVector().removeElementAt(index);
							}
	
						}
						callList.setSize(0);
						saveCSV();	
					}
				}
			}
		};
	private MenuItem menuSummary = new MenuItem("Summary", 600000, 2000) 	// delete all numbers
	{
		public void run() 
		{
			String duration;	// current row duration
			String[] durationSeparated;	// total duration separated strings
			int total = 0;	// total duration
			int i = 0;	//counter
			int hours;
			int minutes;
			if (filteredByNumberFlag == false && filteredByTypeFlag == false ) 	// if in non filtered display
			{
				for (; i < Historia.getVector().size(); i++)
				{
					duration = ((CallStrings)Historia.getVector().elementAt(i)).strings[CallStrings.DURATION];
					durationSeparated = split(duration,":");
					if ( durationSeparated.length == 3)
					{
						total+= Integer.parseInt(durationSeparated[0]) * 3600 + Integer.parseInt(durationSeparated[1]) * 60 + Integer.parseInt(durationSeparated[2]); 
					}
					else
					{
						total+= Integer.parseInt(durationSeparated[0]) * 60 + Integer.parseInt(durationSeparated[1]);
					}

				}

			}
			else if (filteredByNumberFlag == true )		// if in filtered by number display 
			{
				for (; i < vectorFilteredByNumberCalls.size(); i++)
				{
					duration = ((CallStrings)vectorFilteredByNumberCalls.elementAt(i)).strings[CallStrings.DURATION];
					durationSeparated = split(duration,":");
					if ( durationSeparated.length == 3)
					{
						total+= Integer.parseInt(durationSeparated[0]) * 3600 + Integer.parseInt(durationSeparated[1]) * 60 + Integer.parseInt(durationSeparated[2]); 
					}
					else
					{
						total+= Integer.parseInt(durationSeparated[0]) * 60 + Integer.parseInt(durationSeparated[1]);
					}

				}
			}
			else if (filteredByNumberFlag == false && filteredByTypeFlag == true)	// if in filtered by type display 
			{
				for (; i < vectorFilteredByTypeCalls.size(); i++)
				{
					duration = ((CallStrings)vectorFilteredByTypeCalls.elementAt(i)).strings[CallStrings.DURATION];
					durationSeparated = split(duration,":");
					if ( durationSeparated.length == 3)
					{
						total+= Integer.parseInt(durationSeparated[0]) * 3600 + Integer.parseInt(durationSeparated[1]) * 60 + Integer.parseInt(durationSeparated[2]); 
					}
					else
					{
						total+= Integer.parseInt(durationSeparated[0]) * 60 + Integer.parseInt(durationSeparated[1]);
					}

				}
			}
			//---- set duration, converting seconds to 0:00:00 format----
			duration = "";
			hours = total/3600;
			total %= 3600;
			duration += hours == 0 ? "" : (Integer.toString(hours) +":");
			minutes = total/60;
			duration += (Integer.toString(minutes).length() == 2 ? Integer.toString(minutes) : ("0" + Integer.toString(minutes))) + ":";
			total %= 60;
			duration +=  Integer.toString(total).length() == 2 ? Integer.toString(total) : ("0" + Integer.toString(total));
			//------------------------
			Dialog.inform("Number of Calls: " + i + "\n" +"Total Duration: " + duration);
		}
	};

	public void notifyScreenClosed(MainScreen screen)	// when settings is closed invalidate display
	{
		try
		{
			// check the event source object
			if (screen.equals(settings)) 
			{
				int callsLimit = Integer.parseInt((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS)); // get call limit from file

				Font myFont;
				Graphics g = this.getGraphics();	// for getting font height
				int size = Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) + 6;
				if (Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)) != 0)		// set font
				{
					myFont = Font.getDefault().derive(Font.getDefault().getStyle(), size);	// for setting font size
				}
				else 
				{
					myFont = Font.getDefault();
				}
				g.setFont(myFont);	// set font size
				callList.setRowHeight(g.getFont().getHeight()*ROWCONST/100);	// set row height in list
				if (callsLimit < Historia.getVector().size())    // if call limit is less then current number of calls
				{
					callList.setSize(0);
					while (callsLimit < Historia.getVector().size()) // loop and delete all lines after the call limit location
						Historia.getVector().removeElementAt(callsLimit);
					saveCSV();	// store new calls in csv file;  // update the csv file

					if (filteredByTypeFlag == false && filteredByNumberFlag == false) // if display is not filtered
					{
						for (int i = 0 ; i < Historia.getVector().size(); i++)
							callList.insert(i);
					}
					else if (filteredByTypeFlag == true && filteredByNumberFlag == false )    // if table is filtered by type
					{
						if (menuMissed.toString().equals(Characters.CHECK_MARK  + "Filter by Missed"))   // if filtered to missed
						{
							filterByType("Missed");   //filter again
						}
						else if (menuOutgoing.toString().equals(Characters.CHECK_MARK  + "Filter by Outgoing"))   // if filtered to Outgoing
						{
							filterByType("Outgoing");   //filter again
						}
						else    // if filtered to incoming
						{
							filterByType("Incoming");   //filter again
						}
						for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
							callList.insert(i);
					}
					else if (filteredByNumberFlag == true && filteredByTypeFlag == false)   // if filtered by number
					{
						filterByNumber(((CallStrings) vectorFilteredByNumberCalls.elementAt(0)).strings[CallStrings.NUMBER]);   // filter by number again
						for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
							callList.insert(i);

					}
					else if (filteredByNumberFlag == true && filteredByTypeFlag == true)   // if filtered by number after filtered by type
					{
						if (menuMissed.toString().equals(Characters.CHECK_MARK  + "Filter by Missed"))   // if filtered to missed
							filterByType("Missed");   //filter again
						else if (menuOutgoing.toString().equals(Characters.CHECK_MARK  + "Filter by Outgoing"))   // if filtered to Outgoing
							filterByType("Outgoing");   //filter again
						else    // if filtered to incoming
							filterByType("Incoming");   //filter again

						filterByNumber(((CallStrings) vectorFilteredByNumberCalls.elementAt(0)).strings[CallStrings.NUMBER]);   // filter by number again
						for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
							callList.insert(i);
					}
				}
			}
		}
		catch(Exception e)
		{
			Dialog.alert("Error in closing settings window, if error persists contact support.\n" + e.getMessage());
			System.exit(1);
		}
	};
	public int binarySearch(Vector calls, String sn)
	{
		int min = 0;
		int max = calls.size() - 1;
		int mid;

		while(min <= max)
		{
			mid = (min+max) /2;
			if ((((CallStrings)calls.elementAt(mid)).strings[CallStrings.SN]).compareTo(sn) > 0)
				min = mid + 1;
			else if ((((CallStrings)calls.elementAt(mid)).strings[CallStrings.SN]).compareTo(sn) < 0)
				max = mid - 1;
			else
				return mid;
		}
		return -1;
	};
	protected void onExposed() 	//update list upon exposing
	{
		try
		{
		 	boolean flagCallsAdded = false;
					flagNewCallInCSV = false;	
							getCSV();
							flagCallsAdded = getCalls(false);					
						if (flagCallsAdded == true || flagNewCallInCSV == true)
						{
							Status.show("Loading...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
							saveCSV();	// store new calls in csv file
							callList.setSize(0);	// zero list
							if (filteredByTypeFlag == false && filteredByNumberFlag == false) // if display is not filtered
								for (int i = 0 ; i < Historia.getVector().size(); i++)
									callList.insert(i);
							else if (filteredByTypeFlag == true && filteredByNumberFlag == false )    // if table is filtered by type
							{
								if (menuMissed.toString().equals(Characters.CHECK_MARK  + "Filter by Missed"))   // if filtered to missed
									filterByType("Missed");   //filter again
								else if (menuOutgoing.toString().equals(Characters.CHECK_MARK  + "Filter by Outgoing"))   // if filtered to Outgoing
									filterByType("Outgoing");   //filter again
								else    // if filtered to incoming
									filterByType("Incoming");   //filter again
								for (int i = 0 ; i < vectorFilteredByTypeCalls.size(); i++)
									callList.insert(i);
							}
							else if (filteredByNumberFlag == true && filteredByTypeFlag == false && vectorFilteredByNumberCalls.size() != 0)   // if filtered by number
							{
								filterByNumber(((CallStrings) vectorFilteredByNumberCalls.elementAt(0)).strings[CallStrings.NUMBER]);   // filter by number again
								for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
									callList.insert(i);
							}
							else if (filteredByNumberFlag == true && filteredByTypeFlag == true)   // if filtered by number after filtered by type
							{
								if(menuMissed.toString().equals(Characters.CHECK_MARK  + "Filter by Missed"))   // if filtered to missed
									filterByType("Missed");   //filter again
								else if (menuOutgoing.toString().equals(Characters.CHECK_MARK  + "Filter by Outgoing"))   // if filtered to Outgoing
									filterByType("Outgoing");   //filter again
								else    // if filtered to incoming
									filterByType("Incoming");   //filter again
								if (vectorFilteredByNumberCalls.size() != 0 )
								{
									filterByNumber(((CallStrings) vectorFilteredByNumberCalls.elementAt(0)).strings[CallStrings.NUMBER]);   // filter by number again
									for (int i = 0 ; i < vectorFilteredByNumberCalls.size(); i++)
										callList.insert(i);
								}
							}
						}
		}
		catch(Exception e)
		{
			Dialog.alert("Error updating call log, if error persists contact support.\n" + e.getMessage());
			System.exit(1);
			
		}
	}

	public boolean onClose()	// restore previous values
	{
		if (filteredByNumberFlag == true)	// if filtered by number , go to previous screen
		{
			menuBack.run();
		}
		else
			super.onClose();
		return true;
	}
	public static int checkStorage()
	{
		boolean flagSD = false;
		boolean flagStore = false;
		String root = null;
		Enumeration e = FileSystemRegistry.listRoots();
		while (e.hasMoreElements())
		{
		     root = (String) e.nextElement();
		     if( root.equalsIgnoreCase("sdcard/"))
		     {
		        //device has a microSD inserted
		    	flagSD = true;
		     } 
		     else if( root.equalsIgnoreCase("store/") ) 
		     {
		       //internal memory identifier
		    	 flagStore = true;
		     }
		}
		if (flagSD == false && flagStore == false)
		{
			return 0;
		}
		else if (flagSD == false && ((String)Historia.keys.elementAt(Historia.LOCATION)).compareTo("1") == 0 ) // sd is not inserted and current location is storage
		{
			return -1;
		}
		else if (flagSD == false && ((String)Historia.keys.elementAt(Historia.LOCATION)).compareTo("0") == 0 ) // sd is not inserted and current location is device
		{
			return 2;
		}
		else
			return 1;
	}	
	public static void displayStorageError()
	{
		UiApplication.getUiApplication().invokeLater (new Runnable() {
		    public void run()
		    {
		    	switch(storageStatus)
				{
				case 0:
					Dialog.alert("Application can't access file in Mass Storage Mode. Exit Mass Storage Mode and try again.");
					break;
				case -1:
				case 2:
					Dialog.alert("SD-Card is not available. Insert card and try again.");
					break;
				}
		    }
		});	
		
	}
	public static void addCalendarEvent(CallStrings callStrings)
	{
		try 
		{
			String addString;
			// check if call should be added:
			if (((String)Historia.keys.elementAt(Historia.CALLTYPETOLOG)).compareTo("Custom") == 0 )
			{
				if(((String)Historia.keys.elementAt(Historia.LOGOUTGOING)).compareTo("0") == 0 &&
						callStrings.strings[CallStrings.TYPE].compareTo("Outgoing") == 0
						&& callStrings.strings[CallStrings.DURATION].compareTo("00:00") != 0)
					return;
				if(((String)Historia.keys.elementAt(Historia.LOGINCOMING)).compareTo("0") == 0 &&
						callStrings.strings[CallStrings.TYPE].compareTo("Incoming") == 0)
					return;
				if(((String)Historia.keys.elementAt(Historia.LOGMISSED)).compareTo("0") == 0 &&
						callStrings.strings[CallStrings.TYPE].compareTo("Missed") == 0)
					return;
				if(((String)Historia.keys.elementAt(Historia.LOGOUTGOING)).compareTo("0") == 0 &&
						callStrings.strings[CallStrings.TYPE].compareTo("Outgoing") == 0
						&& callStrings.strings[CallStrings.DURATION].compareTo("00:00") == 0)
					return;
					
			}
			if (((String)Historia.keys.elementAt(Historia.NUMBERSTOLOG)).compareTo("Custom") == 0 )
			{
				numbersToVector();
				int index = NumbersListScreen.binarySearchNumber(Historia.vectorNumbers, callStrings.strings[CallStrings.NUMBER]);
				if (Historia.vectorNumbers.size() <= index || ((String)(Historia.vectorNumbers.elementAt(index))).compareTo(
						callStrings.strings[CallStrings.NUMBER]) != 0)
				{
    				return;
    			}
					
			}
				
			// Creates/Opens an instance of the specified PIM list (Event List) in Read/Write mode
			EventList eventList = (EventList)PIM.getInstance().openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
			BlackBerryEvent bbe = (BlackBerryEvent) eventList.createEvent();
			addString = callStrings.strings[CallStrings.TYPE] + " call ";
			
			if(	callStrings.strings[CallStrings.TYPE].compareTo("Outgoing") == 0
					&& callStrings.strings[CallStrings.DURATION].compareTo("00:00") == 0)
				addString = "Unanswered" + " call ";
			addString += callStrings.strings[CallStrings.TYPE].compareTo("Outgoing") == 0 ? " to: " : " from: ";
			if (callStrings.strings[CallStrings.NAME].compareTo("Unknown") != 0)
				addString += callStrings.strings[CallStrings.NAME] + ", " ;
			addString += callStrings.strings[CallStrings.NUMBER];
			bbe.addString(Event.SUMMARY, 0, addString);
			addString = "Duration: " + callStrings.strings[CallStrings.NUMBER];
			if (callStrings.strings[CallStrings.CELLID].compareTo("Not Available") != 0 )
				bbe.addString(Event.LOCATION, 0, callStrings.strings[CallStrings.CELLID]);
			bbe.addString(Event.NOTE,0,"Duration: " + callStrings.strings[CallStrings.DURATION]);
			bbe.addDate(Event.START,0 , Long.parseLong(callStrings.strings[CallStrings.SN]));
			bbe.addDate(Event.END,0 , Long.parseLong(callStrings.strings[CallStrings.SN]));
			if(eventList.isSupportedField(BlackBerryEvent.FREE_BUSY))
			{
                if (bbe.countValues(BlackBerryEvent.FREE_BUSY) > 0)
                {
                        bbe.setInt(BlackBerryEvent.FREE_BUSY, 0, Event.ATTR_NONE, BlackBerryEvent.FB_FREE);
                }
                else 
                {
                        bbe.addInt(BlackBerryEvent.FREE_BUSY, Event.ATTR_NONE, BlackBerryEvent.FB_FREE);
                }
			}
			if (eventList.items(bbe).hasMoreElements() == false)
				bbe.commit();		
		}
		catch(Exception e)
		{
			// Handle Exception
		}
	}
}

