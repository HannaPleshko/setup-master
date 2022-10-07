/* Decompiler 119ms, total 474ms, lines 978 */
package com.ibm.de.tpsmagic;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.function.Function;
import lotus.domino.ACL;
import lotus.domino.ACLEntry;
import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Registration;
import lotus.domino.Session;
import lotus.domino.View;

public class DominoConfigUpdate {
   private Boolean debug = false;

   public static String help() {
      System.out.println("(c) Thomas Hampel, thampel@thomashampel.com");
      return "Usage: java -jar DominoUpdateConfig.jar [PathToJSON]";
   }

   public static void main(String[] argv) throws FileNotFoundException {
      DominoConfigUpdate dcfg = null;
      boolean success = true;

      try {
         if (argv.length < 1) {
            System.out.println(help());
            success = false;
         } else {
            System.out.println("Domino Configuration Utility v1.2");
            System.out.println("(c) Thomas Hampel, thomas.hampel@thomashampel.com");
            System.out.println("\tBuilt with friendly support from #dominoforever community.");
            System.out.println("\t");
            String paramFileName = argv[0];
            File paramFile = new File(paramFileName);
            if (!paramFile.exists()) {
               System.err.println("No such file: " + paramFileName);
               success = false;
            } else {
               CharSource source = Files.asCharSource(paramFile, Charsets.UTF_8);
               String jsonString = source.read();
               if (!"".equals(jsonString)) {
                  dcfg = new DominoConfigUpdate();
                  dcfg.run(jsonString);
               }
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         success = false;
      }

      if (!success) {
         System.exit(1);
      }

   }

   public void run(String jsonString) throws NotesException {
      NotesThread.sinitThread();
      Session s = null;

      try {
         s = NotesFactory.createSession();
         JsonElement jElement = (new JsonParser()).parse(jsonString);
         JsonObject jObject = jElement.getAsJsonObject();
         String title = jObject.get("title").getAsString();
         System.out.println("Title: " + title);
         JsonElement debugFlagJson = jObject.get("debug");
         boolean debug = debugFlagJson != null ? debugFlagJson.getAsBoolean() : false;
         JsonObject notesIniObject = jObject.get("notesini").getAsJsonObject();
         if (notesIniObject != null) {
            this.jsonToNotesIni(s, notesIniObject);
         }

         JsonElement databasesObject = jObject.get("database");
         if (databasesObject != null) {
            if (databasesObject.isJsonArray()) {
               this.jsonToDatabases(s, databasesObject.getAsJsonArray());
            } else {
               System.out.println("'database' is not a Json array");
            }
         }

         JsonElement registrationObject = jObject.get("registration");
         JsonElement jCommand;
         if (registrationObject != null) {
            JsonObject certifierPasswordObject = registrationObject.getAsJsonObject();
            jCommand = certifierPasswordObject.get("certifierPassword");
            String certifierPassword = jCommand != null ? jCommand.getAsString() : "";
            JsonElement userObject = jObject.get("users");
            if (userObject != null) {
               if (userObject.isJsonArray()) {
                  this.jsonToUsers(s, certifierPassword, userObject.getAsJsonArray());
               } else {
                  System.out.println("'registration.users' is not a Json array");
               }
            }
         }

         JsonElement testUserObject = jObject.get("testusers");
         if (testUserObject != null) {
            this.jsonToTestUsers(s, testUserObject.getAsJsonObject());
            if (debug) {
               System.out.println("\tDebug: testUserObject completed");
            }
         } else if (debug) {
            System.out.println("\tDebug: testUserObject is null");
         }

         jCommand = jObject.get("commands");
         JsonElement certificatesObject = jObject.get("certificates");
         if (certificatesObject != null) {
            if (certificatesObject.isJsonArray()) {
               this.jsonToCertificates(s, certificatesObject.getAsJsonArray());
            } else {
               System.out.println("'Certificates' is not a Json array");
            }
         }

         System.out.println("Configuration update completed.");
      } catch (Exception var18) {
         var18.printStackTrace();
      } finally {
         NotesThread.stermThread();
      }

   }

   private Database getDatabase(Session s, String fileName, boolean createIfMissing, String fromTemplate) throws NotesException {
      Database db = null;

      try {
         db = this.getDataBaseFromHost(s, s.getEffectiveUserName(), fileName, createIfMissing, fromTemplate);
         if (db == null) {
            if (this.debug) {
               System.out.println("\tDebug: trying to open database on local " + fileName);
            }

            db = this.getDataBaseFromHost(s, (String)null, fileName, createIfMissing, fromTemplate);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return db;
   }

   private Database getDataBaseFromHost(Session s, String host, String fileName, boolean createIfMissing, String fromTemplate) {
      Database db = null;
      DbDirectory dbDir = null;

      try {
         dbDir = s.getDbDirectory(host);
      } catch (Exception var10) {
         System.out.println("Can't open Server" + host + " (" + var10.getMessage() + ")");
         return null;
      }

      if (dbDir == null) {
         System.out.println("dbDir = null");
         return null;
      } else {
         try {
            db = dbDir.openDatabase(fileName);
         } catch (Exception var9) {
            System.err.println("Can't open DB:" + fileName + " (" + var9.getMessage() + ")");
         }

         try {
            if (db != null && db.isOpen()) {
               if (this.debug) {
                  System.out.println("\tDEBUG: using existing file : " + fileName);
               }
            } else if (createIfMissing) {
               System.out.println("\tCreating new database : " + fileName);
               Database template = dbDir.openDatabase(fromTemplate);
               if (template.isOpen()) {
                  System.out.println("\t\tCreating database from template : " + fromTemplate);
                  db = template.createFromTemplate((String)null, fileName, true);
                  if (db != null) {
                     System.out.println("\t\tDatabase created : " + db.getFilePath());
                  } else if (this.debug) {
                     System.out.println("\tDEBUG: Database NOT created : " + fileName);
                  }
               } else {
                  System.out.println("\t\tUnable to open template : " + fromTemplate);
               }
            } else if (this.debug) {
               System.out.println("\t\tDEBUG: file could not be opened : " + fileName);
            }
         } catch (Exception var11) {
            System.err.println("Can't open DB:" + fileName + " (" + var11.getMessage() + ")");
         }

         return db;
      }
   }

   private void jsonAclToDatabase(JsonArray aclsObject, Database db) {
      try {
         ACL dbAcl = db.getACL();
         aclsObject.forEach((aclElement) -> {
            if (aclElement.isJsonObject()) {
               JsonObject aclObject = aclElement.getAsJsonObject();

               try {
                  if (aclObject.has("name") && aclObject.has("level")) {
                     String aclName = aclObject.get("name").getAsString();
                     int aclLevel = aclObject.get("level").getAsInt();
                     ACLEntry entry = dbAcl.getEntry(aclName);
                     if (entry == null) {
                        entry = dbAcl.createACLEntry(aclName, aclLevel);
                     }

                     if (entry != null) {
                        if (aclObject.has("type")) {
                           int aclType = aclObject.get("type").getAsInt();
                           entry.setUserType(aclType);
                        }

                        String[] aclRolesArray;
                        String aclRoleSingle;
                        int var9;
                        int var10;
                        String[] var11;
                        String aclRoles;
                        if (aclObject.has("flags")) {
                           aclRoles = aclObject.get("flags").getAsString();
                           aclRolesArray = aclRoles.split(",");
                           var11 = aclRolesArray;
                           var10 = aclRolesArray.length;

                           for(var9 = 0; var9 < var10; ++var9) {
                              aclRoleSingle = var11[var9];
                              entry.setCanCreateDocuments(aclRoleSingle.toLowerCase() == "createdocuments");
                              entry.setCanDeleteDocuments(aclRoleSingle.toLowerCase() == "deletedocuments");
                              entry.setCanCreatePersonalAgent(aclRoleSingle.toLowerCase() == "createpersonalagent");
                              entry.setCanCreatePersonalFolder(aclRoleSingle.toLowerCase() == "createpersonalfolder");
                              entry.setCanCreateSharedFolder(aclRoleSingle.toLowerCase() == "createsharedfolder");
                              entry.setCanCreateLSOrJavaAgent(aclRoleSingle.toLowerCase() == "createlsorjavaagent");
                              entry.setPublicReader(aclRoleSingle.toLowerCase() == "publicreader");
                              entry.setPublicWriter(aclRoleSingle.toLowerCase() == "publicwriter");
                              entry.setCanReplicateOrCopyDocuments(aclRoleSingle.toLowerCase() == "replicateorcopydocuments");
                           }
                        }

                        if (aclObject.has("roles")) {
                           aclRoles = aclObject.get("roles").getAsString();
                           aclRolesArray = aclRoles.split(",");
                           var11 = aclRolesArray;
                           var10 = aclRolesArray.length;

                           for(var9 = 0; var9 < var10; ++var9) {
                              aclRoleSingle = var11[var9];
                              entry.enableRole(aclRoleSingle);
                           }
                        }

                        dbAcl.save();
                     } else {
                        System.err.println("\t\tCan not create or update ACL entry " + aclName);
                     }
                  } else {
                     System.out.println("\t\tSkipping ACL entry due to missing json definition (name or level)");
                  }
               } catch (Exception var12) {
                  var12.printStackTrace();
               }
            }

         });
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   private void jsonAgentsToDatabase(Session s, JsonArray propertiesObject, Database db) {
      propertiesObject.forEach((element) -> {
         if (element.isJsonObject()) {
            try {
               this.jsonAgentToDatabase(s, element.getAsJsonObject(), db);
            } catch (NotesException var5) {
               var5.printStackTrace();
            }
         }

      });
   }

   private void jsonAgentToDatabase(Session s, JsonObject agentObject, Database db) throws NotesException {
      if (agentObject.has("name")) {
         JsonElement agentNameJson = agentObject.get("name");
         JsonElement agentActionJson = agentObject.get("action");
         String agentAction = agentActionJson == null ? null : agentActionJson.getAsString();
         Agent agent = db.getAgent(agentNameJson.getAsString());
         if (agent != null && agentAction != null) {
            switch(agentAction.hashCode()) {
            case -1298848381:
               if (agentAction.equals("enable")) {
                  agent.setEnabled(true);
                  agent.save();
               }
               break;
            case 113291:
               if (agentAction.equals("run")) {
                  agent.run();
               }
               break;
            case 3530173:
               if (agentAction.equals("sign")) {
                  agent.save();
               }
               break;
            case 945953133:
               if (agentAction.equals("runonserver")) {
                  agent.runOnServer();
               }
            }
         }
      } else {
         System.err.println("\tAgent : JSON element 'name' missing");
      }

   }

   private void jsonPropertiesToDatabase(Session s, JsonArray propertiesObject, Database db) {
      propertiesObject.forEach((element) -> {
         if (element.isJsonObject()) {
            this.jsonPropertyToDatabase(s, element.getAsJsonObject(), db);
         }

      });
   }

   private void jsonPropertyToDatabase(Session s, JsonObject propertyObject, Database db) {
      propertyObject.entrySet().forEach((entry) -> {
         JsonElement propertyValue = (JsonElement)entry.getValue();
         Boolean bValue = propertyValue.getAsBoolean();

         try {
            String propertyName = ((String)entry.getKey()).toUpperCase();
            switch(propertyName.hashCode()) {
            case -1893376848:
               if (propertyName.equals("NOUNREAD")) {
                  db.setOption(37, bValue);
               }
               break;
            case -1617637504:
               if (propertyName.equals("OUTOFOFFICEENABLED")) {
                  db.setOption(74, bValue);
               }
               break;
            case -898196343:
               if (propertyName.equals("RESPONSETHREADHISTORY")) {
                  db.setOption(75, bValue);
               }
               break;
            case -641703397:
               if (propertyName.equals("NOSIMPLESEARCH")) {
                  db.setOption(76, bValue);
               }
               break;
            case -410141039:
               if (propertyName.equals("REPLICATEUNREADMARKSTOCLUSTER")) {
                  db.setOption(70, bValue);
               }
               break;
            case -258849026:
               if (propertyName.equals("REPLICATEUNREADMARKSNEVER")) {
                  db.setOption(32001, bValue);
               }
               break;
            case -253029885:
               if (propertyName.equals("REPLICATEUNREADMARKSTOANY")) {
                  db.setOption(71, bValue);
               }
               break;
            case 75875:
               if (propertyName.equals("LZ1")) {
                  db.setOption(65, bValue);
               }
               break;
            case 10615362:
               if (propertyName.equals("NOTRANSACTIONLOGGING")) {
                  db.setOption(45, bValue);
               }
               break;
            case 231811748:
               if (propertyName.equals("MAINTAINLASTACCESSED")) {
                  db.setOption(44, bValue);
               }
               break;
            case 301793184:
               if (propertyName.equals("COMPRESSDESIGN")) {
                  db.setOption(32002, bValue);
               }
               break;
            case 570434824:
               if (propertyName.equals("USEDAOS")) {
                  db.setOption(81, bValue);
               }
               break;
            case 805641813:
               if (propertyName.equals("SOFTDELETE")) {
                  db.setOption(49, bValue);
               }
               break;
            case 973565642:
               if (propertyName.equals("NOOVERWRITE")) {
                  db.setOption(36, bValue);
               }
               break;
            case 1056707150:
               if (propertyName.equals("MOREFIELDS")) {
                  db.setOption(54, bValue);
               }
               break;
            case 1217886573:
               if (propertyName.equals("OPTIMIZATION")) {
                  db.setOption(41, bValue);
               }
               break;
            case 1274267824:
               if (propertyName.equals("NORESPONSEINFO")) {
                  db.setOption(38, bValue);
               }
               break;
            case 1493696248:
               if (propertyName.equals("LZCOMPRESSION")) {
                  db.setOption(65, bValue);
               }
               break;
            case 1677307886:
               if (propertyName.equals("NOHEADLINEMONITORS")) {
                  db.setOption(46, bValue);
               }
            }
         } catch (NotesException var6) {
            var6.printStackTrace();
         }

      });
   }

   private void jsonDocumentsToDatabase(Session s, JsonArray documentsObject, Database db) throws NotesException {
      int max = documentsObject.size();

      for(int i = 0; i < max; ++i) {
         JsonElement element = documentsObject.get(i);
         if (element.isJsonObject()) {
            this.jsonDocumentToDatabase(s, element.getAsJsonObject(), db);
         }
      }

   }

   private void jsonDocumentToDatabase(Session s, JsonObject documentObject, Database db) {
      Document doc = null;
      View vServers = null;

      try {
         JsonElement createDocumentJson = documentObject.get("create");
         JsonElement docTypeJson = documentObject.get("type");
         boolean createDocument = createDocumentJson != null ? createDocumentJson.getAsBoolean() : false;
         String docType = docTypeJson == null ? null : docTypeJson.getAsString();
         JsonElement computeWithFormJson;
         if (docType != null) {
            if (docType.compareToIgnoreCase("server") == 0) {
               vServers = db.getView("$Servers");
               if (vServers != null) {
                  doc = vServers.getDocumentByKey(s.getUserName());
                  if (doc != null && this.debug) {
                     System.out.println("\t\tDEBUG: Updating server document for : " + s.getUserName());
                  }
               }
            } else if (docType.compareToIgnoreCase("config") == 0) {
               vServers = db.getView("$Configuration");
               doc = vServers.getDocumentByKey(s.getUserName());
               if (doc != null && this.debug) {
                  System.out.println("\t\tDEBUG: Updating server configuration document for : " + s.getUserName());
               }
            } else if (docType.compareToIgnoreCase("group") == 0) {
               vServers = db.getView("$Groups");
               computeWithFormJson = documentObject.get("name");
               String groupName = computeWithFormJson == null ? null : computeWithFormJson.getAsString();
               if (groupName != null) {
                  doc = vServers.getDocumentByKey(groupName);
                  if (doc != null && this.debug) {
                     System.out.println("\t\tDEBUG: Updating group document : " + s.getUserName());
                  }
               }
            } else {
               System.err.println("\t\tUnknown document type or type not specified");
            }
         }

         if (doc == null && createDocument) {
            doc = db.createDocument();
         }

         if (doc != null) {
            if (documentObject.has("fields")) {
               System.out.println("\tField settings...");
               computeWithFormJson = documentObject.get("fields");
               if (computeWithFormJson != null && computeWithFormJson.isJsonArray()) {
                  this.updateDocument(doc, computeWithFormJson.getAsJsonArray());
               }
            }

            if (documentObject.has("computewithform")) {
               System.out.println("\tCompute with form");
               computeWithFormJson = documentObject.get("computewithform");
               boolean computeWithForm = computeWithFormJson != null ? computeWithFormJson.getAsBoolean() : false;
               if (computeWithForm) {
                  doc.computeWithForm(true, false);
               }
            }

            doc.save(true, false);
         }
      } catch (Exception var15) {
         var15.printStackTrace();
      } finally {
         Utils.shred(doc);
         Utils.shred(vServers);
      }

   }

   private void jsonToCertificates(Session s, JsonArray directoriesArray) {
   }

   private void jsonToRegistration(Session s, JsonArray registrationArray) {
      registrationArray.forEach((jsonElement) -> {
         jsonElement.isJsonObject();
      });

      try {
         Registration reg = s.createRegistration();
         reg.setRegistrationServer(s.getEffectiveUserName());
         reg.setCreateMailDb(false);
         reg.setCertifierIDFile("/local/notesdata/cert.id");
         reg.setRegistrationLog((String)null);
         DateTime dt = s.createDateTime("Today");
         dt.setNow();
         dt.adjustYear(1);
         reg.setExpiration(dt);
         reg.setIDType(172);
         reg.setMinPasswordLength(5);
         reg.setNorthAmerican(true);
         reg.setUpdateAddressBook(true);
         reg.setSynchInternetPassword(true);
      } catch (NotesException var5) {
         var5.printStackTrace();
      }

   }

   private boolean regCrossCertifyID(Session s, String idFile, String certifierIDFile, String certifierPassword) {
      try {
         Registration reg = s.createRegistration();
         reg.setRegistrationServer(s.getEffectiveUserName());
         reg.setCertifierIDFile(certifierIDFile);
         DateTime dt = s.createDateTime("Today");
         dt.setNow();
         dt.adjustYear(1);
         reg.setExpiration(dt);
         if (reg.crossCertify(idFile, certifierPassword, "")) {
            System.out.println("Recertification succeeded");
         } else {
            System.out.println("Recertification failed");
         }
      } catch (NotesException var7) {
         System.out.println(var7.id + " " + var7.text);
         var7.printStackTrace();
      }

      return true;
   }

   private boolean simpleProcess(JsonObject jObject, String paramName, Function<JsonElement, Boolean> function) {
      if (jObject.has(paramName)) {
         JsonElement jElement = jObject.get(paramName);
         if (jElement != null) {
            return (Boolean)function.apply(jObject);
         }
      }

      return false;
   }

   private void jsonToUser(Session s, JsonObject jsonDirectory) {
   }

   private void jsonToDatabase(Session s, JsonObject jsonDirectory) {
      Database db = null;

      try {
         JsonElement createFileJson = jsonDirectory.get("create");
         boolean createFile = createFileJson != null ? createFileJson.getAsBoolean() : false;
         if (!jsonDirectory.has("filename")) {
            System.err.println("\tJSON element 'filename' missing");
         } else {
            JsonElement createFromTemplateJson = jsonDirectory.get("template");
            String templateName = createFromTemplateJson != null ? createFromTemplateJson.getAsString() : null;
            JsonElement fileNameObject = jsonDirectory.get("filename");
            if (createFile) {
               System.out.println("Creating new database : " + fileNameObject.getAsString());
               db = this.getDatabase(s, fileNameObject.getAsString(), true, templateName);
            } else {
               System.out.println("Processing existing database : " + fileNameObject.getAsString());
               db = this.getDatabase(s, fileNameObject.getAsString(), false, "");
            }

            if (db == null) {
               System.out.println("DB is null ?!?!");
            }

            if (db != null && db.isOpen()) {
               if (jsonDirectory.has("title")) {
                  System.out.println("\tDatabase title...");
                  String dbTitle = jsonDirectory.get("title").getAsString();
                  if (dbTitle != null) {
                     db.setTitle(dbTitle);
                  }
               }

               JsonElement jAgents;
               if (jsonDirectory.has("acl")) {
                  System.out.println("\tACL settings...");
                  jAgents = jsonDirectory.get("acl");
                  if (jAgents != null && jAgents.isJsonArray()) {
                     this.jsonAclToDatabase(jAgents.getAsJsonArray(), db);
                  }
               }

               if (jsonDirectory.has("properties")) {
                  System.out.println("\tDatabase properties...");
                  jAgents = jsonDirectory.get("properties");
                  if (jAgents != null) {
                     jAgents.isJsonArray();
                  }
               }

               if (jsonDirectory.has("documents")) {
                  System.out.println("\tDocuments...");
                  jAgents = jsonDirectory.get("documents");
                  if (jAgents != null && jAgents.isJsonArray()) {
                     this.jsonDocumentsToDatabase(s, jAgents.getAsJsonArray(), db);
                  }
               }

               if (jsonDirectory.has("properties")) {
                  System.out.println("\tDatabase properties...");
                  jAgents = jsonDirectory.get("properties");
                  if (jAgents != null && jAgents.isJsonArray()) {
                     this.jsonPropertiesToDatabase(s, jAgents.getAsJsonArray(), db);
                  }
               }

               if (jsonDirectory.has("agents")) {
                  System.out.println("\tAgents...");
                  jAgents = jsonDirectory.get("agents");
                  if (jAgents != null && jAgents.isJsonArray()) {
                     this.jsonAgentsToDatabase(s, jAgents.getAsJsonArray(), db);
                  }
               }

               if (jsonDirectory.has("signwithadminp") && jsonDirectory.get("signwithadminp").getAsBoolean()) {
                  System.out.println("\tAdminP Signing request");
                  this.signWithAdminP(s, fileNameObject.getAsString());
               }
            } else if (this.debug) {
               System.err.println("\t\tUnable to open or create : " + fileNameObject.getAsString());
            }
         }
      } catch (NotesException var13) {
         var13.printStackTrace();
      } finally {
         Utils.shred(db);
      }

   }

   private void addSignedField(Document doc, String fieldname, String value) throws NotesException {
      doc.replaceItemValue(fieldname, value);
      Item nitem = doc.getFirstItem(fieldname);
      nitem.setSigned(true);
   }

   private boolean signWithAdminP(Session s, String pathFileName) {
      boolean result = false;

      try {
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
            this.addSignedField(doc, "FullName", s.getEffectiveUserName());
            this.addSignedField(doc, "ProxyAuthor", s.getEffectiveUserName());
            this.addSignedField(doc, "ProxyAction", "101");
            this.addSignedField(doc, "ProxyDatabasePath", pathFileName);
            this.addSignedField(doc, "ProxyNameList", "");
            this.addSignedField(doc, "ProxyOriginatingAuthor", s.getEffectiveUserName());
            this.addSignedField(doc, "ProxyOriginatingOrganization", nam.getOrganization());
            this.addSignedField(doc, "ProxyProcess", "Adminp");
            this.addSignedField(doc, "ProxyServer", s.getEffectiveUserName());
            this.addSignedField(doc, "ProxyTextItem1", "0");
            DateTime dt = s.createDateTime("Today");
            dt.setNow();
            this.addSignedField(doc, "ProxyOriginatingTimeDate", dt.getLocalTime());
            doc.sign();
            doc.save(true, false);
            result = true;
         }
      } catch (NotesException var9) {
         result = false;
         var9.printStackTrace();
      }

      return result;
   }

   private void jsonToDatabases(Session s, JsonArray directoriesArray) {
      directoriesArray.forEach((jsonElement) -> {
         if (jsonElement.isJsonObject()) {
            this.jsonToDatabase(s, jsonElement.getAsJsonObject());
         }

      });
   }

   private void jsonToUsers(Session s, String certifierPassword, JsonArray usersArray) {
      usersArray.forEach((jsonElement) -> {
         if (jsonElement.isJsonObject()) {
            this.jsonToUser(s, certifierPassword, jsonElement.getAsJsonObject());
         }

      });
   }

   private void jsonToUser(Session s, String certifierPassword, JsonObject asJsonObject) {
      try {
         Registration reg = s.createRegistration();
         reg.setRegistrationServer(s.getEffectiveUserName());
         reg.setCreateMailDb(false);
         reg.setCertifierIDFile("/local/notesdata/cert.id");
         reg.setRegistrationLog((String)null);
         DateTime dt = s.createDateTime("Today");
         dt.setNow();
         dt.adjustYear(1);
         reg.setExpiration(dt);
         reg.setIDType(172);
         reg.setMinPasswordLength(5);
         reg.setNorthAmerican(true);
         reg.setUpdateAddressBook(true);
         reg.setSynchInternetPassword(true);
         String lastName = "";
         String firstName = "";
         String userPasssword = "";
         String idfile = "/tmp/" + lastName.toLowerCase() + ".id";
         if (reg.registerNewUser(lastName, idfile, "", firstName, "", certifierPassword, "", "", "", "", userPasssword)) {
            System.out.println("Registration of " + idfile + " successful!");
         } else {
            System.out.println("Registration of " + idfile + " failed!");
         }
      } catch (NotesException var10) {
         System.err.println("Failed processing registration of user (" + var10.getMessage() + ")");
      }

   }

   private void jsonToTestUsers(Session s, JsonObject testUserObject) {
      System.out.println("\tRegistering test user accounts");

      try {
         if (this.debug) {
            System.out.println("\t\tDEBUG : test user account registration : starting");
         }

         Registration reg = s.createRegistration();
         reg.setCreateMailDb(false);
         reg.setCertifierIDFile("cert.id");
         reg.setRegistrationLog("log.nsf");
         DateTime dt = s.createDateTime("Today");
         dt.setNow();
         dt.adjustYear(1);
         reg.setExpiration(dt);
         reg.setIDType(172);
         reg.setMinPasswordLength(5);
         reg.setUpdateAddressBook(true);
         reg.setSynchInternetPassword(true);
         reg.setStoreIDInAddressBook(true);
         JsonElement certifierPasswordElement = testUserObject.get("certifierPassword");
         String certifierPassword = certifierPasswordElement != null ? certifierPasswordElement.getAsString() : "";
         JsonElement countElement = testUserObject.get("count");
         int count = countElement != null ? countElement.getAsInt() : 1;
         JsonElement lastNameElement = testUserObject.get("lastName");
         String baseLastName = lastNameElement != null ? lastNameElement.getAsString() : "Adams";
         JsonElement firstNameElement = testUserObject.get("firstName");
         String firstName = firstNameElement != null ? firstNameElement.getAsString() : "Susan";
         JsonElement passwordElement = testUserObject.get("userPassword");
         String userPasssword = passwordElement != null ? passwordElement.getAsString() : "passw0rd";
         JsonElement idFilePathElement = testUserObject.get("idFilePath");
         String idFilePath = passwordElement != null ? idFilePathElement.getAsString() : "/local/notesdata/";

         for(int i = 0; i < count; ++i) {
            String lastName = baseLastName;
            if (count > 1) {
               lastName = baseLastName + (i + 1);
            }

            String idfile = idFilePath + lastName.toLowerCase() + ".id";
            if (reg.registerNewUser(lastName, idfile, "", firstName, "", certifierPassword, "", "", "", "", userPasssword)) {
               System.out.println("Registration of " + idfile + " successful!");
            } else {
               System.out.println("Registration of " + idfile + " failed!");
            }
         }
      } catch (NotesException var20) {
         System.err.println("Failed processing registration (" + var20.getMessage() + ")");
      } catch (Exception var21) {
         System.err.println("testUserRegistration error : " + var21.getMessage());
      }

   }

   private void jsonToNotesIni(Session s, JsonObject notesIniObject) {
      notesIniObject.entrySet().forEach((entry) -> {
         JsonElement iniValue = (JsonElement)entry.getValue();
         String iniName = (String)entry.getKey();
         boolean var4 = true;

         try {
            char iniNameFirstChar = iniName.charAt(0);
            if (!iniValue.isJsonNull() && iniValue.isJsonPrimitive()) {
               JsonPrimitive jp = iniValue.getAsJsonPrimitive();
               if (jp.isBoolean()) {
                  s.setEnvironmentVar(iniName, jp.getAsBoolean(), true);
                  System.out.println("Notes.ini : " + iniName + "=" + jp.getAsBoolean());
               } else if (jp.isNumber()) {
                  s.setEnvironmentVar(iniName, jp.getAsInt(), true);
               } else if (jp.isString()) {
                  s.setEnvironmentVar(iniName, jp.getAsString(), true);
                  System.out.println("Notes.ini : " + iniName + "=" + jp.getAsString());
               }
            } else {
               System.err.println("Invalid notes.ini key:" + iniName);
            }
         } catch (Exception var7) {
            System.err.println("Notes.ini error with key:" + iniName + ": " + var7.getMessage());
         }

      });
   }

   private void printDebug() {
      System.out.println("java.library.path: " + System.getProperty("java.library.path"));
      System.out.println("PATH: " + System.getenv("PATH"));
   }

   private void updateDocument(Document doc, JsonArray fieldsArray) {
      try {
         fieldsArray.forEach((fieldItem) -> {
            if (fieldItem.isJsonObject()) {
               JsonObject fieldObject = fieldItem.getAsJsonObject();

               try {
                  if (!fieldObject.has("name") && !fieldObject.has("value")) {
                     System.err.println("\t\tJSON file does not contain mandatory field properties (name or value)");
                  } else {
                     JsonElement fieldNameObject = fieldObject.get("name");
                     JsonElement fieldValueObject = fieldObject.get("value");
                     if (fieldNameObject == null) {
                        System.err.println("\t\tJSON file missing mandatory property 'name' or 'value'");
                     } else {
                        String fieldName = fieldNameObject.getAsString();
                        if (this.debug) {
                           System.err.println("\t\tDEBUG : processing field " + fieldName);
                        }

                        JsonPrimitive fieldValue = fieldValueObject.getAsJsonPrimitive();
                        JsonElement appendFlagJson = fieldObject.get("append");
                        boolean appendFlag = appendFlagJson != null ? appendFlagJson.getAsBoolean() : false;
                        if (this.debug) {
                           System.out.println("\t\tDEBUG : append flag is " + appendFlag);
                        }

                        if (appendFlag && doc.hasItem(fieldName)) {
                           Vector<String> vec = doc.getItemValue(fieldName);
                           vec.add(fieldValue.getAsString());
                           doc.replaceItemValue(fieldName, vec);
                        } else if (!fieldValue.isJsonNull() && fieldValue.isJsonPrimitive()) {
                           JsonPrimitive jp = fieldValue.getAsJsonPrimitive();
                           if (jp.isNumber()) {
                              doc.replaceItemValue(fieldName, jp.getAsInt());
                              if (this.debug) {
                                 System.out.println("\tDocument field as Integer : " + fieldName + "=" + jp.getAsInt());
                              }
                           } else if (jp.isString()) {
                              doc.replaceItemValue(fieldName, fieldValue.getAsString());
                              if (this.debug) {
                                 System.out.println("\tDocument field as String: " + fieldName + "=" + jp.getAsString());
                              }
                           }
                        }

                        Item item = doc.getFirstItem(fieldName);
                        JsonElement isNamesFlagJson = fieldObject.get("isnames");
                        boolean isNamesFlag = isNamesFlagJson != null ? isNamesFlagJson.getAsBoolean() : false;
                        if (isNamesFlag) {
                           item.setNames(true);
                        }

                        JsonElement isReadersFlagJson = fieldObject.get("isreaders");
                        boolean isReadersFlag = isReadersFlagJson != null ? isReadersFlagJson.getAsBoolean() : false;
                        if (isReadersFlag) {
                           item.setReaders(true);
                        }

                        JsonElement isAuthorsFlagJson = fieldObject.get("isauthors");
                        boolean isAuthorsFlag = isAuthorsFlagJson != null ? isAuthorsFlagJson.getAsBoolean() : false;
                        if (isAuthorsFlag) {
                           item.setAuthors(true);
                        }

                        JsonElement isProtectedFlagJson = fieldObject.get("isauthors");
                        boolean isProtectedFlag = isProtectedFlagJson != null ? isProtectedFlagJson.getAsBoolean() : false;
                        if (isProtectedFlag) {
                           item.setProtected(true);
                        }

                        JsonElement isSignedFlagJson = fieldObject.get("issigned");
                        boolean isSignedFlag = isSignedFlagJson != null ? isSignedFlagJson.getAsBoolean() : false;
                        if (isSignedFlag) {
                           item.setSigned(true);
                        }
                     }
                  }
               } catch (Exception var21) {
                  var21.printStackTrace();
               }
            }

         });
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }
}
