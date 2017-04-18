package catan.ui;

class NumberField extends SaneTextField
{
	NumberField(final String placeholder)
	{
		super(placeholder);
		setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
	}

	int getNumericValue() throws NumberFormatException
	{
		return Integer.parseInt(getText());
	}
}
