 package cecs.secureshare.connector.messages;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
     * Converts this object to a serialized string
     * @return
     */
    public String toSerializedString() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(this);
            so.flush();
            return bo.toString();
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Deserialize a string into a object that is castable to some Message subclass
     * @param serialized
     * @return
     */
    public static <T> T deserialize(String serialized, Class<T> clazz) {
        try {
            byte b[] = serialized.getBytes();
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return clazz.cast(si.readObject());
        } catch (IOException | ClassNotFoundException e) {
            Log.d(TAG, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
