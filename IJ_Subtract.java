import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class IJ_Subtract implements PlugInFilter {

	ImagePlus imp;
	
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL;
	}

	public void run(ImageProcessor ip) {
		ImageStack ims = imp.getStack();
		int num = ims.getSize();
		int wid = ims.getWidth();
		int hei = ims.getHeight();
		double[][] average = new double[hei][wid]; 
		for(int i = 1; i <= num; i++) {
			ip = ims.getProcessor(i);
			for(int h = 0; h < hei; h++) {
				for(int w = 0; w < wid; w++) {
					average[h][w] += (double)ip.getPixel(w, h)/num;
				}
			}
		}
		for(int j = 1; j <= num; j++) {
			ip = ims.getProcessor(j);
			for(int h = 0; h < hei; h++) {
				for(int w = 0; w < wid; w++) {
					if(ip.getPixel(w, h) < average[h][w]) ip.set(w, h, 0);
					else ip.set(w, h, (int)(ip.getPixel(w, h) - average[h][w]));
				}
			}
		}
	}
}
