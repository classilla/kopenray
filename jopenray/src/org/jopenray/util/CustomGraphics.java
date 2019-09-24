/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jopenray.util;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RepaintManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A custom Graphics2D which forward Graphics2D function and save the result
 * It's a test that will allow us to create a SwingAdapter... :)
 * 
 * */
public class CustomGraphics extends Graphics2D {

	private Graphics2D g;
	private BufferedImage bimage;

	public CustomGraphics(Graphics2D g) {
		this.g = g;
		new File("output").mkdir();
		bimage = new BufferedImage(1280, 1024, BufferedImage.TYPE_INT_ARGB);
		this.g = (Graphics2D) bimage.getGraphics();

	}

	static CustomGraphics cg;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final CustomGraphics cg = new CustomGraphics(null);
		JFrame f = new JFrame("Hello") {
			@Override
			public Graphics getGraphics() {
				Graphics g = super.getGraphics();
				System.out.println(".getGraphics()");

				return cg;
			}
		};

		RepaintManager repaintManager = RepaintManager.currentManager(f);
		repaintManager.setDoubleBufferingEnabled(false);

		JPanel p = new JPanel();

		p.add(new JButton("Button"));
		p.add(new JTextField("Cool"));
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);

	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		System.out.println("CustomGraphics.clearRect():" + x + "," + y + " "
				+ width + "x" + height);
		g.clearRect(x, y, width, height);

	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		System.out.println("CustomGraphics.clipRect():" + x + "," + y + " "
				+ width + "x" + height);
		g.clipRect(x, y, width, height);

	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		System.out.println("CustomGraphics.copyArea():" + x + "," + y + " "
				+ width + "x" + height + " d:" + dx + "," + dy);
		g.copyArea(x, y, width, height, dx, dy);

	}

	@Override
	public Graphics create() {
		System.out.println("CustomGraphics.create()");
		g = (Graphics2D) g.create();
		return bimage.getGraphics();
	}

	int c = 0;

	@Override
	public void dispose() {
		System.out.println("CustomGraphics.dispose()");
		g.dispose();
		try {
			ImageIO.write(this.bimage, "PNG",
					new File("output/im" + c + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c++;
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		System.out.println("CustomGraphics.drawArc()");
		g.drawArc(x, y, width, height, startAngle, arcAngle);

	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage1()" + img.getWidth(null)
				+ "x" + img.getHeight(null));
		return g.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage2()");
		return g.drawImage(img, x, y, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage3()");
		return g.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage4()");
		return g.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage5()");
		return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		System.out.println("CustomGraphics.drawImage6()");
		return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				bgcolor, observer);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		System.out.println("CustomGraphics.clipRect():" + x1 + "," + y1 + " "
				+ x2 + "," + y2);
		g.drawLine(x1, y1, x2, y2);

	}

	@Override
	public void drawOval(int x, int y, int width, int height) {

		System.out.println("CustomGraphics.drawOval()");
		g.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		System.out.println("CustomGraphics.drawPolygon()");
		g.drawPolygon(xPoints, yPoints, nPoints);

	}

	@Override
	public void drawPolyline(int[] points, int[] points2, int points3) {
		System.out.println("CustomGraphics.drawPolyline()");
		g.drawPolyline(points, points2, points3);

	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		System.out.println("CustomGraphics.drawRoundRect()");
		g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	@Override
	public void drawString(String str, int x, int y) {
		System.out.println("CustomGraphics.drawString():" + str + " at " + x
				+ "," + y);
		g.drawString(str, x, y);

	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		System.out.println("CustomGraphics.drawString()2 :" + iterator + " at "
				+ x + "," + y);
		g.drawString(iterator, x, y);

	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		System.out.println("CustomGraphics.fillArc()");
		g.fillArc(x, y, width, height, startAngle, arcAngle);

	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		System.out.println("CustomGraphics.fillOval():" + x + "," + y + " "
				+ width + "x" + height);
		g.fillOval(x, y, width, height);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		System.out.println("CustomGraphics.fillPolygon()");
		g.fillPolygon(xPoints, yPoints, nPoints);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		System.out.println("CustomGraphics.fillRect():" + x + "," + y + " "
				+ width + "x" + height);
		g.fillRect(x, y, width, height);

	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		System.out.println("CustomGraphics.fillRoundRect()");
		g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	@Override
	public Shape getClip() {
		System.out.println("CustomGraphics.getClip()");
		return g.getClip();
	}

	@Override
	public Rectangle getClipBounds() {
		System.out.println("CustomGraphics.getClipBounds()");
		return g.getClipBounds();
	}

	@Override
	public Color getColor() {
		System.out.println("CustomGraphics.getColor()");
		return g.getColor();
	}

	@Override
	public Font getFont() {

		return g.getFont();
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {

		return g.getFontMetrics();
	}

	@Override
	public void setClip(Shape clip) {

		g.setClip(clip);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		g.setClip(x, y, width, height);
	}

	@Override
	public void setColor(Color c) {
		System.out.println("CustomGraphics.setColor():" + c);
		g.setColor(c);
	}

	@Override
	public void setFont(Font font) {
		g.setFont(font);
	}

	@Override
	public void setPaintMode() {
		g.setPaintMode();
	}

	@Override
	public void setXORMode(Color c1) {
		System.out.println("CustomGraphics.setXORMode():" + c1);
		g.setXORMode(c1);
	}

	@Override
	public void translate(int x, int y) {
		g.translate(x, y);
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		g.addRenderingHints(hints);

	}

	@Override
	public void clip(Shape s) {
		g.clip(s);
	}

	@Override
	public void draw(Shape s) {
		g.draw(s);
	}

	@Override
	public void drawGlyphVector(GlyphVector v, float x, float y) {
		g.drawGlyphVector(v, x, y);
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return g.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		g.drawImage(img, op, x, y);
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		g.drawRenderableImage(img, xform);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		g.drawRenderedImage(img, xform);

	}

	@Override
	public void drawString(String str, float x, float y) {
		g.drawString(str, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		g.drawString(iterator, x, y);
	}

	@Override
	public void fill(Shape s) {
		g.fill(s);
	}

	@Override
	public Color getBackground() {
		return g.getBackground();
	}

	@Override
	public Composite getComposite() {
		return g.getComposite();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return g.getDeviceConfiguration();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return g.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return g.getPaint();
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return g.getRenderingHint(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return g.getRenderingHints();
	}

	@Override
	public Stroke getStroke() {
		return g.getStroke();
	}

	@Override
	public AffineTransform getTransform() {
		return g.getTransform();
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return g.hit(rect, s, onStroke);
	}

	@Override
	public void rotate(double theta) {
		g.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		g.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		g.scale(sx, sy);
	}

	@Override
	public void setBackground(Color color) {
		g.setBackground(color);

	}

	@Override
	public void setComposite(Composite comp) {
		g.setComposite(comp);
	}

	@Override
	public void setPaint(Paint paint) {
		g.setPaint(paint);
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		g.setRenderingHint(hintKey, hintValue);
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		g.setRenderingHints(hints);

	}

	@Override
	public void setStroke(Stroke s) {
		g.setStroke(s);

	}

	@Override
	public void setTransform(AffineTransform Tx) {
		g.setTransform(Tx);
	}

	@Override
	public void shear(double shx, double shy) {
		g.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform Tx) {
		g.transform(Tx);
	}

	@Override
	public void translate(double tx, double ty) {
		g.translate(tx, ty);
	}

}
