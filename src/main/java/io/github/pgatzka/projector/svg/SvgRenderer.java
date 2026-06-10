package io.github.pgatzka.projector.svg;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import io.github.pgatzka.projector.layout.LaidOutDiagram;
import io.github.pgatzka.projector.layout.LaidOutEdge;
import io.github.pgatzka.projector.layout.LaidOutNode;
import io.github.pgatzka.projector.layout.Point;
import io.github.pgatzka.projector.layout.TextMetrics;

/**
 * Renders a {@link LaidOutDiagram} to a self-contained SVG document. Styling lives in one
 * embedded {@code <style>} block using {@code pj-}-namespaced classes ("clean minimal"
 * theme); arrowheads use a single reusable {@code <marker>}.
 *
 * <p>Paint order is background, edges, nodes, then guard labels, so nodes sit above edges
 * and guards stay readable on top.
 */
@Component
public class SvgRenderer {

	private static final double MARGIN = 12;
	private static final String FONT_FAMILY =
			"-apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif";

	private static final String STYLE = """
			text { font-family: %s; }
			.pj-edge { fill: none; stroke: #333; stroke-width: 1.5; }
			.pj-arrowhead { fill: #333; }
			.pj-action { fill: #fff; stroke: #333; stroke-width: 1.5; }
			.pj-decision { fill: #fff; stroke: #333; stroke-width: 1.5; }
			.pj-terminal { fill: #333; stroke: #333; }
			.pj-end-ring { fill: #fff; stroke: #333; stroke-width: 1.5; }
			.pj-end-dot { fill: #333; }
			.pj-bar { fill: #333; }
			.pj-label { fill: #111; font-size: %spx; }
			.pj-guard { fill: #555; font-size: %spx; }
			.pj-bg { fill: #fff; }
			""".formatted(FONT_FAMILY, num(TextMetrics.FONT_SIZE), num(TextMetrics.GUARD_FONT_SIZE));

	public String render(LaidOutDiagram diagram) {
		double width = diagram.width() + 2 * MARGIN;
		double height = diagram.height() + 2 * MARGIN;

		StringBuilder svg = new StringBuilder(1024);
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
				.append(num(width)).append(' ').append(num(height)).append("\" width=\"")
				.append(num(width)).append("\" height=\"").append(num(height)).append("\">\n");
		svg.append("<style>\n").append(STYLE).append("</style>\n");
		svg.append("""
				<defs>
				<marker id="pj-arrow" viewBox="0 0 10 10" refX="9" refY="5" \
				markerWidth="8" markerHeight="8" orient="auto">
				<path class="pj-arrowhead" d="M0,0 L10,5 L0,10 z"/>
				</marker>
				</defs>
				""");
		svg.append("<rect class=\"pj-bg\" x=\"0\" y=\"0\" width=\"").append(num(width))
				.append("\" height=\"").append(num(height)).append("\"/>\n");

		// translate everything by MARGIN so the drawing isn't flush against the edge
		svg.append("<g transform=\"translate(").append(num(MARGIN)).append(',').append(num(MARGIN)).append(")\">\n");

		for (LaidOutEdge edge : diagram.edges()) {
			appendEdgePath(svg, edge);
		}
		for (LaidOutNode node : diagram.nodes()) {
			appendNode(svg, node);
		}
		for (LaidOutEdge edge : diagram.edges()) {
			appendGuard(svg, edge);
		}

		svg.append("</g>\n</svg>\n");
		return svg.toString();
	}

	private void appendEdgePath(StringBuilder svg, LaidOutEdge edge) {
		List<Point> points = edge.points();
		if (points.size() < 2) {
			return;
		}
		StringBuilder d = new StringBuilder();
		d.append('M').append(num(points.getFirst().x())).append(' ').append(num(points.getFirst().y()));
		for (int i = 1; i < points.size(); i++) {
			d.append(" L").append(num(points.get(i).x())).append(' ').append(num(points.get(i).y()));
		}
		svg.append("<path class=\"pj-edge\" marker-end=\"url(#pj-arrow)\" d=\"").append(d).append("\"/>\n");
	}

	private void appendNode(StringBuilder svg, LaidOutNode node) {
		double cx = node.x() + node.width() / 2;
		double cy = node.y() + node.height() / 2;
		switch (node.type()) {
			case START -> svg.append("<circle class=\"pj-terminal\" cx=\"").append(num(cx))
					.append("\" cy=\"").append(num(cy)).append("\" r=\"").append(num(node.width() / 2)).append("\"/>\n");
			case END -> {
				double r = node.width() / 2;
				svg.append("<circle class=\"pj-end-ring\" cx=\"").append(num(cx)).append("\" cy=\"").append(num(cy))
						.append("\" r=\"").append(num(r)).append("\"/>\n");
				svg.append("<circle class=\"pj-end-dot\" cx=\"").append(num(cx)).append("\" cy=\"").append(num(cy))
						.append("\" r=\"").append(num(r * 0.5)).append("\"/>\n");
			}
			case ACTION -> {
				svg.append("<rect class=\"pj-action\" x=\"").append(num(node.x())).append("\" y=\"").append(num(node.y()))
						.append("\" width=\"").append(num(node.width())).append("\" height=\"").append(num(node.height()))
						.append("\" rx=\"6\"/>\n");
				appendLabel(svg, node.label(), cx, cy);
			}
			case DECISION, MERGE -> {
				appendDiamond(svg, node);
				appendLabel(svg, node.label(), cx, cy);
			}
			case FORK, JOIN -> svg.append("<rect class=\"pj-bar\" x=\"").append(num(node.x())).append("\" y=\"")
					.append(num(node.y())).append("\" width=\"").append(num(node.width())).append("\" height=\"")
					.append(num(node.height())).append("\" rx=\"2\"/>\n");
		}
	}

	private void appendDiamond(StringBuilder svg, LaidOutNode node) {
		double cx = node.x() + node.width() / 2;
		double cy = node.y() + node.height() / 2;
		String points = num(cx) + ',' + num(node.y()) + ' '
				+ num(node.x() + node.width()) + ',' + num(cy) + ' '
				+ num(cx) + ',' + num(node.y() + node.height()) + ' '
				+ num(node.x()) + ',' + num(cy);
		svg.append("<polygon class=\"pj-decision\" points=\"").append(points).append("\"/>\n");
	}

	private void appendLabel(StringBuilder svg, String label, double cx, double cy) {
		if (label == null || label.isBlank()) {
			return;
		}
		svg.append("<text class=\"pj-label\" x=\"").append(num(cx)).append("\" y=\"").append(num(cy))
				.append("\" text-anchor=\"middle\" dominant-baseline=\"central\">")
				.append(escape(label)).append("</text>\n");
	}

	private void appendGuard(StringBuilder svg, LaidOutEdge edge) {
		if (edge.guard() == null || edge.guard().isBlank()) {
			return;
		}
		Point mid = midpoint(edge.points());
		if (mid == null) {
			return;
		}
		svg.append("<text class=\"pj-guard\" x=\"").append(num(mid.x() + 4)).append("\" y=\"").append(num(mid.y() - 4))
				.append("\" text-anchor=\"middle\">").append(escape(edge.guard())).append("</text>\n");
	}

	/** Returns the point at half the total polyline length. */
	private static Point midpoint(List<Point> points) {
		if (points.size() < 2) {
			return points.isEmpty() ? null : points.getFirst();
		}
		double total = 0;
		for (int i = 1; i < points.size(); i++) {
			total += distance(points.get(i - 1), points.get(i));
		}
		double half = total / 2;
		double walked = 0;
		for (int i = 1; i < points.size(); i++) {
			Point a = points.get(i - 1);
			Point b = points.get(i);
			double seg = distance(a, b);
			if (walked + seg >= half) {
				double t = seg == 0 ? 0 : (half - walked) / seg;
				return new Point(a.x() + t * (b.x() - a.x()), a.y() + t * (b.y() - a.y()));
			}
			walked += seg;
		}
		return points.get(points.size() / 2);
	}

	private static double distance(Point a, Point b) {
		return Math.hypot(b.x() - a.x(), b.y() - a.y());
	}

	/** Formats a coordinate compactly (integers without a decimal point, else up to 2 dp). */
	private static String num(double value) {
		String s = String.format(Locale.US, "%.2f", value);
		if (s.contains(".")) {
			s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return s;
	}

	private static String escape(String text) {
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '&' -> out.append("&amp;");
				case '<' -> out.append("&lt;");
				case '>' -> out.append("&gt;");
				case '"' -> out.append("&quot;");
				case '\'' -> out.append("&#39;");
				default -> out.append(c);
			}
		}
		return out.toString();
	}
}
