package dyvil.tools.compiler.ast.reference;

import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.collection.mutable.MapBasedSet;
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
	
	protected IField						field;
	
	private boolean							isUnique;
	private String							className;
	private String							idFieldName;
	private String							refFieldType;
	
	public StaticFieldReference(IField field)
	{
		this.field = field;
	}
	
	private static boolean addToMap(String className, IField field)
	{
		Set<IField> set = map.get(className);
		if (set == null)
		{
			set = new MapBasedSet<IField>(new IdentityHashMap<>());
			map.put(className, set);
		}
		
		return set.add(field);
	}
	
	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
		map.clear();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (addToMap(this.className, this.field))
		{
			compilableList.addCompilable(this);
			this.isUnique = true;
		}
	}
	
	private String getRefFieldName()
	{
		if (this.idFieldName != null)
		{
			return this.idFieldName;
		}
		
		return this.idFieldName = "$fieldRef$" + this.className.replace('/', '$') + "$" + this.field.getName().qualified;
	}
	
	private String getRefFieldType()
	{
		if (this.refFieldType != null)
		{
			return this.refFieldType;
		}
		
		return this.refFieldType = 'L' + Types.getInternalRef(this.field.getType(), "") + ';';
	}
	
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
		
		String fieldName = this.field.getName().qualified;
		String fieldClassType = 'L' + this.field.getTheClass().getInternalName() + ';';
		
		String refFieldName = this.getRefFieldName();
		String refFieldType = this.getRefFieldType();

		String factoryMethodName = Types.getAccessFactoryName(this.field.getType(), true);
		String factoryMethodType = "(Ljava/lang/Class;Ljava/lang/String;)" + refFieldType;

		// Load the field class
		writer.writeLDC(Type.getType(fieldClassType));
		// Load the field name
		writer.writeLDC(fieldName);

		// Invoke the factory method
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/runtime/FieldAccessFactory", factoryMethodName, factoryMethodType, false);
		
		// Assign the reference field
		writer.writeFieldInsn(Opcodes.PUTSTATIC, this.className, refFieldName, refFieldType);
	}
	
	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		if (this.field.hasModifier(Modifiers.STATIC))
		{
			writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, this.getRefFieldName(), this.getRefFieldType());
		}
	}
}
