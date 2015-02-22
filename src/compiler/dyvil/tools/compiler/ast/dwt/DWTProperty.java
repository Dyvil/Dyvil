package dyvil.tools.compiler.ast.dwt;

import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.util.DWTUtil;

public class DWTProperty
{
	public DWTNode node;
	public String	fullName;
	public String	key;
	public IValue	value;
	public IMethod	setter;
	
	public DWTProperty(DWTNode node, String key, IValue value)
	{
		this.fullName = DWTUtil.getFullName(node.fullName, key);
		this.key = key;
		this.value = value;
	}
}
