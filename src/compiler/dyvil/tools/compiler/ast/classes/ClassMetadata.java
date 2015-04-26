package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.ClassParameterSetter;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected final IClass	theClass;
	
	protected Constructor	constructor;
	protected IConstructor	superConstructor;
	
	public ClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
		
		Constructor constructor = new Constructor(this.theClass);
		constructor.modifiers = Modifiers.PUBLIC;
		this.constructor = constructor;
	}
	
	@Override
	public IConstructor getConstructor()
	{
		return this.constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.constructor.type = this.theClass.getType();
		this.constructor.setParameters(this.theClass.getParameters(), this.theClass.parameterCount());
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IType superType = this.theClass.getSuperType();
		if (superType == null)
		{
			return;
		}
		
		IConstructor match = IContext.resolveConstructor(markers, superType, EmptyArguments.INSTANCE);
		if (match != null)
		{
			this.superConstructor = match;
			return;
		}
		
		markers.add(this.theClass.getPosition(), "constructor.super", superType.toString());
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		int match = this.constructor.getSignatureMatch(arguments);
		if (match > 0)
		{
			list.add(new ConstructorMatch(this.constructor, match));
		}
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		StatementList list = new StatementList();
		if (instanceFields != null)
		{
			list.addValue(instanceFields);
		}
		if (this.superConstructor != null)
		{
			list.addValue(new InitializerCall(null, this.superConstructor, EmptyArguments.INSTANCE, true));
		}
		int count = this.theClass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = this.theClass.getParameter(i);
			list.addValue(new ClassParameterSetter(this.theClass, param));
		}
		
		this.constructor.setValue(list);
		this.constructor.write(writer, instanceFields);
	}
}
