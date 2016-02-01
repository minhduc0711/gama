/*********************************************************************************************
 *
 *
 * 'StringDrawer.java', in plugin 'msi.gama.jogl2', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.opengl.scene;

import java.awt.Font;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import ummisco.gama.opengl.JOGLRenderer;

/**
 *
 * The class StringDrawer.
 *
 * @author drogoul
 * @since 4 mai 2013
 *
 */

public class StringDrawer extends ObjectDrawer<StringObject> {

	static final boolean USE_VERTEX_ARRAYS = true;

	public StringDrawer(final JOGLRenderer r) {
		super(r);
	}

	@Override
	public void draw(final GL2 gl, final StringObject object) {
		_draw(gl, object);
	}

	@Override
	protected void _draw(final GL2 gl, final StringObject s) {
		float x = (float) s.getLocation().x;
		float y = (float) s.getLocation().y;
		float z = (float) s.getLocation().z;

		if ( s.getFont() != null && s.getBitmap() ) {
			float scale = 1f / (float) renderer.getGlobalYRatioBetweenPixelsAndModelUnits();
			// gl.glPushMatrix();
			Font f = s.getFont();
			TextRenderer r = renderer.get(f);
			if ( r == null ) { return; }
			r.setColor(s.getColor());
			r.begin3DRendering();
			r.draw3D(s.string, x, y, z, scale);
			r.flush();
			r.end3DRendering();
			// gl.glPopMatrix();
		} else {
			int fontToUse = GLUT.BITMAP_HELVETICA_18;
			// float scale = 1f;
			Font f = s.getFont();
			if ( f != null ) {
				if ( f.getSize() < 10 ) {
					fontToUse = GLUT.BITMAP_HELVETICA_10;
					// scale = f.getSize2D() / 10f;
				} else if ( f.getSize() < 16 ) {
					fontToUse = GLUT.BITMAP_HELVETICA_12;
					// scale = f.getSize2D() / 12f;
				} else {
					// scale = f.getSize2D() / 18f;
				}
			}
			gl.glPushMatrix();
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			gl.glDisable(GL.GL_BLEND);
			gl.glColor4d(s.getColor().getRed() / 255.0, s.getColor().getGreen() / 255.0, s.getColor().getBlue() / 255.0,
				s.getColor().getAlpha() / 255.0 * s.getAlpha());
			gl.glRasterPos3d(x, y, z);
			glut.glutBitmapString(fontToUse, s.string);
			gl.glColor4d(1, 1, 1, 1);
			gl.glEnable(GL.GL_BLEND);
			gl.glEnable(GLLightingFunc.GL_LIGHTING);
			gl.glPopMatrix();
		}
	}

}