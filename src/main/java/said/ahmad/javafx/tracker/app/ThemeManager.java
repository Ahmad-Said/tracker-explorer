package said.ahmad.javafx.tracker.app;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class ThemeManager {
	// icons stuff
	public static final Image DEFAULT_ICON_IMAGE = new Image(ResourcesHelper.getResourceAsStream("/img/icon.png"));

	// themes stuff
	private static THEME appliedTheme = THEME.BOOTSTRAPV3;
	private static THEME_COLOR appliedThemeColor = THEME_COLOR.LIGHT;

	/**
	 *
	 * @see THEME
	 */
	public static enum THEME_COLOR {
		NONE, LIGHT, DARK
	}

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
	public static enum THEME {
		MODENAFX, // default theme no style sheet
		BOOTSTRAPV3, // bootstrapv3 css style sheet
		WINDOWS, // JMetro theme LIGHT or DARK
	}

	/** @return applied theme */
	public static THEME applyTheme(Scene scene) {
		scene.getStylesheets().clear();
		switch (appliedTheme) {
		case MODENAFX:
			break;
		case BOOTSTRAPV3:
			scene.getStylesheets().add(ResourcesHelper.getResourceAsString("/css/bootstrap3.css"));
			break;
		case WINDOWS:
			Style chosenStyle = appliedThemeColor.equals(THEME_COLOR.LIGHT) ? Style.LIGHT : Style.DARK;
			JMetro jmetro = new JMetro();
			jmetro.setAutomaticallyColorPanes(true);
			jmetro.setStyle(chosenStyle);
			jmetro.setScene(scene);
			break;
		default:
			break;
		}
		scene.getStylesheets().add(ResourcesHelper.getResourceAsString("/css/base.css"));
		return appliedTheme;
	}

	public static void changeDefaultTheme(THEME THEME, THEME_COLOR THEME_COLORe) {
		appliedTheme = THEME;
		appliedThemeColor = THEME_COLORe == null ? THEME_COLOR.NONE : THEME_COLORe;
	}

	public static void changeThemeAndApply(Scene scene, THEME THEME, THEME_COLOR THEME_COLOR) {
		changeDefaultTheme(THEME, THEME_COLOR);
		applyTheme(scene);
	}

	/**
	 * @return the appliedTheme
	 */
	public static THEME getAppliedTheme() {
		return appliedTheme;
	}

	/**
	 * @param appliedTheme the appliedTheme to set
	 */
	public static void setAppliedTheme(THEME appliedTheme) {
		ThemeManager.appliedTheme = appliedTheme;
	}

	/**
	 * @return the appliedThemeColor
	 */
	public static THEME_COLOR getAppliedThemeColor() {
		return appliedThemeColor;
	}

	/**
	 * @param appliedThemeColor the appliedThemeColor to set
	 */
	public static void setAppliedThemeColor(THEME_COLOR appliedThemeColor) {
		ThemeManager.appliedThemeColor = appliedThemeColor;
	}
}
