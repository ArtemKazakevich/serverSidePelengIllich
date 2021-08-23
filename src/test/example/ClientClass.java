package example;

import wt.util.WTException;

import java.io.IOException;
import java.io.InputStream;

public class ClientClass {

    public static void main(String[] args) throws IOException {
        InputStream is;
        ContentDownload cd = new ContentDownload( );
        cd.addContentStream( appData );
        try
        {
            ContentDownloadThread downloadThread = new ContentDownloadThread(this);
            downloadThread.start();
            is = downloadThread.getInputStream();
            byte buf[] = new byte[4096];
            int count;
            int total = 0;
            while ((count = is.read(buf, 0, 4096)) > 0)
                total += count;
            System.out.println("Downloaded " + total + " bytes.");
        }
        finally
        {
            if (is != null)
                is.close();
            downloadThread.done();
            try {
                cd.checkStatus();
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
    }

}
