package comokit.gama.tutorial;

import java.util.ArrayList;
import java.util.List;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.operators.Random;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@vars({
	@variable(
		name = TeachingSkill.DISCIPLINE,
		type = IType.STRING,
		init = "'cs'"
	),
	@variable(
		name = TeachingSkill.PEDALOGICAL_LEVEL,
		type = IType.INT,
		init = "1"
	)
})
@skill(
	name = "teaching"
)
public class TeachingSkill extends Skill {
	public static final String DISCIPLINE = "discipline";
	public static final String PEDALOGICAL_LEVEL = "pedalogical_level";

	@action(
		name = "create_tutorial",
		args = {
			@arg(
				name = "difficulty",
				type = IType.INT
			)
		}
	)
	public TutorialType createTutorial(IScope scope) throws GamaRuntimeException {
		IAgent teacher = getCurrentAgent(scope);
		int dif = (int) scope.getArg("difficulty", IType.INT);
		dif += Random.opRnd(scope, 5 - getPedalogicalLevel(teacher));
		dif = Math.max(1, Math.min(dif, 5));
		
		List<String> authors = new ArrayList<>();
		authors.add(teacher.getName());
		return new TutorialType(authors, dif);
	}

	/**
	 * @return the discipline
	 */
	@getter(DISCIPLINE)
	public static String getDiscipline(IAgent teacher) {
		return (String) teacher.getAttribute(DISCIPLINE);
	}

	/**
	 * @param discipline the discipline to set
	 */
	@setter(DISCIPLINE)
	public static void setDiscipline(IAgent teacher, String discipline) {
		teacher.setAttribute(DISCIPLINE, discipline);
	}

	/**
	 * @return the pedalogicalLevel
	 */
	@getter(PEDALOGICAL_LEVEL)
	public static int getPedalogicalLevel(IAgent teacher) {
		return (int) teacher.getAttribute(PEDALOGICAL_LEVEL);
	}

	/**
	 * @param pedalogicalLevel the pedalogicalLevel to set
	 */
	@setter(PEDALOGICAL_LEVEL)
	public static void setPedalogicalLevel(IAgent teacher, int pedalogicalLevel) {
		teacher.setAttribute(PEDALOGICAL_LEVEL, pedalogicalLevel);
	}
}
