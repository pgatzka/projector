package io.github.pgatzka.projector.layout;

import io.github.pgatzka.projector.model.Node;

/**
 * Heuristic node sizing. Text width is estimated as {@code length * CHAR_WIDTH} rather than
 * measured with AWT, which keeps layout headless-safe and deterministic. The same font
 * constants are reused by the SVG renderer so text and boxes stay consistent.
 */
final class NodeSizer {

	/** Font size (px) used for node labels; shared with the SVG renderer. */
	static final double FONT_SIZE = 13;

	/** Estimated average glyph width (px) at {@link #FONT_SIZE} for a sans-serif font. */
	static final double CHAR_WIDTH = 7.2;

	private static final double ACTION_MIN_WIDTH = 90;
	private static final double ACTION_HEIGHT = 40;
	private static final double LABEL_H_PADDING = 24;

	private static final double TERMINAL_DIAMETER = 28; // start / end
	private static final double DECISION_MIN_WIDTH = 80;
	private static final double DECISION_HEIGHT = 56;   // diamonds need vertical room for text
	private static final double DECISION_H_PADDING = 40;

	private static final double BAR_LENGTH = 60;        // fork / join synchronization bar
	private static final double BAR_THICKNESS = 10;

	private NodeSizer() {
	}

	static double textWidth(String text) {
		return text == null ? 0 : text.length() * CHAR_WIDTH;
	}

	/** Returns {@code [width, height]} for the node. */
	static double[] size(Node node) {
		return switch (node.type()) {
			case START, END -> new double[] { TERMINAL_DIAMETER, TERMINAL_DIAMETER };
			case FORK, JOIN -> new double[] { BAR_LENGTH, BAR_THICKNESS };
			case ACTION -> new double[] {
					Math.max(ACTION_MIN_WIDTH, textWidth(node.label()) + LABEL_H_PADDING),
					ACTION_HEIGHT };
			case DECISION, MERGE -> new double[] {
					Math.max(DECISION_MIN_WIDTH, textWidth(node.label()) + DECISION_H_PADDING),
					DECISION_HEIGHT };
		};
	}
}
