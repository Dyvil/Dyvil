package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class LambdaType implements IObjectType, ITyped, ITypeList
{
	private static final IClass[] functionClasses = new IClass[22];

	protected IType returnType;

	protected IType[] parameterTypes;
	protected int     parameterCount;

	// Metadata
	protected ICodePosition position;
	protected boolean       extension;

	public LambdaType()
	{
		this.parameterTypes = new IType[2];
	}

	public LambdaType(IType parameterType)
	{
		this.parameterTypes = new IType[1];
		this.parameterTypes[0] = parameterType;
		this.parameterCount = 1;
	}

	public LambdaType(IType[] types, int typeCount, IType returnType)
	{
		this.parameterTypes = types;
		this.parameterCount = typeCount;
		this.returnType = returnType;
	}

	public LambdaType(int typeCount)
	{
		this.parameterTypes = new IType[typeCount];
	}

	public LambdaType(ICodePosition position)
	{
		this();
		this.position = position;
	}

	public LambdaType(ICodePosition position, IType parameterType)
	{
		this.position = position;

		if (parameterType != null)
		{
			this.parameterTypes = new IType[] { parameterType };
			this.parameterCount = 1;
			return;
		}

		this.parameterTypes = new IType[2];
	}

	public LambdaType(ICodePosition position, IType receiverType, TupleType tupleType)
	{
		this.position = position;

		if (receiverType == null)
		{
			this.parameterTypes = tupleType.types;
			this.parameterCount = tupleType.typeCount;
			return;
		}

		final int count = tupleType.typeCount + 1;

		this.extension = true;
		this.parameterCount = count;
		this.parameterTypes = new IType[count];
		this.parameterTypes[0] = receiverType;

		System.arraycopy(tupleType.types, 0, this.parameterTypes, 1, tupleType.typeCount);
	}

	public static IClass getLambdaClass(int typeCount)
	{
		IClass iclass = functionClasses[typeCount];
		if (iclass != null)
		{
			return iclass;
		}

		iclass = Package.dyvilFunction.resolveClass("Function" + typeCount);
		functionClasses[typeCount] = iclass;
		return iclass;
	}

	@Override
	public int typeTag()
	{
		return LAMBDA;
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	@Override
	public Name getName()
	{
		return Names.Function;
	}

	@Override
	public void setType(IType type)
	{
		this.returnType = type;
	}

	@Override
	public IType getType()
	{
		return this.returnType;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setExtension(boolean extension)
	{
		this.extension = extension;
	}

	@Override
	public boolean isExtension()
	{
		return this.extension;
	}

	// ITypeList Overrides

	@Override
	public int typeCount()
	{
		return this.parameterCount;
	}

	@Override
	public void setType(int index, IType type)
	{
		this.parameterTypes[index] = type;
	}

	@Override
	public void addType(IType type)
	{
		int index = this.parameterCount++;
		if (this.parameterCount > this.parameterTypes.length)
		{
			IType[] temp = new IType[this.parameterCount];
			System.arraycopy(this.parameterTypes, 0, temp, 0, index);
			this.parameterTypes = temp;
		}
		this.parameterTypes[index] = type;
	}

	@Override
	public IType getType(int index)
	{
		return this.parameterTypes[index];
	}

	// IType Overrides

	@Override
	public IClass getTheClass()
	{
		return getLambdaClass(this.parameterCount);
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!IObjectType.super.isSuperTypeOf(type))
		{
			return false;
		}

		final IClass functionClass = this.getTheClass();

		ITypeParameter typeVar = functionClass.getTypeParameter(this.parameterCount);
		IType resolvedType = Types.resolveTypeSafely(type, typeVar);

		// Return Type is Covariant
		if (!Types.isSuperType(this.returnType, resolvedType))
		{
			return false;
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			typeVar = functionClass.getTypeParameter(i);
			resolvedType = type.resolveType(typeVar);

			// Parameter Types are Contravariant
			if (!Types.isSuperType(resolvedType, this.parameterTypes[i]))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return this.parameterCount == 0 && Types.isSuperType(this.returnType, type);
	}

	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.parameterCount != 0)
		{
			return value.withType(this, typeContext, markers, context);
		}

		final IType valueType = value.getType();
		if (valueType == Types.UNKNOWN || IObjectType.super.isSuperTypeOf(valueType))
		{
			return value.withType(this, typeContext, markers, context);
		}

		final IValue typedReturnValue = value.withType(this.returnType, typeContext, markers, context);
		if (typedReturnValue != null)
		{
			return this.wrapLambda(typedReturnValue);
		}
		return null;
	}

	public LambdaExpr wrapLambda(IValue value)
	{
		IType returnType = value.getType();

		final LambdaExpr lambdaExpr = new LambdaExpr(value.getPosition(), null, 0);
		lambdaExpr.setImplicitParameters(true);
		lambdaExpr.setMethod(this.getFunctionalMethod());
		lambdaExpr.setValue(value);
		lambdaExpr.inferReturnType(this, returnType);
		return lambdaExpr;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() != this.getTheClass())
		{
			return null;
		}

		final int index = typeParameter.getIndex();
		if (index == this.parameterCount)
		{
			return this.returnType;
		}
		if (index > this.parameterCount)
		{
			return null;
		}
		return this.parameterTypes[index];
	}

	@Override
	public boolean hasTypeVariables()
	{
		if (this.returnType.hasTypeVariables())
		{
			return true;
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameterTypes[i].hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType[] parameterTypes = GenericType.getConcreteTypes(this.parameterTypes, this.parameterCount, context);
		final boolean parametersChanged = parameterTypes != this.parameterTypes;

		final IType returnType = this.returnType.getConcreteType(context);
		if (!parametersChanged && returnType == this.returnType)
		{
			// Nothing changed, no need to create a new instance
			return this;
		}

		// Create a defensive copy of the parameter types in case the array has not changed
		final LambdaType lt = new LambdaType(parametersChanged ? parameterTypes : parameterTypes.clone(), this.parameterCount, returnType);
		lt.extension = this.extension;
		return lt;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		boolean found = false;

		ITypeParameter typeVar;
		IType elementType;
		final IClass thisClass = this.getTheClass();
		for (int i = 0; i < this.parameterCount; i++)
		{
			typeVar = thisClass.getTypeParameter(i);
			elementType = concrete.resolveType(typeVar);
			if (elementType != null)
			{
				this.parameterTypes[i].inferTypes(elementType, typeContext);
				found = true;
			}
		}

		typeVar = thisClass.getTypeParameter(this.parameterCount);
		elementType = concrete.resolveType(typeVar);
		if (elementType != null)
		{
			this.returnType.inferTypes(elementType, typeContext);
			found = true;
		}

		if (!found && this.parameterCount == 0 && Types.isSuperType(this.returnType, concrete))
		{
			this.returnType.inferTypes(concrete, typeContext);
		}
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i] = this.parameterTypes[i].resolveType(markers, context);
		}
		if (this.returnType == null)
		{
			this.returnType = Types.UNKNOWN;
			markers.add(Markers.semanticError(this.position, "type.lambda.return"));
		}
		else
		{
			this.returnType = this.returnType.resolveType(markers, context).asReturnType();
		}

		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].resolve(markers, context);
		}
		this.returnType.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if (position == TypePosition.CLASS)
		{
			markers.add(Markers.semantic(this.position, "type.lambda.class"));
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].checkType(markers, context, TypePosition.PARAMETER_TYPE);
		}
		this.returnType.checkType(markers, context, TypePosition.RETURN_TYPE);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].check(markers, context);
		}
		this.returnType.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].foldConstants();
		}
		this.returnType.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].cleanup(context, compilableList);
		}
		this.returnType.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		getLambdaClass(this.parameterCount).getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		getLambdaClass(this.parameterCount).getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return getLambdaClass(this.parameterCount).getFunctionalMethod();
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.returnType.writeTypeExpression(writer);

		writer.visitLdcInsn(this.parameterCount);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvilx/lang/model/type/Type");
		for (int i = 0; i < this.parameterCount; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.parameterTypes[i].writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/FunctionType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;[Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/FunctionType;",
		                       false);
	}

	@Override
	public String getInternalName()
	{
		return "dyvil/function/Function" + this.parameterCount;
	}

	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/function/Function").append(this.parameterCount).append(';');
	}

	@Override
	public void appendSignature(StringBuilder buffer, boolean genericArg)
	{
		buffer.append("Ldyvil/function/Function").append(this.parameterCount).append('<');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].appendSignature(buffer, true);
		}
		this.returnType.appendSignature(buffer, true);
		buffer.append(">;");
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		int index = typePath.getStepArgument(step);
		if (index < this.parameterCount)
		{
			this.parameterTypes[index] = IType
				                             .withAnnotation(this.parameterTypes[index], annotation, typePath, step + 1,
				                                             steps);
			return;
		}
		this.returnType = IType.withAnnotation(this.returnType, annotation, typePath, step + 1, steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameterTypes[i].writeAnnotations(visitor, typeRef, typePath + i + ';');
		}
		this.returnType.writeAnnotations(visitor, typeRef, typePath + this.parameterCount + ';');
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.extension ? -this.parameterCount : this.parameterCount);
		for (int i = 0; i < this.parameterCount; i++)
		{
			IType.writeType(this.parameterTypes[i], out);
		}
		IType.writeType(this.returnType, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		int len = in.readByte();
		if (len < 0)
		{
			len = -len;
			this.extension = true;
		}

		this.parameterCount = len;
		if (len > this.parameterTypes.length)
		{
			this.parameterTypes = new IType[len];
		}
		for (int i = 0; i < len; i++)
		{
			this.parameterTypes[i] = IType.readType(in);
		}
		this.returnType = IType.readType(in);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		if (this.parameterCount > 0)
		{
			sb.append(this.parameterTypes[0]);
			for (int i = 1; i < this.parameterCount; i++)
			{
				sb.append(", ").append(this.parameterTypes[i]);
			}
		}
		sb.append(") -> ").append(this.returnType);
		return sb.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final IType parameterType;
		final int parameterTypeTag;
		if (this.parameterCount == 1 && (parameterTypeTag = (parameterType = this.parameterTypes[0]).typeTag()) != TUPLE
			    && parameterTypeTag != LAMBDA && !Formatting.getBoolean("lambda.single.wrap"))
		{
			// Single Parameter Type that is neither a Lambda Type nor a Tuple Type

			parameterType.toString(prefix, buffer);

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (this.parameterCount > 0)
		{
			buffer.append('(');
			if (Formatting.getBoolean("lambda.open_paren.space_after"))
			{
				buffer.append(' ');
			}

			Util.astToString(prefix, this.parameterTypes, this.parameterCount,
			                 Formatting.getSeparator("lambda.separator", ','), buffer);

			if (Formatting.getBoolean("lambda.close_paren.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(')');

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (Formatting.getBoolean("lambda.empty.wrap"))
		{
			buffer.append("()");

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append("->");

		if (Formatting.getBoolean("lambda.arrow.space_after"))
		{
			buffer.append(' ');
		}

		this.returnType.toString("", buffer);
	}

	@Override
	public IType clone()
	{
		final LambdaType lt = new LambdaType(this.parameterCount);
		lt.parameterCount = this.parameterCount;
		System.arraycopy(this.parameterTypes, 0, lt.parameterTypes, 0, this.parameterCount);
		lt.returnType = this.returnType;
		lt.extension = this.extension;
		return lt;
	}
}
