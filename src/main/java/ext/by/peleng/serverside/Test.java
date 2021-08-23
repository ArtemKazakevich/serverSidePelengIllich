package ext.by.peleng.serverside;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeIdentifierHelper;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.ContainerSpec;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteAccess;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;

import java.io.*;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import wt.vc.config.*;
import java.beans.PropertyVetoException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Test implements Serializable, RemoteAccess {

    public static void main(String[] args) {

        try {
            System.out.println("1marker1");
            System.out.println(ConnectionWind.getInstance().testMethod("7155.31.20.400", "7199.00.00.000", "DD3", "V1.00"));
            //

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

    }


//    public static List<String> getWTDocFileNameListWhichStartsLike(String aTR_PO_PART, String aTR_PO_END_PART, String aTR_PO_PARTREFDES, String aTR_PO_VER) throws Exception {
//
//        String prim, endPart, refdes, ver;
//        ArrayList<String> lDocs = new ArrayList<String>();
//        LWCNormalizedObject lwc;
//
//        LatestConfigSpec configSpec = new LatestConfigSpec();
//        WTDocument doc = null;
//
//        QuerySpec qs = new QuerySpec(wt.doc.WTDocument.class);
//        int idx = qs.addClassList(wt.doc.WTDocument.class, true);
//
//        com.ptc.core.meta.common.TypeIdentifier identifier = com.ptc.core.meta.common.TypeIdentifierHelper.getTypeIdentifier("by.peleng.PO_KD");
//        qs.appendWhere(wt.type.TypedUtilityServiceHelper.service.getSearchCondition(identifier, true), new int[] {idx });
//
//        configSpec.appendSearchCriteria(qs);
//        QueryResult qr = PersistenceHelper.manager.find( (wt.pds.StatementSpec)qs);
//        System.out.println("WTDocuments found by TypeIdentifier: " + qr.size());
//        qr = configSpec.process(qr);
//        System.out.println("filtered by ConfigSpec found " + qr.size() + " matching documents.");
//
//        while (qr.hasMoreElements()) {
//            doc = (WTDocument) qr.nextElement();
//            lwc = new LWCNormalizedObject( doc, null, null, null);
//            lwc.load( "ATR_PO_PART", "ATR_PO_END_PART", "ATR_PO_PARTREFDES", "ATR_PO_VER");
//
//            prim = (String)lwc.get( "ATR_PO_PART");
//            endPart = (String)lwc.get( "ATR_PO_END_PART");
//            refdes = (String)lwc.get( "ATR_PO_PARTREFDES");
//            ver = (String)lwc.get( "ATR_PO_VER");
//
//            if( prim.equals(aTR_PO_PART) & endPart.equals(aTR_PO_END_PART) & refdes.equals(aTR_PO_PARTREFDES) & ver.equals(aTR_PO_VER)) {
//                lDocs.add( doc.getNumber()); // то что искали !
//            }
//
//        }
//
//        return lDocs;
//
//
//    }


}


