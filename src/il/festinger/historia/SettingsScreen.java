package il.festinger.historia;

import java.util.Vector;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.*;

final class SettingsScreen extends MainScreen implements ScreenClosedListener
{
	private Bitmap titleBitmap = Bitmap.getBitmapResource("view_title_settings.png");	// title icon
	private Bitmap aboutBitmap = Bitmap.getBitmapResource("settings_about.png");	// title icon
	private Bitmap calendarBitmap = Bitmap.getBitmapResource("settings_calendar_logging.png");
	private Bitmap generalBitmap = Bitmap.getBitmapResource("settings_general.png");
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private ListField listSettings;
	private ListCallBack callBack;	// callBack of list of calls
	private HorizontalFieldManager horizontalTitle;
	private VerticalFieldManager verticalSettings;
	private Vector vectorNumbers;
	private LicenseScreen licenseScreen;
	private Vector vectorSettings;
	private int firstTime;	// for hacking the selection of first row
	private GeneralSettings generalSettings;	// create a settings window	
	private CalendarSettingsScreen calendarSettings;	// create a settings window	
    private ScreenClosedListener listener;	// listener for closing event of the window
    public SettingsScreen(ScreenClosedListener passedListener, Vector passedVectorNumbers) //class of a call
    {
    	super(MainScreen.FOCUSABLE | MainScreen.NO_VERTICAL_SCROLL);
    	try
    	{
    		firstTime = 0;
    		listener = passedListener;
    		vectorNumbers = passedVectorNumbers;
    		vectorSettings = new Vector(3);
    		vectorSettings.addElement("1. General Settings");
    		vectorSettings.addElement("2. Calendar Logging");
    		vectorSettings.addElement(((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0?
    				"3. About/Register" : "3. About");
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" Settings");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(new LabelField(" "));
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	        //-----------UI-------------------------------------   
			showList();
			
    	}
    	catch(Exception e)
    	{
    		Dialog.inform("Error opening settings . If error persists contact support.\n" + e.getMessage());
    		UiApplication.getUiApplication().popScreen(this);
    	}
    }
    public boolean navigationClick (int status , int time)
    {
    	Field focus = UiApplication.getUiApplication().getActiveScreen() .getLeafFieldWithFocus();
    	if (focus instanceof ListField)
    	{
    		firstTime++;
        	((ListField) focus).invalidate();
    		switch(((ListField) focus).getSelectedIndex())
    		{
    			case 0:
    				generalSettings = new GeneralSettings();	// create the settings window
    				UiApplication.getUiApplication().pushScreen(generalSettings);
    				break;
    			case 1:
    				calendarSettings = new CalendarSettingsScreen(vectorNumbers);	// create the settings window
    				UiApplication.getUiApplication().pushScreen(calendarSettings);
    				break;
    			case 2:
    				licenseScreen = new LicenseScreen(this);	// create the settings window
    				UiApplication.getUiApplication().pushScreen(licenseScreen);
    				break;
    		}
    	}
    	return true;    			
    }
    public void showList()	// show call list on screen
	{
    	listSettings = new ListField();	// list of calls
		callBack = new ListCallBack();	//callback for list of calls
		listSettings.setCallback(callBack);
		Graphics g = this.getGraphics();	// for getting font height
		listSettings.setRowHeight(g.getFont().getHeight() * 300/100);	// set row height in list
		verticalSettings = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.USE_ALL_WIDTH); //manager for calls
		for (int i=0; i < vectorSettings.size(); i++) // insert elements to the call row
		{
			listSettings.insert(i);
		}	
		verticalSettings.add(listSettings);	//add list of call to calls manager
		add(verticalSettings);	//add main manager to screen
		 if (DeviceInfo.getDeviceName().startsWith("95")) // fix touch devices click outside the setting window
	        {
			 	add(new BasicEditField(BasicEditField.FOCUSABLE | BasicEditField.READONLY));
	        }
		
	}
    public boolean onClose()	// restore previous values
    {
    	UiApplication.getUiApplication().popScreen(this);
    	return true;
    }
    private class ListCallBack implements ListFieldCallback // drawing call list
    {
    	public void drawListRow(ListField list, Graphics g, int index, int y, int w)	// draw row 
    	{
    		int height = g.getFont().getHeight();
    		int width = Display.getWidth();
    		int selectedItem = list.getSelectedIndex();
    		// hack to prevent the stupid selection on the first row
    		if (index == 0 && firstTime < 2)
    		{
    			selectedItem = - 1;
    			firstTime ++;
    		}
    		if(selectedItem == index)
    	    {
    	         g.setColor(Color.BLUE);
    	         g.fillRect(0,y,Display.getWidth(),height*300/100);
    	         g.setColor(Color.WHITE);
    	    }
    	    else
    	    {
    	    	g.setColor(Color.LIGHTGREY);
   	         	g.fillRect(0,y,Display.getWidth(),height*300/100);
   	         	g.setColor(Color.BLACK);
    	    }
    		int pos = 10;	// starting position of strings
    		//---- add icon---------
			switch(index)
			{
			case 0:
				g.drawBitmap(pos, y + height*80/100, 30 , 30,generalBitmap, 0, 0 );
				break;
			case 1:
				g.drawBitmap(pos, y+ height*80/100, 30,30,calendarBitmap, 0, 0 );
				break;
			case 2:
				g.drawBitmap(pos, y + height*80/100, 30,30,aboutBitmap, 0, 0 );
				break;
			}
			pos = 60;
    		g.drawText((String)vectorSettings.elementAt(index), pos , y + height*100/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - pos) *100/100 );
			g.setColor(Color.DARKGRAY);
			g.drawLine(0 , y + height*300/100 - 1 ,Display.getWidth() ,  y + height*300/100 - 1);	// draw line under row	
    	}
    	public Object get(ListField list, int index)
    	{
    		return vectorSettings.elementAt(index);
    	}
    	public int indexOfList(ListField list, String p, int s) 
    	{
    		return vectorSettings.indexOf(p, s);
    	}
    	public int getPreferredWidth(ListField list)
    	{
    		return Display.getWidth();
    	}
    }
    protected boolean keyDown( int keycode, int status ){

    	if (firstTime != 2)
    		invalidate();   
       
        return super.keyDown( keycode, status );

    }



    protected boolean navigationMovement( int dx, int dy, int status, int time ){

        invalidate();
        if (listSettings.getSelectedIndex() == 2 && dy > 0)
        	return true;
        else if (firstTime == 2 && listSettings.getSelectedIndex() == 0 && dy > 0)
        {
        	firstTime ++;
        	listSettings.setSelectedIndex(0);
        	return true;
        }
        return super.navigationMovement( dx, dy, status, time );

    }
    public boolean trackwheelRoll(int amount, int status, int time)  // for trackwheel devices  
    {
        return navigationMovement(0,amount,status,time);
    }



    protected int moveFocus(int amount, int status, int time){

       invalidate();

       return super.moveFocus(amount, status, time);

    } 
    protected void onUndisplay() 	//listen to closed event
    {
    	listener.notifyScreenClosed(this);
    } 
	public void notifyScreenClosed(MainScreen screen)	// when settings is closed invalidate display
	{
		try
		{
			// check the event source object
			if (screen.equals(licenseScreen)) 
			{
				if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)
					vectorSettings.setElementAt("3. About/Register",2);
				else
					vectorSettings.setElementAt("3. About",2);
			}
		}
		catch(Exception e)
		{
			Dialog.alert("Error in closing license window, if error persists contact support.\n" + e.getMessage());
			System.exit(1);
		}
	};
    
}
