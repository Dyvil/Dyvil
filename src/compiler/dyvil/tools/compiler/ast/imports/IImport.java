package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.lang.Name;
import dyvil.tools.parsing.ASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IImport extends ASTNode
{
	int SINGLE   = 1;
	int WILDCARD = 2;
	int MULTI    = 3;
	int KINDED = 4;

	static IImport fromTag(int tag)
	{
		switch (tag)
		{
		case SINGLE:
			return new SingleImport();
		case WILDCARD:
			return new WildcardImport();
		case MULTI:
			return new MultiImport();
		case KINDED:
			return new KindedImport();
		}
		return null;
	}
	
	@Override
	default SourcePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(SourcePosition position)
	{
	}

	int importTag();

	default Name getAlias()
	{
		return null;
	}

	default void setAlias(Name alias)
	{
	}

	IImport getParent();

	void setParent(IImport parent);

	IImportContext asContext();

	IImportContext asParentContext();

	void resolveTypes(MarkerList markers, IContext context, IImportContext parentContext, int mask);

	void resolve(MarkerList markers, IContext context, IImportContext parentContext, int mask);

	// Compilation
	
	static void writeImport(IImport theImport, DataOutput out) throws IOException
	{
		if (theImport == null)
		{
			out.writeByte(0);
			return;
		}
		
		out.writeByte(theImport.importTag());
		theImport.writeData(out);
	}
	
	static IImport readImport(DataInput in) throws IOException
	{
		final byte type = in.readByte();
		if (type == 0)
		{
			return null;
		}

		final IImport theImport = fromTag(type);
		assert theImport != null;

		theImport.readData(in);
		return theImport;
	}
	
	void writeData(DataOutput out) throws IOException;
	
	void readData(DataInput in) throws IOException;
}
