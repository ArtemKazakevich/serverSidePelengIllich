package ext.by.peleng.serverside;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import sun.misc.BASE64Encoder;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.vc.config.LatestConfigSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


//Singletone
public class ConnectionWind implements RemoteAccess {
    static {

    }

    private RemoteMethodServer remotemethodserver;
    private static ConnectionWind connection;

    private ConnectionWind() {
        configSocket();
    }

    //Singletone realisation
    public static ConnectionWind getInstance() {
        if (connection == null) {
            try {
                connection = new ConnectionWind();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return connection;
    }

    //login and password unactive
    //TODO: login and password pass automatically
    private void configSocket() {
        String serverUrl = "https://windchill.peleng.by/Windchill/"; //windchill connection
        URL url = null;

        try {
            url = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String serviceName = "MethodServer";
        remotemethodserver = RemoteMethodServer.getInstance(url, serviceName);

    }

//download


    public List<String> testMethod(String ATR_PO_PART, String ATR_PO_END_PART, String ATR_PO_PARTREFDES, String ATR_PO_VER) throws Exception {
        if ( !ATR_PO_PART.equals(null) & !ATR_PO_END_PART.equals(null) & !ATR_PO_PARTREFDES.equals(null) & ATR_PO_VER.equals(null)) {
            Class[] rmiArgTypes = new Class[4];
            rmiArgTypes[0] = java.lang.String.class;
            rmiArgTypes[1] = java.lang.String.class;
            rmiArgTypes[2] = java.lang.String.class;
            rmiArgTypes[3] = java.lang.String.class;

            Object[] rmiArgs = new Object[4];
            rmiArgs[0] = ATR_PO_PART;
            rmiArgs[1] = ATR_PO_END_PART;
            rmiArgs[2] = ATR_PO_PARTREFDES;
            rmiArgs[3] = ATR_PO_VER;

            Object obj = null;
            obj = remotemethodserver.invoke("getWTDocFileNameListWhichStartsLike","ext.by.peleng.serverside.ServerSide",null,rmiArgTypes,rmiArgs);
            return (List<String>) obj;

        } else return null;
    }
}


