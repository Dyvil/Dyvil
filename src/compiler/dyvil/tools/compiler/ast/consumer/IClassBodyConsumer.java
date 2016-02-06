package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;

public interface IClassBodyConsumer
{
	void addField(IField field);
	
	void addProperty(IProperty property);
	
	void addConstructor(IConstructor constructor);

	void addInitializer(IInitializer initializer);
	
	void addMethod(IMethod method);
}
