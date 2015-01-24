package dyvil.tools.compiler.ast.imports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SimpleImport extends ASTNode implements IImport, IImportContainer
{
	public IImport				parent;
	public String				name;
	public String				alias;
	public IImport				child;
	
	private IClass				theClass;
	private Package				thePackage;
	
	private FieldMatch			field;
	private List<MethodMatch>	methods;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, IImport parent, String name)
	{
		this.position = position;
		this.parent = parent;
		this.name = name;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.child = iimport;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context, boolean isStatic)
	{
		if (isStatic && this.child == null)
		{
			FieldMatch field = context.resolveField(this.name);
			if (field != null)
			{
				this.field = field;
				return;
			}
			
			this.methods = new ArrayList();
			context.getMethodMatches(this.methods, null, this.name, null);
			if (!this.methods.isEmpty())
			{
				Collections.sort(this.methods);
				return;
			}
			
			markers.add(Markers.create(this.position, "resolve.method_field", this.name));
			return;
		}
		
		Package pack = context.resolvePackage(this.name);
		if (pack != null)
		{
			this.thePackage = pack;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, pack, isStatic);
			}
			return;
		}
		
		IClass iclass = context.resolveClass(this.name);
		if (iclass != null)
		{
			this.theClass = iclass;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, iclass, isStatic);
			}
			return;
		}
		
		markers.add(Markers.create(this.position, "resolve.package", this.name));
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		if (this.name.equals(name))
		{
			return this.thePackage;
		}
		if (this.alias != null && this.alias.equals(name))
		{
			return this.thePackage;
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.child != null)
		{
			return this.child.resolveClass(name);
		}
		if (this.name.equals(name))
		{
			return this.theClass;
		}
		if (this.alias != null && this.alias.equals(name))
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.child != null)
		{
			return this.child.resolveField(name);
		}
		if (this.name.equals(name))
		{
			return this.field;
		}
		if (this.alias != null && this.alias.equals(name))
		{
			return this.field;
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
	{
		if (this.child != null)
		{
			return this.child.resolveMethod(instance, name, arguments);
		}
		if (!this.methods.isEmpty())
		{
			if (this.name.equals(name))
			{
				return this.methods.get(0);
			}
			if (this.alias != null && this.alias.equals(name))
			{
				return this.methods.get(0);
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		if (this.child != null)
		{
			this.child.getMethodMatches(list, instance, name, arguments);
		}
		if (this.name.equals(name))
		{
			list.addAll(this.methods);
		}
		if (this.alias != null && this.alias.equals(name))
		{
			list.addAll(this.methods);
		}
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (this.alias != null)
		{
			buffer.append(Formatting.Import.aliasSeperator);
			buffer.append(this.alias);
		}
		if (this.child != null)
		{
			buffer.append('.');
			this.child.toString(prefix, buffer);
		}
	}
}
