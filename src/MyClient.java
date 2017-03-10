import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.net.InetAddress;
import java.security.MessageDigest;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class MyClient {

	static String usage = 
	"===============================================================================\n" +
	"                          PLEASE READ THIS USAGE NOTE                          \n" + 
	"===============================================================================\n" +
	"Invoke like this:\n" + 
	"\033[1m$ java MyClient <server-hostname> <server-port> <username>\033[0m\n" +
	"  Example: $ java MyClient localhost 8080 \"James Moriarty\"\n" +
	"The following commands are available:\n" +
	"\033[1mClient$\033[0m file <file-path> <downloaded-filename>\n" +
	"  <file-path> is the requested file\'s path relative to the server root\n" +
	"  <downloaded-filename> is the name with which the\n" +
	"  downloaded file will be written to disk.\n" +
	"  Example: file /config config.cfg\n" +
	"\033[1mClient$\033[0m price <Stock-name>\n" +
	"  Returns the stock price of the stock <Stock-name>\n" +
	"  Example: price TATAMOTORS\n" +
	"\033[1mClient$\033[0m buy <Stock-name> <Quantity>\n" + 
	"  Example: buy TATAMOTORS 20\n" +
	"\033[1mClient$\033[0m sell <Stock-name> <Quantity>\n" +
	"  Example: sell TATAMOTORS 20\n" +
	"\033[1mClient$\033[0m checkout\n" + 
	"  Commits the cart contents and then empties the cart.\n" +
	"\033[1mClient$\033[0m total <Stock-name>\n" +
	"  Returns the total units bought/commited for <Stock-name>\n" +
	"  Example: total TATAMOTORS\n" +
	"\033[1mClient$\033[0m exit\n" + 
	"===============================================================================\n";

	static String[] valid_cmds = { "file", "buy", "price", "sell", "exit", "checkout", "total"};
	static Integer[] num_args = { 1, 2, 1, 2, 0, 0, 1 };
 
	public static boolean is_valid_cmd (String[] arr) {
		if (arr.length < 1)
			return false;

		boolean found = false;
		int i = 0;
		for (String s : valid_cmds) {
			if (s.equals (arr[0])) {
				found = true;
				break;
			}
			i++;
		}

		if (!found)
			return false;

		if (arr.length - 1 != num_args[i])
			return false;

		return true;
	}

	public static void main (String[] args) throws java.io.IOException,
  java.net.UnknownHostException {

    System.out.println (MyClient.usage);
    String ip_str = args[0];
    String port_str = args[1];
		String usr_name = args[2];
		String str_hsh = null;

		try {
			MessageDigest md = MessageDigest.getInstance ("MD5");
			md.reset ();
			md.update (usr_name.getBytes ());
			str_hsh = (new HexBinaryAdapter ()).marshal (md.digest ());
		} catch (Exception e) {
			e.printStackTrace ();
		}
    
		int port = Integer.parseInt (port_str);
      
    InetAddress ip = InetAddress.getByName (ip_str);
    
    BufferedReader cons = new BufferedReader (
      new InputStreamReader (System.in));
    
    String str_cons;
    while (System.out.printf ("\033[1mClient$\033[0m ") != null && (str_cons = cons.readLine ()) != null) {
      try {

        String[] arr = str_cons.split (" ");
        boolean download = false;
				
				if (!is_valid_cmd (arr)) {
					System.out.println ("Invalid command!\n");
					continue;
				}

				if (arr[0].equals ("exit")) {
          System.out.println ("");
          System.exit (0);
        }
					

        Socket s = new Socket (ip, port);

        BufferedInputStream inb = new BufferedInputStream (s.getInputStream ());
        BufferedOutputStream out = new BufferedOutputStream (s.getOutputStream ());
        
				if (arr[0].equals ("file")) {
          download = true;
          out.write (("GET /file" + arr[1] + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          out.write ("Content-Length: 0\r\n\r\n".getBytes ());
        } else if (arr[0].equals ("price")) {
          out.write (("POST /java/Stock.jar" + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          String body = "function=ChkStockPrice\nsymbol=" + arr[1] + "\n";
          out.write (("Content-Length: " + body.length () + 
            "\r\n\r\n").getBytes ());
          out.write (body.getBytes ());
        } else if (arr[0].equals ("buy")) {
          out.write (("POST /java/Stock.jar" + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          String body = "function=Buy\nsymbol=" + arr[1] + 
            "\nqty=" + arr[2] + "\n";
          out.write (("Content-Length: " + body.length () + 
            "\r\n\r\n").getBytes ());
          out.write (body.getBytes ());
        } else if (arr[0].equals ("sell")) {
          out.write (("POST /java/Stock.jar" + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          String body = "function=Sell\nsymbol=" + arr[1] + 
            "\nqty=" + arr[2] + "\n";
          out.write (("Content-Length: " + body.length () + 
            "\r\n\r\n").getBytes ());
          out.write (body.getBytes ());
        } else if (arr[0].equals ("checkout")) {
          out.write (("POST /java/Stock.jar" + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          String body = "function=CheckOut\n";
          out.write (("Content-Length: " + body.length () + 
            "\r\n\r\n").getBytes ());
          out.write (body.getBytes ());
        } else if (arr[0].equals ("total")) {
          out.write (("POST /java/Stock.jar" + " HTTP/1.0\r\n").getBytes ());
					out.write (("Cookie: " + str_hsh + "\r\n").getBytes ());
          String body = "function=totalStocks\nsymbol=" + arr[1] + "\n";
          out.write (("Content-Length: " + body.length () + 
            "\r\n\r\n").getBytes ());
          out.write (body.getBytes ());
        } else {
					System.out.println ("Invalid Command!");
					continue;
				}
        out.flush ();

        String str = ".";
        boolean firstLine = true;
        String flname = "";
        int cont_len = 0;
        
        // Read and Parse Header
        //System.out.println ("[Header]");
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
            flname = tokens[1];
            firstLine = false;
          } else {
            String[] token = str.split (":");

            if (token[0].equals ("Content-Length"))
              cont_len = Integer.parseInt (token[1].replaceAll ("\\s+", ""));
            else if (token[0].equals ("Set-Cookie"))
              str_hsh = token[1].replaceAll ("\\s+", "");
          }

          //System.out.println (str);
        }
        //System.out.println ("[End of header]");
            
        if (flname.equals ("200")) {
          if (download) {
            BufferedOutputStream outfl = new BufferedOutputStream (
              new FileOutputStream (arr[2]));

            // Read and Save Body 
            //System.out.println ("[Body | Content-Length = " + cont_len + "]");
            try {
              int br = 0;
              int c = 0;
              str = "";
              while (br < cont_len) {
                c = inb.read ();
                outfl.write (c);
                br++;
              }
              outfl.flush ();
              outfl.close ();
              System.out.println ("File written to \"" + arr[2] + "\"\n");
            } catch (Exception e) {
              System.out.println ("Error " + e);
            }
            //System.out.println ("[End of body]");
          } else {
            //System.out.println ("[Body | Content-Length = " + cont_len + "]");
            try {
              int br = 0;
              int c = 0;
              str = "";
              while (br < cont_len) {
                c = inb.read ();
                str += (char) c;
                br++;
              }
              System.out.println (str);
            } catch (Exception e) {
              System.out.println ("Error " + e);
            }
            //System.out.println ("[End of body]");
          }
        } else {
          System.out.println ("Error");
        }
        s.close ();
      } catch (Exception e) {
        e.printStackTrace ();
      }
    }
  }
}   

