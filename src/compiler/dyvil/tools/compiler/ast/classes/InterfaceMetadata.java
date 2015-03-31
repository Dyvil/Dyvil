package dyvil.tools.compiler.ast.classes;

import org.objectweb.asm.ClassWriter;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class InterfaceMetadata implements IClassMetadata
{
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields)
	{
	}
}
