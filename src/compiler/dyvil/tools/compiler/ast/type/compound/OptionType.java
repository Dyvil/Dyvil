package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class OptionType implements IObjectType
{
	public static final class LazyFields
	{
		public static final IClass         OPTION_CLASS = Package.dyvilUtil.resolveClass("Option");
		public static final ITypeParameter OPTION_TYPE  = OPTION_CLASS.getTypeParameter(0);
		
		private LazyFields()
		{
			// no instances
		}
	}
	
	private IType type;

	public OptionType()
	{
	}

	public OptionType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public int typeTag()
	{
		return OPTIONAL;
	}
	
	@Override
	public boolean isGenericType()
	{
		return true;
	}
	
	@Override
	public Name getName()
	{
		return this.type.getName();
	}
	
	@Override
	public IClass getTheClass()
	{
		return LazyFields.OPTION_CLASS;
	}
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return typeParameter == LazyFields.OPTION_TYPE ? this.type : null;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType type = this.type.getConcreteType(context);
		if (type != this.type)
		{
			return new OptionType(type);
		}
		return this;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		IType type = concrete.resolveType(LazyFields.OPTION_TYPE);
		this.type.inferTypes(type, typeContext);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.type.checkType(markers, context, position);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		LazyFields.OPTION_CLASS.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return "dyvil/util/Option";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/util/Option;");
	}
	
	@Override
	public String getSignature()
	{
		StringBuilder sb = new StringBuilder();
		this.appendSignature(sb);
		return sb.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append("Ldyvil/util/Option<");
		this.type.appendSignature(buffer);
		buffer.append(">;");
	}
	
	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.INVOKESTATIC, "dyvil/util/Option", "apply", "()Ldyvil/util/Option;");
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
	}
	
	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
	}
	
	@Override
	public IConstantValue getDefaultValue()
	{
		return null;
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
	}
	
	@Override
	public String toString()
	{
		return this.type.toString() + '?';
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('?');
	}
	
	@Override
	public IType clone()
	{
		return new OptionType(this.type);
	}
}
