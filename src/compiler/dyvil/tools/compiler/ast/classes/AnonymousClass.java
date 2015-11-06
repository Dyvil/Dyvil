package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class AnonymousClass extends CodeClass
{
	protected CaptureField[]	capturedFields;
	protected int				capturedFieldCount;
	
	protected FieldThis thisField;
	
	public transient IContext context;
	
	public AnonymousClass(ICodePosition position)
	{
		this.interfaces = new IType[1];
		this.body = new ClassBody(this);
		this.position = position;
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		String outerName = this.outerClass.getName().qualified;
		String indexString = Integer.toString(index);
		
		this.name = Name.getQualified(outerName + '$' + indexString);
		this.fullName = this.outerClass.getFullName() + '$' + indexString;
		this.internalName = this.outerClass.getInternalName() + '$' + indexString;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = super.resolveField(name);
		if (field != null)
		{
			return field;
		}
		return this.context.resolveField(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		IClass iclass = super.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		return this.context.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		IType type = super.resolveType(name);
		if (type != null)
		{
			return type;
		}
		return this.context.resolveType(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		super.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}
		
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureField[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureField(this, variable);
		}
		
		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			if (this.capturedFields[i].field == variable)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return this.capturedFields[i];
			}
		}
		
		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureField[] temp = new CaptureField[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureField(this, variable);
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		if (type == this)
		{
			return VariableThis.DEFAULT;
		}
		
		IAccessible outer = this.outerClass.getAccessibleThis(type);
		if (outer == null)
		{
			return null;
		}
		
		if (this.thisField == null)
		{
			return this.thisField = new FieldThis(this, outer, type);
		}
		return this.thisField;
	}
}
