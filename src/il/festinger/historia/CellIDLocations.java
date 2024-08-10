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
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.*;

final class CellIDLocations extends MainScreen
{
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private ListField listCellID;
	private ListCallBack callBack;	// callBack of list of calls
	private HorizontalFieldManager horizontalTitle;
	private VerticalFieldManager verticalCells;
	private VerticalFieldManager verticalBottom;
	private HorizontalFieldManager horizontalButtons;
	private BasicEditField basicEditCell;
	private BasicEditField basicEditLocation;
	private ButtonField buttonDelete;
	private ButtonField buttonAdd;
	private Vector vectorCellID;
//	private Vector copyVectorCellID;
	private int cellID;
	private String location;
    public CellIDLocations(Vector passedVectorCellID,int passedCellID) //class of a call
    {
	    super();
    	try
    	{
    		vectorCellID = new Vector(passedVectorCellID.size());
    		for (int i = 0 ; i < passedVectorCellID.size(); i ++)
    			vectorCellID.addElement(passedVectorCellID.elementAt(i));
    		cellID = passedCellID;
			Graphics g = this.getGraphics();	// for getting font height
			int spaceHeight = (g.getFont().getHeight()) * 50/100;	// space height
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" Cell IDs");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	        //-----------UI-------------------------------------   
			showList();
			if ( cellID != -1 )
			{
				int index = binarySearchCellID(cellID);
				if ( index <= vectorCellID.size() &&  ((CellIDStrings)(vectorCellID.elementAt(index))).getCellID() == cellID )
				{
					location = ((CellIDStrings)(vectorCellID.elementAt(index))).getLocation();
				}
				
			}
			
			//-----add to bottom--------
			basicEditCell = new BasicEditField("Cell ID: ",cellID == -1? "" : String.valueOf(cellID));
			basicEditLocation = new BasicEditField("Location: ",location);
			verticalBottom = new VerticalFieldManager();
			verticalBottom.add(new SpaceField(Display.getWidth(),spaceHeight));
			verticalBottom.add(basicEditCell);
			verticalBottom.add(basicEditLocation);
			verticalBottom.add(new SpaceField(Display.getWidth(),spaceHeight));
			horizontalButtons = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
			buttonDelete = new ButtonField("Delete",ButtonField.CONSUME_CLICK );
			buttonAdd = new ButtonField("Add/Modify",ButtonField.CONSUME_CLICK );
			horizontalButtons.add(buttonAdd);
			horizontalButtons.add(new LabelField(" "));
			horizontalButtons.add(buttonDelete);
			verticalBottom.add(horizontalButtons);
			verticalBottom.add(new SpaceField(Display.getWidth(),spaceHeight));
			setStatus(verticalBottom);
	        //------------UI--------------------------------------
    	}
    	catch(Exception e)
    	{
    		Dialog.inform("Error opening cell IDs . If error persists contact support.\n" + e.getMessage());
    		UiApplication.getUiApplication().popScreen(this);
    	}
        FieldChangeListener listenerDelete = new FieldChangeListener()	//delete button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		
        	}
        };
        buttonDelete.setChangeListener(listenerDelete);	// listen to button
        FieldChangeListener listenerAdd = new FieldChangeListener()	//add button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		if (containsOnlyNumbers(basicEditCell.getText()) == true && basicEditLocation.getText().length() != 0)
        		{
        			listCellID.setSize(0);
        			int index = binarySearchCellID(Integer.parseInt(basicEditCell.getText()));
        				
        			if (vectorCellID.size() <= index || ((CellIDStrings)(vectorCellID.elementAt(index))).getCellID() != Integer.parseInt(basicEditCell.getText()))
        			{
        				vectorCellID.insertElementAt(new CellIDStrings(Integer.parseInt(basicEditCell.getText()),
        						basicEditLocation.getText()), index);
        			}
        			else
        				((CellIDStrings)(vectorCellID.elementAt(index))).setLocation(basicEditLocation.getText());
        			for (int i = 0 ; i < vectorCellID.size(); i++)
        				listCellID.insert(i);       		
        		}
    			
        	}
        };
        buttonAdd.setChangeListener(listenerAdd);	// listen to button
    }
    /**
     * This method checks if a String contains only numbers
     */
    public boolean containsOnlyNumbers(String str) {
        
        //It can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {

            //If we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }
	public int binarySearchCellID(int cellID)
	{
		int min = 0;
		int max = vectorCellID.size() - 1;
		int mid;

		while(min <= max)
		{
			mid = (min+max) /2;
			if (((CellIDStrings)vectorCellID.elementAt(mid)).getCellID() < cellID)
				min = mid + 1;
			else if (((CellIDStrings)vectorCellID.elementAt(mid)).getCellID() > cellID)
				max = mid - 1;
			else
				return mid;
		}
		return min;
	};
    public void showList()	// show call list on screen
	{
    	listCellID = new ListField();	// list of calls
		callBack = new ListCallBack();	//callback for list of calls
		listCellID.setCallback(callBack);
		Graphics g = this.getGraphics();	// for getting font height
		listCellID.setRowHeight(g.getFont().getHeight() * 150/100);	// set row height in list
		verticalCells = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.USE_ALL_WIDTH); //manager for calls
		
		for (int i=0; i < vectorCellID.size(); i++) // insert elements to the call row
		{
			listCellID.insert(i);
		}
		verticalCells.add(listCellID);	//add list of call to calls manager
		add(verticalCells);	//add main manager to screen

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
    		final int POS = 10;	// starting position of strings
    		
    		String stringLocation  = ((CellIDStrings) vectorCellID.elementAt(index)).getLocation();
    		String stringCellID  = String.valueOf(((CellIDStrings) vectorCellID.elementAt(index)).getCellID());
    		g.drawText(stringCellID, POS , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP),(width - POS) *50/100 );
			g.drawText(stringLocation, POS + (width - POS) *50/100  + 5 , y + height*25/100, (DrawStyle.LEFT + DrawStyle.ELLIPSIS + DrawStyle.TOP), (width - POS) *50/100  - 10);
			g.setColor(Color.LIGHTGREY);
			g.drawLine(0 , y + height*150/100 - 1 ,Display.getWidth() ,  y + height*150/100 - 1);	// draw line under row
			g.drawLine((width - POS) *50/100  , y  ,(width - POS) *50/100, y + height*150/100 - 1);	// draw line vertically
    					
    	}
    	public Object get(ListField list, int index)
    	{
    		return vectorCellID.elementAt(index);
    	}
    	public int indexOfList(ListField list, String p, int s) 
    	{
    		return vectorCellID.indexOf(p, s);
    	}
    	public int getPreferredWidth(ListField list)
    	{
    		return Display.getWidth();
    	}
    }
}


