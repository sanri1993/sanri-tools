package systest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class TestMain {
    public static void main(String[] args) {
        for (int i = 0; i < 256; i++) {
            final boolean[] booleans = byteToBoolArray((byte) i);
//            System.out.println(StringUtils.join(booleans,","));
            for (boolean aBoolean : booleans) {
                System.out.print(aBoolean+" ");
            }
            System.out.println();
        }

//        ArrayUtils.addAll()
    }

    public static boolean[] byteToBoolArray(byte value){
        boolean[] booleans = new boolean[8];
        for (int i = 0; i < booleans.length; i++) {
            booleans[i] = ((byte)(value >> i) & 0x1 ) == 0x1;
        }
        return booleans;
    }
}
