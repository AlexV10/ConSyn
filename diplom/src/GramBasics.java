import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class GramBasics 
{
	String verb;
	ArrayList<String> nouns;
	ArrayList<String> synonims;
	ArrayList<String> nouns2;
	
	GramBasics() {
		verb="";
		nouns = new ArrayList<String>();
		nouns2 = new ArrayList<String>();
		synonims = new ArrayList<String>();
	}
	
	GramBasics(GramBasics other)
	{
		this.verb = other.verb;
		this.nouns = new ArrayList<String>(other.nouns);
		this.synonims = new ArrayList<String>(other.synonims);
		this.nouns2 = new ArrayList<String>(other.nouns2);
	}
	
	public static String gramBasicToString(GramBasics gb)
	{
		String res="";
		res+=gb.verb+"\n";
		for(int i=0; i<gb.nouns.size(); ++i)
		{
			res+=gb.nouns.get(i)+"\t";
		}
		res+="\n";
		if (gb.synonims.size() == 0)
			res+="Синонимов нет";
		else
		{
			for(int i=0; i<gb.synonims.size(); ++i)
			{
				res+=gb.synonims.get(i)+"\t";
			}
		}
		res+="\nnouns2\n";
		for(int i=0; i<gb.nouns2.size(); ++i)
		{
			res+=gb.nouns2.get(i)+"\t";
		}
		return res;		
		
	}
	
	
	//возможны глюки
	public static ArrayList<GramBasics> removeRepetitions(ArrayList<GramBasics> gb)
	{
		ArrayList<GramBasics> res = new ArrayList<GramBasics>();
		
		ArrayList<String> Verbs = new ArrayList<String>();
		for(int i=0; i<gb.size(); ++i)
		{
			Verbs.add(gb.get(i).verb);
		}		
		
		ArrayList<String> sortVerbs = new ArrayList<String>(new HashSet<String>(Verbs)); 
		Collections.sort(sortVerbs);
		
		
		for(int i=0; i < sortVerbs.size(); ++i)
		{	
			GramBasics tmp = new GramBasics();
			tmp.verb = sortVerbs.get(i);
			for(int j=0; j<gb.size(); ++j)
			{
				if (tmp.verb.equals(gb.get(j).verb))
				{
					tmp.nouns.addAll(gb.get(j).nouns);
					ArrayList<String> sort = new ArrayList<String>(new HashSet<String>(tmp.nouns));
					Collections.sort(sort);
					tmp.nouns = sort;
					tmp.synonims.addAll(gb.get(j).synonims);
					ArrayList<String> sort2 = new ArrayList<String>(new HashSet<String>(tmp.synonims));
					Collections.sort(sort2);					
					tmp.synonims = sort2;	
					
					tmp.nouns2.addAll(gb.get(j).nouns2);
					ArrayList<String> sort3 = new ArrayList<String>(new HashSet<String>(tmp.nouns2));
					Collections.sort(sort3);
					tmp.nouns2 = sort3;
				}				
			}
			res.add(tmp);
			
		}
		
		
		
		
		return res;
	}
	
}
