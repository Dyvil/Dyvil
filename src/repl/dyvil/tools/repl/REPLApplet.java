package dyvil.tools.repl;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class REPLApplet extends Applet
{
	static final long serialVersionUID = 1;

	protected JTextArea textArea;
	protected JTextField textBox;

	protected DyvilREPL repl;

	/**
	 * This function is called when they hit ENTER or click GO.
	 */
	private final ActionListener actionListener = e -> {
		String userInput = REPLApplet.this.textBox.getText();
		REPLApplet.this.repl.getOutput().println("> " + userInput);
		this.repl.processInput(userInput);
		REPLApplet.this.textBox.setText("");
	};

	public REPLApplet() throws IOException
	{
		this.initGUI();

		this.repl = new DyvilREPL(new PrintStream(new ByteArrayOutputStream()
		{
			@Override
			public synchronized void write(byte[] b, int off, int len)
			{
				for (; off < len; off++)
				{
					this.write(b[off]);
				}
			}

			@Override
			public synchronized void write(int b)
			{
				super.write(b);

				if ('\n' == (char) b)
				{
					final String string = new String(this.buf, 0, this.count, StandardCharsets.UTF_8);
					REPLApplet.this.textArea.append(string);
					this.reset();
				}
			}
		}));

		this.repl.launch(new String[] {});
	}

	private void initGUI()
	{
		this.setName("Dyvil REPL");

		final Font monaco = new Font("Monaco", Font.TRUETYPE_FONT, 12);

		this.textArea = new JTextArea(30, 50);

		this.textArea.setAutoscrolls(true);
		this.textArea.setFont(monaco);
		this.textArea.setEditable(false);
		this.textArea.setTabSize(4);

		final JScrollPane jScrollPane = new JScrollPane(this.textArea);
		this.add(BorderLayout.NORTH, jScrollPane);

		this.textBox = new JTextField(50);
		this.textBox.setFont(monaco);
		this.textBox.addActionListener(this.actionListener);
		this.add(BorderLayout.SOUTH, this.textBox);
	}
}

