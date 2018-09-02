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

        //System.out.println(Arrays.toString(hanningAppliedValues));

        //Real FFT Begins

        int N = 2048;

        System.out.println(N);

        Complex[] baseComplexArray = new Complex[N];
        for (int i = 0; i < N; i++)
            baseComplexArray[i] = new Complex(hanningAppliedValues[i], 0);


        Complex[] complexArrayFFTValues = FastFourierTransform.simpleFFT(baseComplexArray);
        Complex[] fftPolarSingle = new Complex[N/2];
        double[] fftSmooth = new double[N/2];


        //Smooths the complex FFT after getting Single
        for (int i = 0 ; i < (N/2) ; i++){
            fftPolarSingle[i] = complexArrayFFTValues[i+1].divides(new Complex(N, 0));
            fftSmooth[i] = fftPolarSingle[i].abs();
        }

        //Scales THe array
        for (int i = 2 ; i < (N/2 - 1) ; i++) {
            fftPolarSingle[i] = fftPolarSingle[i].times(new Complex(2,0));
            fftSmooth[i] *= 2;
        }


        //astFourierTransform.show(fftSmooth, "FFT smoothed");

        double Fs = 1/scaledTime[1];
        double[] freqBins = MathOps.scaleFrequencyBins(N, Fs);

        int[] peaks = new int[] {102, 205};

        // FastFourierTransform.show(fftPolarSingle, "FFT Polar Single");

        double[] amplitudes = MathOps.peaksFromTransform(fftSmooth, peaks);

        double[] thetaAngles = MathOps.phaseAngles(peaks, fftPolarSingle);
        double fundamentalFrequency = MathOps.getfundamentalFrequency(peaks, freqBins);

        double depth = MathOps.compressionDepth(amplitudes, peaks.length, scaledTime, fundamentalFrequency, thetaAngles);
        double rate = MathOps.compressionRate(fundamentalFrequency);

        System.out.println(depth);
        System.out.println(rate);



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
        //System.out.println(file);

        //String str = new String(data.getBytes());

        try {
            stream.write(data.getBytes());
            //Log.d(TAG, "IMUWriteRawData: " + data.getBytes());
        } finally {
            stream.close();
        }

    }

    public double[] peakDetection(double[] fftSmooth) {

        int min_dist = 2;
        int numPeaks = 0;

        double max_val = MathOps.getMaxValue(fftSmooth);
        double min_val = MathOps.getMinValue(fftSmooth);

        double threshold = 0.3 * (max_val - min_val) + min_val;

        int h = 1; // intervals between datapoints


        double[] derivative = new double[fftSmooth.length - 1];
        int sizeDerivate = derivative.length;

        ArrayList<Integer> zeroes = new ArrayList<>();

        double[] peaks = new double[3];


        // Take the derivative of the interval of values
        for (int i = 0; i < sizeDerivate; i++) {
            derivative[i] = (fftSmooth[i + h] - fftSmooth[i]) / h;
            if (derivative[i] == 0) {
                zeroes.add(i);
            }

        }

        if (zeroes.size() == 0)
            return new double[1];

        return new double[1];
    }
}
