package example;
import wt.content.ApplicationData;
import wt.content.ContentServerHelper;
import wt.util.WTException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.FilterInputStream;
import java.io.WriteAbortedException;
public class ContentDownloadStream
        implements Serializable
{
    // Buffer size for passing input stream data
    private static final int BUFSIZ = 8192;
    // The content item to download
    private transient ApplicationData appData;
    // Flag indicating that serialization should download input stream
    private transient boolean download = false;
    // [client] Construct download stream object for the given content item
    public ContentDownloadStream (ApplicationData app_data)
    {
        appData = app_data;
    }
    // [both] Called during marshaling on receiving side
    private void readObject (ObjectInputStream input_stream)
            throws IOException, ClassNotFoundException
    {
        boolean download = input_stream.readBoolean();
        if (download) // receiving content in client
        {
// Make stream available to content download thread
            ContentDownloadThread download_thread = (ContentDownloadThread)Thread.currentThread();
            synchronized (download_thread)
            {
                while (!download_thread.isReady())
                {
                    try
                    {
                        download_thread.wait();
                    }
                    catch (InterruptedException e)
                    {
// Ignore
                    }
                }
                // Wrap input stream to protect close calls
                InputStream in = new FilterInputStream(input_stream) {
                    public void close ()
                    {
// Ignore
                    }
                };
                download_thread.setInputStream(in);
                download_thread.notifyAll();
                while (!download_thread.isReady())
                {
                    try
                    {
                        download_thread.wait();
                    }
                    catch (InterruptedException e)
                    {
// Ignore
                    }
                }
            }
        }
        else // receiving content item in server
        {
            appData = (ApplicationData) input_stream.readObject();
// Mark this object for content download on next serialization
            this.download = true;
        }
    }
    // [both] Called during marshaling on the sending side
    private void writeObject (ObjectOutputStream output_stream)
            throws IOException
    {
        output_stream.writeBoolean(download);
        if (download) // sending content from server
        {
            InputStream in = null;
            try
            {
                in = findContentStream(appData);
// Send content
                byte buf[] = new byte[BUFSIZ];
                int count;
                while ((count = in.read(buf, 0, BUFSIZ)) > 0)
                    output_stream.write(buf, 0, count);
            }
            catch (WTException e)
            {
                throw new WriteAbortedException((String)null, e);
            }
            finally
            {
                if (in != null)
                    in.close();
            }
        }
        else // sending content item from client
        {
            output_stream.writeObject(appData);
        }
    }
    // [server] find content stream - use separate method to avoid JIT induced class
// resolution on client. This method does not cause bytecode verification to load
// ContentServerHelper because the method signature exactly matches actual argument types
// so it is verifiably type safe. If this had not been the case, this method could be moved to
// a separate class to prevent unnessary classes from being loaded into the client when
// performing bytecode verification.
    private InputStream findContentStream (ApplicationData app_data)
            throws WTException
    {
        return ContentServerHelper.service.findContentStream(app_data);
    }
}