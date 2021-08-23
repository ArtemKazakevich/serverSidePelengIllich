package example;
import java.io.InputStream;
public class ContentDownloadThread extends Thread
{
    // Flag indicating that thread is ready to receive a new input stream
    private boolean ready = false;
    // Flag indicating that caller is done accessing input streams
    private boolean done = false;
    // Current input stream being downloaded
    private InputStream inputStream;
    // Constructor takes a runnable
    public ContentDownloadThread (Runnable target)
    {
        super(target);
    }
    // Check ready flag
    public boolean isReady ()
    {
        return ready || done;
    }
    // Set current input stream
    public void setInputStream (InputStream input_stream)
    {
        inputStream = input_stream;
        ready = false;
    }
    // Get current input stream - waits for next stream to become available
    public synchronized InputStream getInputStream ()
    {
        ready = true;
        notifyAll();
        while (ready)
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
// Ignore
            }
        }
        return inputStream;
    }
    // Indicate that input stream processing is complete
    public synchronized void done ()
    {
        done = true;
        notifyAll();
    }
}