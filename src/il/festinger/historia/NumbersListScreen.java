package il.festinger.historia;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.*;

final class NumbersListScreen extends MainScreen
{
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private ListField listNumbers;
	private ListCallBack callBack;	// callBack of list of calls
	private HorizontalFieldManager horizontalTitle;
	private VerticalFieldManager verticalNumbers;
	private VerticalFieldManager verticalTop;
	private HorizontalFieldManager horizontalButtons;
	private BasicEditField basicEditNumber;
	private ButtonField buttonAdd;
	private Vector vectorNumbers;
	private Vector returnedVectorNumbers;
	private int selectedIndex = 0;
    public NumbersListScreen(Vector passedVectorNumbers) //class of a call
    {
	    super();
    	try
    	{
    		if (Main.storageStatus == 0)	// if in mass storage mode
    		{
    			Main.displayStorageError();
    			UiApplication.getUiApplication().popScreen(this);
    		}	
    		returnedVectorNumbers = passedVectorNumbers;
    		vectorNumbers = new Vector(passedVectorNumbers.size());
    		for (int i = 0 ; i < passedVectorNumbers.size(); i ++)
    			vectorNumbers.addElement(passedVectorNumbers.elementAt(i));
			Graphics g = this.getGraphics();	// for getting font height
			int spaceHeight = (g.getFont().getHeight()) * 50/100;	// space height
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" Settings");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	        //-----------UI-------------------------------------   
			//-----add to top--------
			basicEditNumber = new BasicEditField("Number: ","",20, BasicEditField.FILTER_PHONE );
			verticalTop = new VerticalFieldManager();
			verticalTop.add(new SpaceField(Display.getWidth(),spaceHeight));
			verticalTop.add(basicEditNumber);
			verticalTop.add(new SpaceField(Display.getWidth(),spaceHeight));
			horizontalButtons = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
			buttonAdd = new ButtonField("Add",ButtonField.CONSUME_CLICK );
			horizontalButtons.add(buttonAdd);
			verticalTop.add(horizontalButtons);
			verticalTop.add(new SpaceField(Display.getWidth(),spaceHeight));
			//----------------------
			add(verticalTop);
			add(new CustomSeparatorField()); 
			showList();
			
	        //------------UI--------------------------------------
    	}
    	catch(Exception e)
    	{
    		Dialog.inform("Error opening Number List. If error persists contact support.\n" + e.getMessage());
    		UiApplication.getUiApplication().popScreen(this);
    	}
        FieldChangeListener listenerAdd = new FieldChangeListener()	//add button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		if (basicEditNumber.getText().length() != 0)
        		{
        			listNumbers.setSize(0);
        			int index = binarySearchNumber(vectorNumbers, basicEditNumber.getText());
        				
        			if (vectorNumbers.size() <= index || ((String)(vectorNumbers.elementAt(index))).compareTo(basicEditNumber.getText()) != 0)
        			{
        				vectorNumbers.insertElementAt(basicEditNumber.getText(), index);
        				basicEditNumber.setText("");
        			}
        			else
        			{
        				Dialog.inform("Number already exists.");
        			}
        			for (int i = 0 ; i < vectorNumbers.size(); i++)
        				listNumbers.insert(i);  
        			setDirty(true);
        		}
    			
        	}
        };
        buttonAdd.setChangeListener(listenerAdd);	// listen to button
    }
	public static int binarySearchNumber(Vector vector, String number)
	{
		int min = 0;
		int max = vector.size() - 1;
		int mid;

		while(min <= max)
		{
			mid = (min+max) /2;
			if (((String)vector.elementAt(mid)).compareTo(number) < 0)
				min = mid + 1;
			else if (((String)vector.elementAt(mid)).compareTo(number) > 0)
				max = mid - 1;
			else
				return mid;
		}
		return min;
	};
    public void showList()	// show call list on screen
	{
    	listNumbers = new ListField();	// list of calls
		callBack = new ListCallBack();	//callback for list of calls
		listNumbers.setCallback(callBack);
		Graphics g = this.getGraphics();	// for getting font height
		listNumbers.setRowHeight(g.getFont().getHeight() * 150/100);	// set row height in list
		verticalNumbers = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.USE_ALL_WIDTH); //manager for calls
		
		for (int i=0; i < vectorNumbers.size(); i++) // insert elements to the call row
		{
			listNumbers.insert(i);
		}
		verticalNumbers.add(listNumbers);	//add list of call to calls manager
		add(verticalNumbers);	//add main manager to screen

	}
    protected boolean onSave()		// if save was chosen
    {
		returnedVectorNumbers.removeAllElements();
		for (int i = 0 ; i < vectorNumbers.size(); i ++)
			returnedVectorNumbers.addElement(vectorNumbers.elementAt(i));
		Main.saveToFile(vectorNumbers, "Numbers.txt");
    	return true;
    }
	protected void makeMenu(Menu menu, int instance)	// make application menu
	{
		super.makeMenu(menu, instance);
		
		if (instance == Menu.INSTANCE_DEFAULT)
		{
			if (listNumbers.getSize() != 0)	// if calls exist in list
			{
				selectedIndex = listNumbers.getSelectedIndex();
				menu.add(menuDelete);
			}
		}
		else if (listNumbers.getSize() != 0)	// context menu if not filtered by number and list is not empty
		{
			selectedIndex = listNumbers.getSelectedIndex();
			menu.add(menuDelete);
		}
	}
	private MenuItem menuDelete = new MenuItem("Delete", 100060, 2000) 	// delete a number
	{
		public void run() 
		{
			listNumbers.setSize(0);
			vectorNumbers.removeElementAt(selectedIndex);
    			for (int i = 0 ; i < vectorNumbers.size(); i++)
    				listNumbers.insert(i);
    		setDirty(true);
		}
	};
    private class ListCallBack implements ListFieldCallback // drawing call list
    {
    	public void drawListRow(ListField list, Graphics g, int index, int y, int w)	// draw row 
    	{
    			
    		int height = g.getFont().getHeight();
    		int width = Display.getWidth();
    		final int POS = 10;	// starting position of strings
    		
    		String stringNumber  = (String) vectorNumbers.elementAt(index);
    		g.drawText(String.valueOf(index+1), POS , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - POS) *20/100 );
			g.drawText(stringNumber, POS + (width - POS) *20/100  + 5 , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - POS) *80/100  - 10);
			g.setColor(Color.LIGHTGREY);
			g.drawLine(0 , y + height*150/100 - 1 ,Display.getWidth() ,  y + height*150/100 - 1);	// draw line under row
			g.drawLine((width - POS) *20/100  , y  ,(width - POS) *20/100, y + height*150/100 - 1);	// draw line vertically
    					
    	}
    	public Object get(ListField list, int index)
    	{
    		return vectorNumbers.elementAt(index);
    	}
    	public int indexOfList(ListField list, String p, int s) 
    	{
    		return vectorNumbers.indexOf(p, s);
    	}
    	public int getPreferredWidth(ListField list)
    	{
    		return Display.getWidth();
    	}
    }
}


