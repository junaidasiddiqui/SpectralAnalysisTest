import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {
    private final static int txyz = 3;
    //private final static int compressionresetTime = 2;
    private final static float GRAVITY = 9.80665f;
    private final static float offsetBenchmark = 0.2f;

    private final static float offsetFailedVal = 100f;



    public static void main(String[] args) {


        String stationaryFile = null;
        String compressionsFile = null;

        try {
            stationaryFile = readFile("src/offsetData.txt");
            compressionsFile = readFile("src/data.txt");
        }
        catch (IOException e) {
            System.out.println(e);
        }

        CalibrateData calibrate = new CalibrateData();

        //createOffsetBenchAccelerationBenchmark();

        // Offsetting for stationary data
        ArrayList<float[]> formattedData = calibrate.getData(stationaryFile);

        float[][] accelerometeroffsetData = formattedData.toArray(new float[][]
                                                {new float[formattedData.size()]});

        float[] oRawAcceleration = calibrate.getAccelerationFromRawData(accelerometeroffsetData, txyz);
        float accelerationOffset = calibrate.offsetAcceleration(oRawAcceleration, offsetBenchmark, offsetFailedVal);
        System.out.println(accelerationOffset);
//////////////////////////////////////////////////////////


        //Getting compressions info
        formattedData = calibrate.getData(compressionsFile);

        float[][] accelerometerCompressionData = formattedData.toArray(new float[][]
                                                     {new float[formattedData.size()]});

        float[] cRawAcceleration = calibrate.getAccelerationFromRawData(accelerometerCompressionData, txyz);
        float[] acceleration = calibrate.setAcceleration(cRawAcceleration, accelerationOffset, GRAVITY);


        int[] timeArray  = calibrate.getTimearray(accelerometerCompressionData);
        float[] scaledTime = calibrate.scaleTime(accelerometerCompressionData);


        System.out.println(Arrays.toString(scaledTime));
        System.out.println(Arrays.toString(acceleration));
        //System.out.println(accelerationOffset);

    }

    private static String readFile(String filename) throws IOException {
        File file = new File(filename);

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

    static void createOffsetBenchAccelerationBenchmark() {

    }
}
