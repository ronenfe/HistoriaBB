package il.festinger.historia;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.SeparatorField;

class CustomSeparatorField extends SeparatorField
{
	 public int getPreferredWidth()
     {
         return Display.getWidth();
     }
     public int getPreferredHeight()
     {
         return 1;
     }
     protected void layout( int width, int height )
     {
         super.layout(getPreferredWidth(), 
                      getPreferredHeight());
         setExtent(getPreferredWidth(), getPreferredHeight());
     }
     public void paint(Graphics graphics)
     {
         graphics.setBackgroundColor(Color.LIGHTGREY);//red
         graphics.clear();
         super.paint(graphics);
     }
}