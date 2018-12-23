package tool;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.ext.sql.ResultSet;

import java.io.*;

public class ResultSetCover implements MessageCodec<ResultSet,ResultSet> {
    @Override
    public void encodeToWire(Buffer buffer, ResultSet s) {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o;
        try {
            o = new ObjectOutputStream(b);
            o.writeObject(s);
            o.close();
            buffer.appendBytes(b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResultSet decodeFromWire(int pos, Buffer buffer) {
        final ByteArrayInputStream b = new ByteArrayInputStream(buffer.getBytes());
        ObjectInputStream o = null;
        ResultSet msg = null;
        try {
            o = new ObjectInputStream(b);
            msg = (ResultSet) o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Override
    public ResultSet transform(ResultSet resultSet) {
        return resultSet;
    }

    @Override
    public String name() {
        return "resultSet";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
