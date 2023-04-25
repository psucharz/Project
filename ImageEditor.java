package Project;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ImageEditor {
    private JPanel ImageEditor;
    private JTextArea notificationsTextArea;
    private JSlider hueSlider;
    private JButton importButton;
    private JButton exportButton;
    private JLabel brightnessLabel;
    private JSlider brightnessSlider;
    private JSlider contrastSlider;
    private JButton applyButton;
    private JLabel imageLabel;
    private JTextField filenameField;
    private JLabel filenameLabel;
    private JButton resetButton;
    private JCheckBox grayscaleCheckbox;
    private JCheckBox negativeCheckBox;
    private JLabel hueLabel;
    private JSlider saturationSlider;
    private JCheckBox flipVerticallyCheckBox;
    private JCheckBox flipHorizontallyCheckBox;
    private JLabel saturationLabel;
    private JLabel hueDisplayLabel;
    private JLabel saturationDisplayLabel;
    private JLabel brightnessDisplayLabel;

    BufferedImage image, imageCopy = null;


    private File openFile() throws NullPointerException{
        JFileChooser fileChooser =new JFileChooser();
        fileChooser.setCurrentDirectory(new File("C:\\Users\\patry\\OneDrive\\2022_psw_prm2\\src\\pl\\basicpack\\Project"));
        int response = fileChooser.showOpenDialog(null);
        if(response == JFileChooser.APPROVE_OPTION) {
            return new File(fileChooser.getSelectedFile().getAbsolutePath());
        }
        else return null;
    }

    private void updateImage(BufferedImage image) throws IOException {
        int scaleFactor = Math.max(image.getHeight() / imageLabel.getMaximumSize().height ,
                 image.getWidth() / imageLabel.getMaximumSize().width);
        ImageIcon imageIcon;
            imageIcon = new ImageIcon(resizeImage(image, image.getWidth() / scaleFactor,
                    image.getHeight() / scaleFactor));
        imageLabel.setIcon(imageIcon);
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, width, height, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private void exportImage(BufferedImage image, String filename) throws IOException {
        File destination = new File(filename);
            ImageIO.write(image,"JPG",destination);
    }

    private void changeHSB(BufferedImage image, int hueSlider, int saturationSlider, int brightnessSlider) {
        for(int y=0; y<image.getHeight();y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x,y));
                float HSB[] = new float[3];
                Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),HSB);
                HSB[0] = (HSB[0] + hueSlider/360f)%1.00001f;
                HSB[1] = HSB[1] + saturationSlider/100f;
                HSB[2] = HSB[2] + brightnessSlider/100f;
                if(HSB[1]<0) HSB[1]=0;
                if(HSB[1]>1) HSB[1]=1;
                if(HSB[2]<0) HSB[2]=0;
                if(HSB[2]>1) HSB[2]=1;

                image.setRGB(x,y,Color.getHSBColor(HSB[0],HSB[1],HSB[2]).getRGB());
            }
        }
    }

    private void doGrayScale(BufferedImage image) {
        changeHSB(image,0,-100,0);
    }

    private void doNegative(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                Color c = new Color(image.getRGB(x, y));
                int red = (int)(c.getRed() * 0.299);
                int green = (int)(c.getGreen() * 0.587);
                int blue = (int)(c.getBlue() *0.114);
                red = 255 - red;
                green = 255 - green;
                blue = 255 - blue;
                Color newColor = new Color(red, green, blue);
                image.setRGB(x,y,newColor.getRGB());
            }
        }
    }

    private void flipVertically(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, 0, height, width, 0, null);
        g.dispose();
        image.setData(flippedImage.getData());
    }

    private void flipHorizontally(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        g.dispose();
        image.setData(flippedImage.getData());
    }

    static BufferedImage copyImage(BufferedImage image) {
        ColorModel colormodel = image.getColorModel();
        boolean isAlphaPremultiplied = colormodel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(colormodel, raster, isAlphaPremultiplied, null);
    }

    private void flushNotifications(){ notificationsTextArea.setText("");}

    public ImageEditor() {
        imageLabel.setMaximumSize(new Dimension(400,400));

        importButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    image = ImageIO.read(Objects.requireNonNull(openFile()));
                    imageCopy = copyImage(image);
                    updateImage(image);
                    flushNotifications();
                }
                catch(IllegalArgumentException | IOException ex) {
                    notificationsTextArea.setText("Cannot open file.");
                    ex.printStackTrace();
                }
            }
        });

        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    exportImage(image, filenameField.getText());
                    flushNotifications();
                }
                catch (IllegalArgumentException | IOException ex) {
                    notificationsTextArea.setText("Cannot export image.");
                    ex.printStackTrace();
                }
            }
        });

        applyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    changeHSB(image,hueSlider.getValue(),saturationSlider.getValue(),brightnessSlider.getValue());
                    if(negativeCheckBox.isSelected()) doNegative(image);
                    if(grayscaleCheckbox.isSelected()) doGrayScale(image);
                    if(flipVerticallyCheckBox.isSelected()) flipVertically(image);
                    if(flipHorizontallyCheckBox.isSelected()) flipHorizontally(image);

                    updateImage(image);
                    flushNotifications();
                }
                catch (IOException | IllegalArgumentException ex) {
                    notificationsTextArea.setText("Image not loaded.");
                    ex.printStackTrace();
                }
                catch (NullPointerException ex) {
                    notificationsTextArea.setText("There is no image to modify.");
                }
            }
        });

        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    hueSlider.setValue(0);
                    saturationSlider.setValue(0);
                    brightnessSlider.setValue(0);
                    hueDisplayLabel.setText("0");
                    saturationDisplayLabel.setText("0");
                    brightnessDisplayLabel.setText("0");
                    grayscaleCheckbox.setSelected(false);
                    negativeCheckBox.setSelected(false);
                    flipHorizontallyCheckBox.setSelected(false);
                    flipVerticallyCheckBox.setSelected(false);

                    image.setData(imageCopy.getData());
                    updateImage(image);
                    flushNotifications();

                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                catch (NullPointerException ex) {
                    notificationsTextArea.setText("There is no image to reset.");
                    ex.printStackTrace();
                }
            }
        });

        hueSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                hueDisplayLabel.setText(String.valueOf(hueSlider.getValue()));
            }
        });

        saturationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                saturationDisplayLabel.setText(String.valueOf(saturationSlider.getValue()));
            }
        });

        brightnessSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                brightnessDisplayLabel.setText(String.valueOf(brightnessSlider.getValue()));
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ImageEditor");
        frame.setContentPane(new ImageEditor().ImageEditor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
