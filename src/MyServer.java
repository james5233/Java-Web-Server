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
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.IOException;

public class MyServer {
	
  protected void start () throws java.io.IOException {
    ServerSocket s;
    try {
      s = new ServerSocket (8080);
    } catch (Exception e) {
      System.out.println ("[Socket-Creation] Error: " + e);
      return;
    }
		
		List<String> lst_key = new ArrayList<String> ();
		List<String> lst_handler = new ArrayList<String> ();
		int n_handlers = 0;

		try {
			BufferedReader r = new BufferedReader (new FileReader ("config"));
			String str;

			while ((str = r.readLine ()) != null) {
				String[] arr = str.split (" ");
				lst_key.add (arr[0]);
				lst_handler.add (arr[1]);
				n_handlers++;
			}
			r.close ();

			for (int i = 0; i < n_handlers; i++) {
				System.out.println ("Pattern : [" + lst_key.get (i) + "]\t" +
					"Handler: [" + lst_handler.get (i) + "]");
			}

		} catch (Exception e) {
			e.printStackTrace ();
		}
		
		System.out.println ("---INIT: SESSION-MANAGER---");
		System.out.println ("Starting Session Manager...");
		String[] cmds_sessmgr = {"java", "session_manager"};
		Process io_sessmgr = Runtime.getRuntime ().exec (cmds_sessmgr);
		System.out.println ("Session Manager Started");
		System.out.println ("---END-INIT---");

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
        String flname = "";
				int cont_len = 0;
				String cookie = "";
				boolean io_err = false;
				
				// Read and Parse Header
				System.out.println ("---REQUEST---");
				while (true) {
					int c;
					str = "";
					while (((c = inb.read ()) != -1) && ( c != '\n')) {
						if (c != '\r')
							str += (char)c;
					}
					
					if (c == -1) {
						io_err = true;
						break;
					}

					if (str == "")
						break;
          
					if (firstLine) {
            String[] tokens = str.split (" ");
						flname = tokens[1];
            firstLine = false;
          } else {
						String[] token = str.split (":");

						if (token[0].equals ("Content-Length"))
							cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
						else if (token[0].equals ("Cookie"))
							cookie = token[1].replaceAll ("\\s+", "");
					}
          System.out.println (str);
        }
				
				if (io_err) {
					System.out.println ("---REQUEST DROPPED---\n");
					continue;
				}

				System.out.println (str);	
				// Read and Save Body 
				try {
					int br = 0;
					int c = 0;
					str = "";
					while (br < cont_len) {
						c = inb.read ();
						str += (char)c;
						br++;
					}
					System.out.print (str);
				} catch (Exception e) {
					System.out.println ("Error " + e);
				}
				System.out.println ("---END OF REQUEST---\n");
				
				int flname_len = flname.length ();
				int i;
				for (i = 1; i < flname_len && flname.charAt (i) != '/'; i++);
				String patt = flname.substring (1, i);
				flname = flname.substring (i);
				
				if (patt == null)
					continue;
				
				String handler = "";
				for (i = 0; i < n_handlers; i++) {
					if (lst_key.get(i).equals (patt)) {
						handler = lst_handler.get (i);
						break;
					}
				}

				if (handler.equals (""))
					continue;

				System.out.println ("---INTERNAL---");
				System.out.println ("Handler: " + handler);
				System.out.println ("Principal argument: " + flname);
				System.out.println ("---END OF INTERNAL---\n");

				// Invoke file_handler --> Get Response from it --> Send response to client
				try {
					String[] cmds = {"java", handler};
					Process io = Runtime.getRuntime ().exec (cmds);
					
					BufferedInputStream in_pipe = new BufferedInputStream (io.getInputStream ());
					BufferedOutputStream out_pipe = new BufferedOutputStream (io.getOutputStream ());
					
					out_pipe.write ((flname + "\r\n").getBytes ());
					out_pipe.write (("Cookie: " + cookie + "\r\n").getBytes ());
					out_pipe.write (("Content-Length: " + cont_len + "\r\n\r\n").getBytes ());
					out_pipe.write (str.getBytes ());
					out_pipe.flush ();
					out_pipe.close ();
					
					System.out.println ("---RESPONSE---");
					System.out.println ("!! SUPPRESSED !!");
					byte[] buff = new byte[1024];
					int c = 0;
					while ((c = in_pipe.read (buff, 0, 1024)) > 0) {
						out.write (buff, 0, c);
						//System.out.write (buff, 0, c);
					}
				} catch (Exception e) {
						e.printStackTrace();
				}
				
				// Flush All and close connection
				System.out.flush ();
				System.out.println ("---END OF RESPONSE---\n");
      	out.flush ();
				out.close ();
      	remote.close ();
      } catch (Exception e) {
        System.out.println ("[Outer] Error: " + e);
      }
		}
  }

  public static void main (String args[]) throws java.io.IOException {
    MyServer srv = new MyServer ();
    srv.start ();
  }
}
