import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class Emails {

    public static final double FULL = 8.5 / 11;
    public static final double HALF = 8.5 / 5.5;
    public static final double EIGHTH = 4.25 / 2.75;

    private final String protocol;
    private String directory;

    public Emails(String protocol) {
        this.protocol = protocol;
    }

    public Store connectStore(Properties properties, String username, String password) throws MessagingException {
        Session session = Session.getDefaultInstance(properties);
        Store store = session.getStore(protocol);
        store.connect(username, password);
        return store;
    }

    public Folder openFolder(Store store) throws MessagingException {
        Folder folderInbox = store.getFolder("INBOX");
        folderInbox.open(Folder.READ_ONLY);
        return folderInbox;
    }

    public void closeFolder(Folder folderInbox) throws MessagingException {
        folderInbox.close(false);
    }

    public void disconnectStore(Store store) throws MessagingException {
        store.close();
    }

    public Message[] openMessages(Folder inbox) throws MessagingException {
        return inbox.getMessages();
    }

    public String getFromAddress(Message message) throws MessagingException {
        return message.getFrom()[0].toString();
    }

    public String getSubject(Message message) throws MessagingException {
        return message.getSubject();
    }

    public void downloadAttachments(Message message) throws MessagingException, IOException {
        String type = message.getContentType();
        if (type.contains("multipart")) {
            Multipart multipart = (Multipart) message.getContent();
            parseMultipart(multipart);
        }
        else if (message.getContent() instanceof String) {
            System.out.println(message.getContent().toString());
            System.out.println(message.getContentType());
        }
    }

    public void parseMultipart(Multipart multipart) throws MessagingException, IOException {
            for (int partCount = 0; partCount < multipart.getCount(); partCount++) {
                BodyPart part = multipart.getBodyPart(partCount); //one part of the email
                String disposition = part.getDisposition();

                String filename = part.getFileName();
                System.out.println("The file name is: " + filename);

                //save all words plain/html to a single doc, if that doc does not already exist, create one
                if (part.getContentType().contains("multipart")) {
                    parseMultipart((Multipart) part.getContent());
                }
                else if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition) || Part.INLINE.equalsIgnoreCase(disposition)) {
                        if (!part.getContentType().contains("pdf")) {
                            //if this part is an attachment/inline attachment, save the file
                            SizedPart attachment = new SizedPart((MimeBodyPart) part);
                            filename = attachment.changeFileType();
                            filename = renameDuplicates(filename, attachment.getDimension());
                            attachment.saveFile(new File(directory + File.separator + attachment.getDimension() + File.separator + filename));
                            System.out.println("Saved an " + disposition + ": " + filename + ".");
                            getText(part);
                        }
                        else {
                            MimeBodyPart bodyPart = (MimeBodyPart) part;
                            createTempDirectory(directory + File.separator + "temp");
                            String path = directory + File.separator + "temp" + File.separator + filename;
                            bodyPart.saveFile(new File(path));
                            pdfToImage(path);
                            //loop through each file in temp folder, only apply size finding on png images
                            //read file into bufferedimage
                            //then find aspectratio, then find size
                            //then save into correct folder
                            //then delete temp folder
                            SizedPart calc = new SizedPart();
                            File[] files = new File(directory + File.separator + "temp").listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    String name = file.getName();
                                    if (name.contains(".png")) {
                                        BufferedImage picture = ImageIO.read(new File(directory + File.separator + "temp" + File.separator + name));
                                        calc.setAspectRatio(picture.getWidth(), picture.getHeight());
                                        calc.setDimension();
                                        name = renameDuplicates(name, calc.getDimension());
                                        ImageIO.write(picture, "png", new File(directory + File.separator + calc.getDimension() + File.separator + name));
                                    }
                                }
                            }
                            FileUtils.deleteDirectory(new File(directory + File.separator + "temp"));
                        }
                    }
                else
                    getText(part);
            }
    }

    public void createSubDirectories() throws IOException {
        if (dirIsEmpty(directory)) {
            new File(directory + File.separator + "full").mkdirs();
            new File(directory + File.separator + "half").mkdirs();
            new File(directory + File.separator + "eighth").mkdirs();
        }
        else {
            ArrayList<Boolean> statuses = new ArrayList<>();
            String[] filenames = new File(directory).list();
            assert filenames != null;
            for (String name : filenames) {
                switch (name) {
                    case "full":
                    case "half":
                    case "eighth":
                        statuses.add(true);
                }
            }
            if (statuses.size() != 3) {
                directory = directory + File.separator + "workspace";
                new File(directory).mkdirs();
                new File(directory + File.separator + "full").mkdirs();
                new File(directory + File.separator + "half").mkdirs();
                new File(directory + File.separator + "eighth").mkdirs();
            }
        }
    }

    public void createTempDirectory(String path) {
        if (!Files.exists(Paths.get(path)))
            new File(path).mkdirs();
    }

    public boolean dirIsEmpty(String path) throws IOException {
        try (Stream<Path> entries = Files.list(Paths.get(path))) {
            return entries.count() == 0;
        }
    }

    public void getText(BodyPart part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            String text = part.getContent().toString(); //note: in a multipart of inline images, one particular piece of text describes what's in the multipart and the filenames of the images
            System.out.println(text);
            System.out.println(part.getContentType());
        }
        else if (part.isMimeType("text/html")) {
            System.out.println(Jsoup.parse(part.getContent().toString()).text());
            System.out.println(part.getContentType());
        }
    }

    public void pdfToImage (String filename) throws IOException { //figure out if filename means absolute path
        PDDocument document = PDDocument.loadNonSeq(new File(filename), null);
        //parses PDF with a non sequential parser
        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        //the document catalog is the center where info about the PDF is found
        int page = 0;
        for (PDPage pdpage : pages) {
            page++;
            BufferedImage img = pdpage.convertToImage(BufferedImage.TYPE_INT_RGB, 300);
            ImageIOUtil.writeImage(img, filename.substring(0, filename.indexOf(".")) + "_" + page + ".png", 300); //figure out if this writeimage method writes to an absolute path
        }
        document.close();
    }

    public String renameDuplicates(String name, String folder) {
        String[] filenames = new File(directory + File.separator + folder).list();
        for (String filename : filenames) {
            if (name.equals(filename)) {
                name = name.substring(0, name.indexOf("."));
                if (name.charAt(name.length() - 1) == ')') {
                    /* this section forces the first instance of ")" and "(" to be at the front of the string,
                    guranteeing that the correct number between the parenthesis is found.
                     */
                    StringBuilder namecopy = new StringBuilder(name);
                    String number = namecopy.reverse().substring(namecopy.indexOf(")") + 1, namecopy.indexOf("("));
                    number = new StringBuilder(number).reverse().toString();
                    int num = Integer.parseInt(number) + 1;
                    name = name.substring(0, name.indexOf(" (")) + " (" + num + ").png";
                } else {
                    name = name + " (1).png";
                }
                return renameDuplicates(name, folder);
            }
        }
        return name;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
