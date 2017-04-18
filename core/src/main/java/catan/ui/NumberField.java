package catan.ui;

public class NumberField extends SaneTextField
{
	public NumberField(final String placeholder)
	{
		super(placeholder);
		setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
	}

	public int getNumericValue() throws NumberFormatException
	{
		return Integer.parseInt(getText());
	}
}
