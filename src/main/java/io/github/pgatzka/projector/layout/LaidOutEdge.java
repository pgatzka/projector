package io.github.pgatzka.projector.layout;

import java.util.List;

/**
 * An edge with its computed route. {@code points} runs from the source attachment through
 * any bend points to the target attachment (always at least two points).
 */
public record LaidOutEdge(String from, String to, String guard, List<Point> points) {

	public LaidOutEdge {
		points = List.copyOf(points);
	}
}
