import java.util.UUID;
import java.util.HashMap;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ObjectInput;

public class session_manager {
	public static void main (String[] args) {
		int n_keys = 0;
		HashMap<String, String> dict = new HashMap<String, String> ();
		HashMap<String, byte[] > sess = new HashMap<String, byte[]> ();
		
		try {
			BufferedReader r = new BufferedReader (new FileReader ("sessmgr.conf"));
			String str;

			while ((str = r.readLine ()) != null) {
				String[] arr = str.split (" ");
				dict.put (arr[0], arr[1]);	
			}
			r.close ();

			for (String s : dict.keySet ())
				System.out.println (s + ": " + dict.get (s));

		} catch (Exception e) {
			e.printStackTrace ();
		}
    
		n_keys = dict.size ();
		ServerSocket s;
    try {
      s = new ServerSocket (Integer.parseInt (dict.get ("port")));
    } catch (Exception e) {
      System.out.println ("[Socket-Creation] Error: " + e);
      return;
    }

    for (;;) {
      try {
        Socket remote = s.accept ();
				InputStream ins = remote.getInputStream ();
				BufferedInputStream inb = new BufferedInputStream (ins);
				DataOutputStream out = new DataOutputStream (
            new BufferedOutputStream (
              remote.getOutputStream ()));
        
				String str = ".";
        boolean firstLine = true;
        String cmd = "";
				String hsh = "";
				int cont_len = 0;
				
				// Read and Parse Header
				System.out.println ("---REQUEST---");
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
          System.out.println (str);
        }
				System.out.println (str);	

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
					System.out.println ("Error " + e);
				}

				byte[] body = body_str.toByteArray();
				System.out.println ("---END OF REQUEST---\n");
				
				if (cmd.equals ("PUT")) {
					sess.put (hsh, body);
					out.write (("SUCCESS " + hsh + "\r\n").getBytes ());
					out.write (("Content-Length: " + "0" + "\r\n\r\n").getBytes ());
				} else if (cmd.equals ("GET")) {
					if (sess.containsKey (hsh)) {
						byte[] res_body = sess.get (hsh);
						if (res_body == null) {
							out.write (("SUCCESS " + hsh + "\r\n").getBytes ());
							out.write (("Content-Length: " + "0" + "\r\n\r\n").getBytes ());
						} else {
							out.write (("SUCCESS " + hsh + "\r\n").getBytes ());
							out.write (("Content-Length: " + res_body.length + "\r\n\r\n").getBytes ());
							out.write (res_body);
						}
					} else {
						out.write (("FAIL " + hsh + "\r\n").getBytes ());
						out.write (("Content-Length: " + "0" + "\r\n\r\n").getBytes ());
					}
				} else if (cmd.equals ("DEL")) {
					if (sess.containsKey (hsh)) {
						sess.remove (hsh);
						out.write (("SUCCESS " + hsh + "\r\n").getBytes ());
						out.write (("Content-Length: " + "0" + "\r\n\r\n").getBytes ());
					} else {
						out.write (("FAIL " + hsh + "\r\n").getBytes ());
						out.write (("Content-Length: " + "0" + "\r\n\r\n").getBytes ());
					}
				} else if (cmd.equals ("MAKE")) {
					String str_hsh = null;
					/*
					do { 
						str_hsh = UUID.randomUUID().toString ();
					} while (sess.containsKey (str_hsh));
					*/
					out.write (("SUCCESS " + hsh + "\r\n").getBytes ());
					out.write (("Content-Length: " + hsh.length () + "\r\n\r\n").getBytes ());
					out.write (hsh.getBytes ());
					byte[] hmp = null;
					sess.put (hsh, hmp);
				}
				
				out.flush ();
				out.close ();
			} catch (Exception et) {
				et.printStackTrace ();
			}
		}
	}
}
