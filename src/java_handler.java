import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.net.URL;
import java.util.Enumeration;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.PrintStream;
import java.io.FileReader;

public class java_handler {
	@SuppressWarnings ("unchecked")
	public static void main (String args[]) throws java.io.IOException {

		BufferedInputStream inb = new BufferedInputStream (System.in);
		BufferedOutputStream out = new BufferedOutputStream (System.out);
		
		String sessmgr_ip = "localhost";
		Integer sessmgr_port = 12480;
		HashMap<String, String> dict = new HashMap<String, String> ();
		try {
			BufferedReader r = new BufferedReader (new FileReader ("sessmgr.conf"));
			String str;

			while ((str = r.readLine ()) != null) {
				String[] arr = str.split (" ");
				dict.put (arr[0], arr[1]);	
			}
			r.close ();
			sessmgr_ip = dict.get ("host").trim ();
			sessmgr_port = Integer.parseInt (dict.get ("port").trim ());
		} catch (Exception e) {
			e.printStackTrace ();
		}

		PrintStream ps = new PrintStream("handler_error_log");
		System.setErr(ps);
		
		String str = ".";
		boolean firstLine = true;
		boolean dumpIndex = false;
		String flname = "";
		int cont_len = 0;
		String cookie = "";
		
		while (true) {
			int c;
			str = "";
			while ((c = inb.read ()) != '\n') {
				if (c != '\r')
					str += (char)c;
			}
			if (str == "")
				break;
			
			if (firstLine) {
				flname = str.substring (1);
				firstLine = false;
			} else {
				String[] token = str.split (":");

				if (token[0].equals ("Content-Length"))
					cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
				else if (token[0].equals ("Cookie"))
					cookie = token[1].replaceAll ("\\s+", "");
			}

			System.err.println (str);
		}
		System.err.println ("Service name = " + flname);
		HashMap<String, String> hmap = new HashMap<String, String>();
		ArrayList<String> arr_lst = new ArrayList<String>();
		int n_elem = 0;

		System.err.println ("[Body | Content-Length = " + cont_len + "]");
		try {
			int br = 0;
			int c = 0;
			str="";
			while (br < cont_len) {
				c = inb.read ();
				char d = (char) c;
				if (d == '\n') {
					String[] arr = str.split ("=");
					hmap.put (arr[0], arr[1]);	
					if (!arr[0].equals("function"))
						arr_lst.add (arr[1]);
					n_elem++;
					str = "";
				} else {
					str += d;
				}
				
				br++;
			}
			System.err.println ("Handler" + str);
		} catch (Exception e) {
			System.err.println ("Error " + e);
		}
		System.err.println ("[End of body]");
			
		out.write("HTTP/1.0 200 OK\r\n".getBytes ());
		out.write(("Content-Type: text/plain\r\n").getBytes ());
		out.write("Server: Bot\r\n".getBytes ());
		Class req_class = null;
		Object req_obj = null;
		Method func_to_invoke = null;
		
		if (n_elem > 0) {
			String func_name = hmap.get ("function");
			String symbol = hmap.get ("symbol");
			String qty = hmap.get ("qty");

			String pathToJar = flname;
			JarFile jarFile = new JarFile(pathToJar);
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = {new URL("jar:file:" + pathToJar + "!/")};

			while (e.hasMoreElements()) {
				try {
					JarEntry je = e.nextElement();
					if (je.isDirectory() || !je.getName().endsWith(".class")) {
						continue;
					}
					String className = je.getName().substring(0, 
							je.getName().length() - 6);
					className = className.replace('/', '.');
					/*
						 System.err.println (className);
						 for (URL u : urls)
						 System.err.println ("" + u.toString ());
						 System.err.flush ();
						 */
					URLClassLoader cl = URLClassLoader.newInstance(urls);
					Class c = cl.loadClass(className);
					Object t = c.newInstance();

					Method[] allMethods = c.getDeclaredMethods();
					for (Method m : allMethods) {
						String mname = m.getName();
						Class ret_type = m.getReturnType ();
						Class[] pType = m.getParameterTypes ();

						/*
							 System.out.println ("Method Name = " + mname);
							 System.out.println ("No. of arguments = " + pType.length);
							 System.out.println ("Return Type = " + ret_type.getName ());
							 System.out.println ("Argument List: ");
							 for (Class ct : pType) {
							 System.out.println ( "" + ct.getName ().substring (2));
							 }
							 */

						if (func_name != null && func_name.equals (mname)) {
							req_class = c;
							req_obj = req_class.newInstance ();
							func_to_invoke = m;
							break;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			String res = "";
			if (func_to_invoke != null) {
				try {
					sess_transactor sesstr = new sess_transactor (sessmgr_ip, sessmgr_port);
					Method sess_setter = req_class.getDeclaredMethod ("setSessionData", Class.forName("java.lang.Object"));
					Method sess_getter = req_class.getDeclaredMethod ("getSessionData");

					Object ob = sesstr.get (cookie);
					if (sesstr.getStatus () != 0) {
						sesstr.make (cookie);
						ob = sesstr.get (cookie);
					}

					sess_setter.invoke (req_obj, ob);
					
					Object arr[] = arr_lst.toArray ();
					try {
						func_to_invoke.setAccessible(true);
						Object o = func_to_invoke.invoke(req_obj, arr);
						res = (String)o;
						
						ob = sess_getter.invoke (req_obj);
						sesstr.put (cookie, ob);
						
						//System.out.println ("Returned: " + (String)o);

						// Handle any exceptions thrown by method to be invoked.
					} catch (InvocationTargetException x) {
						Throwable cause = x.getCause();
						//System.err.format("invocation of %s failed: %s%n",
						//	mname, cause.getMessage());
					} catch (Exception exc) {
						exc.printStackTrace ();
					}
				} catch (Exception excp) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					excp.printStackTrace(pw);
					res = sw.toString();
				}
			}

			out.write (("Set-Cookie: " + cookie + "\r\n").getBytes ());
			out.write (("Content-Length: " + res.length () + "\r\n").getBytes ());
			out.write ("\r\n".getBytes ());
			out.write(res.getBytes ());
		} else {
			out.write (("Set-Cookie: " + cookie + "\r\n").getBytes ());
			out.write ("Content-Length: 0\r\n\r\n".getBytes ());
		}

		out.flush ();
	}
}
