%_PACKAGE_%

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.frame.*;

public class %_CLASS_% extends PlugInFrame {

	public Plugin_Frame() {
		super("Plugin_Frame");
		TextArea ta = new TextArea(15, 50);
		add(ta);
		pack();
		GUI.center(this);
		show();
	}

}
