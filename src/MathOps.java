import java.util.ArrayList;
import java.util.Arrays;

public class MathOps {

    static float getMaxValue(float[] array) {
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    static double getMaxValue(double[] array) {
        double maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    // getting the miniumum value
    static float getMinValue(float[] array) {
        float minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    static double getMinValue(double[] array) {
        double minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    static float getMeanValue(float[] array) {
        float sum = 0;
        for (int i = 1; i < array.length; i++) {
            sum += array[i];
        }
        return sum / array.length;
    }

    static float getSumofArray(double[] emptyWindow) {
        float sum = 0;
        for (double val : emptyWindow) {
            //System.out.println(val);
            sum += val;
        }

        return sum;
    }

    static double[] applyHanningWindow(float[] acceleration, int windowSize) {
        double[] window = new double[windowSize];
        double[] hanningApplied = new double[windowSize];

        for (int i = 0; i < windowSize; i ++) {
            double tmp = (0.5 - 0.5*Math.cos(2.0 * Math.PI * i/ (windowSize - 1)));

            window[i] = tmp;
        }

        double coherentGain = getSumofArray(window) /  windowSize;

        for (int i = 0; i < windowSize; i++) {
            hanningApplied[i] = acceleration[i] * window[i] / coherentGain;
        }

        return hanningApplied;
    }

    static double[] scaleFrequencyBins(int N, double frequency) {
        double[] freqBin = new double[(int)N/2];

        for (int i = 0; i < N/2; i ++)
            freqBin[i] = i*frequency/N;

        return freqBin;

    }

    static double getfundamentalFrequency(int[] indexes, double[] freqBins) {
        return freqBins[indexes[0]];
    }

    public static int[] peakDetection(double[] fftSmooth) {

        int min_dist = 5;
        int numPeaks = 0;

//        double max_val = MathOps.getMaxValue(fftSmooth);
//        double min_val = MathOps.getMinValue(fftSmooth);
//
//        double threshold = 2 * (max_val - min_val) + min_val;

        ArrayList<Integer> roots = new ArrayList<>();

        int[] cyclical = new int[3];

        int distBetweenPeaks = 2;
        // Traverse all the values until 3 peaks which fulfill the criteria are
        // found using a cyclical array.
        for (int i = 0; i < fftSmooth.length && numPeaks < 3; i++) {
            int temp = cyclical[1];
            cyclical[1] = cyclical[2];
            cyclical[0] = temp;
            cyclical[2] = i;
            if (cyclical[1] > cyclical[0] && cyclical[1] < cyclical[2]) {
                if ((fftSmooth[cyclical[1]] > fftSmooth[cyclical[0]] + 0.35) && (fftSmooth[cyclical[1]] > fftSmooth[cyclical[2]] + 0.35)
                        && (distBetweenPeaks > min_dist) && fftSmooth[cyclical[1]] > 1.5 ) {
                    roots.add(cyclical[1]);
                    numPeaks++;
                    distBetweenPeaks = 0;
                } else {
                    distBetweenPeaks++;
                }
            }
        }

        // Maps from Integer array list to int[]
        // Found: https://stackoverflow.com/a/23945015
        int[] peaks = roots.stream().mapToInt(i->i).toArray();

        if (roots.size() == 0)
            return new int[1];

        return peaks;
    }


    static double[] phaseAngles(int[] indexes, Complex[] fftPolarSingle) {
        int len = indexes.length;

        Complex[] z = new Complex[len];
        double[] theta = new double[len];

        if (len > 1) {
            for (int i = 0; i < len; i++) {
                z[i] = fftPolarSingle[indexes[i]];
                theta[i] = Math.atan2(z[i].im(), z[i].re());
            }
        }

        return theta;


    }

    public static double phaseAngles(int indexes, Complex[] fftPolarSingle) {
        Complex z = fftPolarSingle[indexes];
        return Math.atan2(z.im(), z.re());
    }

    static double[] peaksFromTransform(double[] fftSmooth, int[] peakIndex) {
        double[] amplitude = new double[peakIndex.length];

        for (int i = 0; i < peakIndex.length; i++)
            amplitude[i] = fftSmooth[peakIndex[i]];

        return amplitude;
    }


    public static double compressionDepth(double[] amplitude, int lenHarmonics, float[] time, double fundamentalFrequency, double[] thetaAngles) {
        int lenTime = time.length;
        final double PI = Math.PI;

        double[] A_k = new double[lenHarmonics];

        double[] tmp = new double[lenHarmonics];
        double[] S_k = new double[lenHarmonics];

        double[] sofT = new double[lenTime];

        double[] phiAngle = new double[lenHarmonics];

        for (int i = 0; i < lenHarmonics; i ++){
            A_k [i] = amplitude[i];

            tmp[i] = ((i+1) * 2.0 * PI * fundamentalFrequency) ;
            tmp[i] *= tmp[i];

            S_k[i] = 100*(A_k[i]/tmp[i]);

            phiAngle[i] = PI + thetaAngles[i];

        }

        tmp = new double[lenHarmonics];

        for (int i =0; i < lenHarmonics; i++)  {
            for (int j =0; j < lenTime; j++) {
                if (i == 0)
                    sofT[j] = S_k[i] * Math.cos(2 * PI * (i+1) *fundamentalFrequency * time[j] + phiAngle[i]);
                 else
                    sofT[j] += S_k[i] * Math.cos(2 * PI * (i+1) *fundamentalFrequency * time[j] + phiAngle[i]);
            }

        }


        double depth = getMaxValue(sofT) - getMinValue(sofT);

        return depth;
    }


    static double compressionRate(double fcc) {return fcc*60; }



}
