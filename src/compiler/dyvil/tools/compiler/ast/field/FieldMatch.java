package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;

public class FieldMatch
{
	public IField	theField;
	public int		match;
	
	public FieldMatch(IField theField, int match)
	{
		this.theField = theField;
		this.match = match;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.theField.toString("", buf);
		return buf.toString();
	}
}
