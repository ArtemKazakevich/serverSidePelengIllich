package example;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.WriteAbortedException;

import ext.by.iba.e3.rmi.E3Util;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.pom.Transaction;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.wip.WorkInProgressHelper;

public class ContentWTDocument implements Serializable{
    private static final int BUFSIZ = 8192;
    private String oidDoc = null;
    private String fileName = null;
    private String filePath = null;
    private long fileSize = 0;
    private boolean upload = false, download = false;

    private InputStream in;

    public ContentWTDocument (Document e3Document) throws FileNotFoundException {
        this.filePath = e3Document.getFilePath();
        this.fileName = e3Document.getFileName();
        this.oidDoc = e3Document.getOid();
        File file = new File(this.filePath+"\\"+this.fileName);
        this.fileSize=file.length();
        this.in = new FileInputStream(file);
        this.upload=true;
        this.download=false;
    }

    public ContentWTDocument (WTDocument doc, String filePath) throws FileNotFoundException, WTException, PropertyVetoException {
        doc = (WTDocument) ContentHelper.service.getContents(doc);
        ApplicationData theContent=(ApplicationData) doc.getPrimary();
        this.fileName=theContent.getFileName();
        this.fileSize=theContent.getFileSize();
        this.filePath=filePath;
        in = ContentServerHelper.service.findContentStream(theContent);
        this.upload=false;
        this.download=true;
    }

    private void updateWTDocumentContent(InputStream in) throws WTException, FileNotFoundException, PropertyVetoException, IOException {
        String sessionUser = SessionHelper.manager.getPrincipal().getName();
        boolean needCheckIn = false;
        String message = sessionUser+" says hello, "+oidDoc;
        System.out.println("updateWTDocumentContent >> "+message);

        WTDocument doc = null;
        ApplicationData theContent = null;

        Transaction trx = new Transaction();
        try{
            trx.start();
            doc = (WTDocument) E3Util.getObject(oidDoc);
            if(!WorkInProgressHelper.isCheckedOut(doc)){
                doc = (WTDocument) WorkInProgressHelper.service.checkout(doc, WorkInProgressHelper.service.getCheckoutFolder(), "checked out from E3 WGM").getWorkingCopy();
                needCheckIn = true;
            } else if(WorkInProgressHelper.isCheckedOut( doc, SessionHelper.getPrincipal())){
                if(!WorkInProgressHelper.isWorkingCopy(doc)){
                    doc = (WTDocument) WorkInProgressHelper.service.workingCopyOf(doc);
                }
            } else {
                throw new WTException("ОШИБКА: Взят другим пользователем");
            }

            doc = (WTDocument) ContentHelper.service.getContents(doc);
            theContent=(ApplicationData) doc.getPrimary();
            if(theContent==null){
                theContent = ApplicationData.newApplicationData(doc);
            }
            theContent.setFileName(fileName);
            theContent.setUploadedFromPath(filePath);
            theContent.setRole(ContentRoleType.toContentRoleType("PRIMARY")); //if it’s secondary, use “SECONDARY”
            theContent.setFileSize(fileSize);
            theContent = ContentServerHelper.service.updateContent(doc, theContent, in);
            ContentServerHelper.service.updateHolderFormat(doc);

            if(needCheckIn)
                doc = (WTDocument) WorkInProgressHelper.service.checkin(doc, "checked in from E3 WGM");

            trx.commit();
            trx = null;
            System.out.println("Done");
        } finally {
            if (trx != null) {
                trx.rollback();
                System.out.println("ERROR: ROLLBACK updateWTDocumentContent!!!");
            }
        }
    }

    private void downloadWTDocumentContent(ObjectInputStream input_stream) throws IOException {
        File downloadFile;
        FileOutputStream output_stream = null;
        try{

            File folderPath = new File(filePath);
            if(!folderPath.canRead()){
                folderPath.mkdirs();
            }

            downloadFile = new File(filePath+"\\"+fileName);
            output_stream = new FileOutputStream(downloadFile);
            if (!downloadFile.exists()) {
                downloadFile.createNewFile();
            }

            byte buf[] = new byte[BUFSIZ];
            int count;
            int total = 0;
            while ((count = input_stream.read(buf, 0, BUFSIZ))>0){
                output_stream.write(buf,0,count);
                total += count;
                System.out.println("filesize "+total);
            }
            System.out.println("Downloaded " + total + " bytes.");
            output_stream.flush();
            output_stream.close();
        } finally {
            if(output_stream!=null){
                output_stream.close();
            }
        }
    }

    private void writeObject(ObjectOutputStream output_stream) throws IOException {
        System.out.println("ContentWTDocument.writeObject");
        System.out.println("writeBoolean upload >> "+upload);
        output_stream.writeBoolean(upload);
        System.out.println("writeBoolean download >> "+download);
        output_stream.writeBoolean(download);
        System.out.println("writeObject >> "+oidDoc);
        output_stream.writeObject(oidDoc);
        System.out.println("writeObject >> "+fileName);
        output_stream.writeObject(fileName);
        System.out.println("writeObject >> "+fileSize);
        output_stream.writeLong(fileSize);
        if(download){
            System.out.println("writeObject >> "+filePath);
            output_stream.writeObject(filePath);
        }
        if(upload||download){
            System.out.println("writeObject >> in...");
            output_stream.flush();
            byte buf[] = new byte[BUFSIZ];
            int count;
            int total = 0;
            while ((count = in.read(buf, 0, BUFSIZ))>0){
                output_stream.write(buf,0,count);
                total += count;
                System.out.println("filesize "+total);
            }
            in.close();
        }
    }

    private void readObject(ObjectInputStream input_stream) throws ClassNotFoundException, IOException {
        System.out.println("ContentWTDocument.readObject");
        upload = input_stream.readBoolean();
        System.out.println("readBoolean upload >> "+upload);
        download = input_stream.readBoolean();
        System.out.println("readBoolean upload >> "+download);
        oidDoc = (String) input_stream.readObject();
        System.out.println("readObject >> "+oidDoc);
        fileName = (String) input_stream.readObject();
        System.out.println("readObject >> "+fileName);
        fileSize = input_stream.readLong();
        System.out.println("readObject >> "+fileSize);
        if(upload){
            try {
                updateWTDocumentContent(input_stream);
            } catch(Exception e) {
                while(input_stream.read()>=0)
                    ;
                throw new WriteAbortedException((String)null, e);
            }
        }
        if(download){
            filePath = (String) input_stream.readObject();
            System.out.println("readObject >> "+filePath);
            try {
                downloadWTDocumentContent(input_stream);
            } catch(Exception e) {
                while(input_stream.read()>=0)
                    ;
                throw new WriteAbortedException((String)null, e);
            }
        }
        System.out.println("readObject >> in... ");
    }

    public String getOidDoc() {
        return oidDoc;
    }
}
