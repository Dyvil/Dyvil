package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.classes.AnonymousClass;
import dyvil.tools.compiler.ast.classes.AnonymousClassMetadata;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ClassConstructor extends ConstructorCall
{
	private AnonymousClass         nestedClass;
	private AnonymousClassMetadata metadata;
	
	public ClassConstructor(ICodePosition position)
	{
		this.position = position;
		this.nestedClass = new AnonymousClass(position);
	}
	
	public ClassConstructor(ICodePosition position, IType type, IArguments arguments)
	{
		super(position, type, arguments);
	}
	
	public AnonymousClass getNestedClass()
	{
		return this.nestedClass;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.type.getTheClass().isInterface())
		{
			this.nestedClass.addInterface(this.type);
		}
		else
		{
			this.nestedClass.setSuperType(this.type);
		}
		
		this.nestedClass.context = context;
		this.nestedClass.resolveTypes(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.arguments.resolve(markers, context);
		
		if (this.type.getTheClass().isInterface())
		{
			this.constructor = IContext.resolveConstructor(Types.OBJECT_CLASS, this.arguments);
		}
		else
		{
			this.constructor = IContext.resolveConstructor(this.type, this.arguments);
		}
		if (this.constructor == null)
		{
			this.reportResolve(markers, context);
		}
		
		this.metadata = new AnonymousClassMetadata(this.nestedClass, this.constructor);
		this.nestedClass.setMetadata(this.metadata);
		this.nestedClass.setOuterClass(context.getThisClass());
		
		IDyvilHeader header = context.getHeader();
		this.nestedClass.setHeader(header);
		header.addInnerClass(this.nestedClass);
		
		this.nestedClass.context = context;
		this.nestedClass.resolve(markers, context);
		this.nestedClass.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		this.nestedClass.context = context;
		this.nestedClass.checkTypes(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
		
		this.nestedClass.context = context;
		this.nestedClass.check(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		
		this.nestedClass.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.cleanup(context, compilableList);
		this.nestedClass.cleanup(context, compilableList);
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.metadata.writeConstructorCall(writer, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		IClassBody body = this.nestedClass.getBody();
		if (body != null)
		{
			buffer.append(' ');
			body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(" {}");
		}
	}
}
