package dyvil.tools.compiler.ast.imports;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class MultiImport extends ASTNode implements IImport, IImportContainer
{
	public IImport			parent;
	public List<IImport>	children	= new ArrayList();
	
	public MultiImport(ICodePosition position, IImport parent)
	{
		this.position = position;
		this.parent = parent;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.children.add(iimport);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic)
	{
		for (IImport i : this.children)
		{
			i.resolveTypes(markers, context, isStatic);
		}
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		for (IImport i : this.children)
		{
			Package pack = i.resolvePackage(name);
			if (pack != null)
			{
				return pack;
			}
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		for (IImport i : this.children)
		{
			IClass iclass = i.resolveClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (IImport i : this.children)
		{
			IField match = i.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		for (IImport i : this.children)
		{
			i.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Import.multiImportStart);
		Util.astToString(prefix, this.children, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd);
	}
}
