package comokit.gama.tutorial;

import java.util.Arrays;
import java.util.List;

import msi.gama.common.interfaces.IValue;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

@vars({
	@variable(
		name = TutorialType.AUTHORS,
		type = IType.LIST,
		of = IType.STRING
	),
	@variable(
		name = TutorialType.DIFFICULTY,
		type = IType.INT
	)
})
public class TutorialType implements IValue {
	public static final String AUTHORS = "authors";
	public static final String DIFFICULTY = "difficulty";

	List<String> authors;
	int difficulty;

	/**
	 * @param authors
	 * @param difficulty
	 */
	public TutorialType(List<String> authors, int difficulty) {
		this.authors = authors;
		this.difficulty = difficulty;
	}

	@getter(TutorialType.AUTHORS)
	public List<String> getAuthors() {
		return authors;
	}

	@setter(TutorialType.AUTHORS)
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	@getter(TutorialType.DIFFICULTY)
	public int getDifficulty() {
		return difficulty;
	}

	@setter(TutorialType.DIFFICULTY)
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return "Tutorial by " + authors + ", difficulty: " + difficulty;
	}
}
