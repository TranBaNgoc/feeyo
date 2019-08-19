package javaapplication6;


import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class NoiseSuppressTest {

    public static void main(String[] args) throws LineUnavailableException {

        NoiseSuppress ns = new NoiseSuppress(8000, 1024);
//		NoiseSample noiseSample = new NoiseSample();
//		String filePath = "F:\\test\\1.pcm";
//        byte[] fileBuff = null;
//		try {
//			fileBuff = Files.readAllBytes(new File(filePath.toString()).toPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        
        DataLine.Info info1 = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info1)) {
            throw new LineUnavailableException("not supported");
        }
        TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        // Obtain and open the line.
        microphone.open(format);
        // Begin audio capture.
        microphone.start();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("not supported");
        }
        SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
        speaker.open();
        speaker.start();
        int i = 0;
        long start = System.nanoTime();
        while (true) {

            byte[] buff11;
//            System.arraycopy(tempbuff, 0, buff11, 0, 2048);
//toShortArray(buff11);
            //short[] ioPcm = recordFromMicrophone(microphone);
            short[] ioPcm = toShortArray(NoiseSample.WHITE_NOISE);
            
            ioPcm = ns.noiseReductionProcess(ioPcm);
//            buff11 = toByteArray(ioPcm);
//			write(buff11);
//
//            speaker.write(buff11, 0, buff11.length);

            buff11 = toByteArray(ioPcm);
            
            System.out.println(((System.nanoTime() -  start) / 1000000000)+ " ------- " + Arrays.toString(buff11));
            speaker.write(buff11, 0, buff11.length);
        }

    }

    private static short[] recordFromMicrophone(TargetDataLine microphone) throws LineUnavailableException {
        // Assume that the TargetDataLine, line, has already been obtained and
        // opened.

        byte[] data = new byte[48000];
        // probably way too big
        // Here, stopped is a global boolean set by another thread.
        int numBytesRead;
        numBytesRead = microphone.read(data, 0, data.length);
        //System.out.println("rawEnc:"+StringUtil.toHexString(data));
        ShortBuffer shortBuffer = ShortBuffer.allocate(numBytesRead * 2);
        shortBuffer.put(ByteBuffer.wrap(data).asShortBuffer());
        shortBuffer.flip();

        short[] tmp = new short[shortBuffer.remaining()];
        shortBuffer.duplicate().get(tmp);

        return tmp;
    }

    public static byte[] toByteArray(short[] shortArr) {
        byte[] byteArr = new byte[shortArr.length * 2];
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArr);
        return byteArr;
    }

    public static short[] toShortArray(byte[] byteArr) {
        short[] shortArr = new short[byteArr.length / 2];
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArr);
        return shortArr;
    }

    private static byte[] ShortToByte_Twiddle_Method(final short[] input) {
        final int len = input.length;
        final byte[] buffer = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            buffer[(i * 2) + 1] = (byte) (input[i]);
            buffer[(i * 2)] = (byte) (input[i] >> 8);
        }
        return buffer;
    }

    private static void write(byte[] content) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("E:\\1_a2.pcm", true);
            fos.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
