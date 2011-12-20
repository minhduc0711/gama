/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.gui.swt.commands;

import java.util.*;
import java.util.List;
import msi.gama.common.interfaces.*;
import msi.gama.gui.swt.SwtGui;
import msi.gama.kernel.simulation.ISimulation;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.GAMA;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class AgentsMenu extends ContributionItem {

	public AgentsMenu() {}

	public AgentsMenu(final String id) {
		super(id);
	}

	static SelectionAdapter adapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			MenuItem mi = (MenuItem) e.widget;
			IAgent a = (IAgent) mi.getData("agent");
			if ( a != null && !a.dead() ) {
				GAMA.getExperiment().getOutputManager().selectionChanged(a);
			}
		}
	};

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	private static void fillAgentsMenu(final Menu menu, final IPopulation species) {
		List<IAgent> agents = species.getAgentsList();
		int size = agents.size();
		if ( size < 100 ) {
			for ( IAgent agent : agents ) {
				MenuItem agentItem = new MenuItem(menu, SWT.PUSH);
				agentItem.setData("agent", agent);
				agentItem.setText(agent.getName());
				agentItem.addSelectionListener(adapter);
				agentItem.setImage(SwtGui.agentImage);
			}
		} else {
			int nb = size / 100;
			for ( int i = 0; i < nb; i++ ) {
				MenuItem rangeItem = new MenuItem(menu, SWT.CASCADE);
				int begin = i * 100;
				int end = Math.min((i + 1) * 100, size);
				rangeItem.setText("From " + begin + " to " + (end - 1));
				Menu rangeMenu = new Menu(rangeItem);
				for ( int j = begin; j < end; j++ ) {
					IAgent agent = agents.get(j);
					MenuItem agentItem = new MenuItem(rangeMenu, SWT.PUSH);
					agentItem.setData("agent", agent);
					agentItem.setText(agent.getName());
					agentItem.addSelectionListener(adapter);
					agentItem.setImage(SwtGui.agentImage);
				}
				rangeItem.setMenu(rangeMenu);
			}
		}
	}

	// TODO adapt this to multi-scale organization!!!
	public static Menu createSpeciesSubMenu(final Control parent, final IPopulation species) {
		Menu agentsMenu = new Menu(parent);
		fillAgentsMenu(agentsMenu, species);
		parent.setMenu(agentsMenu);
		return agentsMenu;
	}

	private void populateComponents(final Menu parent, final IAgent macro) {
		MenuItem microAgentsItem = new MenuItem(parent, SWT.CASCADE);
		microAgentsItem.setText("Micro agents");

		Menu microSpeciesMenu = new Menu(microAgentsItem);
		microAgentsItem.setMenu(microSpeciesMenu);

		IPopulation microPopulation;
		List<String> microSpeciesNames = macro.getSpecies().getMicroSpeciesNames();
		Collections.sort(microSpeciesNames);
		for ( String microSpec : microSpeciesNames ) {
			microPopulation = macro.getMicroPopulation(microSpec);
			if ( microPopulation != null && microPopulation.size() > 0 ) {
				populateSpecies(microSpeciesMenu, microPopulation, false);
			}
		}
	}

	private void populateAgentContent(final Menu parent, final IAgent agent) {
		MenuItem agentItem = new MenuItem(parent, SWT.PUSH);
		agentItem.setData("agent", agent);
		agentItem.setText(agent.getName());
		agentItem.addSelectionListener(adapter);
		agentItem.setImage(SwtGui.agentImage);

		populateComponents(parent, agent);
	}

	private void populateAgent(final Menu parent, final IAgent agent) {
		if ( !agent.hasMembers() ) { // TODO review IAgent.isGridAgent
			MenuItem agentItem = new MenuItem(parent, SWT.PUSH);
			agentItem.setData("agent", agent);
			agentItem.setText(agent.getName());
			agentItem.setImage(SwtGui.agentImage);
			agentItem.addSelectionListener(adapter);
		} else {
			MenuItem agentItem = new MenuItem(parent, SWT.CASCADE);
			agentItem.setData("agent", agent);
			agentItem.setText(agent.getName() + " (macro)");
			agentItem.setImage(SwtGui.agentImage); // TODO a suitable icon for macro-agent?

			Menu agentMenu = new Menu(agentItem);
			agentItem.setMenu(agentMenu);

			populateAgentContent(agentMenu, agent);
		}
	}

	private void populateSpecies(final Menu parent, final IPopulation population,
		final boolean isGlobal) {
		MenuItem speciesItem = null;
		if ( isGlobal ) {
			speciesItem = new MenuItem(parent, SWT.CASCADE, 0);
		} else {
			speciesItem = new MenuItem(parent, SWT.CASCADE);
		}
		speciesItem.setText("Species " + population.getName());
		speciesItem.setData("agent", population);
		speciesItem.setImage(SwtGui.speciesImage);

		Menu speciesMenu = new Menu(speciesItem);
		speciesItem.setMenu(speciesMenu);

		List<IAgent> agents = population.getAgentsList();
		int size = agents.size();

		if ( size < 100 ) {
			for ( IAgent a : agents ) {
				populateAgent(speciesMenu, a);
			}
		} else {
			int nb = size / 100;
			for ( int i = 0; i < nb; i++ ) {
				MenuItem rangeItem = new MenuItem(speciesMenu, SWT.CASCADE);
				int begin = i * 100;
				int end = Math.min((i + 1) * 100, size);
				rangeItem.setText("From " + begin + " to " + (end - 1));
				Menu rangeMenu = new Menu(rangeItem);
				for ( int j = begin; j < end; j++ ) {
					IAgent agent = agents.get(j);
					populateAgent(rangeMenu, agent);
				}
				rangeItem.setMenu(rangeMenu);
			}
		}
	}

	@Override
	public void fill(final Menu parent, final int index) {
		ISimulation sim = GAMA.getFrontmostSimulation();
		IPopulation worldPopulation = sim.getWorldPopulation();
		populateSpecies(parent, worldPopulation, true);
	}
}
