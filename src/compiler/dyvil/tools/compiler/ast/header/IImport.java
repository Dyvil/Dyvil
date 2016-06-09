package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IImport extends IASTNode, IImportContext
{
	int SINGLE   = 1;
	int WILDCARD = 2;
	int MULTI    = 3;
	
	static IImport fromTag(int tag)
	{
		switch (tag)
		{
		case SINGLE:
			return new SingleImport(null);
		case WILDCARD:
			return new WildcardImport(null);
		case MULTI:
			return new MultiImport(null);
		}
		return null;
	}
	
	@Override
	default ICodePosition getPosition()
	{
		return null;
	}
	
	int importTag();
	
	void resolveTypes(MarkerList markers, IContext context, boolean using);
	
	void setParent(IImport parent);
	
	IImport getParent();
	
	default void setAlias(Name alias)
	{
	}
	
	default Name getAlias()
	{
		return null;
	}
	
	IContext getContext();
	
	// Compilation
	
	static void writeImport(IImport iimport, DataOutput dos) throws IOException
	{
		if (iimport == null)
		{
			dos.writeByte(0);
			return;
		}
		
		dos.writeByte(iimport.importTag());
		iimport.writeData(dos);
	}
	
	static IImport readImport(DataInput dis) throws IOException
	{
		final byte type = dis.readByte();
		if (type == 0)
		{
			return null;
		}

		final IImport iimport = fromTag(type);
		assert iimport != null;

		iimport.readData(dis);
		return iimport;
	}
	
	void writeData(DataOutput out) throws IOException;
	
	void readData(DataInput in) throws IOException;
}
