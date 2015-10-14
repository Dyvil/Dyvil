package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class OptionType implements IObjectType
{
	public static final class Types
	{
		public static final IClass			OPTION_CLASS	= Package.dyvilUtil.resolveClass("Option");
		public static final ITypeVariable	OPTION_TYPE		= OPTION_CLASS.getTypeVariable(0);
		
		private Types()
		{
			// no instances
		}
	}
	
	private IType type;
	
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
		return Types.OPTION_CLASS;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return typeVar == Types.OPTION_TYPE ? this.type : null;
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
		IType type = concrete.resolveType(Types.OPTION_TYPE);
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
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		Types.OPTION_CLASS.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
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
