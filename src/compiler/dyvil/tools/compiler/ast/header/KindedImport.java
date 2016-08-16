package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class KindedImport implements IImport
{
	private IImport child;
	private int     mask;

	public static final int PACKAGE  = 0x1;
	public static final int HEADER   = 0x2;
	public static final int OPERATOR = 0x4;
	public static final int TYPE     = 0x8;
	public static final int CLASS    = 0x10;
	public static final int FUNC     = 0x20;
	public static final int VAR      = 0x40;

	public static final int STATIC = VAR | FUNC;
	public static final int PARENT = PACKAGE | HEADER | CLASS;
	public static final int ANY    = -1;

	public KindedImport()
	{
	}

	public KindedImport(IImport child, int mask)
	{
		this.child = child;
		this.mask = mask;
	}

	public static int parseMask(int type)
	{
		switch (type)
		{
		case DyvilKeywords.PACKAGE:
			return KindedImport.PACKAGE;
		case DyvilKeywords.HEADER:
			return KindedImport.HEADER;
		case DyvilKeywords.OPERATOR:
			return KindedImport.OPERATOR;
		case DyvilKeywords.TYPE:
			return KindedImport.TYPE;
		case DyvilKeywords.CLASS:
			return KindedImport.CLASS;
		case DyvilKeywords.STATIC:
			return KindedImport.STATIC;
		case DyvilKeywords.VAR:
		case DyvilKeywords.CONST:
		case DyvilKeywords.LET:
			return KindedImport.VAR;
		case DyvilKeywords.FUNC:
			return KindedImport.FUNC;
		}
		return 0;
	}

	@Override
	public int importTag()
	{
		return KINDED;
	}

	@Override
	public IImport getParent()
	{
		return null; // always parent-less
	}

	@Override
	public void setParent(IImport parent)
	{
	}

	@Override
	public IImportContext asContext()
	{
		return this.child.asContext();
	}

	@Override
	public IImportContext asParentContext()
	{
		return null; // never viewed by children as a parent
	}

	private int orMask(int mask)
	{
		return mask == ANY ? this.mask : this.mask | mask;
	}

	@Override
	public void resolveTypes(MarkerList markers, IImportContext context, int mask)
	{
		this.child.resolveTypes(markers, context, this.orMask(mask));
	}

	@Override
	public void resolve(MarkerList markers, IImportContext context, int mask)
	{
		this.child.resolve(markers, context, this.orMask(mask));
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		// currently, only 7 bits are used, so a byte is sufficient
		out.writeByte(this.mask);
		IImport.writeImport(this.child, out);
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.mask = in.readUnsignedByte();
		this.child = IImport.readImport(in);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		switch (this.mask & STATIC)
		{
		case STATIC:
			buffer.append("static ");
			break;
		case VAR:
			buffer.append("var ");
			break;
		case FUNC:
			buffer.append("func ");
			break;
		}

		if ((this.mask & CLASS) != 0)
		{
			buffer.append("class ");
		}
		if ((this.mask & TYPE) != 0)
		{
			buffer.append("type ");
		}
		if ((this.mask & OPERATOR) != 0)
		{
			buffer.append("operator ");
		}
		if ((this.mask & HEADER) != 0)
		{
			buffer.append("header ");
		}
		if ((this.mask & PACKAGE) != 0)
		{
			buffer.append("package ");
		}

		this.child.toString(prefix, buffer);
	}
}
