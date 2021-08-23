
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.CreateOperationIdentifier;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.util.WTException;




public class Test {

    public static void getWTDocByAttributes2(String ATR_PO_PART, String ATR_PO_END_PART, String ATR_PO_VER) throws WTException {
        ReferenceFactory rf = new ReferenceFactory();
        WTReference context_ref = (WTReference) rf.getReference("OR:wt.inf.library.WTLibrary:25846571");

        LWCNormalizedObject obj = new LWCNormalizedObject("WCTYPE|wt.part.WTPart|com.ptc.ptcnet.SoftPart", null,
                new CreateOperationIdentifier());
    }

}
