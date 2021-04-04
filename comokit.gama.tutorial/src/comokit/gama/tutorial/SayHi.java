package comokit.gama.tutorial;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.AbstractStatement;
import msi.gaml.types.IType;

@symbol(
	name = "say_hi",
	kind = ISymbolKind.SINGLE_STATEMENT,
	with_sequence = false
)
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets(
	value = {
		@facet(
			name = "name",
			type = IType.NONE,
			optional = true
		)
	},
	omissible = "name"
)
public class SayHi extends AbstractStatement {
	IExpression name;

	public SayHi(final IDescription desc) {
		super(desc);
		name = getFacet("name");
	}

	protected Object privateExecuteIn(IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		String s = "Hi ";// + name;
		if (agent != null && !agent.dead()) {
			s += Cast.asString(scope, name.value(scope));
			scope.getGui().getConsole().informConsole(s, scope.getRoot());
		}
		return s;
	}
}
