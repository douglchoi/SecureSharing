 package cecs.secureshare.connector.messages;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Douglas on 12/6/2015.
 */
public abstract class Message implements Serializable {

    protected static final String TAG = "Message";

    public enum Action {
        SEND_INFO, SEND_FILE
    };

    protected Action action;

    public Message(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    /**
     * Writes this message as serialized to output stream
     * @param oos
     */
    public boolean writeToOutputStream(ObjectOutputStream oos) {
        try {
            oos.writeObject(this);
            oos.flush();
            return true;
        } catch(IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Reads the input stream as serialized Message object
     * @param ois
     * @return
     */
    public static Message readInputStream(ObjectInputStream ois) {
         try {
             return (Message) ois.readObject();
         } catch (IOException | ClassNotFoundException e) {
             Log.d(TAG, e.getLocalizedMessage(), e);
             return null;
         }
    }

    /**
     * Reads input stream and returns object
     * @param ois
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T readInputStream(ObjectInputStream ois, Class<T> clazz) {
        try {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
