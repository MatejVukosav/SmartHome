package websocket;

import models.Device;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mvukosav on 4.1.2017..
 */
@ApplicationScoped
public class DeviceSessionHandler {
    String TAG = DeviceSessionHandler.class.getCanonicalName();
    private int deviceId = 0;
    private final List<Session> sessions;
    private final List<Device> devices;

    public DeviceSessionHandler() {
        sessions = new ArrayList<>();
        devices = new ArrayList<>();
    }

    public void addSession(Session session) {
        session.setMaxIdleTimeout(1000);
        sessions.add(session);

        Device d = new Device();
        d.setStatus("off");
        d.setId(112);
        d.setType("dasd");
        d.setDescription("nameed");
        d.setName("dads");
        devices.add(d);

        //send the list of the devices to the connected client
        for (Device device : devices) {
            JsonObject addMessage = createAddMessage(device);
            sendToSession(session, addMessage);
        }
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void addDevice(Device device) {
        device.setId(deviceId);
        devices.add(device);

        //increment unique device identificator
        deviceId++;
        //notify all clients about new device
        JsonObject addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }

    public void removeDevice(int id) {
        Device device = getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder()
                    .add("action", "remove")
                    .add("id", id)
                    .build();
            sendToAllConnectedSessions(removeMessage);
        }
    }

    public void toggleDevice(int id) {
        JsonProvider provider = JsonProvider.provider();
        Device device = getDeviceById(id);
        if (device != null) {
            if ("On".equals(device.getStatus())) {
                device.setStatus("Off");
            } else {
                device.setStatus("On");
            }
            JsonObject updateDeviceMessage = provider.createObjectBuilder()
                    .add("action", "toggle")
                    .add("id", id)
                    .add("status", device.getStatus())
                    .build();
            sendToAllConnectedSessions(updateDeviceMessage);
        }
    }

    private Device getDeviceById(int id) {
        for (Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }

    private JsonObject createAddMessage(Device device) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("action", "add")
                .add("id", device.getId())
                .add("name", device.getName())
                .add("type", device.getType())
                .add("description", device.getDescription())
                .add("status", device.getStatus())
                .build();
        return addMessage;
    }

    private void sendToAllConnectedSessions(JsonObject message) {
        for (Session session : sessions) {
            sendToSession(session, message);
        }
    }

    private void sendToSession(Session session, JsonObject message) {
        try {
            String msg = message.toString();
            System.out.println(msg);
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            if (basic != null) {
                basic.sendText(message.toString());
            } else {
                System.out.println("basic je null");
            }
        } catch (IOException e) {
            sessions.remove(session);
            Logger.getLogger(TAG).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, e);
        }
    }

}
