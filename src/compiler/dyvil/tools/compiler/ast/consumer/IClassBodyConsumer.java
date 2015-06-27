package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;

public interface IClassBodyConsumer
{
	public void addField(IField field);
	
	public void addProperty(IProperty property);
	
	public void addConstructor(IConstructor constructor);
	
	public void addMethod(IMethod method);
}
