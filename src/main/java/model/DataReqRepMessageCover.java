package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;


public class DataReqRepMessageCover implements MessageCodec<DataReqRepMessage, DataReqRepMessage> {

	@Override
	public void encodeToWire(Buffer buffer, DataReqRepMessage s) {
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
	public DataReqRepMessage decodeFromWire(int pos, Buffer buffer) {

		final ByteArrayInputStream b = new ByteArrayInputStream(buffer.getBytes());
		ObjectInputStream o = null;
		DataReqRepMessage msg = null;
		try {
			o = new ObjectInputStream(b);
			msg = (DataReqRepMessage) o.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return msg;
	}

	@Override
	public DataReqRepMessage transform(DataReqRepMessage s) {

		return s;
	}

	@Override
	public String name() {

		return "DataMessage";
	}

	@Override
	public byte systemCodecID() {

		return -1;
	}

}
