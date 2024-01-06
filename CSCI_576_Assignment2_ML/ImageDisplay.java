
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.util.*;

public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	BufferedImage img2;
	int width;
	int height;
	ArrayList<int[]> initialCodebook;
	ArrayList<int[]> finalCodebook = null;

	public ImageDisplay(int width, int height, byte[] bytes, int vectorSize) {
		this.width = width;
		this.height = height; 
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		int pix = 0;
		for(int y = 0; y < height; y++){

			for(int x = 0; x < width; x++){
				byte r = bytes[ind];
				byte g = bytes[ind];
				byte b = bytes[ind];

				pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x,y,pix);
				ind++;
			}
		} 
		initialCodebook = initializeCodebookVectors(img, vectorSize);
	}

	public void setFinalCodebook(ArrayList<int[]> finalCodebook) {
		this.finalCodebook = finalCodebook;
	}

	public void drawCodebookVector (BufferedImage image, int x1, int y1) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.RED);
		g.drawOval(x1, y1, 5, 5);
		g.drawImage(image, 0, 0, null);
	}

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2, Color color) {
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	public BufferedImage createPlot (BufferedImage image, int pWidth, int pHeight, byte[] bytes) {
		BufferedImage newPlot = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_RGB);
		int pix = 0;
		// Initialize white background of vector space
		for(int y = 0; y < pHeight; y++){

			for(int x = 0; x < pWidth; x++){
				byte r = (byte) 255;
				byte g = (byte) 255;
				byte b = (byte) 255;

				pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				newPlot.setRGB(x,y,pix);
			}
		}
		drawLine(newPlot, 0, 0, pWidth-1, 0, Color.BLACK);				// top edge
		drawLine(newPlot, 0, 0, 0, pHeight-1, Color.BLACK);				// left edge
		drawLine(newPlot, 0, pHeight-1, pWidth-1, pHeight-1, Color.BLACK);	// bottom edge
		drawLine(newPlot, pWidth-1, pHeight-1, pWidth-1, 0, Color.BLACK); 	// right edge

		// Draw the vector space visualization
		for (int i = 0; i < width-1; i+=2) {
			for (int j = 0; j < height; j++) {
				
				int clr1 = image.getRGB(i, j);
				int x = clr1 & 0xff;
				int clr2 = image.getRGB(i+1, j);
				int y = clr2 & 0xff;

				drawLine(newPlot, x, y, x, y, Color.BLACK);
			}
		} 
		if (finalCodebook != null) {
			for (int i = 0; i < finalCodebook.size(); i++) {
				drawCodebookVector(newPlot, finalCodebook.get(i)[0], finalCodebook.get(i)[1]);
			}
		}
		return newPlot;
	}

	public ArrayList<int[]> getInitialCodebook () {
		return initialCodebook;
	}

	public ArrayList<int[]> initializeCodebookVectors (BufferedImage image, int M) {
		ArrayList<int[]> result = new ArrayList<int[]>();

		for (int i = 0; i < width-1; i+=2) {
			for (int j = 0; j < height; j++) {
				int[] vector = new int[M];
				int clr1 = image.getRGB(i, j);
				int x = clr1 & 0xff;
				int clr2 = image.getRGB(i+1, j);
				int y = clr2 & 0xff;
				vector[0] = x;
				vector[1] = y;
				result.add(vector);
			}
		}
		return result;
	} 
	public double imageEuclidean (double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

	public void createOutputImage(BufferedImage image, int width, int height, int M) {
		for (int i = 0; i < width; i+=2) {
			for (int j = 0; j < height; j++) {
				int[] vector = new int[M];
				int clr1 = image.getRGB(i, j);
				int x = clr1 & 0xff;
				int clr2 = image.getRGB(i+1, j);
				int y = clr2 & 0xff;
				vector[0] = x;
				vector[1] = y;
				double minDist = Double.MAX_VALUE;
				int centroidAssignment = -1;
				for (int k = 0; k < finalCodebook.size(); k++) {
					double dist = imageEuclidean(x, y, finalCodebook.get(k)[0], finalCodebook.get(k)[1]);
					if (dist < minDist) {
						minDist = dist;
						centroidAssignment = k;
					}
				}
				int val1 = finalCodebook.get(centroidAssignment)[0];
				int val2 = finalCodebook.get(centroidAssignment)[1];
				int pix1 = 0xff000000 | ((val1 & 0xff) << 16) | ((val1 & 0xff) << 8) | (val1 & 0xff);
				int pix2 = 0xff000000 | ((val2 & 0xff) << 16) | ((val2 & 0xff) << 8) | (val2 & 0xff);
				img2.setRGB(i, j, pix1);
				img2.setRGB(i+1, j, pix2);
			}
		}
	}

	public void showIms(int width, int height, byte[] bytes, int vectorSize) {
		BufferedImage plot = createPlot(img, 255, 255, bytes);
		if (finalCodebook != null) {
			createOutputImage(img, width, height, vectorSize);
		}
			// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(img));
		if (finalCodebook != null) {
			lbIm2 = new JLabel(new ImageIcon(img2));
		}
		else {
			lbIm2 = new JLabel(new ImageIcon(img));
		}
		JLabel lbText3 = new JLabel("Vector Space");
		lbText3.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbIm3 = new JLabel(new ImageIcon(plot));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 0;
		frame.getContentPane().add(lbText3, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 1;
		frame.getContentPane().add(lbIm3, c);

		frame.pack();
		frame.setVisible(true);
	}

}