import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
//import java.sql.ResultSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import ij.text.TextWindow;


public class Library_Measure extends PlugInFrame implements ActionListener {

	
	String prefPath;
	
	
	
	SqliteDB db;
	ImagePlus imp;
	ImageProcessor ip;
	Overlay ol;
	String dbpath = "";
	double threshold_p = 0.2;
	double threshold_n = -0.2;
	int lane = 9;
	double mass2mol = 1.0;
	double correct = 1.0;
	double[] standard = {250.0, 500.0, 1000.0, 2000.0};
	double[] curve_coef = new double[6];
	
	Library_Measure instance;
	
	JTable dataTbl;
	DefaultTableModel model;
	
	JButton openBtn, calcBtn, saveBtn, uploadBtn;
	JTextField dbpathTF, pthreTF, nthreTF, laneTF, convertTF, correctionTF;
	
	
	public Library_Measure() {
		super("LibraryMeasure");
		setLayout(new BorderLayout());
		
		
		if(IJ.isMacOSX()) prefPath = System.getProperty("user.home")+File.separator+"Library"+File.separator+"Preferences"+File.separator+"LibMeasure.pref";
		else if(IJ.isWindows()) prefPath = System.getProperty("user.home")+File.separator+"AppData"+File.separator+"Local"+File.separator+"LibMeasure.pref";
		else if(IJ.isLinux()) prefPath = System.getProperty("user.home")+File.separator+"Preferences"+File.separator+"LibMeasure.pref";
		
		if((new File(prefPath)).exists()) loadPref(prefPath);
		else savePref(prefPath);
		
		instance = this;
		db = null;
		openDB();
		
		imp = WindowManager.getCurrentImage();
		imp.setCalibration(null);
		ip = imp.getProcessor();
		ol = new Overlay();
		imp.setOverlay(ol);
		
		Panel pane = new Panel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		createEmptyModel();
		dataTbl = new JTable(model);
		JScrollPane jsp = new JScrollPane(dataTbl);
		pane.add(jsp);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane1 = new JPanel();
		pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
		pane1.add(new JLabel("Database:"));
		dbpathTF = new JTextField(""+dbpath);
		dbpathTF.setPreferredSize(new Dimension(120,30));
		pane1.add(dbpathTF);
		openBtn = new JButton("Open");
		openBtn.addActionListener(this);
		pane1.add(openBtn);
		pane1.add(Box.createHorizontalGlue());
		pane.add(pane1);
		pane.add(Box.createVerticalStrut(5));

		JPanel pane2 = new JPanel();
		pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
		pane2.add(new JLabel("pg->pM:"));
		convertTF = new JTextField(""+mass2mol);
		convertTF.setPreferredSize(new Dimension(120,30));
		pane2.add(convertTF);
		pane2.add(Box.createHorizontalGlue());
		pane.add(pane2);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane3 = new JPanel();
		pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
		pane3.add(new JLabel("Correct:"));
		correctionTF = new JTextField(""+correct);
		correctionTF.setPreferredSize(new Dimension(120,30));
		pane3.add(correctionTF);
		pane3.add(Box.createHorizontalGlue());
		pane.add(pane3);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane4 = new JPanel();
		pane4.setLayout(new BoxLayout(pane4, BoxLayout.X_AXIS));
		pane4.add(new Label("Number of lanes:"));
		laneTF = new JTextField(""+lane);
		laneTF.setPreferredSize(new Dimension(120,30));
		pane4.add(laneTF);
		pane4.add(Box.createHorizontalGlue());
		pane.add(pane4);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane5 = new JPanel();
		pane5.setLayout(new BoxLayout(pane5, BoxLayout.X_AXIS));
		pane5.add(new Label("Threshold of left edge:"));
		pthreTF = new JTextField(""+threshold_p);
		pthreTF.setPreferredSize(new Dimension(120,30));
		pane5.add(pthreTF);
		pane5.add(Box.createHorizontalGlue());
		pane.add(pane5);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane6 = new JPanel();
		pane6.setLayout(new BoxLayout(pane6, BoxLayout.X_AXIS));
		pane6.add(new Label("Threshold of right edge:"));
		nthreTF = new JTextField(""+threshold_n);
		nthreTF.setPreferredSize(new Dimension(120,30));
		pane6.add(nthreTF);
		pane6.add(Box.createHorizontalGlue());
		pane.add(pane6);
		pane.add(Box.createVerticalStrut(5));
		
		JPanel pane7 = new JPanel();
		pane7.setLayout(new BoxLayout(pane7, BoxLayout.X_AXIS));
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(this);
		pane7.add(saveBtn);
		pane7.add(Box.createHorizontalGlue());
		uploadBtn = new JButton("Upload");
		uploadBtn.addActionListener(this);
		calcBtn = new JButton("Measure");
		calcBtn.addActionListener(this);
		pane7.add(uploadBtn);
		pane7.add(calcBtn);
		pane.add(pane7);
		
		this.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
		this.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);
		this.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
		this.add(Box.createVerticalStrut(5), BorderLayout.EAST);
		this.add(pane, BorderLayout.CENTER);
		
		this.addWindowListener(this);
		
		this.pack();
		this.setVisible(true);
		
	}

	private void createEmptyModel() {
		String[] column = {"Lane","Sample","Fluorescent","Conc.(pg/ul)","Conc.(pM)"};
		String[][] dat = new String[10][5];
		for(int i = 1; i <= 10; i++) {
			dat[i-1][0] = ""+i;
			if(i <= 4) {
				dat[i-1][1] = "Std."+i;
				dat[i-1][3] = ""+standard[i-1];
			}
		}
		model = new DefaultTableModel(dat, column);
	}

	private void openDB() {
		db = new SqliteDB(dbpath);
		if(!db.connected()) db = null;
	}

	private void savePref(String prefpath) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefpath),"UTF-8"));
			bw.write("dbpath\t"+dbpath+"\n");
			bw.write("lane\t"+lane+"\n");
			bw.write("pthreshold\t"+threshold_p+"\n");
			bw.write("nthreshold\t"+threshold_n+"\n");
			bw.write("massmol\t"+mass2mol+"\n");
			bw.write("correction\t"+correct+"\n");
			bw.write("standard\t"+standard[0]+"\t"+standard[1]+"\t"+standard[2]+"\t"+standard[3]);
			bw.close();
		} catch(Exception ex) {
			IJ.showMessage(ex.getStackTrace()[0].toString());
		}
	}

	private void loadPref(String prefpath) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(prefpath),"UTF-8"));
			String row;
			while((row = br.readLine()) != null) {
				String[] dat = row.split("\t");
				if(dat[0].matches("dbpath")) dbpath = dat[1];
				else if(dat[0].matches("lane")) lane = new Integer(dat[1]);
				else if(dat[0].matches("pthreshold")) threshold_p = new Double(dat[1]);
				else if(dat[0].matches("nthreshold")) threshold_n = new Double(dat[1]);
				else if(dat[0].matches("massmol")) mass2mol = new Double(dat[1]);
				else if(dat[0].matches("correction")) correct = new Double(dat[1]);
				else if(dat[0].matches("standard")) {
					standard[0] = new Double(dat[1]);
					standard[1] = new Double(dat[2]);
					standard[2] = new Double(dat[3]);
					standard[3] = new Double(dat[4]);
				}
			}
			br.close();
		} catch(Exception ex) {
			IJ.showMessage(ex.getStackTrace()[0].toString());
		}
		
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == openBtn) {
			FileDialog fd = new FileDialog(this, "Open Database", FileDialog.LOAD);
			fd.setVisible(true);
			if(fd.getFile() != null) {
				dbpath = fd.getDirectory()+fd.getFile();
				dbpathTF.setText(dbpath);
				openDB();
			}
		}
		else if(ae.getSource() == calcBtn) {
			if(db == null) IJ.showMessage("Database is not connected. Please input the concentration to database manually.");
			ol.clear();
			Roi roi = imp.getRoi();
			dbpath = dbpathTF.getText();
			mass2mol = (new Double(convertTF.getText())).doubleValue();
			correct = (new Double(correctionTF.getText())).doubleValue();
			standard[0] = (new Double(model.getValueAt(0, 3).toString())).doubleValue();
			standard[1] = (new Double(model.getValueAt(1, 3).toString())).doubleValue();
			standard[2] = (new Double(model.getValueAt(2, 3).toString())).doubleValue();
			standard[3] = (new Double(model.getValueAt(3, 3).toString())).doubleValue();
			lane = (new Integer(laneTF.getText())).intValue();
			threshold_p = (new Double(pthreTF.getText())).doubleValue();
			threshold_n = (new Double(nthreTF.getText())).doubleValue();
			
			if(roi != null) {
				int x = roi.getBounds().x;
				int y = roi.getBounds().y;
				int w = roi.getBounds().width;
				int h = roi.getBounds().height;
				
				int[] colval = new int[w];
				for(int i = y; i < y+h; i++) { colval[0] += ip.get(x, i); }
				
				int minval = colval[0];
				int maxval = colval[0];
				
				for(int c = 1; c < w; c++) {
					for(int r = y; r < y+h; r++) { colval[c] += ip.get(c+x, r); }
					if(colval[c] < minval) minval = colval[c];
					if(colval[c] > maxval) maxval = colval[c];
				}
				
				double[] ncolval = new double[w];
				
				for(int c2 = 0; c2 < w; c2++) { ncolval[c2] = (double)(colval[c2]-minval)/(maxval-minval); }
				
				double[] ratio = new double[w];
				
				for(int c3 = 0; c3 < w; c3++) {
					if(c3 < 20 || c3 >= w-20) {
						ratio[c3] = 0;
						continue;
					}
					
					double ave1 = 0.0;
					for(int c_ = 20; c_ > 0; c_--) {
						ave1 += ncolval[c3-c_];
					}
					ave1 /= 20.0;
					
					double ave2 = 0.0;
					for(int c_ = 20; c_ > 0; c_--) {
						ave2 += ncolval[c3+c_];
					}
					ave2 /= 20.0;
					
					ratio[c3] = (ave2-ave1)/ave1;
				}
				
				int[] border = new int[2*lane+2];
				border[0] = x;
				border[2*lane+1] = x+w;
				int k = 1;
				
				double max = threshold_p;
				double min = threshold_n;
				boolean pos = true;
				
				for(int c4 = 1; c4 < w-1; c4++) {
					if(ratio[c4] > max && ratio[c4-1] < ratio[c4] && ratio[c4] > ratio[c4+1] && k < 2*lane+1) {
						if(pos) {
							border[k] = c4+x;
							max = ratio[c4];
						}
						else {
							k++;
							border[k] = c4+x;
							max = ratio[c4];
							pos = true;
							min = threshold_n;
						}
						
					}
					else if(ratio[c4] < min && ratio[c4-1] > ratio[c4] && ratio[c4] < ratio[c4+1] && k < 2*lane+1) {
						if(pos) {
							k++;
							border[k] = c4+x;
							min = ratio[c4];
							pos = false;
							max = threshold_p;
						}
						else {
							border[k] = c4+x;
							min = ratio[c4];
						}
						
					}
				}
				
				double[] fluo = new double[lane];
				
				double ave_area = (double)w*h/10.0;
				
				for(int j = 0; j < lane; j++) {
					Roi roi_ = new Roi(border[2*j],y,(border[2*j+1]-border[2*j]),h);
					ol.add(roi_);
					imp.setRoi(roi_);
					double cave1 = getAverage();
					roi_ = new Roi(border[2*j+1],y,(border[2*j+2]-border[2*j+1]),h);
					ol.add(roi_);
					imp.setRoi(roi_);
					double save = getAverage();
					int sarea = getArea();
					roi_ = new Roi(border[2*j+2],y,(border[2*j+3]-border[2*j+2]),h);
					ol.add(roi_);
					imp.setRoi(roi_);
					double cave2 = getAverage();
					fluo[j] = (double)sarea*(save-(cave1+cave2)/2.0)/ave_area;
					dataTbl.setValueAt(""+fluo[j], j, 2);
				}
				imp.updateAndDraw();
				
				
				double a = (((standard[3]-standard[2])*(fluo[2]-fluo[1])-(standard[2]-standard[1])*(fluo[3]-fluo[2]))/((fluo[3]-fluo[2])*(fluo[2]-fluo[1])*(fluo[3]-fluo[1]))-
						((standard[2]-standard[1])*(fluo[1]-fluo[0])-(standard[1]-standard[0])*(fluo[2]-fluo[1]))/((fluo[2]-fluo[1])*(fluo[1]-fluo[0])*(fluo[2]-fluo[0])))/(fluo[3]-fluo[0]);
				double b =  ((standard[3]-standard[2])*(fluo[2]-fluo[1])-(standard[2]-standard[1])*(fluo[3]-fluo[2]))/((fluo[3]-fluo[2])*(fluo[2]-fluo[1])*(fluo[3]-fluo[1]))-a*(fluo[1]+fluo[2]+fluo[3]);
				double c = (standard[1]-standard[0])/(fluo[1]-fluo[0])-a*(fluo[0]*fluo[0]+fluo[0]*fluo[1]+fluo[1]*fluo[1])-b*(fluo[0]+fluo[1]);
				double d = standard[0]-a*fluo[0]*fluo[0]*fluo[0]-b*fluo[0]*fluo[0]-c*fluo[0];
				curve_coef[0] = a;
				curve_coef[1] = b;
				curve_coef[2] = c;
				curve_coef[3] = d;
				/*
				double a1 = ((standard[2]-standard[1])*(fluo[1]-fluo[0])-(standard[1]-standard[0])*(fluo[2]-fluo[1]))/((fluo[2]-fluo[1])*(fluo[1]-fluo[0])*(fluo[2]-fluo[0]));//{(y3-y2)(x2-x1)-(y2-y1)(x3-x2)}/{(x3-x2)(x2-x1)(x3-x1)}
				double b1 = (standard[1]-standard[0]-a1*(fluo[1]*fluo[1]-fluo[0]*fluo[0]))/(fluo[1]-fluo[0]);//((y2-y1)-c(x2^2-x1^2))/(x2-x1)
				double c1 = standard[0]-fluo[0]*b1-fluo[0]*fluo[0]*a1;
				
				curve_coef[0] = a1;
				curve_coef[1] = b1;
				curve_coef[2] = c1;
				
				double a2 = ((standard[3]-standard[2])*(fluo[2]-fluo[1])-(standard[2]-standard[1])*(fluo[3]-fluo[2]))/((fluo[3]-fluo[2])*(fluo[2]-fluo[1])*(fluo[3]-fluo[1]));//{(y3-y2)(x2-x1)-(y2-y1)(x3-x2)}/{(x3-x2)(x2-x1)(x3-x1)}
				double b2 = (standard[2]-standard[1]-a2*(fluo[2]*fluo[2]-fluo[1]*fluo[1]))/(fluo[2]-fluo[1]);//((y2-y1)-c(x2^2-x1^2))/(x2-x1)
				double c2 = standard[1]-fluo[1]*b2-fluo[1]*fluo[1]*a2;
				
				curve_coef[3] = a2;
				curve_coef[4] = b2;
				curve_coef[5] = c2;
				*/
				
				for(int l = 4; l < lane; l++) {
					BigDecimal massConc = new BigDecimal(a*fluo[l]*fluo[l]*fluo[l]+b*fluo[l]*fluo[l]+c*fluo[l]+d);
					massConc = massConc.setScale(2, BigDecimal.ROUND_HALF_UP);
					dataTbl.setValueAt(massConc.doubleValue()+"", l, 3);
					BigDecimal molConc = new BigDecimal(massConc.doubleValue()*mass2mol*correct);
					molConc = molConc.setScale(2, BigDecimal.ROUND_HALF_UP);
					dataTbl.setValueAt(molConc.doubleValue()+"", l, 4);
					/*
					if(fluo[l] <= fluo[2]) {
						BigDecimal massConc = new BigDecimal(a1*fluo[l]*fluo[l]+b1*fluo[l]+c1);
						massConc = massConc.setScale(2, BigDecimal.ROUND_HALF_UP);
						dataTbl.setValueAt(massConc.doubleValue()+"", l, 3);
						BigDecimal molConc = new BigDecimal(massConc.doubleValue()*mass2mol*correct);
						molConc = molConc.setScale(2, BigDecimal.ROUND_HALF_UP);
						dataTbl.setValueAt(molConc.doubleValue()+"", l, 4);
					}
					else {
						BigDecimal massConc = new BigDecimal(a2*fluo[l]*fluo[l]+b2*fluo[l]+c2);
						massConc = massConc.setScale(2, BigDecimal.ROUND_HALF_UP);
						dataTbl.setValueAt(massConc.doubleValue()+"", l, 3);
						BigDecimal molConc = new BigDecimal(massConc.doubleValue()*mass2mol*correct);
						molConc = molConc.setScale(2, BigDecimal.ROUND_HALF_UP);
						dataTbl.setValueAt(molConc.doubleValue()+"", l, 4);
					}
					*/
				}
				
			}
		}
		else if(ae.getSource() == saveBtn) {
			dbpath = dbpathTF.getText();
			mass2mol = (new Double(convertTF.getText())).doubleValue();
			correct = (new Double(correctionTF.getText())).doubleValue();
			standard[0] = (new Double(model.getValueAt(0, 3).toString())).doubleValue();
			standard[1] = (new Double(model.getValueAt(1, 3).toString())).doubleValue();
			standard[2] = (new Double(model.getValueAt(2, 3).toString())).doubleValue();
			standard[3] = (new Double(model.getValueAt(3, 3).toString())).doubleValue();
			lane = (new Integer(laneTF.getText())).intValue();
			threshold_p = (new Double(pthreTF.getText())).doubleValue();
			threshold_n = (new Double(nthreTF.getText())).doubleValue();
			this.savePref(prefPath);
			
			FileDialog fd = new FileDialog(this, "Save", FileDialog.SAVE);
			fd.setVisible(true);
			if(fd.getFile() != null) {
				String path = fd.getDirectory()+fd.getFile()+".xls";
				try {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
					bw.write("[Standard Curve]\n");
					bw.write("Curve1:"+curve_coef[0]+"\t"+curve_coef[1]+"\t"+curve_coef[2]+"\t"+curve_coef[3]+"\n");
					//bw.write("Curve2:"+curve_coef[3]+"*x^2+"+curve_coef[4]+"*x+"+curve_coef[5]+"\n");
					bw.write("[Data]\n");
					bw.flush();
					for(int r = -1; r < lane; r++) {
						for(int c = 0; c < 5; c++) {
							if(r == -1) bw.write(model.getColumnName(c)+"\t");
							else bw.write(model.getValueAt(r, c)+"\t");
						}
						bw.write("\n");
					}
					bw.close();
				} catch(Exception ex) {
					IJ.showMessage(ex.getStackTrace()[0].toString());
				}
			}
		}
		else if(ae.getSource() == uploadBtn) {
			try {
				String sql;
				for(int r = 4; r < lane; r++) {
					int id = new Integer(model.getValueAt(r, 1).toString());
					sql = "SELECT COUNT(*) FROM LIBRARY WHERE ID="+id;
					ResultSet rs = db.select(sql);
					if(rs.getInt(1) == 0) {
						IJ.showMessage("Library ID="+id+" is not registered in database.\nPlease register library information before measurement.");
						continue;
					}
					else {
						sql = "UPDATE LIBRARY SET LIBRARY_CONC_MASS="+model.getValueAt(r, 3)+",LIBRARY_CONC_MOL="+model.getValueAt(r, 4)+" WHERE ID="+id;
						db.execute(sql);
					}
				}
			} catch(Exception ex) {
				IJ.showMessage(ex.getStackTrace()[0].toString());
			}
		}
	}
	
	private double getAverage() {
		Roi roi = imp.getRoi();
		int x = roi.getBounds().x;
		int y = roi.getBounds().y;
		int w = roi.getBounds().width;
		int h = roi.getBounds().height;
		double average = 0.0;
		for(int c = x; c < x+w; c++) {
			for(int r = y; r < y+h; r++) {
				average += ip.get(c, r);
			}
		}
		average /= (double)w*h;
		return average;
	}
	
	private int getArea() {
		Roi roi = imp.getRoi();
		int w = roi.getBounds().width;
		int h = roi.getBounds().height;
		return w*h;
	}
}
