package dyvil.io;

import dyvil.annotation.Utility;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.array.ByteArray;
import dyvil.reflect.Modifiers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * The {@linkplain Utility utility interface} <b>WebUtils</b> can be used for several {@link URL}-related operations
 * such as checking for website availability, download the website as a {@code byte} array or saving it to a file on the
 * disk.
 *
 * @author Clashsoft
 * @version 1.0
 */
@Utility(URL.class)
public final class WebUtils
{
	private WebUtils()
	{
		// no instances
	}
	
	/**
	 * Checks if the website at the given {@link String} {@code url} is available using the HTTP request {@code HEAD}.
	 * The created {@link HttpURLConnection} is set to not follow redirects.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return true, if the website is available
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	public static boolean isAvailable(String url)
	{
		try
		{
			return isAvailable(new URL(url));
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	/**
	 * Checks if the website at the given {@link URL} {@code url} is available using the HTTP request {@code HEAD}. The
	 * created {@link HttpURLConnection} is set to not follow redirects.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return true, if the website is available
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean isAvailable(URL url) throws IOException
	{
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(false);
		con.setRequestMethod("HEAD");
		return con.getResponseCode() == HttpURLConnection.HTTP_OK;
	}

	/**
	 * Downloads the website at the given {@link String} {@code url} and stores it in a {@code byte} array. If the
	 * website is formatted as a sequence of characters (i.e., it is a text or HTML), {@code new String(byte[])} can be
	 * used to convert it to a String.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return the byte array containing the website data
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	public static byte[] download(String url)
	{
		try
		{
			return download(new URL(url));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return ByteArray.EMPTY;
		}
	}
	
	/**
	 * Downloads the website at the given {@link URL} {@code url} and stores it in a {@code byte} array. If the website
	 * is formatted as a sequence of characters (i.e., it is a text or HTML), {@code new String(byte[])} can be used to
	 * convert it to a String.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return the byte array containing the website data
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	public static byte[] download(URL url) throws IOException
	{
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		ByteBuffer bytebuf = ByteBuffer.allocate(1024);
		rbc.read(bytebuf);
		return bytebuf.array();
	}
	
	/**
	 * Downloads the website at the given {@link String} {@code url} and saves it at the given file.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return true, if downloading and saving the website was successful
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	public static boolean download(String url, File output)
	{
		try (ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());
		     FileOutputStream fos = new FileOutputStream(output))
		{
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}
	
	/**
	 * Downloads the website at the given {@link URL} {@code url} and saves it at the given file.
	 *
	 * @param url
	 * 		the URL of the website to check
	 *
	 * @return true, if downloading and saving the website was successful
	 *
	 * @throws IOException
	 * 		if an IOException occurred.
	 */
	public static boolean download(URL url, File output)
	{
		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		     FileOutputStream fos = new FileOutputStream(output))
		{
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}
}
