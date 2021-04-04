package comokit.gama.tutorial;

import java.util.List;

import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaPair;
import msi.gaml.types.GamaType;

import ummisco.gama.dev.utils.DEBUG;

@type(
	name = "tutorial",
	id = TutorialTypeWrapper.ID,
	wraps = { TutorialType.class },
	kind = ISymbolKind.Variable.REGULAR
)
public class TutorialTypeWrapper extends GamaType<TutorialType> {
	public static final int ID = 1250;

	static {
		DEBUG.ON();
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public TutorialType getDefault() {
		return new TutorialType(null, 0);
	}

	@Override
	public TutorialType cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		DEBUG.LOG("Type of casted obj is: " + obj.getClass().getName());
		if (TutorialType.class.isInstance(obj)) {
			return (TutorialType) obj;
		} else if (GamaPair.class.isInstance(obj)) {
			GamaPair<List<String>, Integer> pair = (GamaPair<List<String>, Integer>) obj;
			return new TutorialType(pair.getKey(), pair.getValue());
		} else {
			return null;
		}
	}

}
