package io.github.pgatzka.projector.layout;

import io.github.pgatzka.projector.model.Node;

/**
 * Heuristic node sizing built on {@link TextMetrics}, which keeps layout headless-safe and
 * deterministic and keeps box sizes consistent with the text the SVG renderer draws.
 */
final class NodeSizer {

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

	/** Returns {@code [width, height]} for the node. */
	static double[] size(Node node) {
		return switch (node.type()) {
			case START, END -> new double[] { TERMINAL_DIAMETER, TERMINAL_DIAMETER };
			case FORK, JOIN -> new double[] { BAR_LENGTH, BAR_THICKNESS };
			case ACTION -> new double[] {
					Math.max(ACTION_MIN_WIDTH, TextMetrics.width(node.label()) + LABEL_H_PADDING),
					ACTION_HEIGHT };
			case DECISION, MERGE -> new double[] {
					Math.max(DECISION_MIN_WIDTH, TextMetrics.width(node.label()) + DECISION_H_PADDING),
					DECISION_HEIGHT };
		};
	}
}
