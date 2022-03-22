<%@ page import="wt.util.WTException" %>
<%@ page import="wt.maturity.PromotionNotice" %>
<%@ page import="wt.team.Team" %>
<%@ page import="wt.project.Role" %>
<%@ page import="wt.org.WTGroup" %>
<%@ page import="wt.org.WTPrincipalReference" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="wt.folder.SubFolder" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.folder.FolderHelper" %>
<%@ page import="wt.fc.*" %>
<%@ page import="java.util.*" %>
<%@ page import="wt.doc.WTDocument" %>
<%@ page import="wt.workflow.notebook.WfFolderedBookmark" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>

<%
    String param_1 = request.getParameter("param_1"); // 7084.00.00.000-06.02
    String param_2 = request.getParameter("param_2"); // 7084.33.06.400

    List<String> files = getWTDocFileNameListWhichStartsLike(param_1, param_2);

    for (String s : files) {
%>

<p><%=s%>
</p>
<%
    }
%>


<%!
    public static List<String> getWTDocFileNameListWhichStartsLike(String ispoln, String izd) throws Exception {
        WTLibrary primary = getFolder(); //ПО
        System.out.println(primary);
        SubFolder sec = getSubFolder(primary); // ПООИ
        System.out.println(sec);
        SubFolder tri = getSubFolders(ispoln.substring(0, 4), sec); // плата
        System.out.println(tri);
        SubFolder quad = getSubFolders(ispoln, tri); //исполнение
        System.out.println(quad);
        SubFolder fin = getSubFolders(izd, quad); //конечное изделие
        System.out.println(fin);
        List<String> listDocs = getFinalFilesList(fin);
        System.out.println(listDocs);


        return listDocs;
    }
%>

<%!
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
%>

<%!
    public static SubFolder getSubFolder(WTLibrary folder) {
        SubFolder subFolder = new SubFolder();

        try {

            QueryResult results = FolderHelper.service.findSubFolders(folder.getDefaultCabinet());

            while (results.hasMoreElements()) {
                subFolder = (SubFolder) results.nextElement();
                if (subFolder.getName().equals("ПО основных изделий")) break;
            }


        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return subFolder;
    }
%>

<%!
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

            Collections.sort(listSubFolders, new Comparator<WTObject>() {
                @Override
                public int compare(WTObject o1, WTObject o2) {
                    return ((SubFolder) o1).getName().compareTo(((SubFolder) o2).getName());
                }
            });

            for (int i = 0; i<listSubFolders.size(); i++) {
                if (((SubFolder) listSubFolders.get(i)).getName().contains(folderName)) {

                    System.out.println("***-/-***");
                    System.out.println(((SubFolder) listSubFolders.get(i)).getName());
                    System.out.println("***-/-***");

                    if (folderName.length() == 14 && ((SubFolder) listSubFolders.get(i)).getName().length() == 17 && ((SubFolder) listSubFolders.get(i)).getName().charAt(14) == '-') {
                        i=i;
                    } else {
                        sub = (SubFolder) listSubFolders.get(i);
                        break;
                    }

                }
            }

        } catch (WTException e) {
            e.printStackTrace();
        }

        return sub;
    }%>

<%!
    public static List<String> getFinalFilesList(SubFolder subF) {
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


            for (int i = 0; i < listRefs.size(); i++) {
                listWTDoc.add((WTDocument) ((WfFolderedBookmark) listRefs.get(i)).getObject()); //instead of refcheck
            }

            for (int i = 0; i < listWTDoc.size(); i++) {
                filesNames.add(((WTDocument) listWTDoc.get(i)).getNumber() + "%" + ((WTDocument) listWTDoc.get(i)).getState());
            }


        } catch (WTException e) {
            e.printStackTrace();
        }
        return filesNames;
    }
%>

</body>
</html>