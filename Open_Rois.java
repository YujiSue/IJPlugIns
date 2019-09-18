import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;

public class Open_Rois implements PlugIn {

	public void run(String arg) {
		try {
		ImagePlus imp = IJ.getImage();
		String name = imp.getTitle();
		name = name.substring(0, name.lastIndexOf("."));
		RoiManager rom = (RoiManager)WindowManager.getFrame("ROI Manager");
		ImageProcessor ip = imp.getProcessor();
		Roi[] rois = rom.getRoisAsArray();
		for (int i = 0; i < rois.length; i++) {
			ip.setRoi(rois[i]);
			ImagePlus imp2 = new ImagePlus(name+"_"+(i+1), ip.crop());
			imp2.show();
		}
	} catch(IOException ie) { ie.printStackTrace(); }  
	}

}
