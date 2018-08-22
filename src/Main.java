import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private final static int txyz = 3;
    private final static int compressionresetTime = 2;
    private final static float GRAVITY = 9.80665f;
    private final static float offsetBenchmark = 0.2f;

    private final static float offsetFailedVal = 100f;



    public static void main(String[] args) {


        String tmpFile = null;
        try {
            tmpFile = readFile();
        }
        catch (IOException e) {
            System.out.println(e);
        }

        CalibrateData calibrate = new CalibrateData();

        ArrayList<float[]> formattedData = calibrate.getData(tmpFile);
        System.out.println(formattedData.size());
        float[][] accelerometerData = formattedData.toArray(new float[][]
        {new float[formattedData.size()]});

        //System.out.println(Arrays.toString(x[2668]));


        int[] timeArray  = calibrate.getTimearray(accelerometerData);
        float[] acceleration = calibrate.getAcceleration(accelerometerData, txyz);




    }

    static String readFile() throws IOException {
        File file = new File("src/data.txt");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8));
        String st;
        String tmp = "";

        while ((st = br.readLine()) != null ) {
            tmp +=  st + ";";
        }

        return tmp;
    }
}
