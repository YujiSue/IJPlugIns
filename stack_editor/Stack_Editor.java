import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.Calibrator;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;


public class Stack_Editor implements ExtendedPlugInFilter {

	int start, end, interval;
	int nslice;
	int cslice;
	boolean ori;
	
	ImagePlus imp;
	ImageStack ims;
	
	int nPass;
	
	
	public int setup(String arg, ImagePlus imp) {
		if (imp==null) {
			IJ.noImage();
			return DONE;
		}
		this.imp = imp;
		ims = this.imp.getStack();
		nslice = ims.getSize();
		start = 1;
		end = nslice;
		interval = 1;
		IJ.register(Stack_Editor.class);
		return DOES_ALL|STACK_REQUIRED;
	}

	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		ImageStack ims_ = new ImageStack(ims.getWidth(), ims.getHeight());
		String[] label = ims.getSliceLabels();
		for(int i = start; i <= end; i += interval) {
			ims_.addSlice(label[i], ims.getProcessor(i));
		}
		if(!ori) {
			ImagePlus imp_ = new ImagePlus(imp.getTitle()+"_Editted", ims_);
			imp_.show();
		} else {
			imp.setStack(ims_);
		}
		
	}

	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		GenericDialog gd = new GenericDialog("Stack Controler");
		gd.addNumericField("Start:", start, 0, 10, "");
		gd.addNumericField("End:", end, 0, 10, "");
		gd.addNumericField("Interval:", interval, 0, 10, "");
		cslice = pfr.getSliceNumber();
		if(cslice < 1) cslice =1;
		gd.addSlider("Slice:", start, end, cslice);
		Scrollbar sslider = (Scrollbar)(gd.getSliders().get(0));
		this.imp.setSlice(cslice);
		sslider.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				cslice = ae.getValue();
				Stack_Editor.this.imp.setSlice(cslice);
			}
		});
		gd.addCheckbox("Edit original stack", false);
		gd.showDialog();
		if(gd.wasCanceled()) return DONE;
		else {
			Vector v1 = gd.getNumericFields();
			start = new Integer(((TextField)v1.get(0)).getText());
			end = new Integer(((TextField)v1.get(1)).getText());
			interval = new Integer(((TextField)v1.get(2)).getText());
			ori = ((Checkbox)gd.getCheckboxes().get(0)).getState();
			return DOES_ALL|STACK_REQUIRED;
		}
	}

	public void setNPasses(int nPasses) {
		nPass = nPasses;
	}

}
