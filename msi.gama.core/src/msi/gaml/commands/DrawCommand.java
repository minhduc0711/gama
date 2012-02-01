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
package msi.gaml.commands;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.ImageUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.precompiler.GamlAnnotations.combination;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.compilation.ISymbolKind;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.*;
import msi.gaml.types.IType;

// A command that is used to draw shapes, figures, text on the display

@symbol(name = IKeyword.DRAW, kind = ISymbolKind.SINGLE_COMMAND)
@facets(value = {
	@facet(name = IKeyword.SHAPE, type = IType.ID, optional = true),
	@facet(name = IKeyword.TEXT, type = IType.STRING_STR, optional = true),
	@facet(name = IKeyword.IMAGE, type = IType.STRING_STR, optional = true),
	@facet(name = IKeyword.EMPTY, type = IType.BOOL_STR, optional = true),
	@facet(name = IKeyword.AT, type = IType.POINT_STR, optional = true),
	@facet(name = IKeyword.SIZE, type = IType.FLOAT_STR, optional = true),
	@facet(name = IKeyword.TO, type = IType.POINT_STR, optional = true),
	@facet(name = IKeyword.COLOR, type = IType.COLOR_STR, optional = true),
	@facet(name = IKeyword.SCALE, type = IType.FLOAT_STR, optional = true),
	@facet(name = IKeyword.ROTATE, type = IType.INT_STR, optional = true),
	@facet(name = IKeyword.FONT, type = IType.STRING_STR, optional = true),
	@facet(name = IKeyword.STYLE, type = IType.ID, values = { "plain", "bold", "italic" }, optional = true) },

combinations = {
	@combination({ IKeyword.SHAPE, IKeyword.COLOR, IKeyword.SIZE, IKeyword.AT, IKeyword.EMPTY,
		IKeyword.ROTATE }),
	@combination({ IKeyword.TO, IKeyword.SHAPE, IKeyword.COLOR, IKeyword.SIZE, IKeyword.EMPTY }),
	@combination({ IKeyword.SHAPE, IKeyword.COLOR, IKeyword.SIZE, IKeyword.EMPTY, IKeyword.ROTATE }),
	@combination({ IKeyword.TEXT, IKeyword.SIZE, IKeyword.COLOR, IKeyword.AT, IKeyword.ROTATE }),
	@combination({ IKeyword.IMAGE, IKeyword.SIZE, IKeyword.AT, IKeyword.SCALE, IKeyword.ROTATE,
		IKeyword.COLOR }) }, omissible = IKeyword.SHAPE)
@inside(symbols = { IKeyword.ASPECT }, kinds = { ISymbolKind.SEQUENCE_COMMAND })
public class DrawCommand extends AbstractCommandSequence {

	private static final Map<String, Integer> STYLES = new HashMap();
	private static final Map<String, Integer> SHAPES = new HashMap();
	static {
		STYLES.put("plain", 0);
		STYLES.put("bold", 1);
		STYLES.put("italic", 2);
		SHAPES.put("geometry", 0);
		SHAPES.put("square", 1);
		SHAPES.put("circle", 2);
		SHAPES.put("triangle", 3);
		SHAPES.put("rectangle", 1);
		SHAPES.put("disc", 2);
		SHAPES.put("line", 4);
	}

	private final DrawExecuter executer;

	public DrawCommand(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		final boolean isShape = getFacet(IKeyword.SHAPE) != null;
		final boolean isText = getFacet(IKeyword.TEXT) != null;
		final boolean isImage = getFacet(IKeyword.IMAGE) != null;
		executer =
			isShape ? new ShapeExecuter(desc) : isText ? new TextExecuter(desc) : isImage
				? new ImageExecuter(desc) : new ShapeExecuter(desc);

	}

	@Override
	public Rectangle2D privateExecuteIn(final IScope stack) throws GamaRuntimeException {
		IGraphics g = (IGraphics) stack.getContext();
		if ( g == null ) { return null; }
		return executer.executeOn(stack, g);
	}

	private abstract class DrawExecuter {

		IExpression size, location, color, rotate;

		Color constantColor = null;
		ILocation constantSize = null;
		Integer constantRotate = null;

		DrawExecuter(final IDescription desc) throws GamaRuntimeException {
			IScope scope = GAMA.getDefaultScope();
			size = getFacet(IKeyword.SIZE);
			if ( size == null ) {
				constantSize = new GamaPoint(1.0, 1.0);
			} else if ( size.isConst() ) {
				constantSize = Cast.asPoint(scope, size.value(scope));
			}
			location = getFacet(IKeyword.AT);
			color = getFacet(IKeyword.COLOR);
			if ( color != null && color.isConst() ) {
				constantColor = Cast.asColor(scope, color.value(scope));
			}
			rotate = getFacet(IKeyword.ROTATE);
			if ( rotate != null && rotate.isConst() ) {
				constantRotate = Cast.asInt(scope, rotate.value(scope));
			}
		}

		double scale(final double val, final IGraphics g) {
			return val * g.getXScale();
		}

		ILocation scale(final ILocation p, final IGraphics g) {
			return new GamaPoint(p.getX() * g.getXScale(), p.getY() * g.getYScale());
		}

		Integer getRotation(final IScope scope) throws GamaRuntimeException {
			return constantRotate == null ? rotate == null ? null : Cast.asInt(scope,
				rotate.value(scope)) : constantRotate;
		}

		abstract Rectangle2D executeOn(IScope agent, IGraphics g) throws GamaRuntimeException;

	}

	private class ShapeExecuter extends DrawExecuter {

		private final IExpression shape, empty, toExpr;

		private final Boolean constantEmpty;
		private final Integer constantShape;

		private ShapeExecuter(final IDescription desc) throws GamaRuntimeException {
			super(desc);
			shape = getFacet(IKeyword.SHAPE);
			if ( shape == null ) {
				constantShape = SHAPES.get("geometry");
			} else if ( shape.isConst() ) {
				constantShape = SHAPES.get(shape.literalValue());
			} else {
				constantShape = null;
			}

			empty = getFacet(IKeyword.EMPTY);
			if ( empty == null ) {
				constantEmpty = false;
			} else if ( empty.isConst() ) {
				constantEmpty =
					Cast.asBool(GAMA.getDefaultScope(), empty.value(GAMA.getDefaultScope()));
			} else {
				constantEmpty = null;
			}
			toExpr = getFacet(IKeyword.TO);

		}

		@Override
		Rectangle2D executeOn(final IScope scope, final IGraphics g) throws GamaRuntimeException {
			final IAgent agent = scope.getAgentScope();

			ILocation loc;
			if ( location == null ) {
				loc = agent.getLocation();
			} else {
				loc = Cast.asPoint(scope, location.value(scope));
				// ??? loc = agent.getLocation();
			}

			if ( loc == null ) {
				scope.setStatus(ExecutionStatus.skipped);
				return null;
			}
			final ILocation from = scale(loc, g);
			Integer shapeIndex =
				constantShape == null ? SHAPES.get(Cast.asString(scope, shape.value(scope)))
					: constantShape;
			if ( shapeIndex == null ) {
				shapeIndex = 0;
			}
			Color c;
			if ( constantColor == null ) {
				if ( color != null ) {
					c = Cast.asColor(scope, color.value(scope));
				} else {
					Object o = scope.getAgentVarValue(agent, IKeyword.COLOR);
					if ( o != null ) {
						c = Cast.asColor(scope, o);
					} else {
						c = Color.yellow;
					}
				}
			} else {
				c = constantColor;
			}

			int displaySize =
				constantSize == null ? Maths
					.round(scale(Cast.asFloat(scope, size.value(scope)), g)) : Maths.round(scale(
					constantSize.getX(), g));
			if ( shapeIndex == 4 ) { // line
				if ( toExpr == null ) {
					scope.setStatus(ExecutionStatus.skipped);
					return null;
				}

				ILocation target = Cast.asPoint(scope, toExpr.value(scope));
				target = scale(agent.getLocation(), g);
				return drawLine(from, target, displaySize, c, g);

			}
			boolean isEmpty =
				constantEmpty == null ? empty == null ? false : Cast.asBool(scope,
					empty.value(scope)) : constantEmpty;
			return draw(scope, shapeIndex, from, displaySize, c, isEmpty, g);
		}

		private Rectangle2D draw(final IScope scope, final Integer shapeIndex, final ILocation at,
			final int displaySize, final Color c, final boolean isEmpty, final IGraphics g)
			throws GamaRuntimeException {
			int x = Maths.round(at.getX()) - (displaySize >> 1);
			int y = Maths.round(at.getY()) - (displaySize >> 1);
			g.setDrawingCoordinates(x, y);
			g.setDrawingDimensions(displaySize, displaySize);
			switch (shapeIndex) {
				case 0: {
					// TODO What if the agent is a point ?
					return g.drawGeometry(scope.getAgentScope().getInnerGeometry(), c, !isEmpty,
						getRotation(scope));
				}
				case 1: {
					return g.drawRectangle(c, !isEmpty, getRotation(scope));
				}
				case 2: {
					return g.drawCircle(c, !isEmpty, null);
				}
				case 3: {
					return g.drawTriangle(c, !isEmpty, getRotation(scope));
				}
			}
			return null;
		}

		private Rectangle2D drawLine(final ILocation from, final ILocation target,
			final int displaySize, final Color c, final IGraphics g) {
			int x = Maths.round(from.getX());
			int y = Maths.round(from.getY());
			g.setDrawingCoordinates(x, y);
			int toX = Maths.round(target.getX());
			int toY = Maths.round(target.getY());
			// TODO Size ??
			return g.drawLine(c, toX, toY);
		}
	}

	private class ImageExecuter extends DrawExecuter {

		private final IExpression image;
		private final String constantImage;
		private BufferedImage workImage;
		Graphics2D g2d = null;

		private ImageExecuter(final IDescription desc) throws GamaRuntimeException {
			super(desc);
			image = getFacet(IKeyword.IMAGE);
			constantImage =
				image.isConst() ? Cast.asString(GAMA.getDefaultScope(),
					image.value(GAMA.getDefaultScope())) : null;
		}

		@Override
		Rectangle2D executeOn(final IScope scope, final IGraphics g) throws GamaRuntimeException {
			IAgent agent = scope.getAgentScope();
			ILocation from = null;
			if ( location == null ) {
				from = scale(agent.getLocation(), g);
			} else {
				from = scale(Cast.asPoint(scope, location.value(scope)), g);
			}
			int displayWidth =
				constantSize == null ? Maths
					.round(scale(Cast.asFloat(scope, size.value(scope)), g)) : Maths.round(scale(
					constantSize.getX(), g));
			String img =
				constantImage == null ? Cast.asString(scope, image.value(scope)) : constantImage;
			BufferedImage image;
			try {
				image = ImageUtils.getInstance().getImageFromFile(img);
			} catch (IOException e) {
				throw new GamaRuntimeException(e);
			}
			int image_width = image.getWidth();
			int image_height = image.getHeight();
			double ratio = image_width / (double) image_height;
			int displayHeight = Maths.round(displayWidth / ratio);
			int x = Maths.round(from.getX()) - displayWidth / 2;
			int y = Maths.round(from.getY()) - displayHeight / 2;
			g.setDrawingDimensions(displayWidth, displayHeight);
			g.setDrawingCoordinates(x, y);
			Color c = null;
			Integer angle =
				constantRotate == null ? rotate == null ? null : Cast.asInt(scope,
					rotate.value(scope)) : constantRotate;
			if ( color != null ) {
				if ( constantColor == null ) {
					c = Cast.asColor(scope, color.value(scope));
				} else {
					c = constantColor;
				}
				if ( workImage == null || workImage.getWidth() != image.getWidth() ||
					workImage.getHeight() != image.getHeight() ) {
					if ( workImage != null ) {
						workImage.flush();
					}
					workImage =
						new BufferedImage(image.getWidth(), image.getHeight(),
							BufferedImage.TYPE_INT_ARGB);
					if ( g2d != null ) {
						g2d.dispose();
					}
					g2d = workImage.createGraphics();
					if ( constantImage != null ) {
						g2d.drawImage(image, 0, 0, null);
					}
				}
				if ( constantImage == null ) {
					g2d.drawImage(image, 0, 0, null);
				}
				g2d.setPaint(c);
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
				// Pourquoi l'alpha ne fonctionne pas ??
				g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
				// g2d.dispose();

				Rectangle2D result = g.drawImage(workImage, angle, true);
				workImage.flush();
				return result;
			}
			return g.drawImage(image, angle, true);
		}

	}

	private class TextExecuter extends DrawExecuter {

		private final IExpression text;
		private final String constantText;
		private final IExpression font;
		private final String constantFont;
		private final IExpression style;
		private final Integer constantStyle;

		private TextExecuter(final IDescription desc) throws GamaRuntimeException {
			super(desc);
			IScope scope = GAMA.getDefaultScope();
			text = getFacet(IKeyword.TEXT);
			if ( text.isConst() ) {
				constantText = Cast.asString(scope, text.value(scope));
			} else {
				constantText = null;
			}
			font = getFacet(IKeyword.FONT);
			if ( font == null ) {
				constantFont = Font.SANS_SERIF;
			} else if ( font.isConst() ) {
				constantFont = Cast.asString(scope, font.value(scope));
			} else {
				constantFont = null;
			}
			style = getFacet(IKeyword.STYLE);
			if ( style == null ) {
				constantStyle = Font.PLAIN;
			} else if ( style.isConst() ) {
				constantStyle = STYLES.get(Cast.asString(scope, style.value(scope)));
			} else {
				constantStyle = null;
			}

		}

		@Override
		Rectangle2D executeOn(final IScope scope, final IGraphics g) throws GamaRuntimeException {
			IAgent agent = scope.getAgentScope();
			ILocation from = null;
			if ( location == null ) {
				from = scale(agent.getLocation(), g);
			} else {
				from = scale(Cast.asPoint(scope, location.value(scope)), g);
			}
			int displaySize =
				constantSize == null ? Maths
					.round(scale(Cast.asFloat(scope, size.value(scope)), g)) : Maths.round(scale(
					constantSize.getX(), g));
			String info =
				constantText == null ? Cast.asString(scope, text.value(scope)) : constantText;
			if ( info == null || info.length() == 0 ) {
				scope.setStatus(ExecutionStatus.skipped);
				return null;
			}
			int x = Maths.round(from.getX());
			int y = Maths.round(from.getY());
			Color c =
				constantColor == null ? color != null ? Cast.asColor(scope, color.value(scope))
					: (Color) agent.getAttribute("color") : constantColor;
			Integer angle =
				constantRotate == null ? rotate == null ? null : Cast.asInt(scope,
					rotate.value(scope)) : constantRotate;
			String fName =
				constantFont == null ? Cast.asString(scope, font.value(scope)) : constantFont;
			int fStyle = constantStyle == null ? STYLES.get(style.value(scope)) : constantStyle;
			Font f = new Font(fName, fStyle, displaySize);
			g.setFont(f);
			g.setDrawingCoordinates(x, y);
			return g.drawString(info, c, angle); // ??
		}
	}

}