import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Simple_Tracker implements PlugInFilter {

	ImagePlus imp;
	ImageStack ims;
	PolygonRoi track;
	
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G|DOES_16;
	}

	public void run(ImageProcessor ip) {
		ims = imp.getStack();
		Roi roi = imp.getRoi();
		int width = roi.getBounds().width, height = roi.getBounds().height;
		float[][] pos = new float[2][ims.getSize()];
		for (int s = 1; s <= ims.getSize(); s++) {
			imp.setSlice(s);
			float[] p = trackObj(roi);
			pos[0][s-1]= p[0]; pos[1][s-1] = p[1];
			roi = new Roi((int)(pos[0][s-1]-0.5f*width), (int)(pos[1][s-1]-0.5f*height), width, height);
			imp.setRoi(roi);
		}
		track = new PolygonRoi(pos[0], pos[1], ims.getSize(), Roi.POLYLINE);
		imp.setOverlay(new Overlay(track));
		BigDecimal dist_bd = new BigDecimal(distance(pos), new MathContext(5));
		IJ.showMessage("Distance: "+dist_bd.toString()+" px");
		return;
	}
	
	public float[] trackObj(Roi current) {
		ImageProcessor ip = imp.getProcessor();
		float[] center = new float[] { 0.0f, 0.0f };
		int[] ori = new int[] { current.getBounds().x, current.getBounds().y };
		int width = current.getBounds().width, height = current.getBounds().height;
		int count = 0;
		double min = ip.getMinThreshold(), max = ip.getMaxThreshold();
		while (true) {
			for (int r = 0; r < height; r++) {
				for (int c = 0; c < width; c++) {
					int val = ip.get(ori[0]+c, ori[1]+r);
					if (min <= val && val <= max) {
						center[0] += c;
						center[1] += r;
						++count;
					}
				}
			}
			center[0] /= (float)count;
			center[1] /= (float)count;
			int dx = (int)(center[0]-0.5f*width);
			int dy = (int)(center[1]-0.5f*height);
			if(dx == 0 && dy == 0)  break;
			else {
				ori[0] += dx;
				ori[1] += dy;
				center[0] = 0.0f;
				center[1] = 0.0f;
				count = 0;
			}
		}
		center[0] += ori[0];
		center[1] += ori[1];
		return center;
	}
	
	public double distance(float[][] pos) {
		double dist = 0.0;
		for (int i = 0; i < pos[0].length-1; i++) {
			dist += Math.sqrt((pos[0][i+1]-pos[0][i])*(pos[0][i+1]-pos[0][i])+(pos[1][i+1]-pos[1][i])*(pos[1][i+1]-pos[1][i]));
		}
		return dist;
	}
}
