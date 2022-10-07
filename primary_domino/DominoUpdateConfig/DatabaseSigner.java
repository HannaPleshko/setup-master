/* Decompiler 13ms, total 572ms, lines 88 */
package com.ibm.de.tpsmagic;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class DatabaseSigner {
   private final String nsfName;

   public static void main(String[] args) {
      DatabaseSigner ds = new DatabaseSigner(args[0]);
      ds.execute();
   }

   public DatabaseSigner(String nsfName) {
      this.nsfName = nsfName;
      System.out.println("Creating signing request for : " + nsfName);
   }

   public int execute() {
      int result = 0;
      NotesThread.sinitThread();

      try {
         Session s = NotesFactory.createSession();
         String curUser = s.getUserName();
         System.out.println("Running as:" + curUser);
         Database db = s.getDatabase("", "admin4.nsf");
         if (db.isOpen()) {
            Document doc = db.createDocument();
            Name nam = s.createName(s.getEffectiveUserName());
            doc.replaceItemValue("Form", "AdminRequest");
            doc.replaceItemValue("Type", "AdminRequest");
            doc.save(true, false);
            doc.replaceItemValue("ProxyOriginatingRequestUNID", doc.getUniversalID());
            addSignedField(doc, "FullName", s.getEffectiveUserName());
            addSignedField(doc, "ProxyAuthor", s.getEffectiveUserName());
            addSignedField(doc, "ProxyAction", "101");
            addSignedField(doc, "ProxyDatabasePath", this.nsfName);
            addSignedField(doc, "ProxyNameList", "");
            addSignedField(doc, "ProxyOriginatingAuthor", s.getEffectiveUserName());
            addSignedField(doc, "ProxyOriginatingOrganization", nam.getOrganization());
            addSignedField(doc, "ProxyProcess", "Adminp");
            addSignedField(doc, "ProxyServer", s.getEffectiveUserName());
            addSignedField(doc, "ProxyTextItem1", "0");
            DateTime dt = s.createDateTime("Today");
            dt.setNow();
            addSignedField(doc, "ProxyOriginatingTimeDate", dt.getLocalTime());
            doc.sign();
            doc.save(true, false);
         }

         this.shred(db);
         this.shred(s);
      } catch (NotesException var11) {
         result = 1;
         var11.printStackTrace();
      } finally {
         NotesThread.stermThread();
      }

      return result;
   }

   private static void addSignedField(Document doc, String fieldname, String value) throws NotesException {
      doc.replaceItemValue(fieldname, value);
      Item nitem = doc.getFirstItem(fieldname);
      nitem.setSigned(true);
   }

   private void shred(Base moriturus) {
      if (moriturus != null) {
         try {
            moriturus.recycle();
         } catch (NotesException var3) {
         }
      }

   }
}
