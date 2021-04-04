package ummisco.gama.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;

public class CreateTutorialAgentHandler extends AbstractHandler implements IElementUpdater {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IScope scope = GAMA.getRuntimeScope();

		IPopulation population =  scope.getSimulation().getPopulationFor("teacher");
		List<Map<String, Object>> attrMaps = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		attrMaps.add(map);
		population.createAgents(scope, 1, attrMaps, false, true);
		return this;
	}
	@Override
	public void updateElement(UIElement element, Map parameters) {
		// TODO Auto-generated method stub
		
	}
}
