package dyvil.tools.compiler.ast.header;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.external.ExternalHeader;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
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
	private Name[] nameParts = new Name[3];
	private int namePartCount;

	// Metadata
	private ICodePosition position;
	private IDyvilHeader  header;
	private IContext context = IDefaultContext.DEFAULT;

	public IncludeDeclaration()
	{
	}

	public IncludeDeclaration(ICodePosition position)
	{
		this.position = position;
	}

	public IncludeDeclaration(IDyvilHeader header)
	{
		this.header = header;
	}

	public IncludeDeclaration(ICodePosition position, IDyvilHeader header)
	{
		this.position = position;
		this.header = header;
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
		final int index = this.namePartCount++;
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

	public IContext getContext()
	{
		return this.context;
	}

	public void resolveTypes(MarkerList markers, IDyvilHeader enclosingHeader)
	{
		Package pack = Package.rootPackage;
		final int lastIndex = this.namePartCount - 1;

		for (int i = 0; i < lastIndex; i++)
		{
			final Name namePart = this.nameParts[i];

			pack = pack.resolvePackage(namePart);
			if (pack == null)
			{
				markers.add(Markers.semanticError(this.position, "resolve.package", namePart));
				return;
			}
		}

		final Name headerName = this.nameParts[lastIndex];
		this.header = pack.resolveHeader(headerName.qualified);

		if (markers == null)
		{
			return;
		}

		if (this.header == null)
		{
			markers.add(Markers.semanticError(this.position, "resolve.header", headerName));
			return;
		}

		// Check if the included Unit is a Header or has a Header Declaration
		if (!this.getHeader().isHeader())
		{
			markers.add(Markers.semanticError(this.position, "include.unit", this.header.getName()));
			return;
		}

		// Check if the Header has a Header Declaration
		final HeaderDeclaration headerDeclaration = this.header.getHeaderDeclaration();
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
				return;
			}
			accessLevel &= 0b1111;
		}

		switch (accessLevel)
		{
		case Modifiers.PRIVATE:
			markers.add(Markers.semanticError(this.position, "include.invisible", this.header.getName()));
			return;
		case Modifiers.PACKAGE:
		case Modifiers.PROTECTED:
			if (this.header.getPackage() != enclosingHeader.getPackage())
			{
				markers.add(Markers.semanticError(this.position, "include.invisible", this.header.getName()));
			}
		}

		// All checks passed
		this.context = this.header.getContext();
	}

	public void write(DataOutput out) throws IOException
	{
		out.writeShort(this.namePartCount);
		for (int i = 0; i < this.namePartCount; i++)
		{
			this.nameParts[i].write(out);
		}
	}

	public void read(DataInput in) throws IOException
	{
		this.namePartCount = in.readShort();
		this.nameParts = new Name[this.namePartCount];
		for (int i = 0; i < this.namePartCount; i++)
		{
			this.nameParts[i] = Name.read(in);
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
