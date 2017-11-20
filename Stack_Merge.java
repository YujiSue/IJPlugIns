import com.sun.org.apache.bcel.internal.generic.INEG;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Stack_Merge implements PlugInFilter {

	public int setup(String arg, ImagePlus imp) {
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		int[] ids = WindowManager.getIDList();
		int num = ids.length;
		ImageStack ims = new ImageStack(ip.getWidth(), ip.getHeight());
		for(int i = 0; i < num; ++i) {
			ImagePlus imp = WindowManager.getImage(ids[i]);
			if(imp == null) continue;
			ImageStack stack = imp.getImageStack();
			int snum = stack.getSize();
			for(int s = 0; s < snum; ++s) {
				ip = stack.getProcessor(s+1);
				ims.addSlice("image", ip);
			}
		}
		ImagePlus imp_ = new ImagePlus("Merged", ims);
		imp_.show();
	}
}
