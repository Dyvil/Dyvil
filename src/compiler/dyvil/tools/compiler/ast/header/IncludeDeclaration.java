package dyvil.tools.compiler.ast.header;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalHeader;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IncludeDeclaration implements IASTNode
{
	private ICodePosition position;

	private Name[] nameParts = new Name[3];
	private int namePartCount;

	private IDyvilHeader header;

	public IncludeDeclaration(ICodePosition position)
	{
		this.position = position;
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

	public void addNamePart(Name name)
	{
		int index = this.namePartCount++;
		if (index >= this.nameParts.length)
		{
			Name[] temp = new Name[index + 1];
			System.arraycopy(this.nameParts, 0, temp, 0, this.nameParts.length);
			this.nameParts = temp;
		}
		this.nameParts[index] = name;
	}

	public IDyvilHeader getHeader()
	{
		return this.header;
	}

	public IClass resolveClass(Name name)
	{
		return this.header == null ? null : this.header.resolveClass(name);
	}

	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		return this.header == null ? null : this.header.resolveTypeAlias(name, arity);
	}

	public IDataMember resolveField(Name name)
	{
		return this.header == null ? null : this.header.resolveField(name);
	}

	public void getMethodMatches(MatchList<IMethod> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.header != null)
		{
			this.header.getMethodMatches(list, instance, name, arguments);
		}
	}

	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.header != null)
		{
			this.header.getImplicitMatches(list, value, targetType);
		}
	}

	public void resolve(MarkerList markers, IContext context)
	{
		Package pack = Package.rootPackage;
		int count = this.namePartCount - 1;
		for (int i = 0; i < count; i++)
		{
			pack = pack.resolvePackage(this.nameParts[i]);
			if (pack == null)
			{
				markers.add(Markers.semantic(this.position, "resolve.package", this.nameParts[i]));
				return;
			}
		}

		this.header = pack.resolveHeader(this.nameParts[count].qualified);

		if (markers == null)
		{
			return;
		}

		if (this.header == null)
		{
			markers.add(Markers.semantic(this.position, "resolve.header", this.nameParts[count]));
			return;
		}

		// Check if the included Unit is a Header or has a Header Declaration
		if (!this.getHeader().isHeader())
		{
			markers.add(Markers.semantic(this.position, "include.unit", this.header.getName()));
			return;
		}

		// Check if the Header has a Header Declaration
		HeaderDeclaration headerDeclaration = this.header.getHeaderDeclaration();
		if (headerDeclaration == null)
		{
			return;
		}

		// Header Access Check
		int accessLevel = headerDeclaration.getModifiers().toFlags() & Modifiers.ACCESS_MODIFIERS;
		if ((accessLevel & Modifiers.INTERNAL) != 0)
		{
			if (this.header instanceof ExternalHeader)
			{
				markers.add(Markers.semanticError(this.position, "include.internal", this.header.getName()));
			}
			accessLevel &= 0b1111;
		}

		if (accessLevel == Modifiers.PRIVATE)
		{
			markers.add(Markers.semanticError(this.position, "include.invisible", this.header.getName()));
		}
		if (accessLevel == Modifiers.PACKAGE || accessLevel == Modifiers.PROTECTED)
		{
			if (this.header.getPackage() != context.getHeader().getPackage())
			{
				markers.add(Markers.semanticError(this.position, "include.invisible", this.header.getName()));
			}
		}
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeShort(this.namePartCount);
		for (int i = 0; i < this.namePartCount; i++)
		{
			out.writeUTF(this.nameParts[i].qualified);
		}
	}

	public void read(DataInput in) throws IOException
	{
		this.namePartCount = in.readShort();
		this.nameParts = new Name[this.namePartCount];
		for (int i = 0; i < this.namePartCount; i++)
		{
			this.nameParts[i] = Name.fromRaw(in.readUTF());
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("include ");
		buffer.append(this.nameParts[0]);
		for (int i = 1; i < this.namePartCount; i++)
		{
			buffer.append('.').append(this.nameParts[i]);
		}
	}
}
