import java.util.HashMap;

public class Stock {
	HashMap<String, Integer[]> bought;
	HashMap<String, Integer[]> cart;
	HashMap<String, HashMap<String, Integer[]> > hMap;

	public Stock() {
		hMap = new HashMap<String, HashMap<String, Integer[]> > ();
		bought = new HashMap<String, Integer[]> ();
		cart = new HashMap<String, Integer[]> ();
		hMap.put ("bought", bought);
		hMap.put ("cart", cart);
	}

	@SuppressWarnings ("unchecked")
	public void setSessionData (Object o) {
		if (o != null) {
			hMap = (HashMap<String, HashMap<String, Integer[]> >) o;

			bought = hMap.get ("bought");
			if (bought == null)
				bought = new HashMap<String, Integer[]> ();

			cart = hMap.get ("cart");
			if (cart == null)
				cart = new HashMap<String, Integer[]> ();
		} else {
			hMap = new HashMap<String, HashMap<String, Integer[]> > ();
			bought = new HashMap<String, Integer[]> ();
			cart = new HashMap<String, Integer[]> ();
			hMap.put ("bought", bought);
			hMap.put ("cart", cart);
		}
	}

	public Object getSessionData () {
		if (hMap == null)
				hMap = new HashMap<String, HashMap<String, Integer[]> > ();

		if (bought == null)
			bought = new HashMap<String, Integer[]> ();
		hMap.put ("bought", bought);
		
		if (cart == null)
			cart = new HashMap<String, Integer[]> ();
		hMap.put ("cart", cart);
		
		Object o = hMap;
		return o;
	}

	public String ChkStockPrice (String symbol) {
		String res = "";

		if (symbol == null) 
			return "Invalid Stock Name\n";
		else {
			Integer[] i = bought.get (symbol);
			if (i != null) {
				res = "You have already bought " + i[0] + " units from before!\n";
			} else 
				res = "";

			return res + "Price of " + symbol + " is 220.5\n";
		}
	}
	
	public String Buy (String symbol, String qty) {
		if (symbol == null || qty == null) 
			return "Invalid Stock Name or quantity\n";
		else {
			Integer[] i = cart.get (symbol);
			if (i != null) {
				i[0] += Integer.parseInt (qty);
				cart.put (symbol, i);
			} else {
				i = new Integer[2];
				i[0] = Integer.parseInt (qty);
				i[1] = 0;
				cart.put (symbol, i);
			}

			return qty + " units of " + symbol + " added to cart!\n";
		}
	}

	public String Sell (String symbol, String qty) {
		String res = "";

		if (symbol == null || qty == null)
			return "Invalid Stock Name or quantity\n";
		else {
			Integer i[] = bought.get (symbol);
			Integer c[] = cart.get (symbol);
			if (i != null) {
				Integer q =  Integer.parseInt (qty);
				
				if (c == null) {
					c = new Integer[2];
					c[0] = 0;
					c[1] = 0;
				}

				Integer tot = c[1] + q;
				if (i[0] < tot)
					return "Error: You only have " + i[0] + " units available!\n" +
					"Cannot sell " + q + " units of " + symbol + "\n";
				else
					c[1] = tot;
				
				cart.put (symbol, c);

				return "Sold " + qty + " units of " + symbol + " (In Cart!)\n";
			} else {
				return "You have no stocks available.\n" + 
					"Cannot sell " + qty + " units of " + symbol + "\n";
			}
		}
	}

	public String CheckOut () {
		String res = "";
		for (String s : cart.keySet ()) {
			Integer q[] = cart.get (s);
			Integer i = q[0];
			Integer j = q[1];

			if (bought.containsKey (s)) {
				i += (bought.get (s))[0];
				i -= j;
			}
			
			if (q[0] > 0)
				res += "Bought " + q[0] + " units of " + s + "\n";
			if (q[1] > 0)
				res += "Sold " + q[1] + " units of " + s + "\n";

			Integer[] arr = new Integer[1];
			arr[0] = i;
			if (bought.containsKey (s) && i == 0)
				bought.remove (s);
			else
				bought.put (s, arr);
		}
		
		cart.clear ();
		res += "Checkout Successful!\n";

		return res;
	}

	public String totalStocks (String symbol) {
		String res = "";
		if (symbol == null)
			return "Invalid Stock Name!\n";
		
		Integer[] i = bought.get (symbol);
		if (i == null)
			return "You have no Stocks for " + symbol + "\n";
		else
			return "You have " + i[0] + " units of " + symbol + "\n";
	}
	
	public static void main (String[] args) {
		System.out.println ("Please do not invoke this procedure!");
	}
}
