package ext.by.peleng.serverside;

import com.ptc.core.query.common.QueryException;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Cabinet;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.inf.library.WTLibrary;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;

public class QueryContr {

    //тут получаем библиотеку "Программное обеспечение"
    @SuppressWarnings("deprecation")
    private static WTLibrary getWTLibrary(String libraryName) {
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

    //Тут получаем папки в этой библиотеке
    @SuppressWarnings("deprecation")
    private static ArrayList<SubFolder> getSubFolders(Cabinet cabinet) {
        List<SubFolder> subFolders = new ArrayList<SubFolder>();

        try {
            QueryResult results = FolderHelper.service.findSubFolders(cabinet);

            while (results.hasMoreElements()) {
                subFolders.add((SubFolder) results.nextElement());
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        return (ArrayList<SubFolder>) subFolders;
    }

    public static List<String> getSubFoldersName() {
        List<String> subFoldersName = new ArrayList<String>();
        ArrayList<SubFolder> subFolders = getSubFolders(getWTLibrary("Программное обеспечение").getDefaultCabinet());
        for (int i = 0; i < subFolders.size(); i++) subFoldersName.add(subFolders.get(i).getName());
        return subFoldersName;
    }
}
