package cecs.secureshare.connector;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Douglas on 12/7/2015.
 */
public class FileWriter {

    /**
     * Write input to file
     * @param context
     * @param input
     */
    public static void writeFile(Context context, InputStream input) {
        try {
            // write file to directory
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/secureshare-" + System.currentTimeMillis()
                    + ".jpg");

            File dirs = new File(f.getParent());
            if (!dirs.exists()) {
                dirs.mkdir();
            }
            f.createNewFile();
            copyFile(input, new FileOutputStream(f));
        } catch (IOException e) {

        }
    }

    /**
     * Writes input stream data into output stream
     * @param inputStream
     * @param outputStream
     * @return
     */
    public static boolean copyFile(InputStream inputStream, OutputStream outputStream) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
