import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class Synonims 
{
	public static ArrayList<String> getSynonims(String word) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, SQLException 
	{
		boolean inBase=false;
		 ArrayList<String> resBase = new ArrayList<String>();
		
		
		// стучимся к базе на предмет существования подобного глагола
		System.out.println("-------- PostgreSQL "
				+ "JDBC Connection Testing ------------");
 
		try {
 
			Class.forName("org.postgresql.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			inBase = false;
 
		}
 
		System.out.println("PostgreSQL JDBC Driver Registered!");
 
		Connection connection = null;
 
		try {
 
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/testENG", "postgres",
					"1234");
 
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			inBase = false;
 
		}
 
		if (connection != null) 
		{
			System.out.println("You made it, take control your database now!");
			// ищем глагол в базе
			
			Statement st = connection.createStatement();			
					
			st = connection.createStatement();
			ResultSet rs = st.executeQuery( "SELECT \"syn\" FROM \"Verbs\"  where verb = '"+word+"';" );
					

			while (rs.next())
			{
			  
			  System.out.println(rs.getString(1));			  
			  if (rs.getString(1).isEmpty() == false) 
			  {
				  inBase = true;
				  String[] synsBase = rs.getString(1).split(";");				 
				  for(int i=0; i<synsBase.length; ++i)
				  {
					  resBase.add(synsBase[i]);
				  }
				  
				  
			  }
			  
				  
			}
			
			rs.close();
			st.close();
			
			
			
			
		} 
		
		else {
			System.out.println("Failed to make connection!");
			inBase = false;
		}	
		
		if(inBase == true)
		{
			connection.close();
			resBase.add(word);
			return resBase;
		}
		
		
		//
		
		String str = "https://dictionary.yandex.net/api/v1/dicservice/lookup?key=dict.1.1.20140720T192134Z.58f7923ef8528492.1ab60b26472b47d0c03b2b2d1146958c69860d34&lang=ru-ru&text="+word;
		
		
		
		URLConnection conn = new URL(str).openConnection();

		//çàïîëíèì header request ïàðàìåðû, ìîæíî è íå çàïîëíÿòü
		//conn.setRequestProperty("Referer", "http://google.com/http.example.html");
		//conn.setRequestProperty("Cookie", "a=1");
		//ìîæíî óñòàíîâèòü è äðóãèå ïàðàìåðû, òàêèå êàê User-Agent
		String html = "";
		 try
		 {
			StringBuffer b = new StringBuffer();
			InputStreamReader r = new InputStreamReader(conn.getInputStream(), "UTF-8");
			int c;
			while ((c = r.read()) != -1) 
			{
				b.append((char)c);
			}
			//÷èòàåì òî, ÷òî îòäàë íàì ñåðâåð
			html = b.toString();

		 }
		 catch(IOException ex)
		 {
			// System.out.println(ex.toString());
		 }
		// System.out.println(html);
		 
		 SAXParserFactory factory = SAXParserFactory.newInstance(); 
		 SAXParser parser = factory.newSAXParser(); 
		 SAXPars saxp = new SAXPars();
		 
		 
		 InputStream stream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
		  
		 parser.parse(stream, saxp);			 
		 
		 
		 ArrayList<String> res = saxp.getResult();
		 res.add(word);
		 
		 // Пишем в базу, если глагола там нет
		 if(connection != null)
		 {
			String toBase ="";
			for(int i=0; i<res.size(); ++i)
			{
				if(toBase.length()+res.get(i).length() > 1020)
					break;
				toBase+=res.get(i)+";";
			}
			 
			 Statement st = connection.createStatement();			
			
			 String sql ="INSERT INTO \"Verbs\"(syn, verb)  VALUES ('"+toBase+"', '"+word+"');";
						

				st.executeUpdate(sql);
//						
				st.close();
				connection.close();
				
		 }
		 
		 return res;
		 
		
	}
	
	// оценка сходства слов.
	public static double getMark(GramBasics _gb0, GramBasics _gb1)
	{
		// есть подозрение о перезаписи входных данных
		GramBasics gb0 = new GramBasics(_gb0);
		GramBasics gb1 = new GramBasics(_gb1);
		
		if (gb0.verb.equals(gb1.verb))
			return 1.0;	
		
		
		double s0 = gb0.synonims.size();
		double s1 = gb1.synonims.size();
		
		if (s0 == 0 || s1 == 0)
			return 0.0;
		ArrayList<String> synAll = gb0.synonims;
		
		synAll.addAll(gb1.synonims);
		
		
		
		ArrayList<String> sortSynAll = new ArrayList<String>(new HashSet<String>(synAll)); 
		Collections.sort(sortSynAll);	
		double sAll = sortSynAll.size();
						
		double res =  (s0+s1-sAll)/sAll;
		if (res != 0.0)
			return res;
		return res;
	}
}
