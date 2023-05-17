import mpi.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class MandelbrotSetMPI {
    // Constants and parameters
    static final int maximumIterations = 1000;
    static final double xPos = -470, yPos = 0;
    static final double zoom = 250.0;
    static final int width = 800, height = 600;
    static final Color colorOfSet = Color.BLACK;
    static final Color[] COLORS = new Color[maximumIterations];

    public static void main(String[] args) throws MPIException, IOException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Calculate portion of image this rank will handle
        int portionHeight = height / size;
        int start = rank * portionHeight;
        int end = start + portionHeight;

        long startTime = System.currentTimeMillis();  // Start timing here

        // Generate the portion of the Mandelbrot set
        BufferedImage image = generateMandelbrot(start, end, portionHeight);

        // Gather all portions at root process
        if (rank == 0) {
            BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = finalImage.createGraphics();

            // Draw own portion
            g2d.drawImage(image, 0, 0, null);

            // Receive and draw portions from other processes
            for (int i = 1; i < size; i++) {
                byte[] receivedData = new byte[width * portionHeight * Integer.BYTES];
                MPI.COMM_WORLD.Recv(receivedData, 0, receivedData.length, MPI.BYTE, i, 0);
                BufferedImage receivedImage = toBufferedImage(receivedData, width, portionHeight);
                g2d.drawImage(receivedImage, 0, i * portionHeight, null);
            }
            g2d.dispose();

            long endTime = System.currentTimeMillis();  // End timing here

            // Calculate elapsed time
            long elapsedTime = endTime - startTime;
            System.out.println("Time taken to generate the Mandelbrot set: " + elapsedTime/1000.0 + " sec.");

            // Save final image
            ImageIO.write(finalImage, "png", new File("mandelbrot.png"));
        } else {
            // Send portion to root process
            byte[] sendData = toByteArray(image);
            MPI.COMM_WORLD.Send(sendData, 0, sendData.length, MPI.BYTE, 0, 0);
        }

        MPI.Finalize();
    }

    // Method to generate Mandelbrot set
    static BufferedImage generateMandelbrot(int start, int end, int height) {
        BufferedImage image = new BufferedImage(MandelbrotSetMPI.width, height, BufferedImage.TYPE_INT_ARGB);
        double centerX = MandelbrotSetMPI.width / 2.0;
        double centerY = MandelbrotSetMPI.height / 2.0;

        for (int y = start; y < end; y++) {
            for (int x = 0; x < MandelbrotSetMPI.width; x++) {
                double cr = xPos / MandelbrotSetMPI.width + (x - centerX) / zoom;
                double ci = yPos / height + (y - centerY) / zoom;
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

    // Method to convert BufferedImage to byte array
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

    // Method to convert byte array to BufferedImage
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

