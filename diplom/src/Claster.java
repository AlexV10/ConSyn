import java.util.ArrayList;

public class Claster 
{
	//long id;
	int countOfGB;
	ArrayList<GramBasics> gramBasics;
	//ArrayList<ArrayList<Double>> marks;	
	
	// конструкторы
	Claster()
	{
		//id =0;
		countOfGB = -1;
		gramBasics = new ArrayList<GramBasics>();
		//marks = new ArrayList<ArrayList<Double>>();				
	}
	
	Claster(ArrayList<GramBasics> gramBasics)
	{
		//this.id = id;
		this.gramBasics = gramBasics;
		//this.marks = marks;
		this.countOfGB = gramBasics.size();
	}
	
	Claster(Claster other)
	{
		this.countOfGB = other.countOfGB;
		this.gramBasics = new ArrayList<GramBasics>(other.gramBasics);
	}
		
	
	
	
}
