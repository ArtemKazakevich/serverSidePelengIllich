package example;
import wt.method.RemoteMethodServer;
import wt.util.WTException;
import wt.content.ApplicationData;
import java.util.Vector;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.io.Serializable;
import java.io.InputStream;
public class ContentDownload
        implements wt.method.RemoteAccess, Serializable, Runnable
{
    // Vector of worker objects
    private transient Vector workers;

    // Any throwable caught by the download thread
    private transient Throwable downloadException;
    // [client] Add a content stream to be returned by this download operation
    public void addContentStream (ApplicationData app_data)
    {
        addWorker(new ContentDownloadStream(app_data));
    }
    // [client] Add any worker object - subclasses may support more worker types
    protected void addWorker (Object worker)
    {
        if (workers == null)
            workers = new Vector(5);
        workers.addElement(worker);
    }

    // The run method to execute in download thread
    public void run ()
    {
        try
        {
// Invoke execute method on server passing workers vector as argument
            Class arg_types[] = { Vector.class };
            Object args[] = { workers };
            RemoteMethodServer.getDefault().invoke("execute", null, this, arg_types, args);
        }
        catch (InvocationTargetException e)
        {
            downloadException = e.getTargetException();
        }
        catch (Throwable t)
        {
            downloadException = t;
        }
    }
    // [server] Dispatched to here after call arguments are finished being deserialized
    public Vector execute (Vector workers)
            throws WTException
    {
// Just return vector of workers - reserialization does all the work
        return workers;
    }

    // [client] Signal that caller is done processing input streams
    public void done ()
    {
        if (downloadThread != null)
            downloadThread.done();
    }
    // Throw exceptions caught in download thread
    public void checkStatus ()
            throws WTException
    {
        if (downloadException != null)
        {
            if (downloadException instanceof WTException)
                throw (WTException)downloadException;
            else
                throw new WTException(downloadException);
        }
    }
}