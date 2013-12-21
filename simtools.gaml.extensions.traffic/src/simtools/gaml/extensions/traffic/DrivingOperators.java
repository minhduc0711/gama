package simtools.gaml.extensions.traffic;

import msi.gama.metamodel.topology.graph.GamaSpatialGraph;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.util.IContainer;
import msi.gama.util.graph.IGraph;
import msi.gaml.types.IType;

public class DrivingOperators {
	
	@operator(value = "as_driving_graph", content_type = IType.GEOMETRY, index_type = IType.GEOMETRY)
	@doc(value = "creates a graph from the list/map of edges given as operand and connect the node to the edge", special_cases = "if the operand is a list, the graph will be built with elements of the list as vertices", examples = { "as_edge_graph([{1,5},{12,45},{34,56}])  --:  build a graph with these three vertices and reflexive links on each vertices" }, see = {
		"as_intersection_graph", "as_distance_graph","as_edge_graph" })
	public static IGraph spatialDrivingFromEdges(final IScope scope, final IContainer edges, final IContainer nodes) {
		IGraph graph = new GamaSpatialGraph(edges, nodes,scope);
		return graph;
	}

}
