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
package msi.gaml.factories;

import java.lang.reflect.Constructor;
import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.IErrorCollector;
import msi.gama.precompiler.GamlAnnotations.base;
import msi.gama.precompiler.GamlAnnotations.combination;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.handles;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.no_scope;
import msi.gama.precompiler.GamlAnnotations.remote_context;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.uses;
import msi.gama.precompiler.GamlAnnotations.with_args;
import msi.gama.precompiler.GamlAnnotations.with_sequence;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.commands.Facets;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import msi.gaml.expressions.*;

/**
 * Written by Alexis Drogoul Modified on 11 mai 2010
 * 
 * @todo Description
 * 
 */
@handles({ ISymbolKind.ENVIRONMENT, ISymbolKind.ABSTRACT_SECTION })
public class SymbolFactory implements ISymbolFactory {

	protected Map<String, SymbolMetaDescription> registeredSymbols = new HashMap();

	protected final Set<ISymbolFactory> registeredFactories = new HashSet();

	public SymbolFactory() {
		registerAnnotatedFactories();
		registerAnnotatedSymbols();
	}

	private void registerAnnotatedFactories() {
		uses annot = getClass().getAnnotation(uses.class);
		if ( annot == null ) { return; }
		for ( int kind : annot.value() ) {
			Class c = GamlCompiler.getFactoriesByKind(kind);
			Constructor cc = null;
			try {
				cc = c.getConstructor();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if ( cc == null ) { return; }
			SymbolFactory fact;
			try {
				fact = (SymbolFactory) cc.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			registerFactory(fact);
		}
	}

	protected void registerAnnotatedSymbols() {
		handles annot = getClass().getAnnotation(handles.class);
		if ( annot == null ) { return; }
		for ( int c : annot.value() ) {
			List<Class> classes = GamlCompiler.getClassesByKind().get(c);
			for ( Class clazz : classes ) {
				register(clazz);
			}
		}
	}

	@Override
	public Set<String> getKeywords() {
		return new HashSet(registeredSymbols.keySet());
		// Necessary to copy, since this list can be modified dynamically (esp. by SpeciesFactory)
	}

	public void registerFactory(final ISymbolFactory f) {
		registeredFactories.add(f);

		// OutputManager.debug(this.getClass().getSimpleName() + " registers factory " +
		// f.getClass().getSimpleName() + " for keywords " + f.getKeywords());

	}

	public void register(final Class c) {

		Class baseClass = null;
		String omissible = null;
		// GUI.debug(c.getSimpleName() + " registered in " + getClass().getSimpleName());
		boolean isTopLevel =
			((symbol) c.getAnnotation(symbol.class)).kind() == ISymbolKind.BEHAVIOR;
		boolean canHaveArgs = c.getAnnotation(with_args.class) != null;
		boolean canHaveSequence = c.getAnnotation(with_sequence.class) != null;
		boolean doesNotHaveScope = c.getAnnotation(no_scope.class) != null;
		boolean isRemoteContext = c.getAnnotation(remote_context.class) != null;
		if ( c.getAnnotation(base.class) != null ) {
			baseClass = ((base) c.getAnnotation(base.class)).value();
		}
		List<String> keywords = Arrays.asList(((symbol) c.getAnnotation(symbol.class)).name());
		List<facet> facets = new ArrayList();
		List<combination> combinations = new ArrayList();
		List<String> contexts = new ArrayList();
		facets ff = (facets) c.getAnnotation(facets.class);
		if ( ff != null ) {
			facets = Arrays.asList(ff.value());
			combinations = Arrays.asList(ff.combinations());
			omissible = ff.omissible();

		}
		if ( c.getAnnotation(inside.class) != null ) {
			inside parents = (inside) c.getAnnotation(inside.class);
			for ( String p : parents.symbols() ) {
				contexts.add(p);
			}
			for ( int kind : parents.kinds() ) {
				List<Class> classes = GamlCompiler.getClassesByKind().get(kind);
				for ( Class clazz : classes ) {
					symbol names = (symbol) clazz.getAnnotation(symbol.class);
					if ( names != null ) {
						for ( String name : names.name() ) {
							contexts.add(name);
						}
					}
				}
			}
		}
		contexts = new ArrayList(new HashSet(contexts));

		for ( String k : keywords ) {
			// try {
			registeredSymbols.put(k, new SymbolMetaDescription(c, baseClass, k, canHaveSequence,
				canHaveArgs, isTopLevel, doesNotHaveScope, facets, omissible, combinations,
				contexts, isRemoteContext));
			// } catch (GamlException e) {
			// e.addContext("In compiling the meta description of: " + k);
			// e.printStackTrace();
			// }
		}
	}

	@Override
	public SymbolMetaDescription getMetaDescriptionFor(final IDescription context,
		final String keyword) {
		SymbolMetaDescription md = registeredSymbols.get(keyword);
		String upper = context == null ? null : context.getKeyword();
		if ( md == null ) {
			ISymbolFactory f = chooseFactoryFor(keyword, upper);
			if ( f == null ) {
				if ( context != null ) {
					context.flagError("Unknown symbol " + keyword);
				}
				return null;
			}
			return f.getMetaDescriptionFor(context, keyword);
		}
		return md;
	}

	@Override
	public String getOmissibleFacetForSymbol(final ISyntacticElement e, final String keyword) {
		SymbolMetaDescription md;
		md = getMetaDescriptionFor(null, keyword);
		return md == null ? IKeyword.NAME /* by default */: md.getOmissible();

	}

	protected String getKeyword(final ISyntacticElement cur) {
		return cur.getKeyword();
	}

	/**
	 * Creates a semantic description based on a source element, a super-description, and a --
	 * possibly null -- list of children. In this method, the children of the source element are not
	 * considered, so if "children" is null or empty, the description is created without children.
	 * @see msi.gaml.factories.ISymbolFactory#createDescription(msi.gama.common.interfaces.ISyntacticElement,
	 *      msi.gaml.descriptions.IDescription, java.util.List)
	 */
	@Override
	public IDescription createDescription(final ISyntacticElement source,
		final IDescription superDesc, final List<IDescription> children) {
		Facets facets = source.getFacets();
		String keyword = getKeyword(source);
		ISymbolFactory f = chooseFactoryFor(keyword, null);
		if ( f == null ) {
			superDesc.flagError("Impossible to parse keyword " + keyword, source);
			return null;
		}
		if ( f != this ) { return f.createDescription(source, superDesc, children); }
		List<IDescription> commandList = children == null ? new ArrayList() : children;
		SymbolMetaDescription md = getMetaDescriptionFor(superDesc, keyword);
		md.verifyFacets(source, facets, superDesc);
		return buildDescription(source, keyword, commandList, superDesc, md);
	}

	/**
	 * Creates a semantic description based on a source element and a super-description. The
	 * children of the source element are used as a basis for building, recursively, the semantic
	 * tree.
	 * @see msi.gaml.factories.ISymbolFactory#createDescriptionRecursively(msi.gama.common.interfaces.ISyntacticElement,
	 *      msi.gaml.descriptions.IDescription)
	 */
	@Override
	public IDescription createDescriptionRecursively(final ISyntacticElement source,
		final IDescription superDesc) {
		if ( source == null ) { return null; }
		String keyword = getKeyword(source);

		String context = null;
		if ( superDesc != null ) {
			context = superDesc.getKeyword();
		}
		ISymbolFactory f = chooseFactoryFor(keyword, context);
		if ( f == null ) {
			if ( superDesc != null ) {
				superDesc.flagError("Impossible to parse keyword " + keyword, source);
			}
			return null;
		}
		if ( f != this ) { return f.createDescriptionRecursively(source, superDesc); }

		SymbolMetaDescription md = getMetaDescriptionFor(superDesc, keyword);
		Facets facets = source.getFacets();

		if ( md != null ) {
			md.verifyFacets(source, facets, superDesc);
		}
		List<IDescription> children = new ArrayList();

		for ( ISyntacticElement e : source.getChildren() ) {
			// Instead of having to consider this specific case, find a better solution.
			if ( !source.getKeyword().equals(IKeyword.SPECIES) ) {
				children.add(createDescriptionRecursively(e, superDesc));
			} else if ( source.hasParent(IKeyword.DISPLAY) || source.hasParent(IKeyword.SPECIES) ) {
				// "species" declared in "display" or "species" section
				children.add(createDescriptionRecursively(e, superDesc));
			}
		}

		return buildDescription(source, keyword, children, superDesc, md);
	}

	@Override
	public boolean handlesKeyword(final String keyword) {
		return registeredSymbols.containsKey(keyword);
	}

	@Override
	public ISymbolFactory chooseFactoryFor(final String keyword) {
		if ( handlesKeyword(keyword) ) { return this; }
		for ( ISymbolFactory f : registeredFactories ) {
			if ( f.handlesKeyword(keyword) ) { return f; }
		}
		for ( ISymbolFactory f : registeredFactories ) {
			ISymbolFactory f2 = f.chooseFactoryFor(keyword);
			if ( f2 != null ) { return f2; }
		}
		return null;
	}

	protected ISymbolFactory chooseFactoryFor(final String keyword, final String context) {
		ISymbolFactory contextFactory = context != null ? chooseFactoryFor(context) : this;
		if ( contextFactory == null ) {
			contextFactory = this;
		}
		return contextFactory.chooseFactoryFor(keyword);
	}

	protected IDescription buildDescription(final ISyntacticElement source, final String keyword,
		final List<IDescription> children, final IDescription superDesc,
		final SymbolMetaDescription md) {
		return new SymbolDescription(keyword, superDesc, children, source, md);
	}

	@Override
	public ISymbol compileDescription(final IDescription desc, final IExpressionFactory factory) {
		IDescription superDesc = desc.getSuperDescription();
		ISymbolFactory f =
			chooseFactoryFor(desc.getKeyword(), superDesc == null ? null : superDesc.getKeyword());
		if ( f == null ) {
			desc.flagError("Impossible to compile keyword " + desc.getKeyword());
			return null;
		}
		if ( f != this ) { return f.compileDescription(desc, factory); }
		SymbolMetaDescription md = getMetaDescriptionFor(desc, desc.getKeyword());
		return privateCompile(desc, md, factory);
	}

	protected Facets compileFacets(final IDescription sd, final SymbolMetaDescription md,
		final IExpressionFactory factory) {
		Facets rawFacets = sd.getFacets();
		// Addition of a facet to keep track of the keyword
		rawFacets.putAsLabel(IKeyword.KEYWORD, sd.getKeyword());

		for ( String s : new ArrayList<String>(rawFacets.keySet()) ) {
			IExpression e = compileFacet(s, sd, md, factory);
			rawFacets.put(s, e);
		}
		return rawFacets;
	}

	protected IExpression compileFacet(final String tag, final IDescription sd,
		final SymbolMetaDescription md, final IExpressionFactory factory) {
		if ( md.isLabel(tag) ) { return sd.getFacets().compileAsLabel(tag); }
		try {
			return sd.getFacets().compile(tag, sd, factory);
		} catch (GamaRuntimeException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected ISymbol privateCompile(final IDescription desc, final SymbolMetaDescription md,
		final IExpressionFactory factory) {
		if ( md == null ) { return null; }
		compileFacets(desc, md, factory);
		ISymbol cs = compileSymbol(desc, md.getConstructor());
		if ( cs == null ) { return null; }
		if ( md.hasSequence() ) {
			if ( md.isRemoteContext() ) {
				desc.copyTempsAbove();
			}
			privateCompileChildren(desc, cs, factory);
		}
		return cs;

	}

	protected ISymbol compileSymbol(final IDescription desc, final ISymbolConstructor c) {
		ISymbol cs = c.create(desc);
		return cs;
	}

	protected void privateCompileChildren(final IDescription desc, final ISymbol cs,
		final IExpressionFactory factory) {
		List<ISymbol> lce = new ArrayList();
		for ( IDescription sd : desc.getChildren() ) {
			ISymbol s = compileDescription(sd, factory);
			if ( s != null ) {
				lce.add(s);
			}
		}
		cs.setChildren(lce);

	}

	@Override
	public ISymbol compile(final ModelStructure struct, final IErrorCollector collect)
		throws InterruptedException {
		return null;
	}

}
