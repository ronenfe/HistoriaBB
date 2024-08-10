package il.festinger.historia;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

class SpaceField extends Field
{

    int localWidth, localHeight;
   
    SpaceField(int width, int height)
    {
        super(Field.NON_FOCUSABLE);
        localWidth = width;
        localHeight = height;
    }
   
    protected void layout(int Width, int height)
    {
        setExtent(localWidth, localHeight);
    }
   

    protected void paint(Graphics graphics)
    {
   
    }
   
   
    public int getPreferredWidth()
    {
        return localWidth;
    }

    public int getPreferredHeight()
    {
        return localHeight;
    }
}