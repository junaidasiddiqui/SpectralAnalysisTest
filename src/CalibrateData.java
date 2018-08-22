import java.util.ArrayList;
import java.util.Arrays;

public class CalibrateData {

    public CalibrateData() {}
    //Gets formatted data
    ArrayList<float[]> getData(String string) {
        String[] rawData = string.split(";;");
        ArrayList<float[]> formattedData =  new ArrayList<float[]>() ;

        for (int i = 0; i < rawData.length; i++) {
            formattedData.add(formatData(rawData[i]));
            //System.out.println(Arrays.toString(formattedData.get(i)));
        }

        //System.out.println(Arrays.toString(inputtedLine));
        return formattedData;
    }


    float[] formatData(String string) {
        String[] strArrayData = string.split(",");
        float[] inputtedLine = new float[strArrayData.length];


        for (int i = 0; i < strArrayData.length; i++) {
            strArrayData[i] = strArrayData[i].trim();
            inputtedLine[i] = Float.parseFloat(strArrayData[i]);
        }

        return inputtedLine;
    }

    int[] getTimearray(float[][] array2D) {

        int[] timeArray = new int[array2D.length];

        //System.out.println(timeArray.length);
        int i = 0;
        for (float[] floater : array2D) {
            timeArray[i] = (int) floater[0];
            i++;
        }
        //System.out.println(Arrays.toString(timeArray));

        return timeArray;
    }

    float[] getAccelerationFromRawData (float[][] array2D, int txyz) {
        float[] rawAcceleration = new float[array2D.length];

        int i = 0;
        for (float[] floater : array2D) {
            rawAcceleration[i] = floater[txyz];
            i++;

        }

          return rawAcceleration;
    }

    float[] scaleTime(float[][] array2D) {
        float[] time = new float[array2D.length];

        float zeroTime = array2D[0][0];

        int i = 0;
        for (float[] floater : array2D) {
            time[i] = (floater[0] - zeroTime) / 1000;
            i++;
        }

        return time;


    }

    float offsetAcceleration(float[] rawAcceleration, float offsetBenchmark, float offsetFailedVal) {

        float maxValue = MathOps.getMaxValue(rawAcceleration);
        float minValue = MathOps.getMinValue(rawAcceleration);
        float tmp = maxValue - minValue;

        //System.out.println(Arrays.toString(rawAcceleration));

        if (tmp > offsetBenchmark) {
            //System.out.println(Arrays.toString(rawAcceleration));
            return offsetFailedVal;
        }

        return MathOps.getMeanValue(rawAcceleration);
    }

    float[] setAcceleration(float[] rawAcceleration, float offsetVal, float GRAVITY) {

        float[] acceleration = new float[rawAcceleration.length];

        int i = 0;
        for (float floater : rawAcceleration) {
            acceleration[i] = (rawAcceleration[i] - offsetVal)*GRAVITY;
            i++;
        }

        return acceleration;
    }

}
