import mpi.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class MandelbrotSetMPI {
    static int maximumIterations = 10000;
    static double xStart = -400, yStart = 0;
    static double zoomScale = 250.0;
    static int widthImage = 800, heightImage = 600;
    static Color colorOfSet = Color.BLACK;
    static Color[] COLORS = new Color[maximumIterations];

    public static void main(String[] args) throws MPIException, IOException {
        MPI.Init(args);
        int processNumber = MPI.COMM_WORLD.Rank();
        int totalProcesses = MPI.COMM_WORLD.Size();

        int portion = heightImage / totalProcesses;
        int begin = processNumber * portion;
        int end = begin + portion;

        long beginTime = System.currentTimeMillis();  // Start timing here

        //Generate the portion of the Mandelbrot set
        BufferedImage mandelbrotImage = generateMandelbrot(begin, end, portion);

        //Gather all portions at root process
        if (processNumber == 0) {
            BufferedImage finalImage = new BufferedImage(widthImage, heightImage, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = finalImage.createGraphics();

            //Draw own portion
            imageGraphics.drawImage(mandelbrotImage, 0, 0, null);

            //Receive and draw portions from other processes
            for (int i = 1; i < totalProcesses; i++) {
                byte[] receivedData = new byte[widthImage * portion * Integer.BYTES];
                MPI.COMM_WORLD.Recv(receivedData, 0, receivedData.length, MPI.BYTE, i, 0);
                BufferedImage receivedImage = toBufferedImage(receivedData, widthImage, portion);
                imageGraphics.drawImage(receivedImage, 0, i * portion, null);
            }
            imageGraphics.dispose();

            long finishTime = System.currentTimeMillis();  // End timing here

            //Calculate elapsed time
            long timeToExecute = finishTime - beginTime;
            System.out.println("Time taken to generate the Mandelbrot set: " + timeToExecute/1000.0 + " sec.");

            //Save final image
            ImageIO.write(finalImage, "png", new File("mandelbrot.png"));
        } else {
            //Send portion to root process
            byte[] sendData = toByteArray(mandelbrotImage);
            MPI.COMM_WORLD.Send(sendData, 0, sendData.length, MPI.BYTE, 0, 0);
        }

        MPI.Finalize();
    }

    //Method to generate Mandelbrot set
    static BufferedImage generateMandelbrot(int start, int end, int height) {
        BufferedImage image = new BufferedImage(MandelbrotSetMPI.widthImage, height, BufferedImage.TYPE_INT_ARGB);
        double centerX = MandelbrotSetMPI.widthImage / 2.0;
        double centerY = MandelbrotSetMPI.heightImage / 2.0;

        for (int y = start; y < end; y++) {
            for (int x = 0; x < MandelbrotSetMPI.widthImage; x++) {
                double cr = xStart / MandelbrotSetMPI.widthImage + (x - centerX) / zoomScale;
                double ci = yStart / height + (y - centerY) / zoomScale;
                double zr = 0;
                double zi = 0;

                int iterationsOfZ;
                for (iterationsOfZ = 0; iterationsOfZ < maximumIterations && (zr * zr) + (zi * zi) < 4; iterationsOfZ++) {
                    double oldZr = zr;
                    zr = (zr * zr) - (zi * zi) + cr;
                    zi = 2 * (oldZr * zi) + ci;
                }

                if (iterationsOfZ == maximumIterations) { // inside the set
                    image.setRGB(x, y - start, colorOfSet.getRGB());
                }
                else {
                image.setRGB(x, y - start, COLORS[iterationsOfZ].getRGB());
                }
            }
        }
        return image;
    }

    //Method to convert BufferedImage to byte array
    static byte[] toByteArray(BufferedImage image) {
        DataBufferInt dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
        int[] intData = dataBuffer.getData();
        byte[] byteData = new byte[intData.length * 4];
        for (int i = 0; i < intData.length; i++) {
            byteData[i * 4] = (byte) (intData[i] >> 24);
            byteData[i * 4 + 1] = (byte) (intData[i] >> 16);
            byteData[i * 4 + 2] = (byte) (intData[i] >> 8);
            byteData[i * 4 + 3] = (byte) (intData[i]);
        }
        return byteData;
    }

    //Method to convert byte array to BufferedImage
    static BufferedImage toBufferedImage(byte[] data, int width, int height) {
        int[] intData = new int[data.length / 4];
        for (int i = 0; i < intData.length; i++) {
            intData[i] = ((data[i * 4] & 0xFF) << 24) |
                    ((data[i * 4 + 1] & 0xFF) << 16) |
                    ((data[i * 4 + 2] & 0xFF) << 8) |
                    (data[i * 4 + 3] & 0xFF);
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, intData, 0, width);
        return image;
    }

    static {
        float hueFactor = 0.7f;
        float saturationFactor = 0.7f;
        float brightnessFactor = 1.0f;
        for (int i = 0; i<maximumIterations; i++) {
            float hue = (hueFactor * 360 * (i) % 360 + 240) % 360/360f;
            COLORS[i] = Color.getHSBColor(hue, saturationFactor, brightnessFactor);
        }
    }
}

