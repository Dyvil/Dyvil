package dyvil.tools.compiler.ast.type.raw;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.typevar.ResolvedTypeVarType;
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
	public boolean isSuperClassOf(IType subType)
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
		if (this.parent == null)
		{
			return this.resolveTopLevel(markers, context);
		}

		this.parent = this.parent.resolveType(markers, context);
		if (!this.parent.isResolved())
		{
			return this;
		}

		return this.resolveWithParent(markers);
	}

	private IType resolveWithParent(MarkerList markers)
	{
		final IClass theClass = this.parent.resolveClass(this.name);
		if (theClass != null)
		{
			return new ResolvedClassType(theClass, this.position);
		}

		final Package thePackage = this.parent.resolvePackage(this.name);
		if (thePackage != null)
		{
			return new PackageType(thePackage).atPosition(this.position);
		}

		if (markers == null)
		{
			// FieldAccess support
			return null;
		}
		markers.add(Markers.semanticError(this.position, "resolve.type.package", this.name, this.parent));
		return this;
	}

	private IType resolveTopLevel(MarkerList markers, IContext context)
	{
		final IType primitive = Types.resolvePrimitive(this.name);
		if (primitive != null)
		{
			return primitive.atPosition(this.position);
		}

		IType type = this.resolveTopLevelWith(markers, context);
		if (type != null)
		{
			return type;
		}

		type = this.resolveTopLevelWith(markers, Types.BASE_CONTEXT);
		if (type != null)
		{
			return type;
		}

		final Package thePackage = Package.rootPackage.resolvePackage(this.name);
		if (thePackage != null)
		{
			return new PackageType(thePackage).atPosition(this.position);
		}

		if (markers == null)
		{
			// FieldAccess support
			return null;
		}
		markers.add(Markers.semanticError(this.position, "resolve.type", this.name));
		return this;
	}

	private IType resolveTopLevelWith(@SuppressWarnings("UnusedParameters") MarkerList markers, IContext context)
	{
		final IClass theClass = context.resolveClass(this.name);
		if (theClass != null)
		{
			return new ResolvedClassType(theClass, this.position);
		}

		final ITypeParameter typeParameter = context.resolveTypeParameter(this.name);
		if (typeParameter != null)
		{
			return new ResolvedTypeVarType(typeParameter, this.position);
		}

		final ITypeAlias type = context.resolveTypeAlias(this.name, 0);
		if (type != null)
		{
			return type.getType().getConcreteType(ITypeContext.DEFAULT).atPosition(this.position);
		}
		return null;
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
	public void appendSignature(StringBuilder buffer, boolean genericArg)
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
		this.name = Name.fromRaw(in.readUTF());
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
