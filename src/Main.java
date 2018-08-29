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

    private final static String fileName = "src/fftSmoothed.txt";


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
       // System.out.println(accelerationOffset);
//_____________________________________________________________________________________________________________________________________


        //Getting compressions info
        formattedData = calibrate.getData(compressionsFile);

        float[][] accelerometerCompressionData = formattedData.toArray(new float[][]
                                                     {new float[formattedData.size()]});

        float[] cRawAcceleration = calibrate.getAccelerationFromRawData(accelerometerCompressionData, txyz);
        float[] acceleration = calibrate.setAcceleration(cRawAcceleration, accelerationOffset, GRAVITY);


        int[] timeArray  = calibrate.getTimearray(accelerometerCompressionData);
        float[] scaledTime = calibrate.scaleTime(accelerometerCompressionData);

        StringBuilder tmpString = new StringBuilder();
        double hanningAppliedValues[] = MathOps.applyHanningWindow(acceleration, 2600);

        System.out.println(Arrays.toString(hanningAppliedValues));



        int N = 1024;

        Complex[] baseComplexArray = new Complex[N];
        for (int i = 0; i < N; i++)
            baseComplexArray[i] = new Complex(hanningAppliedValues[i], 0);


        Complex[] complexArrayFFTValues = FastFourierTransform.fft(baseComplexArray);
        Complex[] fftPolarSingle = new Complex[N/2];
        double[] fftSmooth = new double[N/2];


        for (int i = 0 ; i < (N/2) ; i++){
            fftPolarSingle[i] = complexArrayFFTValues[i+1].divides(new Complex(N, 0));
            fftSmooth[i] = fftPolarSingle[i].abs();
        }


        for (int i = 2 ; i < (N/2 - 1) ; i++) {
            fftPolarSingle[i] = fftPolarSingle[i].times(new Complex(2,0));
            fftSmooth[i] *= 2;
        }


        FastFourierTransform.show(fftSmooth, "FFT smoothed");

        for (int i =0; i <fftSmooth.length; i++){
            tmpString.append(fftSmooth[i]).append("\n");
        }

        try {
            writeFile(tmpString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // FastFourierTransform.show(fftPolarSingle, "FFT Polar Single");


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

    public static void writeFile( String data) throws IOException {

        File file = new File(fileName);

        FileOutputStream stream = new FileOutputStream(file);
        System.out.println(file);

        //String str = new String(data.getBytes());

        try {
            stream.write(data.getBytes());
            //Log.d(TAG, "IMUWriteRawData: " + data.getBytes());
        } finally {
            stream.close();
        }

    }

    static void createOffsetBenchAccelerationBenchmark() {

    }
}
