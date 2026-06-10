package io.github.pgatzka.projector.layout;

/**
 * Shared text metrics so layout sizing and SVG rendering agree on font size and the
 * heuristic glyph width. Width is estimated (length * {@link #CHAR_WIDTH}) rather than
 * measured with AWT, keeping everything headless-safe and deterministic.
 */
public final class TextMetrics {

	/** Font size (px) for node labels. */
	public static final double FONT_SIZE = 13;

	/** Font size (px) for edge guard labels. */
	public static final double GUARD_FONT_SIZE = 11;

	/** Estimated average glyph width (px) at {@link #FONT_SIZE} for a sans-serif font. */
	public static final double CHAR_WIDTH = 7.2;

	private TextMetrics() {
	}

	/** Estimated rendered width (px) of {@code text} at {@link #FONT_SIZE}. */
	public static double width(String text) {
		return text == null ? 0 : text.length() * CHAR_WIDTH;
	}
}
