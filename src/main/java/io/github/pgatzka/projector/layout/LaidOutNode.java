package io.github.pgatzka.projector.layout;

import io.github.pgatzka.projector.model.NodeType;

/**
 * A node with its computed position and size. {@code (x, y)} is the top-left corner of the
 * node's bounding box in diagram coordinates.
 */
public record LaidOutNode(String id, NodeType type, String label,
		double x, double y, double width, double height) {
}
