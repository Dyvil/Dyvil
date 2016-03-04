package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.transform.CaptureHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class AnonymousClass extends CodeClass
{
	protected CaptureHelper captureHelper = new CaptureHelper(CaptureField.factory(this));
	
	protected FieldThis thisField;
	
	public AnonymousClass(ICodePosition position)
	{
		this.interfaces = new IType[1];
		this.body = new ClassBody(this);
		this.position = position;
		this.modifiers = EmptyModifiers.INSTANCE;
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		String outerName = this.enclosingClass.getName().qualified;
		String indexString = Integer.toString(index);
		
		this.name = Name.getQualified(outerName + '$' + indexString);
		this.fullName = this.enclosingClass.getFullName() + '$' + indexString;
		this.internalName = this.enclosingClass.getInternalName() + '$' + indexString;
	}
	
	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}
		
		return this.captureHelper.capture(variable);
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		if (type == this)
		{
			return VariableThis.DEFAULT;
		}
		
		IAccessible outer = this.enclosingClass.getAccessibleThis(type);
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
