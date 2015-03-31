package dyvil.tools.compiler.ast.classes;

import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected final IClass	theClass;
	
	protected IConstructor	constructor;
	
	public ClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IConstructor getConstructor()
	{
		if (this.constructor != null)
		{
			return this.constructor;
		}
		
		Constructor constructor = new Constructor(this.theClass);
		constructor.modifiers = Modifiers.PUBLIC | Modifiers.SYNTHETIC;
		return this.constructor = constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.constructor != null)
		{
			int match = this.constructor.getSignatureMatch(arguments);
			if (match > 0)
			{
				list.add(new ConstructorMatch(this.constructor, match));
			}
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.constructor != null)
		{
			// TODO
		}
	}
}
