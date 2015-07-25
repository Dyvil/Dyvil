package dyvil.tools.compiler.ast.imports;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IImport extends IASTNode
{
	public static final int	SIMPLE	= 1;
	public static final int	PACKAGE	= 2;
	public static final int	MULTI	= 3;
	
	public static IImport fromTag(int tag)
	{
		switch (tag)
		{
		case SIMPLE:
			return new SimpleImport(null);
		case PACKAGE:
			return new PackageImport(null);
		case MULTI:
			return new MultiImport(null);
		}
		return null;
	}
	
	@Override
	public default ICodePosition getPosition()
	{
		return null;
	}
	
	public int importTag();
	
	public void resolveTypes(MarkerList markers, IContext context, boolean using);
	
	public void setParent(IImport parent);
	
	public IImport getParent();
	
	public default void setAlias(Name alias)
	{
	}
	
	public default Name getAlias()
	{
		return null;
	}
	
	public IContext getContext();
	
	public Package resolvePackage(Name name);
	
	public IClass resolveClass(Name name);
	
	public IDataMember resolveField(Name name);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	// Compilation
	
	public static void writeImport(IImport iimport, DataOutput dos) throws IOException
	{
		if (iimport == null)
		{
			dos.writeByte(0);
			return;
		}
		
		dos.writeByte(iimport.importTag());
		iimport.write(dos);
	}
	
	public static IImport readImport(DataInput dis) throws IOException
	{
		byte type = dis.readByte();
		if (type == 0)
		{
			return null;
		}
		IImport iimport = fromTag(type);
		iimport.read(dis);
		return iimport;
	}
	
	public void write(DataOutput out) throws IOException;
	
	public void read(DataInput in) throws IOException;
}
