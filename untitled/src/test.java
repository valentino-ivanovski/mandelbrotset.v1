public class test {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "/Users/tino/Local Files/Class Notes/Second Year/First Semester/Mandelbrot Set Parallel/untitled/src/MandelbrotSetMPI.java", "-jar", "/Users/tino/mpj/lib/starter.jar", "MandelbrotSetMPI", "-np", "4");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
