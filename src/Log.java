import java.io.File;
import java.nio.file.*;
import java.io.*;

/**
 * @author wspaeth\
 * @version 1.0.0.0
 * Class to define a log file for each player
 * Holds logic for writing lines into file to keep track of players actions
 *
 */
public class Log {
	private File dir = new File("./Logs");
	private File log;
	
	/**
	 * @param logName
	 * @throws IOException
	 * constructor for log file.
	 * checks if old log files exist, deletes them and creates new log file
	 */
	public Log(String logName) throws IOException
	{
		log = new File(dir + "/" + logName + ".txt");
		if(log.exists())
		{
			log.delete();
			log.createNewFile();
		}
		else if(!log.exists())
		{
			log.createNewFile();
		}
	}

	
	/**
	 * @param msg
	 * @return boolean indicating success of write
	 * method to append a single line into this object's log file
	 */
	public boolean writeLine(String msg) 
	{
		msg += '\n';
		try {
		    Files.write(Paths.get(log.getAbsolutePath()), msg.getBytes(), StandardOpenOption.APPEND);
		    return true;
		}
		catch (IOException e) 
		{
			System.out.println("An error occurred while writing to the log files");
			return false;
		}
	}
}
