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
package msi.gaml.expressions;

import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.reserved;
import msi.gaml.descriptions.*;
import msi.gaml.types.*;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 * 
 * @todo Description
 * 
 */
@reserved({ "nil", "each", "self", "myself", "their", "its", "her", "his",
// TMP (retro-compatibility):
	"world", "visible", "signal" })
public interface IExpressionParser<T> {

	// T is the type of input expected. Either ExpressionDescription or Expression

	public static final String INTERNAL_POINT = "<->";
	public static final List<String> RESERVED = Arrays.asList(IKeyword.THE, IKeyword.FALSE,
		IKeyword.TRUE, IKeyword.NULL, IKeyword.MYSELF, IKeyword.MY, IKeyword.HIS, IKeyword.HER,
		IKeyword.THEIR, IKeyword.ITS);
	public static final List<String> IGNORED = Arrays.asList(IKeyword.THE, IKeyword.THEIR,
		IKeyword.HIS, IKeyword.ITS, IKeyword.HER);
	public static final Map<String, Map<IType, IOperator>> UNARIES = new HashMap();
	public static final Map<String, Map<TypePair, IOperator>> BINARIES = new HashMap();
	public static final Set<String> FUNCTIONS = new HashSet();
	public static final Set<String> ITERATORS = new HashSet();
	public static final Map<String, Short> BINARY_PRIORITIES = new HashMap();

	public abstract IExpression parse(final IExpressionDescription s,
		final IDescription parsingContext);

	public abstract void setFactory(IExpressionFactory factory);

	/**
	 * @param args
	 * @param context
	 * @return
	 */
	Map<String, IExpressionDescription> parseArguments(IExpressionDescription args,
		IDescription context);

	/**
	 * @param s
	 * @return
	 */
	public abstract List<String> parseLiteralArray(final IExpressionDescription s,
		final IDescription context);
}