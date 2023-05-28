import mpi.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class MandelbrotSetMPI {
    static int maximumIterations = 1000;
    static double xStart = -400, yStart = 0;
    static double zoomScale = 250.0;
    static int widthImage = 800, heightImage = 600;
    static Color colorOfSet = Color.BLACK;
    static Color[] COLORS = new Color[maximumIterations];

    //colors
    static{
        float hueFactor = 0.7f;
        float saturationFactor = 0.7f;
        float brightnessFactor = 1.0f;
        for (int i = 0; i<maximumIterations; i++){
            float hue = (hueFactor * 360 * (i) % 360 + 240) % 360/360f;
            COLORS[i] = Color.getHSBColor(hue, saturationFactor, brightnessFactor);
        }
    }

    public static void main(String[] args) throws MPIException, IOException {
        MPI.Init(args);
        int processNumber = MPI.COMM_WORLD.Rank();
        int totalProcesses = MPI.COMM_WORLD.Size();

        int portion = heightImage / totalProcesses;
        int begin = processNumber * portion;
        int end = begin + portion;

        long beginTime = System.currentTimeMillis();

        //generate a section of the mandelbrot set
        BufferedImage mandelbrotImageSection = MandelbrotSet(begin, end, portion);

        //bring all sections at main process if process != 0
        if (processNumber == 0){
            BufferedImage mandelbrotImage = new BufferedImage(widthImage, heightImage, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = mandelbrotImage.createGraphics();

            //draws its portion (doesn't only join img but also draws)
            imageGraphics.drawImage(mandelbrotImageSection, 0, 0, null);

            //receive and draw sections from other processes
            for (int i = 1; i < totalProcesses; i++){
                byte[] receivedData = new byte[widthImage * portion * Integer.BYTES];
                MPI.COMM_WORLD.Recv(receivedData, 0, receivedData.length, MPI.BYTE, i, 0);
                BufferedImage receivedImage = toBufferedImage(receivedData, widthImage, portion);
                imageGraphics.drawImage(receivedImage, 0, i * portion, null);
            }
            imageGraphics.dispose();//releasing the buffers

            long finishTime = System.currentTimeMillis();

            long timeToExecute = finishTime - beginTime;
            System.out.println("Time taken to generate the Mandelbrot set: " + timeToExecute/1000.0 + " sec.");

            //save final image
            ImageIO.write(mandelbrotImage, "png", new File("mandelbrot.png"));
        } else {
            //send section to root process
            byte[] sendData = toByteArray(mandelbrotImageSection);
            MPI.COMM_WORLD.Send(sendData, 0, sendData.length, MPI.BYTE, 0, 0);
        }

        MPI.Finalize();
    }

    static BufferedImage MandelbrotSet(int begin, int end, int height){
        BufferedImage sectionImage = new BufferedImage(MandelbrotSetMPI.widthImage, height, BufferedImage.TYPE_INT_ARGB);
        double centerX = MandelbrotSetMPI.widthImage / 2.0;
        double centerY = MandelbrotSetMPI.heightImage / 2.0;

        for (int y = begin; y < end; y++){
            for (int x = 0; x < MandelbrotSetMPI.widthImage; x++) {
                double c_real = xStart / MandelbrotSetMPI.widthImage + (x - centerX) / zoomScale;
                double c_imag = yStart / MandelbrotSetMPI.heightImage + (y - centerY) / zoomScale;
                double z_real = 0;
                double z_imag = 0;

                int currentIterations;
                for (currentIterations = 0; currentIterations < maximumIterations && (z_real * z_real) + (z_imag * z_imag) < 4; currentIterations++){
                    double old_z_real = z_real;
                    z_real = (z_real * z_real) - (z_imag * z_imag) + c_real;
                    z_imag = 2 * (old_z_real * z_imag) + c_imag;
                }

                if (currentIterations == maximumIterations) { // inside the set
                    sectionImage.setRGB(x, y - begin, colorOfSet.getRGB());
                }
                else {
                sectionImage.setRGB(x, y - begin, COLORS[currentIterations].getRGB());
                }
            }
        }
        return sectionImage;
    }

    //method to convert buffered image to byte array
    static byte[] toByteArray(BufferedImage image){
        DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
        int[] intData = buffer.getData();
        byte[] byteData = new byte[intData.length * 4];
        for (int i = 0; i < intData.length; i++){
            byteData[i * 4] = (byte) (intData[i] >> 24);
            byteData[i * 4 + 1] = (byte) (intData[i] >> 16);
            byteData[i * 4 + 2] = (byte) (intData[i] >> 8);
            byteData[i * 4 + 3] = (byte) (intData[i]);
        }
        return byteData;
    }

    //method to convert byte array to buffered image
    static BufferedImage toBufferedImage(byte[] data, int width, int height){
        int[] intData = new int[data.length / 4];
        for (int i = 0; i < intData.length; i++){
            intData[i] = ((data[i * 4] & 0xFF) << 24) |
                    ((data[i * 4 + 1] & 0xFF) << 16) |
                    ((data[i * 4 + 2] & 0xFF) << 8) |
                    (data[i * 4 + 3] & 0xFF);
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, intData, 0, width);
        return image;
    }
}

