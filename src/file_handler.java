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

public class file_handler {
	public static void main (String args[]) throws java.io.IOException {

		BufferedInputStream inb = new BufferedInputStream (System.in);
		BufferedOutputStream out = new BufferedOutputStream (System.out);


		String str = ".";
		boolean firstLine = true;
		boolean dumpIndex = false;
		String flname = "";
		int cont_len = 0;
		
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
				if (str.equals ("/"))
					dumpIndex = true;
				else
					flname = str.substring (1);
				firstLine = false;
			} else {
				String[] token = str.split (":");

				if (token[0].equals ("Content-Length"))
					cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
			}

			//System.err.println (str);
		}

		//System.err.println ("[Body | Content-Length = " + cont_len + "]");
		try {
			int br = 0;
			int c = 0;
			str = "";
			while (br < cont_len) {
				c = inb.read ();
				str += (char)c;
				br++;
			}

			//System.err.println (str);
		} catch (Exception e) {
			//System.err.println ("Error " + e);
		}
		//System.err.println ("[End of body]");
		
		if (dumpIndex) {
			try {
				out.write("HTTP/1.0 200 OK\r\n".getBytes ());
				out.write("Content-Type: text/html\r\n".getBytes ());
				out.write("Server: Bot\r\n".getBytes ());

				out.write("\r\n".getBytes ());

				out.write("<H1>Index of /</H2>\n".getBytes ());
				File dir = new File (".");
				File[] filesList = dir.listFiles (); 
				for (File file : filesList) {
					if (file.isFile ()) {
						out.write (("<a href=\"" + 
									URLEncoder.encode (file.getName (), "US-ASCII") + 
									"\">" + file.getName () + "</a><br/>\n").getBytes ());
					}
				}
			} catch (Exception e) {
				//System.err.println("[Index] Error: " + e);
			}
		} else {
			try {
				flname = URLDecoder.decode (flname, "US-ASCII");
				File f = new File (flname);
				if (f.exists () && f.isFile ()) {
					Path src_path = Paths.get (flname);
					FileInputStream inp = new FileInputStream (flname);
					out.write ("HTTP/1.0 200 OK\r\n".getBytes ());
					out.write (("Content-Type: " + 
								Files.probeContentType (src_path) + 
								"\r\n").getBytes ());
					out.write (("Content-Length: " + f.length () + 
								"\r\n").getBytes ());

					out.write ("\r\n".getBytes ());

					int c;
					while ((c = inp.read ()) != -1) {
						out.write (c);
					}
					inp.close ();
				} else {
					out.write ("HTTP/1.0 404 Not Found\r\n".getBytes ());
					out.write ("Content-Type: text/html\r\n".getBytes ());
					out.write ("Server: Bot\r\n".getBytes ());

					out.write ("\r\n".getBytes ());

					out.write ("<H1> File not Found!</H1>\n".getBytes ());
				}
			} catch (Exception e) {
				//System.err.println ("[File] Error: " + e);
			}
		}
		out.flush ();
	}
}
