import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import skeleton_analysis.*;
import java.lang.Object.*;
import ij.measure.*;
import java.util.*;

public class Circular_Density implements PlugIn {
ResultsTable rt = ResultsTable.getResultsTable();
int x,y,xmax,ymax,step, xC, yC,cW,cH, diff;
ArrayList<Double> res;

		public void run(String arg) {
			IJ.log("\\Clear");
			ImagePlus imp =IJ.getImage();
			ImagePlus imp2 = new Duplicator().run(imp);
			ChannelSplitter CS = new ChannelSplitter();
			ImageStack imp2S=imp2.getImageStack();
			ImageStack[] IS=CS.splitRGB(imp2S, false);
			ImagePlus impB=new ImagePlus("blue",IS[2]);
			IJ.setAutoThreshold(impB, "Default");
			Prefs.blackBackground = true;
			IJ.run(impB, "Convert to Mask", "");
			IJ.run(impB, "Analyze Particles...", "size=1000-Infinity pixel circularity=0.00-1.00 show=Masks display exclude clear add in_situ");
			RoiManager rm = RoiManager.getInstance();
			Roi[] Rois=rm.getRoisAsArray();
			IJ.run(imp, "Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
			for (int i=0;i<rm.getCount();i++){
				rt.reset();
				IJ.run("Set Measurements...", "area mean center redirect=None decimal=2");
				Roi cRoi=Rois[i];
				imp.setRoi(cRoi);
				IJ.run(imp, "Measure", "");
				xC =(int)rt.getValue("XM",0);
				yC =(int)rt.getValue("YM",0);
				Rectangle cRect=cRoi.getBounds();
				x=cRect.x;
				y=cRect.y;
				xmax=cRect.x+cRect.width;
				ymax=cRect.y+cRect.height;
				diff=cRect.width-cRect.height;
				if (diff<0){
					step=10;
					cH=Math.abs(diff)+10;
					cW=step;
				} else {
					step=10;
					cH=step;
					cW=diff;
				}
				res=new ArrayList<Double>();
				int counter=0;
				double prevTot=0;
				double prevTA=0;
				rt.reset();
				while (xC+cW/2<xmax+50 && xC-cW/2>x-50 &&yC+cH/2<ymax+50 &&yC-cH/2>y-50){
					IJ.run(imp, "Specify...", "width="+cW+" height="+cH+" x="+xC+" y="+yC+" oval centered");
					cW+=step;
					cH+=step;
					IJ.run(imp, "Measure", "");
					res.add((rt.getValue("Mean",counter)*rt.getValue("Area",counter)-prevTot)/(rt.getValue("Area",counter)-prevTA));
					prevTot=rt.getValue("Mean",counter)*rt.getValue("Area",counter);
					prevTA=rt.getValue("Area",counter);
					counter++;
				}
				//rm.addRoi(imp.getRoi());
				String results=Arrays.toString(res.toArray());
				IJ.log(results.substring(1,results.length()-1));
			}
			
		}
}
