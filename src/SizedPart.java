import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SizedPart extends MimeBodyPart {
    private double aspectRatio;
    private String dimension;
    private MimeBodyPart bodypart;

    public SizedPart(MimeBodyPart bodyPart) throws IOException, MessagingException {
        super(bodyPart.getInputStream());
        this.bodypart = bodyPart;
        try {
            //make images out of attachement/inline pics, measure aspect ratio
            if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
                BufferedImage picture = ImageIO.read(bodyPart.getInputStream());
                setAspectRatio(picture.getWidth(), picture.getHeight());
                setDimension();
            }
        }
        catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Messaging Exception while creating SizedPart.");
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException while creating SizedPart.");
        }
    }

    public SizedPart() {}

    public String getDimension() {
        return dimension;
    }

    public void setAspectRatio(double width, double height) {
        aspectRatio = width / height;
    }

    public void setDimension() {
        double fulldif = Math.abs(aspectRatio - Emails.FULL);
        double halfdif = Math.abs(aspectRatio - Emails.HALF);
        double eighthdif = Math.abs(aspectRatio - Emails.EIGHTH);
        double mindif = Math.min(fulldif, Math.min(halfdif, eighthdif));
        if (mindif == fulldif)
            dimension = "full";
        else if (mindif == halfdif)
            dimension = "half";
        else
            dimension = "eighth";

    }

    public void saveFile(File file) throws IOException, MessagingException {
        bodypart.saveFile(file);
    }

    public String changeFileType() throws MessagingException {
        String type = bodypart.getContentType();
        String filename = bodypart.getFileName();
        if (type.contains("jpg") || type.contains("jpeg")) {
            return filename.substring(0, filename.indexOf(".")) + ".png";
        }
        return filename;
    }
}
