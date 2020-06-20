package said.ahmad.javafx.tracker.app.look;

/**
 * Themes:
 * <ul>
 * <li><b>MODENAFX</b> Built in no style sheet, no THEME_COLOR</li>
 *
 * <li><b>BOOTSTRAPV3</b> bootstrapv3 css style sheet, no THEME_COLOR</li>
 *
 * <li><b>WINDOWS</b> JMetro theme Windows style, require refreshing the view.
 * <br>
 * Available {@link THEME_COLOR}:
 * <ul>
 * <li>{@link THEME_COLOR#LIGHT}</li>
 * <li>{@link THEME_COLOR#DARK}</li>
 * </ul>
 * </li>
 *
 * </ul>
 *
 *
 */
public enum THEME {
	MODENAFX, // default theme no style sheet
	BOOTSTRAPV3, // bootstrapv3 css style sheet
	WINDOWS, // JMetro theme LIGHT or DARK
}
