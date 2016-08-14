package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ClassOperator extends AbstractValue implements IConstantValue
{
	public static final class LazyFields
	{
		public static final IClass    CLASS_CLASS = Package.javaLang.resolveClass("Class");
		public static final ClassType CLASS       = new ClassType(CLASS_CLASS);

		public static final IClass CLASS_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                               .resolveClass(Name.fromRaw("FromClass"));
		
		private LazyFields()
		{
			// no instances
		}
	}
	
	protected IType type;
	
	// Metadata
	private IType genericType;
	
	public ClassOperator(ICodePosition position)
	{
		this.position = position;
	}
	
	public ClassOperator(IType type)
	{
		this.setType(type);
	}
	
	@Override
	public int valueTag()
	{
		return CLASS_OPERATOR;
	}
	
	@Override
	public Object toObject()
	{
		return dyvil.tools.asm.Type.getType(this.type.getExtendedName());
	}
	
	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			ClassGenericType generic = new ClassGenericType(LazyFields.CLASS_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IAnnotation annotation = type.getAnnotation(LazyFields.CLASS_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		
		return Types.isSuperType(type, this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType()) || type.getAnnotation(LazyFields.CLASS_CONVERTIBLE) != null;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		final int i = super.getTypeMatch(type);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(LazyFields.CLASS_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = dyvil.tools.compiler.ast.type.builtin.Types.UNKNOWN;
			markers.add(Markers.semantic(this.position, "classoperator.invalid"));
			return;
		}
		
		this.type = this.type.resolveType(markers, context);
		ClassGenericType generic = new ClassGenericType(LazyFields.CLASS_CLASS);
		generic.addType(this.type);
		this.genericType = generic;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		return this;
	}

	@Override
	public int stringSize()
	{
		if (this.type.isPrimitive())
		{
			return this.type.getName().qualified.length();
		}

		IClass iClass = this.type.getTheClass();
		if (iClass == null)
		{
			return 20;
		}

		if (iClass.isInterface())
		{
			return "interface ".length() + iClass.getInternalName().length();
		}

		return "class ".length() + iClass.getInternalName().length();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		if (this.type.isPrimitive())
		{
			builder.append(this.type.getName().qualified);
			return true;
		}

		IClass iClass = this.type.getTheClass();
		if (iClass == null)
		{
			return false;
		}

		if (iClass.isInterface())
		{
			builder.append("interface ");
		}
		else
		{
			builder.append("class ");
		}

		builder.append(iClass.getFullName());
		return true;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.type.writeClassExpression(writer, false);

		if (type != null)
		{
			this.genericType.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public String toString()
	{
		return "class(" + this.type + ")";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("class(");
		this.type.toString(prefix, buffer);
		buffer.append(')');
	}
}
