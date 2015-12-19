package dyvil.tools.compiler.ast.reference;

import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class StaticFieldReference implements IReference, IClassCompilable
{
	private static Map<String, Set<IField>> map = new IdentityHashMap<>();
	
	protected IField field;

	// Metadata
	private boolean isUnique;
	private String  className;
	private String  fieldName;
	private String  fieldOriginClassName;
	private String  refFieldName;
	private String  refFieldType;

	public StaticFieldReference(IField field)
	{
		this.field = field;
	}
	
	private static boolean addToMap(String className, IField field)
	{
		Set<IField> set = map.get(className);
		if (set == null)
		{
			set = new IdentityHashSet<>();
			map.put(className, set);
		}
		
		return set.add(field);
	}
	
	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
		map.clear();

		InstanceFieldReference.checkFinalAccess(this.field, position, markers);
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		compilableList.addCompilable(this);
		if (addToMap(this.className, this.field))
		{
			this.isUnique = true;
		}
	}

	// Lazy Field Getters

	public String getFieldName()
	{
		if (this.fieldName != null)
		{
			return this.fieldName;
		}

		return this.fieldName = this.field.getName().qualified;
	}

	private String getFieldOriginClassName()
	{
		if (this.fieldOriginClassName != null)
		{
			return this.fieldOriginClassName;
		}

		return this.fieldOriginClassName = this.field.getTheClass().getInternalName();
	}
	
	private String getRefFieldName()
	{
		if (this.refFieldName != null)
		{
			return this.refFieldName;
		}

		// Format: $staticRef$[originClassName]$[fieldName]
		return this.refFieldName =
				"$staticRef$" + this.getFieldOriginClassName().replace('/', '$') + '$' + this.getFieldName();
	}
	
	private String getRefFieldType()
	{
		if (this.refFieldType != null)
		{
			return this.refFieldType;
		}
		
		return this.refFieldType = 'L' + Types.getInternalRef(this.field.getType(), "") + ';';
	}

	// IClassCompilable callback implementations

	@Override
	public void setInnerIndex(String internalName, int index)
	{
		this.className = internalName;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}
		
		String refFieldName = this.getRefFieldName();
		String refFieldType = this.getRefFieldType();
		
		final int modifiers = Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		writer.visitField(modifiers, refFieldName, refFieldType, null, null);
	}
	
	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}

		final String fieldName = this.getFieldName();
		final String fieldOriginClassName = this.getFieldOriginClassName();
		
		final String refFieldName = this.getRefFieldName();
		final String refFieldType = this.getRefFieldType();

		final String factoryMethodName = Types.getReferenceFactoryName(this.field.getType(), "Static");
		final String factoryMethodType = "(Ljava/lang/Class;Ljava/lang/String;)" + refFieldType;

		// Load the field class
		writer.writeLDC(Type.getObjectType(fieldOriginClassName));
		// Load the field name
		writer.writeLDC(fieldName);

		// Invoke the factory method
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/runtime/ReferenceFactory", factoryMethodName,
		                       factoryMethodType, false);
		
		// Assign the reference field
		writer.writeFieldInsn(Opcodes.PUTSTATIC, this.className, refFieldName, refFieldType);
	}

	// Reference getter implementation
	
	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		if (this.field.hasModifier(Modifiers.STATIC))
		{
			writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, this.getRefFieldName(), this.getRefFieldType());
		}
	}
}
