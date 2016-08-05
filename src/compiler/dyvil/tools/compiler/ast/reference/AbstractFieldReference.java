package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.IClassCompilable;

public abstract class AbstractFieldReference implements IReference, IClassCompilable
{
	static final int CACHE_FIELD_MODIFIERS = Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;

	protected IField field;
	protected boolean isUnique = true;

	private String fieldOriginClassName;
	private String refFieldName;

	protected String className;

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		final Class<?> thisClass = this.getClass();
		for (int i = 0, count = compilableList.compilableCount(); i < count; i++)
		{
			final IClassCompilable compilable = compilableList.getCompilable(i);
			if (compilable.getClass() != thisClass)
			{
				continue;
			}

			final AbstractFieldReference fieldReference = (AbstractFieldReference) compilable;
			if (this.field == fieldReference.field)
			{
				this.isUnique = false;
				break;
			}
		}
		compilableList.addCompilable(this);
	}

	@Override
	public void setInnerIndex(String internalName, int index)
	{
		this.className = internalName;
	}

	protected String getFieldOriginClassName()
	{
		if (this.fieldOriginClassName != null)
		{
			return this.fieldOriginClassName;
		}

		return this.fieldOriginClassName = this.field.getEnclosingClass().getInternalName();
	}

	protected String getRefFieldName()
	{
		if (this.refFieldName != null)
		{
			return this.refFieldName;
		}

		// Format: $staticRef$[originClassName]$[fieldName]
		final String originClassName = this.getFieldOriginClassName().replace('/', '$');
		return this.refFieldName = "$staticRef$" + originClassName + '$' + this.field.getInternalName();
	}
}
