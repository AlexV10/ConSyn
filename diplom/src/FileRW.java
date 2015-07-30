import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class FileRW 
{
	public static String fileRead(String fileName){
        String result = "";
        BufferedReader input = null;

        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            String tmp;
            while ((tmp = input.readLine()) != null){
                result += tmp;
                result += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }
	
	public static void writeFileOver(String fileName, String content)
	{
		FileWriter writeFile = null;
		try {
		    File logFile = new File(fileName);
		    writeFile = new FileWriter(logFile, true);
		    writeFile.write(content);
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    if(writeFile != null) {
		        try {
		            writeFile.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}
	}
	
	public static void writeFile(String fileName, String content)
	{
		FileWriter writeFile = null;
		try {
		    File logFile = new File(fileName);
		    writeFile = new FileWriter(logFile, false);
		    writeFile.write(content);
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    if(writeFile != null) {
		        try {
		            writeFile.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}
	}
	
    
}

