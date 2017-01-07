package websocket;

import models.Device;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mvukosav on 4.1.2017..
 * <p>
 * http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/HomeWebsocket/WebsocketHome.html#
 */
@ApplicationScoped
@ServerEndpoint("/actions")
public class DeviceWebSocketServer {
    String TAG = DeviceWebSocketServer.class.getCanonicalName();

    @Inject
    private DeviceSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {

        Logger.getLogger(TAG)
                .log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = jsonReader.readObject();

            switch (jsonMessage.getString("action")) {
                case "add":
                    Device newDevice = createDeviceFromJsonMessage(jsonMessage);
                    sessionHandler.addDevice(newDevice);
                    break;
                case "remove":
                    int idFromDeviceToBeRemoved = jsonMessage.getInt("id");
                    sessionHandler.removeDevice(idFromDeviceToBeRemoved);
                    break;
                case "toggle":
                    int idFromDeviceToBeToggled = jsonMessage.getInt("id");
                    sessionHandler.toggleDevice(idFromDeviceToBeToggled);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Device createDeviceFromJsonMessage(JsonObject jsonMessage) {
        Device device = new Device();
        device.setName(jsonMessage.getString("name"));
        device.setDescription(jsonMessage.getString("description"));
        device.setStatus("Off");
        device.setType(jsonMessage.getString("type"));
        return device;
    }

}
