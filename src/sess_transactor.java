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
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class sess_transactor {
	int status;
	InetAddress ip;
	int port;
	
	sess_transactor (String ip_par, int port_par) {
		try {
			status = 0;
			ip = InetAddress.getByName (ip_par);
			port = port_par;
		} catch (Exception ex) {
			ex.printStackTrace ();
		}
	}

	public int getStatus () {
		return status;
	}

	public String make (String key) {
		String res = null;
		try {
			Socket s = new Socket (ip, port);

			BufferedInputStream inb = new BufferedInputStream (s.getInputStream ());
			BufferedOutputStream out = new BufferedOutputStream (s.getOutputStream ());
			out.write (("MAKE " + key + "\r\nContent-Length: 0\r\n\r\n").getBytes ());
			out.flush ();

			String str = ".";
			boolean firstLine = true;
			String cmd = "";
			String hsh = "";
			int cont_len = 0;

			// Read and Parse Header
			//System.out.println ("---REQUEST---");
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
					String[] tokens = str.split (" ");
					cmd = tokens[0];
					hsh = tokens[1];
					firstLine = false;
				} else {
					String[] token = str.split (":");

					if (token[0].equals ("Content-Length"))
						cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
				}
				//System.out.println (str);
			}
			//System.out.println ("[cont-len = " + cont_len + "]");
			//System.out.println (str);	

			// Read and Save Body 
			ByteArrayOutputStream body_str = new ByteArrayOutputStream ();
			try {
				int br = 0;
				int c = 0;
				while (br < cont_len) {
					c = inb.read ();
					body_str.write (c);
					br++;
				}
			} catch (Exception e) {
				//System.out.println ("Error " + e);
			}

			byte[] body = body_str.toByteArray();
			res =  new String (body, Charset.defaultCharset ());
			status = 0;
		} catch (Exception ex) {
			ex.printStackTrace ();
		}
		return res;
	}	
	
	public int put (String key, Object o) {
		try {
			Socket s = new Socket (ip, port);

			BufferedInputStream inb = new BufferedInputStream (s.getInputStream ());
			BufferedOutputStream out = new BufferedOutputStream (s.getOutputStream ());

			ByteArrayOutputStream bos = new ByteArrayOutputStream ();
			ObjectOutput op = null;
			byte[] data = null;
			try {
				op = new ObjectOutputStream (bos);
				op.writeObject (o);
				op.flush ();
				data = bos.toByteArray ();
			} finally {
				try {
					bos.close ();
				} catch (Exception e) {
					e.printStackTrace ();
				}
			}
			out.write (("PUT " + key + "\r\nContent-Length: " + data.length + "\r\n\r\n").getBytes ());
			out.write (data);
			out.flush ();

			String str = ".";
			boolean firstLine = true;
			String cmd = "";
			String hsh = "";
			int cont_len = 0;

			// Read and Parse Header
			//System.out.println ("---REQUEST---");
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
					String[] tokens = str.split (" ");
					cmd = tokens[0];
					hsh = tokens[1];
					firstLine = false;
				} else {
					String[] token = str.split (":");

					if (token[0].equals ("Content-Length"))
						cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
				}
				//System.out.println (str);
			}
			//System.out.println (str);	

			/*
			// Read and Save Body 
			ByteArrayOutputStream body_str = new ByteArrayOutputStream ();
			try {
			int br = 0;
			int c = 0;
			while (br < cont_len) {
			c = inb.read ();
			body_str.write (c);
			br++;
			}
			} catch (Exception e) {
			//System.out.println ("Error " + e);
			}

			byte[] body = body_str.toByteArray();
			String res =  new String (body, Charset.defaultCharset ());
			*/

			if (cmd.equals ("SUCCESS"))
				status = 0;
			else
				status = 1;
		} catch (Exception ex) {
			ex.printStackTrace ();
		}

		return status;
	}	
	
	public Object get (String key) {
		Object o = null;
		try {
		Socket s = new Socket (ip, port);

		BufferedInputStream inb = new BufferedInputStream (s.getInputStream ());
		BufferedOutputStream out = new BufferedOutputStream (s.getOutputStream ());
	
		out.write (("GET " + key + "\r\nContent-Length: " + "0" + "\r\n\r\n").getBytes ());
		out.flush ();
		
		String str = ".";
		boolean firstLine = true;
		String cmd = "";
		String hsh = "";
		int cont_len = 0;

		// Read and Parse Header
		//System.out.println ("---REQUEST---");
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
				String[] tokens = str.split (" ");
				cmd = tokens[0];
				hsh = tokens[1];
				firstLine = false;
			} else {
				String[] token = str.split (":");

				if (token[0].equals ("Content-Length"))
					cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
			}
			//System.out.println (str);
		}
		//System.out.println (str);	
		
		// Read and Save Body 
		ByteArrayOutputStream body_str = new ByteArrayOutputStream ();
		try {
			int br = 0;
			int c = 0;
			while (br < cont_len) {
				c = inb.read ();
				body_str.write (c);
				br++;
			}
		} catch (Exception e) {
			//System.out.println ("Error " + e);
		}

		byte[] body = body_str.toByteArray();
		if (!cmd.equals ("SUCCESS")) {
			status = 1;
			return null;
		}

		if (body.length == 0)
			return null;

		ByteArrayInputStream bis = new ByteArrayInputStream (body);
		try {
			ObjectInputStream op = new ObjectInputStream (bis);
			o = op.readObject ();
			op.close ();
		} finally {
			try {
				bis.close ();
			} catch (Exception e) {
				e.printStackTrace ();
			}
		}

		if (cmd.equals ("SUCCESS"))
			status = 0;
		else
			status = 1;
		} catch (Exception ex) {
			ex.printStackTrace ();
		}

		return o;
	}	
	
	public int del (String key) {
		try {
			Socket s = new Socket (ip, port);

			BufferedInputStream inb = new BufferedInputStream (s.getInputStream ());
			BufferedOutputStream out = new BufferedOutputStream (s.getOutputStream ());
			out.write (("DEL " + key + "\r\nContent-Length: 0\r\n\r\n").getBytes ());
			out.flush ();

			String str = ".";
			boolean firstLine = true;
			String cmd = "";
			String hsh = "";
			int cont_len = 0;

			// Read and Parse Header
			//System.out.println ("---REQUEST---");
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
					String[] tokens = str.split (" ");
					cmd = tokens[0];
					hsh = tokens[1];
					firstLine = false;
				} else {
					String[] token = str.split (":");

					if (token[0].equals ("Content-Length"))
						cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
				}
				//System.out.println (str);
			}
			//System.out.println (str);	

			/*
			// Read and Save Body 
			ByteArrayOutputStream body_str = new ByteArrayOutputStream ();
			try {
			int br = 0;
			int c = 0;
			while (br < cont_len) {
			c = inb.read ();
			body_str.write (c);
			br++;
			}
			} catch (Exception e) {
			//System.out.println ("Error " + e);
			}

			byte[] body = body_str.toByteArray();
			*/
			if (cmd.equals ("SUCCESS"))
				status = 0;
			else
				status = 1;
		} catch (Exception ex) {
			ex.printStackTrace ();
		}

		return status;
	}

	@SuppressWarnings ("unchecked")
	public static void main (String[] args) {
		sess_transactor s = new sess_transactor ("localhost", 12480);
		
		String plain = "James Wilkins Booth";
		String str_hsh = null;
		int status;

		try {
			MessageDigest md = MessageDigest.getInstance ("MD5");
			md.reset ();
			md.update (plain.getBytes ());
			str_hsh = (new HexBinaryAdapter ()).marshal (md.digest ());
		} catch (Exception e) {
			e.printStackTrace ();
		}
		
		String str = s.make (str_hsh);
		System.out.println ("Just Created: " + str);
		
		System.out.println ("Accessing Just Created Object...");
		HashMap<String, Integer> mp = null;
		mp = (HashMap<String, Integer>) s.get (str);
		status = s.getStatus ();
		System.out.println ((status == 0) ? "Successfully Retrieved!" : "Failure!");
		if (mp == null) {
			System.out.println ("But mp is null!");
		} else {
			System.out.println ("mp = " + mp);
		}
		
		HashMap<String, Integer> hmap = new HashMap<String, Integer> ();
		
		System.out.println ("Inserting Empty Object...");
		status = s.put (str, hmap);
		System.out.println ((status == 0)? "Successfully Stored!" : "Error!");
		
		System.out.println ("Accessing Empty Object...");
		mp = null;
		mp = (HashMap<String, Integer>) s.get (str);
		status = s.getStatus ();
		System.out.println ((status == 0) ? "Successfully Retrieved!" : "Failure!");
		if (mp == null) {
			System.out.println ("But mp is null!");
		} else {
			System.out.println ("mp = " + mp);
		}

		hmap.put ("TATA", 520);
		hmap.put ("BIRLA", 600);
		
		System.out.println ("Before storing: " + hmap);
		status = s.put (str, hmap);
		System.out.println ((status == 0)? "Successfully Stored!" : "Error!");
		
		mp = null;
		mp = (HashMap<String, Integer>) s.get (str);
		status = s.getStatus ();
		System.out.println ((status == 0) ? "Successfully Retrieved!" : "Failure!");
		System.out.println ("After storing: " + mp);
		
		status = s.del (str);
		System.out.println ((status == 0) ? "Successfully deleted!" : "Deletion Failed!");
		System.out.println ("Trying to get a deleted string...");
		
		mp = null;
		mp = (HashMap<String, Integer>) s.get (str);
		status = s.getStatus ();
		System.out.println ((status == 0) ? "Successfully Retrieved!" : "Failure!");

	}
}
