// Prototype plugin tool. There are more plugin tools at
// http://imagej.nih.gov/ij/plugins/index.html#tools
%_PACKAGE_%

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.tool.PlugInTool;
import java.awt.event.*;

public class %_CLASS_% extends PlugInTool {

	public void mousePressed(ImagePlus imp, MouseEvent e) {
		IJ.log("mouse pressed: "+e);
	}

	public void mouseDragged(ImagePlus imp, MouseEvent e) {
		IJ.log("mouse dragged: "+e);
	}

	public void showOptionsDialog() {
		IJ.log("icon double-clicked");
	}

}
