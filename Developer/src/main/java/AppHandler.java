import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.BatchModifyMessagesRequest;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListMessagesResponse;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/* class to demonstrate use of Gmail list labels API */
public class AppHandler {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Mabelort";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.unmodifiableList(List.of(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_MODIFY));
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    // Source - https://stackoverflow.com/a/782183
// Posted by Iain, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-03, License - CC BY-SA 3.0
    InputStream in = null;
    try {
      String secret = AccessSecretVersion.accessSecretVersion();
      in = new ByteArrayInputStream(secret.getBytes(StandardCharsets.UTF_8));
    } catch (PermissionDeniedException e) {
      throw e;
    } catch (Exception e) {
      throw new FileNotFoundException("Resource not found: Developer-Credentials");
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

  private static LabelRules buildLabelRules(List<Label> labels) throws IOException {
    InputStream in = new FileInputStream(new File("labels.json"));
    LabelRules rules = LabelRules.load(JSON_FACTORY, new InputStreamReader(in));
    rules.loadLabelIds(labels);
    return rules;
  }

  private static Set<String> getAuthors(int id, boolean keepAuthors) throws IOException {
    Scanner authorsIn = new Scanner(new File("authors/"+id));
    Set<String> authors = new HashSet<>();
    while (authorsIn.hasNextLine()) {
      authors.add(authorsIn.nextLine());
    }
    authorsIn.close();
    FileWriter authorsOut = new FileWriter(new File("authors/"+id));
    if (keepAuthors) {
      for (String s : authors) {
        authorsOut.write(s+"\n");
      }
    }
    authorsOut.close();
    return authors;
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    //inputs
    boolean getNewAuthors = true;
    boolean modifyMail = true;

    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

    // Print the labels in the user's account.
    String user = "me";
    ListLabelsResponse listResponse = service.users().labels().list(user).execute();
    List<Label> labels = listResponse.getLabels();
    if (labels.isEmpty()) {
      System.out.println("No labels found.");
    } else {
      System.out.println("Labels:");
      for (Label label : labels) {
        System.out.printf("%s - %s\n", label.getName(), label.getId());
      }
    }
    System.out.println();

    LabelRules labelRules = null;
    try {
      labelRules = buildLabelRules(labels);
    } catch (IOException e) {
      System.out.println(e.toString());
      System.out.println(e.getStackTrace());
      System.out.println("Label rules not found\nStopping program...");
      new File("labels.json").createNewFile();
      System.out.println("Expected format of label rules:\n{\n    \"label_rules\": [\n        {\n            \"save_authors\": false,\n            \"marker\": \"dirty\",\n            \"destination\": \"kitchen sink\",\n            \"q\": \"label:kitchen counter\",\n            \"remove\": \"clean\"\n        },\n        {\n            \"save_authors\": false,\n            \"marker\": \"clean\",\n            \"destination\": \"kitchen cabinet\",\n            \"q\": \"label:kitchen sink\"\n        },\n        {\n            \"save_authors\": true,\n            \"marker\": \"scumbag\",\n            \"destination\": \"TRASH\",\n            \"q\": \"label:neighbor\"\n        }\n    ]\n}");
      return;
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      return;
    }
    System.out.println();

    if (getNewAuthors) {
      for (int i = 0; i<labelRules.size(); i++) {
        try (FileWriter writeAuthors = new FileWriter("authors/"+i, true)) {
          if (labelRules.get(i).getMarker()==null) {
            writeAuthors.close();
            continue;
          }
          //get messages with marker, max 50
          ListMessagesResponse response = service.users().messages().list(user).setMaxResults((long)50).setLabelIds(Collections.singletonList(
            labelRules.get(i).getMarkerId()
          )).execute();

          List<Message> messages = response.getMessages();
          if (messages==null) {
            System.out.println("No authors found for "+labelRules.get(i));
            continue;
          }
          //write authors found in messages to file
          for (Message m : messages) {
            String id = m.getId();
            System.out.print("Getting message "+id+" ... ");
            Message message = service.users().messages().get(user,id).setFormat("METADATA").setMetadataHeaders(Collections.singletonList("From")).execute();
            String author = message.getPayload().getHeaders().get(0).getValue();
            System.out.println(author);
            writeAuthors.append(author+"\n");
          }
          writeAuthors.close();
        } catch (FileNotFoundException e) {
          System.out.println("Authors not found\nCreating folder...");
          new File("authors").mkdir();
          i--;
        }
      }
      System.out.println();
    }

    //load authors from file
    //also remove duplicate authors from file
    List<Set<String>> authorsPerRule = new ArrayList<>();
    try {
      for (int i = 0; i<labelRules.size(); i++) {
        authorsPerRule.add(getAuthors(i,labelRules.get(i).getKeepAuthors()));
        for (String s : authorsPerRule.get(i)) {
          System.out.println(s);
        }
        System.out.println();
      }
    } catch (IOException e) {
      System.out.println("Failed to get author rules\nStopping program...");
      return;
    }

    if (modifyMail) {
      //Get messages that match authors
      boolean pageTokens = true;
      while (pageTokens) {
        pageTokens = false;
        for (int i = 0; i<labelRules.size(); i++) {
          LabelRules.Rules rules = labelRules.get(i);
          System.out.println("Getting messages for ("+rules.getMarker()+
            ") "+
            rules.getRemove()+
            " -> "+rules.getDestination());

          List<String> toMoveIds = new ArrayList<>();
          Set<String> authors = authorsPerRule.get(i);
          for (String a : authors) {
            String query = "from:" + a;
            if (rules.getQ()!=null) {
              query += " " + rules.getQ();
            }
            ListMessagesResponse response = service.users().messages().list(user).setMaxResults((long)500).setQ(query).execute();
            List<Message> messages = response.getMessages();
            if (messages==null) {
              continue;
            }
            for (Message message : messages) {
              if (toMoveIds.size()==1000) {
                pageTokens = true;
                break;
              }
              String id = message.getId();
              toMoveIds.add(id);
              System.out.println(id);
            }
          }
          if (toMoveIds.size()==0) {
            System.out.println("No messages found");
            continue;
          }
          System.out.print("Moving " + toMoveIds.size() + " messages ... ");

          BatchModifyMessagesRequest request = new BatchModifyMessagesRequest();
          request.set("ids",toMoveIds);
          if (rules.getDestination()!=null)
            request.set("addLabelIds",Collections.singletonList(rules.getDestinationId()));

          List<String> removeIds = new ArrayList<>();
          if (rules.getRemove()!=null)
            removeIds.add(rules.getRemoveId());

          if (rules.getMarker()!=null)
            removeIds.add(rules.getMarkerId());

          if (removeIds.size()>0)
            request.set("removeLabelIds",Collections.unmodifiableList(removeIds));

          service.users().messages().batchModify(user,request).execute();
          System.out.println("done\n");
        }
      }
    }
  }
}