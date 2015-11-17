package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;

public interface IClassBodyConsumer
{
	void addField(IField field);
	
	void addProperty(IProperty property);
	
	void addConstructor(IConstructor constructor);
	
	void addMethod(IMethod method);
}
