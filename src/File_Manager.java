import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class File_Manager {
    private PrintWriter log_file;

    public File_Manager(Integer as) {
        try {
            log_file = new PrintWriter("log-AS" + as + ".txt", "UTF-8");
        } catch (FileNotFoundException e) {
            //DO NOTHING
        } catch (UnsupportedEncodingException e) {
            //DO NOTHING
        }
    }

    public synchronized void writeToFile(String error) {
        log_file.println(error);
    }

    public synchronized void closeFile(){
        log_file.close();
    }
}
