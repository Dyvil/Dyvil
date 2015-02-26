package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ForStatement extends ASTNode implements IStatement, IContext, ILoop
{
	public static final int	DEFAULT		= 0;
	public static final int	ITERATOR	= 1;
	public static final int	ARRAY		= 2;
	
	private IContext		context;
	private IStatement		parent;
	
	public Variable			variable;
	
	public IValue			condition;
	public IValue			update;
	
	public byte				type;
	
	public IValue			then;
	
	protected Label			startLabel;
	protected Label			updateLabel;
	protected Label			endLabel;
	
	protected Variable		var1;
	protected Variable		var2;
	protected Variable		var3;
	
	public ForStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label();
		this.updateLabel = new Label();
		this.endLabel = new Label();
	}
	
	@Override
	public int getValueType()
	{
		return FOR;
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable = new Variable(type.getPosition());
		this.variable.type = type;
	}
	
	@Override
	public Label getContinueLabel()
	{
		return this.updateLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public boolean canVisitStack(IStatement child)
	{
		return true;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.resolveTypes(markers, context);
		}
		if (this.type == 0)
		{
			if (this.condition != null)
			{
				this.condition.resolveTypes(markers, context);
			}
			if (this.update != null)
			{
				this.update.resolveTypes(markers, context);
			}
		}
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				((IStatement) this.then).setParent(this);
			}
			
			this.then.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.context = context;
		
		if (this.type != 0)
		{
			IType varType = this.variable.type;
			IValue value = this.variable.value;
			this.variable.value = value.resolve(markers, context);
			
			IType valueType = value.getType();
			int valueTypeDims = valueType.getArrayDimensions();
			if (valueTypeDims != 0)
			{
				this.type = ARRAY;
				if (!valueType.classEquals(varType) || varType.getArrayDimensions() != valueTypeDims - 1)
				{
					Marker marker = Markers.create(value.getPosition(), "for.array.type");
					marker.addInfo("Array Type: " + valueType);
					marker.addInfo("Variable Type: " + varType);
					markers.add(marker);
				}
				else
				{
					Variable var = new Variable(null);
					var.type = Type.INT;
					var.name = "$index";
					var.qualifiedName = "$index";
					this.var1 = var;
					
					var = new Variable(null);
					var.type = Type.INT;
					var.name = "$length";
					var.qualifiedName = "$length";
					this.var2 = var;
					
					var = new Variable(null);
					var.type = valueType;
					var.name = "$array";
					var.qualifiedName = "$array";
					this.var3 = var;
				}
			}
		}
		else
		{
			if (this.variable != null)
			{
				this.variable.resolve(markers, context);
			}
			if (this.condition != null)
			{
				this.condition = this.condition.resolve(markers, this);
			}
			if (this.update != null)
			{
				this.update = this.update.resolve(markers, this);
			}
		}
		
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, this);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.context = context;
		
		if (this.variable != null)
		{
			if (this.type == 0)
			{
				this.variable.check(markers, context);
			}
			else
			{
				this.variable.value.check(markers, context);
			}
		}
		
		if (this.condition != null)
		{
			this.condition.check(markers, this);
			
			if (!this.condition.isType(Type.BOOLEAN))
			{
				Marker marker = Markers.create(this.condition.getPosition(), "for.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
				markers.add(marker);
			}
		}
		if (this.update != null)
		{
			this.update.check(markers, this);
		}
		
		if (this.then != null)
		{
			this.then.check(markers, this);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IType getThisType()
	{
		return this.context.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.variable.isName(name))
		{
			return new FieldMatch(this.variable, 1);
		}
		else if (this.type == ARRAY)
		{
			if ("$index".equals(name))
			{
				return new FieldMatch(this.var1, 1);
			}
			else if ("$length".equals(name))
			{
				return new FieldMatch(this.var2, 1);
			}
			else if ("$array".equals(name))
			{
				return new FieldMatch(this.var3, 1);
			}
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public Label resolveLabel(String name)
	{
		if ("$forStart".equals(name))
		{
			return this.startLabel;
		}
		else if ("$forUpdate".equals(name))
		{
			return this.updateLabel;
		}
		else if ("$forEnd".equals(name))
		{
			return this.endLabel;
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.writeStatement(writer);
		writer.visitInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		Variable var = this.variable;
		if (this.type == DEFAULT)
		{
			// Variable
			if (var != null)
			{
				writer.addLocal(var.index, var.type);
				var.writeSet(writer, null, var.value);
			}
			
			writer.visitLabel(this.startLabel);
			// Condition
			if (this.condition != null)
			{
				this.condition.writeInvJump(writer, this.endLabel);
			}
			// Then
			if (this.then != null)
			{
				this.then.writeStatement(writer);
			}
			// Update
			writer.visitLabel(this.updateLabel, false);
			if (this.update != null)
			{
				this.update.writeStatement(writer);
			}
			// Go back to Condition
			writer.visitJumpInsn(Opcodes.GOTO, this.startLabel);
			
			writer.removeLocals(1);
			writer.visitLabel(this.endLabel, this.parent == null || this.parent.canVisitStack(this));
			
			// Variable
			if (var != null)
			{
				writer.visitLocalVariable(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), this.startLabel, this.endLabel, var.index);
			}
			return;
		}
		if (this.type == ARRAY)
		{
			Variable arrayVar = this.var3;
			Variable indexVar = this.var1;
			Variable lengthVar = this.var2;
			
			Label scopeLabel = new Label();
			writer.visitLabel(scopeLabel, false);
			
			// Local Variables
			var.index = writer.addLocal(MethodWriter.TOP);
			indexVar.index = writer.addLocal(MethodWriter.INT);
			lengthVar.index = writer.addLocal(MethodWriter.INT);
			arrayVar.index = writer.addLocal(arrayVar.type);
			
			// Load the array
			var.value.writeExpression(writer);
			writer.visitInsn(Opcodes.DUP);
			arrayVar.writeSet(writer, null, null);
			// Load the length
			writer.visitInsn(Opcodes.ARRAYLENGTH);
			lengthVar.writeSet(writer, null, null);
			// Set index to 0
			writer.visitLdcInsn(0);
			indexVar.writeSet(writer, null, null);
			
			// Jump to boundary check
			writer.visitJumpInsn(Opcodes.GOTO, this.updateLabel);
			writer.visitLabel(this.startLabel);
			
			// Load the element
			arrayVar.writeGet(writer, null);
			indexVar.writeGet(writer, null);
			writer.visitInsn(arrayVar.type.getArrayLoadOpcode());
			var.writeSet(writer, null, null);
			
			// Then
			this.then.writeStatement(writer);
			
			// Increase index
			writer.visitIincInsn(indexVar.index, 1);
			// Boundary Check
			writer.visitLabel(this.updateLabel);
			indexVar.writeGet(writer, null);
			lengthVar.writeGet(writer, null);
			writer.visitJumpInsn2(Opcodes.IF_ICMPLT, this.startLabel);
			
			// Local Variables
			writer.removeLocals(4);
			writer.visitLabel(this.endLabel);
			
			writer.visitLocalVariable(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), scopeLabel, this.endLabel, var.index);
			writer.visitLocalVariable("$index", "I", null, scopeLabel, this.endLabel, indexVar.index);
			writer.visitLocalVariable("$length", "I", null, scopeLabel, this.endLabel, lengthVar.index);
			writer.visitLocalVariable("$array", arrayVar.type.getExtendedName(), arrayVar.type.getSignature(), scopeLabel, this.endLabel, arrayVar.index);
			return;
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		if (this.type != 0)
		{
			this.variable.type.toString(prefix, buffer);
			buffer.append(' ').append(this.variable.name).append(Formatting.Statements.forEachSeperator);
			this.variable.value.toString(prefix, buffer);
		}
		else
		{
			if (this.variable != null)
			{
				this.variable.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.condition != null)
			{
				buffer.append(' ');
				this.condition.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.update != null)
			{
				buffer.append(' ');
				this.update.toString(prefix, buffer);
			}
		}
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.then.toString(prefix, buffer);
			}
			else
			{
				buffer.append(' ');
				this.then.toString(prefix, buffer);
			}
		}
	}
}
