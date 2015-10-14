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
import dyvil.tools.compiler.ast.constant.NullValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Map;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class MapType implements IObjectType
{
	private IType	keyType;
	private IType	valueType;
	
	public MapType(IType keyType, IType valueType)
	{
		this.keyType = keyType;
		this.valueType = valueType;
	}
	
	@Override
	public int typeTag()
	{
		return MAP;
	}
	
	@Override
	public boolean isGenericType()
	{
		return true;
	}
	
	public void setKeyType(IType keyType)
	{
		this.keyType = keyType;
	}
	
	public IType getKeyType()
	{
		return this.keyType;
	}
	
	public void setValueType(IType valueType)
	{
		this.valueType = valueType;
	}
	
	public IType getValueType()
	{
		return this.valueType;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified("Map");
	}
	
	@Override
	public IClass getTheClass()
	{
		return Map.Types.MAP_CLASS;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (typeVar == Map.Types.KEY_VARIABLE)
		{
			return this.keyType;
		}
		if (typeVar == Map.Types.VALUE_VARIABLE)
		{
			return this.valueType;
		}
		return Map.Types.MAP_CLASS.resolveType(typeVar, this);
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.keyType.hasTypeVariables() || this.valueType.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType newKeyType = this.keyType.getConcreteType(context);
		IType newValueType = this.valueType.getConcreteType(context);
		if (newKeyType != this.keyType || newValueType != this.valueType)
		{
			return new MapType(newKeyType, newValueType);
		}
		return this;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.keyType.inferTypes(concrete.resolveType(Map.Types.KEY_VARIABLE), typeContext);
		this.valueType.inferTypes(concrete.resolveType(Map.Types.VALUE_VARIABLE), typeContext);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.keyType = this.keyType.resolveType(markers, context);
		this.valueType = this.valueType.resolveType(markers, context);
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.keyType.checkType(markers, context, position);
		this.valueType.checkType(markers, context, position);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.keyType.check(markers, context);
		this.valueType.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.keyType.foldConstants();
		this.valueType.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.keyType.cleanup(context, compilableList);
		this.valueType.cleanup(context, compilableList);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		Map.Types.MAP_CLASS.getMethodMatches(list, instance, name, arguments);
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
		return Map.Types.MAP_CLASS.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/collection/Map;");
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
		buffer.append("Ldyvil/collection/Map;");
		this.keyType.appendSignature(buffer);
		this.valueType.appendSignature(buffer);
		buffer.append('>').append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.keyType.writeTypeExpression(writer);
		this.valueType.writeTypeExpression(writer);
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/MapType", "apply",
				"(Ldyvil/lang/Type;Ldyvil/lang/Type;)Ldyvil/reflect/types/MapType;", true);
	}
	
	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}
		
		int index = typePath.getStepArgument(step);
		if (index == 1)
		{
			this.valueType = IType.withAnnotation(this.valueType, annotation, typePath, step + 1, steps);
		}
		this.keyType = IType.withAnnotation(this.keyType, annotation, typePath, step + 1, steps);
	}
	
	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.keyType.writeAnnotations(visitor, typeRef, typePath + "0;");
		this.valueType.writeAnnotations(visitor, typeRef, typePath + "1;");
	}
	
	@Override
	public IConstantValue getDefaultValue()
	{
		return NullValue.getNull();
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.keyType, out);
		IType.writeType(this.valueType, out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.keyType = IType.readType(in);
		this.valueType = IType.readType(in);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('[');
		this.keyType.toString(prefix, buffer);
		buffer.append(':');
		this.valueType.toString(prefix, buffer);
		buffer.append(']');
	}
	
	@Override
	public IType clone()
	{
		return new MapType(this.keyType, this.valueType);
	}
}
