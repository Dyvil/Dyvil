package dyvil.tools.compiler.ast.imports;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;

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
	
	public int importTag();
	
	public void resolveTypes(MarkerList markers, IContext context, boolean using);
	
	public default void addImport(IImport iimport)
	{
	}
	
	public default IImport getChild()
	{
		return null;
	}
	
	public default void setAlias(Name alias)
	{
	}
	
	public Package resolvePackage(Name name);
	
	public IClass resolveClass(Name name);
	
	public IField resolveField(Name name);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	// Compilation
	
	public void write(DataOutputStream dos) throws IOException;
	
	public void read(DataInputStream dis) throws IOException;
}
