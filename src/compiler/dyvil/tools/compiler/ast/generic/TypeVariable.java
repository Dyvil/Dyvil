package dyvil.tools.compiler.ast.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class TypeVariable extends ASTNode implements ITypeVariable
{
	public static int		captureID;
	
	public String			name;
	
	protected IType			upperBound;
	protected List<IType>	upperBounds;
	protected IType			lowerBound;
	
	protected IClass		captureClass;
	
	public TypeVariable()
	{
	}
	
	public TypeVariable(String name)
	{
		this.name = name;
	}
	
	public TypeVariable(ICodePosition position)
	{
		this.position = position;
	}
	
	public TypeVariable(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.name.equals(name);
	}
	
	@Override
	public IClass getCaptureClass()
	{
		return this.captureClass;
	}
	
	public void setUpperBound(IType bound)
	{
		this.upperBound = bound;
	}
	
	@Override
	public void setUpperBounds(List<IType> bounds)
	{
		this.upperBounds = bounds;
	}
	
	@Override
	public List<IType> getUpperBounds()
	{
		return this.upperBounds;
	}
	
	@Override
	public void addUpperBound(IType bound)
	{
		if (this.upperBounds == null)
		{
			this.upperBounds = new ArrayList(1);
		}
		this.upperBounds.add(bound);
	}
	
	@Override
	public void setLowerBound(IType bound)
	{
		this.lowerBound = bound;
	}
	
	@Override
	public IType getLowerBound()
	{
		return this.lowerBound;
	}
	
	// Misc
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.upperBounds != null)
		{
			for (ListIterator<IType> iterator = this.upperBounds.listIterator(); iterator.hasNext();)
			{
				IType t1 = iterator.next();
				IType t2 = t1.resolve(context);
				
				if (!t2.isResolved())
				{
					markers.add(Markers.create(t2.getPosition(), "resolve.type", t2.toString()));
					continue;
				}
				
				IClass iclass = t2.getTheClass();
				if (iclass != null && !iclass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					if (this.upperBound != null)
					{
						markers.add(Markers.create(t2.getPosition(), "generic.upperbound", this.name));
					}
					
					iterator.remove();
					this.upperBound = t2;
					continue;
				}
				
				if (t1 != t2)
				{
					iterator.set(t2);
				}
			}
			
			if (this.upperBounds.isEmpty())
			{
				this.upperBounds = null;
			}
			
			this.captureClass = new CaptureClass(this, this.upperBound, this.upperBounds);
		}
		else if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolve(context);
			if (!this.lowerBound.isResolved())
			{
				markers.add(Markers.create(this.lowerBound.getPosition(), "resolve.type", this.lowerBound.toString()));
			}
		}
		this.captureClass = Type.OBJECT.theClass;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name == null)
		{
			buffer.append('_');
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.lowerBound != null)
		{
			buffer.append(Formatting.Type.genericLowerBound);
			this.lowerBound.toString(prefix, buffer);
		}
		else if (this.upperBound != null || this.upperBounds != null)
		{
			buffer.append(Formatting.Type.genericUpperBound);
			if (this.upperBound != null)
			{
				this.upperBound.toString(prefix, buffer);
				if (this.upperBounds != null)
				{
					buffer.append(Formatting.Type.genericBoundSeperator);
				}
			}
			if (this.upperBounds != null)
			{
				Util.astToString(this.upperBounds, Formatting.Type.genericBoundSeperator, buffer);
			}
		}
	}
}
