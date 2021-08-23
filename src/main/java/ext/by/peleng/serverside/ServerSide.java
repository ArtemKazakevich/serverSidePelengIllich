package ext.by.peleng.serverside;

import com.ptc.core.htmlcomp.util.SeedObjectsUtilities;
import com.ptc.windchill.uwgm.cadx.caddoc.attributes.AttributeTableRow;
import wt.content.*;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleState;
import wt.method.RemoteAccess;
import wt.pdmlink.PDMLinkProduct;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.queue.entries;
import wt.util.WTException;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import wt.vc.config.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wt.org.WTUser ;
import wt.inf.team.StandardContainerTeamService;
import wt.project.Role;
import wt.inf.team.ContainerTeamManaged;
import wt.inf.team.ContainerTeamHelper;
import java.util.HashSet;
import wt.pdmlink.PDMLinkProduct;

import wt.workflow.notebook.WfFolderedBookmark;


public class ServerSide implements Serializable, RemoteAccess {

 /*
    Map<WTContainer, HashSet<Role>> containersWithSelectedUser = new HashMap<WTContainer, HashSet<Role>>();
    Map.Entry<WTContainer, HashSet<Role>> entries;
        WTContainer container = entries.getKey();
        String role = container.get


*/

    private static List<WTContainer> containers = new ArrayList<WTContainer>();
    private static List<String> containersName = new ArrayList<String>();

    public static String getStringUser() throws WTException {

        return wt.session.SessionHelper.getPrincipal().getName();
    }

    public static String getString() throws WTException {

        return "ServerString";
    }

    public static List<WTContainer> getAllContainersInWindchill(String str) {

        try {
            QuerySpec querySpec = new QuerySpec(WTContainer.class);
            QueryResult qr = PersistenceHelper.manager.find(querySpec);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof WTLibrary) {
                    WTContainer wtContainer = (WTContainer) object;
                    containers.add(wtContainer);
                    containersName.add(wtContainer.getName());
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return containers;
    }

    public static List<WTContainer> containerList() {
        getAllContainersInWindchill("");
        return containers;
    }

    public static List<String> getContainersName() throws WTException {
        QuerySpec querySpec = new QuerySpec(WTContainer.class);
        QueryResult qr = PersistenceHelper.manager.find(querySpec);
        while (qr.hasMoreElements()) {
            Object object = qr.nextElement();
            if (object instanceof WTLibrary) {
                WTContainer wtContainer = (WTContainer) object;
                containersName.add(wtContainer.getName());
            }
        }
        return containersName;
    }

    // 1. Getting the prime folder
    public static WTLibrary getFolder() {
        WTLibrary wtLibrary = null;


        try {
            QuerySpec criteria = new QuerySpec(WTLibrary.class);
            criteria.appendWhere(new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.EQUAL, "Программное обеспечение", false));
            QueryResult results = PersistenceHelper.manager.find(criteria);

            if (results.hasMoreElements()) {
                wtLibrary = (WTLibrary) results.nextElement();
            }

        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return wtLibrary;
    }


    // 2. Getting the subfolder No1
    public static SubFolder getSubFolder(WTLibrary folder) {
        SubFolder subFolder = new SubFolder();

        try {

            QueryResult results = FolderHelper.service.findSubFolders(folder.getDefaultCabinet());

            while (results.hasMoreElements())
            {
                subFolder= (SubFolder) results.nextElement();
                if (subFolder.getName().equals("ПО основных изделий")) break;
            }



        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return subFolder;
    }



    // 3. Getting the further subfolders (use 3 times: 4 digits, iterations, end folder)

    public static SubFolder getSubFolders(String folderName, SubFolder subF) {

        List<WTObject> listSubFolders = new ArrayList<WTObject>();
        SubFolder sub = new SubFolder();

        try {
            QueryResult results = FolderHelper.service.findFolderContents(subF);


            while (results.hasMoreElements()) {
                Object object = results.nextElement();
                if (object instanceof SubFolder) {
                    listSubFolders.add((SubFolder) object);
                }
            }

            for (int i = 0; i<listSubFolders.size(); i++) {
                if (((SubFolder) listSubFolders.get(i)).getName().contains(folderName))
                {
 
                    if (folderName.length() == 14 && ((SubFolder) listSubFolders.get(i)).getName().length() == 17 && ((SubFolder) listSubFolders.get(i)).getName().charAt(14) == '-') i=i;

                    else sub = (SubFolder) listSubFolders.get(i);


                }
            }


        } catch (WTException e) {
            e.printStackTrace();
        }

        return sub;
    }

    // 4. Getting the complete files list

    public static List<String> getFinalFilesList (SubFolder subF) {
        List<WTObject> listWTDoc = new ArrayList<WTObject>();
        List<WTObject> listRefs = new ArrayList<WTObject>();
        List<String> filesNames = new ArrayList<String>();
        try {
            QueryResult results = FolderHelper.service.findFolderContents(subF);

            while (results.hasMoreElements()) {
                Object objectFiles = results.nextElement();
                if (objectFiles instanceof WTDocument) {
                    listWTDoc.add((WTDocument) objectFiles);
                } else if (objectFiles instanceof WfFolderedBookmark) {
                    listRefs.add((WfFolderedBookmark) objectFiles);
                }
            }


            for (int i = 0; i<listRefs.size(); i++) {
                listWTDoc.add((WTDocument)((WfFolderedBookmark) listRefs.get(i)).getObject()); //instead of refcheck
            }

            for (int i = 0; i<listWTDoc.size(); i++) {
                filesNames.add(((WTDocument) listWTDoc.get(i)).getNumber() + "%" + ((WTDocument) listWTDoc.get(i)).getState());
            }


        } catch (WTException e){
            e.printStackTrace();
        }
        return filesNames;
    }






    // Getting the document (последней версии)
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // TODO: доработать этот метод так, чтобы выводились документы со сроком до 90 дней, а не последнюю версию
    public static WTDocument getWTDoc(String wtDocName) {
        WTDocument document = null;

        try {
            QuerySpec criteria = new QuerySpec(WTDocumentMaster.class);
            criteria.appendWhere(new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, wtDocName, false));
            QueryResult results = PersistenceHelper.manager.find(criteria);
            if (results.hasMoreElements()) {
                WTDocumentMaster documentMaster = (WTDocumentMaster)results.nextElement();
                QueryResult qr2 = ConfigHelper.service.filteredIterationsOf(documentMaster, new LatestConfigSpec());
                if(qr2.hasMoreElements()){
                    document = (WTDocument)qr2.nextElement();
                }
            }
        } catch (QueryException var4) {
            var4.printStackTrace();
        } catch (WTException var5) {
            var5.printStackTrace();
        }

        return document;
    }

//
//    public static String getWTDocFileNameAndState(String wtDocName) throws Exception {
//        WTDocument document = null;
//        Object object = null;
//
//        QuerySpec criteria = new QuerySpec(WTDocument.class);
//        criteria.appendWhere(new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, wtDocName, false));
//        QueryResult results = PersistenceHelper.manager.find(criteria);
//
//        if (results.hasMoreElements()) {
//            document = (WTDocument) results.nextElement();
//        }
//
//        ContentHolder content = ContentHelper.service.getContents((ContentHolder) document);
//        QueryResult results2 = ContentHelper.service.getContentsByRole(content, ContentRoleType.PRIMARY);
//
//        while (results2.hasMoreElements()) {
//            object = results2.nextElement();
//        }
//
//        ApplicationData file = (ApplicationData) object;
//        return file.getFileName() +"%" +document.getState();
//    }
//



// forming the list

    public static List<String> getWTDocFileNameListWhichStartsLike(String ispoln, String izd) throws Exception {
        WTLibrary primary = getFolder(); //ПО
        System.out.println(primary);
        SubFolder sec = getSubFolder (primary); // ПООИ
        System.out.println(sec);
        SubFolder tri = getSubFolders(ispoln.substring(0,4), sec); // плата
        System.out.println(tri);
        SubFolder quad = getSubFolders(ispoln, tri); //исполнение
        System.out.println(quad);
        SubFolder fin = getSubFolders(izd, quad); //конечное изделие
        System.out.println(fin);
        List<String>  listDocs = getFinalFilesList(fin);
        System.out.println(listDocs);


        return listDocs;
    }




    public static File donwloadWTDoc2(WTDocument document) {

        File file = null;
        String str = "";
        Object object = null;

        try {
            ContentHolder content = ContentHelper.service.getContents((ContentHolder) document);
            QueryResult results = ContentHelper.service.getContentsByRole(content, ContentRoleType.PRIMARY);

            while (results.hasMoreElements()) {
                object = results.nextElement();
                str=str+object.toString()+"      ";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File saveAsFile= new java.io.File("D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\serverside\\Folder",document.getName()); // input your location and file name
            wt.content.ApplicationData appData = (ApplicationData) object;
            ContentServerHelper.service.writeContentStream((ApplicationData)appData, saveAsFile.getCanonicalPath());
            file = saveAsFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;

    }

    public static ApplicationData getAppData(WTDocument document) {

        File file = null;
        Object object = null;
        ApplicationData applicationData = null;

        try {
            ContentHolder content = ContentHelper.service.getContents((ContentHolder) document);
            QueryResult results = ContentHelper.service.getContentsByRole(content, ContentRoleType.PRIMARY);

            while (results.hasMoreElements()) {
                object = results.nextElement();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File saveAsFile= new java.io.File("D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\serverside\\Folder",document.getName()); // input your location and file name
            applicationData = (ApplicationData) object;
            ContentServerHelper.service.writeContentStream(applicationData, saveAsFile.getCanonicalPath());


            file = saveAsFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return applicationData;

    }

    public static File getWTDocFile(String document) {
        return donwloadWTDoc2(getWTDoc(document));
    }

    public static ApplicationData getAppDataPLZ(String str) {
        return getAppData(getWTDoc(str));
    }

    public static FileOutputStream getFileOutputStream(String doc) {
        try {
            return new FileOutputStream(donwloadWTDoc2(getWTDoc(doc)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Forming the link to download

    public static URL getDownloadableURL(String wtdoc) throws WTException, PropertyVetoException {
        WTDocument document = getWTDoc(wtdoc);
        ContentHolder content = ContentHelper.service.getContents(document); //it was (ContentHolder) document
        ContentItem ad = ((FormatContentHolder)content).getPrimary();
        URL durl = ContentHelper.getDownloadURL(content,(ApplicationData) ad, false);
        return durl;
    }
}