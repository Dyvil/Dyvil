package dyvil.tools.compiler.ast.dwt;

import dyvil.strings.CharUtils;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;

public class DWTProperty
{
	public DWTNode	node;
	public String	fullName;
	public Name		key;
	public IValue	value;
	public IMethod	setter;
	
	public DWTProperty(DWTNode node, Name key, IValue value)
	{
		this.fullName = getFullName(node.fullName, key.qualified);
		this.key = key;
		this.value = value;
	}
	
	private static String getFullName(String prefix, String name)
	{
		StringBuilder builder = new StringBuilder(prefix);
		int len = name.length();
		builder.append(CharUtils.toUpperCase(name.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(name.charAt(i));
		}
		return builder.toString();
	}
}
