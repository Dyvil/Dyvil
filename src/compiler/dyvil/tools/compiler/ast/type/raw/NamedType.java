package dyvil.tools.compiler.ast.type.raw;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NamedType implements IRawType, ITypeConsumer
{
	protected IType         parent;
	protected ICodePosition position;
	protected Name          name;

	public NamedType()
	{
	}

	public NamedType(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	public NamedType(ICodePosition position, Name name, IType parent)
	{
		this.position = position;
		this.name = name;
		this.parent = parent;
	}

	@Override
	public int typeTag()
	{
		return NAMED;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	public IType getParent()
	{
		return this.parent;
	}

	public void setParent(IType parent)
	{
		this.parent = parent;
	}

	@Override
	public void setType(IType type)
	{
		this.parent = type;
	}

	@Override
	public IClass getTheClass()
	{
		return null;
	}

	@Override
	public boolean isSuperClassOf(IType type)
	{
		return false;
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return false;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		IType resolved = resolveType0(markers, context);
		// If the type is not a Type Variable Reference
		if (resolved.getTypeVariable() == null && resolved != Types.UNKNOWN)
		{
			// Replace Type Variable References with their default value
			return resolved.getConcreteType(DEFAULT);
		}
		// Otherwise, simply return it.
		return resolved;
	}

	public IType resolveType0(MarkerList markers, IContext context)
	{
		if (this.parent != null)
		{
			this.parent = this.parent.resolveType(markers, context);

			if (!this.parent.isResolved())
			{
				return this;
			}

			final IType type = this.parent.resolveType(this.name);
			if (type != null)
			{
				return type;
			}

			final Package thePackage = this.parent.resolvePackage(this.name);
			if (thePackage != null)
			{
				return new PackageType(thePackage);
			}

			markers.add(Markers.semanticError(this.position, "resolve.type.package", this.name, this.parent));
			return this;
		}

		final IType primitive = Types.resolvePrimitive(this.name);
		if (primitive != null)
		{
			if (primitive == Types.UNKNOWN)
			{
				markers.add(Markers.semanticWarning(this.position, "type.auto.deprecated"));
			}

			return primitive.atPosition(this.position);
		}

		final IType type = IContext.resolveType(context, this.name);
		if (type != null)
		{
			return type.atPosition(this.position);
		}

		final Package thePackage = Package.rootPackage.resolvePackage(this.name);
		if (thePackage != null)
		{
			return new PackageType(thePackage);
		}

		markers.add(Markers.semanticError(this.position, "resolve.type", this.name));
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
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
		return this.name.qualified;
	}

	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append(this.name.qualified);
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name.qualified);
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.getQualified(in.readUTF());
	}

	@Override
	public String toString()
	{
		if (this.parent != null)
		{
			return this.parent.toString() + '.' + this.name;
		}
		return this.name.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.parent != null)
		{
			this.parent.toString(prefix, buffer);
			buffer.append('.');
		}
		buffer.append(this.name);
	}

	@Override
	public IType clone()
	{
		return new NamedType(this.position, this.name, this.parent);
	}
}
