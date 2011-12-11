/*
 * generated by Xtext
 */
package msi.gama.lang.gaml.ui;

import msi.gama.lang.gaml.ui.highlight.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.contentassist.XtextContentAssistProcessor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.*;

import com.google.inject.Binder;

/**
 * Use this class to register components to be used within the IDE.
 */
public class GamlUiModule extends msi.gama.lang.gaml.ui.AbstractGamlUiModule {

	public GamlUiModule(final AbstractUIPlugin plugin) {
		super(plugin);
	}

	@Override
    public void configure(Binder binder) {
        super.configure(binder);
    binder.bind(String.class).annotatedWith(com.google.inject.name.Names.named(
    		(XtextContentAssistProcessor.COMPLETION_AUTO_ACTIVATION_CHARS))).toInstance(".:");
    }

	/**
	 * @author Pierrick
	 * @return GAMLSemanticHighlightingCalculator
	 */
	public Class<? extends ISemanticHighlightingCalculator> bindSemanticHighlightingCalculator() {
		return GamlSemanticHighlightingCalculator.class;
	}

	public Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return GamlHighlightingConfiguration.class;
	}

}
