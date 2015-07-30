import java.util.ArrayList;


public class Doc
{
	ArrayList<Claster> clasters;
	String _class;
	String name;
	int nouns;
	int nouns2;
	int verbs;
	int words;
	
	Doc()
	{
		_class = "defclass";
		name = "defname";
		clasters = new ArrayList<Claster>();
		nouns=-1;
		nouns2=-1;
		verbs=-1;
		words=-1;
	}
	
	Doc(ArrayList<Claster> data, String _class1, String name1)
	{
		clasters= new ArrayList<Claster>(data);
		_class=_class1;
		name = name1;
		nouns=0;
		nouns2=0;
		verbs=0;
		words=0;
		for(int i=0; i<clasters.size(); ++i)
		{
			verbs+=clasters.get(i).countOfGB;
			for(int j=0; j<clasters.get(i).countOfGB; ++j)
			{
				nouns+=clasters.get(i).gramBasics.get(j).nouns.size();
				nouns2+=clasters.get(i).gramBasics.get(j).nouns2.size();
			}
		}
		words=nouns+nouns2;		
		
	}
}
