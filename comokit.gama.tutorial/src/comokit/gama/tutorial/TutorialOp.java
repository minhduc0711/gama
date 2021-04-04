package comokit.gama.tutorial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;

public class TutorialOp {
	@operator(
		value = "say_hello"
	)
	public static String sayHello(IScope scope, String name) {
		return "Hello " + name + "!";
	}

	@operator(
		value = "aggregate_tutorials",
		content_type = TutorialTypeWrapper.ID
	)
	public static TutorialType aggregateTutorials(IScope scope, List<TutorialType> tutorials) {
		List<String> aggAuthors = new ArrayList<>();
		List<Integer> difs = new ArrayList<>();
		for (TutorialType tut : tutorials) {
			aggAuthors.addAll(tut.getAuthors());
			difs.add(tut.getDifficulty());
		}
		return new TutorialType(aggAuthors, Collections.max(difs));
	}
}
