/**
 * 
 */
package dynsim.graphics.render;

import java.awt.Color;
import java.awt.image.BufferedImage;

import dynsim.exceptions.DynSimException;
import dynsim.graphics.render.bones3D.Camera;
import dynsim.graphics.render.util.FloatRange;
import dynsim.simulator.ResultProcessor;

/**
 * @author maf83
 * 
 */
public interface Renderer extends ResultProcessor {

	public abstract void flush() throws DynSimException;

	public abstract Color getBackgroundColor();

	public abstract Camera getCamera();

	public RenderConfig getConfig();

	public abstract float getDetail();

	public abstract float getGamma();

	public BufferedImage getImage();

	public abstract float getOffset();

	public abstract float getScale();

	public abstract void rasterize();

	public abstract void reset();

	public abstract void setAllAxisRanges(FloatRange ra);

	public abstract void setAutoAxisRanges() throws DynSimException;

	public abstract void setAxisRanges(FloatRange ax, FloatRange ay);

	public abstract void setAxisRanges(FloatRange ax, FloatRange ay, FloatRange az);

	public abstract void setBackgroundColor(Color bg);

	public abstract void setCamera(Camera cam);

	public void setConfig(RenderConfig conf);

	public abstract void setDetail(float detail);

	public abstract void setDimensions(int w, int h, int d);

	public abstract void setDirectory(String string);

	public abstract void setEye(double x, double y, double z);

	public abstract void setFilename(String string);

	public abstract void setGamma(float gamma);

	public abstract void setOffset(float offset);

	public abstract void setRotation(double yaw, double pitch, double roll);

	public abstract void setScale(float scale);

	public abstract void setVarpos(int[] is);
	
	public abstract void initialize();
}