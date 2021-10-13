/*******************************************************************************************************
 *
 * GamaGLCanvas.java, in ummisco.gama.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.8.2).
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package ummisco.gama.opengl.view;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import com.jogamp.opengl.FPSCounter;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.swt.GLCanvas;

import msi.gama.runtime.PlatformHelper;
import ummisco.gama.dev.utils.FLAGS;
import ummisco.gama.opengl.renderer.IOpenGLRenderer;
import ummisco.gama.ui.bindings.IDelegateEventsToParent;

/**
 * The Class GamaGLCanvas.
 */
public class GamaGLCanvas extends Composite implements GLAutoDrawable, IDelegateEventsToParent {

	/** The canvas. */
	final Control canvas;

	/** The drawable. */
	final GLAutoDrawable drawable;

	/** The detached. */
	protected boolean detached = false;

	/**
	 * Instantiates a new gama GL canvas.
	 *
	 * @param parent
	 *            the parent
	 * @param renderer
	 *            the renderer
	 */
	public GamaGLCanvas(final Composite parent, final IOpenGLRenderer renderer) {
		super(parent, SWT.NONE);
		parent.setLayout(new FillLayout());
		this.setLayout(new FillLayout());
		final GLCapabilities cap = defineCapabilities();
		if (FLAGS.USE_NATIVE_OPENGL_WINDOW) {
			drawable = GLWindow.create(cap);
			canvas = new NewtCanvasSWT(this, SWT.NONE, (Window) drawable);
			addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(final ControlEvent e) {
					/* Detached views have not title! */
					if (PlatformHelper.isMac()) {
						boolean isDetached = parent.getShell().getText().length() == 0;
						if (isDetached) {
							if (!detached) {
								// DEBUG.OUT("detach");
								reparentWindow();
								detached = true;
							}

						} else if (detached) {
							// DEBUG.OUT("attach");
							reparentWindow();
							detached = false;
						}
					}
				}
			});
		} else {
			canvas = new GLCanvas(this, SWT.NONE, cap, null);
			drawable = (GLAutoDrawable) canvas;
		}
		drawable.setAutoSwapBufferMode(true);
		drawable.addGLEventListener(renderer);
		GLAnimatorControl animator = defineAnimator();
		renderer.setCanvas(this);
		addDisposeListener(e -> new Thread(() -> { animator.stop(); }).start());
	}

	/**
	 * Define animator.
	 *
	 * @return the GL animator control
	 */
	private GLAnimatorControl defineAnimator() {
		GLAnimatorControl animator =
				FLAGS.USE_OLD_ANIMATOR ? new SingleThreadGLAnimator(drawable) : new MultithreadGLAnimator(drawable);
		animator.setUpdateFPSFrames(FPSCounter.DEFAULT_FRAMES_PER_INTERVAL, null);
		return animator;
	}

	/**
	 * Define capabilities.
	 *
	 * @return the GL capabilities
	 * @throws GLException
	 *             the GL exception
	 */
	private GLCapabilities defineCapabilities() throws GLException {
		final GLProfile profile = GLProfile.getDefault();
		final GLCapabilities cap = new GLCapabilities(profile);
		cap.setDepthBits(24);
		cap.setDoubleBuffered(true);
		cap.setHardwareAccelerated(true);
		cap.setSampleBuffers(true);
		cap.setAlphaBits(8);
		cap.setNumSamples(8);
		return cap;
	}

	@Override
	public void setRealized(final boolean realized) {
		drawable.setRealized(realized);
	}

	@Override
	public boolean isRealized() { return drawable.isRealized(); }

	@Override
	public int getSurfaceWidth() { return drawable.getSurfaceWidth(); }

	@Override
	public int getSurfaceHeight() { return drawable.getSurfaceHeight(); }

	@Override
	public boolean isGLOriented() { return drawable.isGLOriented(); }

	@Override
	public void swapBuffers() throws GLException {
		drawable.swapBuffers();
	}

	@Override
	public GLCapabilitiesImmutable getChosenGLCapabilities() { return drawable.getChosenGLCapabilities(); }

	@Override
	public GLCapabilitiesImmutable getRequestedGLCapabilities() { return drawable.getRequestedGLCapabilities(); }

	@Override
	public GLProfile getGLProfile() { return drawable.getGLProfile(); }

	@Override
	public NativeSurface getNativeSurface() { return drawable.getNativeSurface(); }

	@Override
	public long getHandle() { return drawable.getHandle(); }

	@Override
	public GLDrawableFactory getFactory() { return drawable.getFactory(); }

	@Override
	public GLDrawable getDelegatedDrawable() { return drawable.getDelegatedDrawable(); }

	@Override
	public GLContext getContext() { return drawable.getContext(); }

	@Override
	public GLContext setContext(final GLContext newCtx, final boolean destroyPrevCtx) {
		return drawable.setContext(newCtx, destroyPrevCtx);
	}

	@Override
	public void addGLEventListener(final GLEventListener listener) {
		drawable.addGLEventListener(listener);
	}

	@Override
	public void addGLEventListener(final int index, final GLEventListener listener) throws IndexOutOfBoundsException {
		drawable.addGLEventListener(index, listener);
	}

	@Override
	public int getGLEventListenerCount() { return drawable.getGLEventListenerCount(); }

	@Override
	public boolean areAllGLEventListenerInitialized() {
		return drawable.areAllGLEventListenerInitialized();
	}

	@Override
	public GLEventListener getGLEventListener(final int index) throws IndexOutOfBoundsException {
		return drawable.getGLEventListener(index);
	}

	@Override
	public boolean getGLEventListenerInitState(final GLEventListener listener) {
		return drawable.getGLEventListenerInitState(listener);
	}

	@Override
	public void setGLEventListenerInitState(final GLEventListener listener, final boolean initialized) {
		drawable.setGLEventListenerInitState(listener, initialized);
	}

	@Override
	public GLEventListener disposeGLEventListener(final GLEventListener listener, final boolean remove) {
		return drawable.disposeGLEventListener(listener, remove);
	}

	@Override
	public GLEventListener removeGLEventListener(final GLEventListener listener) {
		return drawable.removeGLEventListener(listener);
	}

	@Override
	public void setAnimator(final GLAnimatorControl animatorControl) throws GLException {
		drawable.setAnimator(animatorControl);
	}

	@Override
	public GLAnimatorControl getAnimator() { return drawable.getAnimator(); }

	@Override
	public Thread setExclusiveContextThread(final Thread t) throws GLException {
		return drawable.setExclusiveContextThread(t);
	}

	@Override
	public Thread getExclusiveContextThread() { return drawable.getExclusiveContextThread(); }

	@Override
	public boolean invoke(final boolean wait, final GLRunnable glRunnable) throws IllegalStateException {
		return drawable.invoke(wait, glRunnable);
	}

	@Override
	public boolean invoke(final boolean wait, final List<GLRunnable> glRunnables) throws IllegalStateException {
		return drawable.invoke(wait, glRunnables);
	}

	@Override
	public void flushGLRunnables() {
		drawable.flushGLRunnables();
	}

	@Override
	public void destroy() {
		drawable.destroy();
	}

	@Override
	public void display() {
		drawable.display();
	}

	@Override
	public void setAutoSwapBufferMode(final boolean enable) {
		drawable.setAutoSwapBufferMode(enable);
	}

	@Override
	public boolean getAutoSwapBufferMode() { return drawable.getAutoSwapBufferMode(); }

	@Override
	public void setContextCreationFlags(final int flags) {
		drawable.setContextCreationFlags(flags);
	}

	@Override
	public int getContextCreationFlags() { return drawable.getContextCreationFlags(); }

	@Override
	public GLContext createContext(final GLContext shareWith) {
		return drawable.createContext(shareWith);
	}

	@Override
	public GL getGL() { return drawable.getGL(); }

	@Override
	public GL setGL(final GL gl) {
		return drawable.setGL(gl);
	}

	@Override
	public Object getUpstreamWidget() { return drawable.getUpstreamWidget(); }

	@Override
	public RecursiveLock getUpstreamLock() { return drawable.getUpstreamLock(); }

	@Override
	public boolean isThreadGLCapable() { return drawable.isThreadGLCapable(); }

	/**
	 * Gets the NEWT window.
	 *
	 * @return the NEWT window
	 */
	public Window getNEWTWindow() {
		if (FLAGS.USE_NATIVE_OPENGL_WINDOW) return (Window) drawable;
		return null;
	}

	/**
	 * Reparent window.
	 */
	public void reparentWindow() {
		if (!FLAGS.USE_NATIVE_OPENGL_WINDOW) return;
		Window w = (Window) drawable;
		w.setVisible(false);
		w.setFullscreen(true);
		w.setFullscreen(false);
		w.setVisible(true);
	}

	/**
	 * Sets the window visible.
	 *
	 * @param b
	 *            the new window visible
	 */
	public void setWindowVisible(final boolean b) {
		if (!FLAGS.USE_NATIVE_OPENGL_WINDOW) return;
		Window w = (Window) drawable;
		w.setVisible(b);
	}

	@Override
	public boolean setFocus() {
		return canvas.setFocus();
	}

}
