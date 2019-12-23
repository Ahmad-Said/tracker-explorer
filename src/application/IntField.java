package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * example of using this class: https://gist.github.com/jewelsea/1962045
 *
 * using in scene builder check
 * https://jaxenter.com/netbeans/making-custom-javafx-controls-available-in-the-scene-builder
 * or simply create new maven project copy class into it and run maven build
 * then import jar into scene builder
 *
 */
// helper text field subclass which restricts text input to a given range of
// natural int numbers
// and exposes the current numeric int value of the edit box as a value
// property.
public class IntField extends TextField {
	final private IntegerProperty value;

	private int minValue;
	private int maxValue;
	private Slider slider = null;

	public IntField() {
		this(0, Integer.MAX_VALUE, 0);
	}

	public IntField(int minValue, int maxValue, int initialValue) {
		if (minValue > maxValue) {
			throw new IllegalArgumentException(
					"IntField min value " + minValue + " greater than max value " + maxValue);
		}
		if (maxValue < minValue) {
			throw new IllegalArgumentException("IntField max value " + minValue + " less than min value " + maxValue);
		}
		if (!(minValue <= initialValue && initialValue <= maxValue)) {
			throw new IllegalArgumentException(
					"IntField initialValue " + initialValue + " not between " + minValue + " and " + maxValue);
		}

		// initialize the field values.
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = new SimpleIntegerProperty(initialValue);
		setText(initialValue + "");

		final IntField intField = this;

		// make sure the value property is clamped to the required range
		// and update the field's text to be in sync with the value.
		value.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					intField.setText("");
				} else {
					if (newValue.intValue() < intField.minValue) {
						value.setValue(intField.minValue);
						return;
					}

					if (newValue.intValue() > intField.maxValue) {
						value.setValue(intField.maxValue);
						return;
					}

					if (newValue.intValue() == 0 && (textProperty().get() == null || "".equals(textProperty().get()))) {
						// no action required, text property is already blank, we don't need to set it
						// to 0.
					} else {
						intField.setText(newValue.toString());
					}
				}
			}
		});

		// restrict key input to numerals.
		this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (!"0123456789".contains(keyEvent.getCharacter())) {
					keyEvent.consume();
				}
			}
		});

		// ensure any entered values lie inside the required range.
		textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (newValue == null || "".equals(newValue)) {
					value.setValue(0);
					return;
				}

				final int intValue = Integer.parseInt(newValue);

				if (intField.minValue > intValue || intValue > intField.maxValue) {
					textProperty().setValue(oldValue);
				}

				value.set(Integer.parseInt(textProperty().get()));
			}
		});
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
		if (this.slider != null) {
			slider.setMin(minValue);
		}
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		if (this.slider != null) {
			slider.setMax(maxValue);
		}

	}

	public Slider getSlider() {
		return slider;
	}

	public void setSlider(Slider slider) {
		this.slider = slider;
		valueProperty().bindBidirectional(slider.valueProperty());
	}

	// expose an integer value property for the text field.
	public int getValue() {
		return value.getValue();
	}

	public void setValue(int newValue) {
		value.setValue(newValue);
	}

	public IntegerProperty valueProperty() {
		return value;
	}

	public void incrementByOne() {
		this.setValue(this.getValue() + 1);
	}

	public void decrementByOne() {
		this.setValue(this.getValue() - 1);
	}

}