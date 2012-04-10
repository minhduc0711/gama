/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.kernel.simulation;

import msi.gama.outputs.IOutputManager;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;

// TODO change to SimulationScheduler or SimulationControler or GlobalScheduler
public class Scheduler extends AbstractScheduler implements  Runnable {
	public final Thread executionThread;

	private static int threadCount = 0;


	protected Scheduler(final ISimulation sim) throws GamaRuntimeException {
		super(sim);
		executionThread = new Thread(null, this, "Scheduler execution thread #" + threadCount++);
	}

	@Override
	public void run() {
		IOutputManager m = GAMA.getExperiment().getOutputManager();
		while (alive) {
			try {
				IScheduler.SCHEDULER_AUTHORIZATION.acquire();

				if ( !paused && alive ) {
					step(simulation.getExecutionScope());
				}
				if ( alive ) {
					m.step(simulation.getExecutionScope());
				}
				if ( alive ) {
					m.updateOutputs();
				}

				// HACK TO TEST

				simulation.getModel().getModelEnvironment().getSpatialIndex().cleanCache();

				paused = stepped ? true : paused;
				stepped = false;

				if ( !paused && alive ) {
					IScheduler.SCHEDULER_AUTHORIZATION.release();

				}
			} catch (GamaRuntimeException e) {
				GAMA.reportError(e);
				alive = false;
				continue;
			} catch (InterruptedException e) {
				alive = false;
				continue;
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		executionThread.interrupt();
		try {
			executionThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stepByStep() {
		stepped = true;
		paused = false;
		alive = true;
		IScheduler.SCHEDULER_AUTHORIZATION.release();
		startThread();
	}

	@Override
	public void start() {
		alive = true;
		paused = false;
		stepped = false;
		IScheduler.SCHEDULER_AUTHORIZATION.release();
		startThread();
	}

	private void startThread() {
		if ( !executionThread.isAlive() ) {
			executionThread.start();
		}
	}

	public void pause() {
		alive = true;
		paused = true;
		stepped = false;
	}

	
}
