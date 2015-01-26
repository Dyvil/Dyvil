package dyvil.tools.compiler.ast.imports;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
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
	public void resolveTypes(List<Marker> markers, IContext context, boolean isStatic)
	{
		for (IImport i : this.children)
		{
			i.resolveTypes(markers, context, isStatic);
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
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
	public IClass resolveClass(String name)
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
	public FieldMatch resolveField(String name)
	{
		for (IImport i : this.children)
		{
			FieldMatch match = i.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		for (IImport i : this.children)
		{
			MethodMatch match = i.resolveMethod(instance, name, arguments);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		for (IImport i : this.children)
		{
			i.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Import.multiImportStart);
		Util.astToString(this.children, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd);
	}
}
