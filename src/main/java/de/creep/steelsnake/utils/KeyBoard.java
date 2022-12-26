// SteelSnake: File was created on 20.08.2020 by Creep (Discord: Creep#4924)

package de.creep.steelsnake.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.creep.steelsnake.SteelSnake;
import de.cybotic.simplegson.SimpleGson;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.*;

public class KeyBoard {

    public static final int width = 10;
    public static final int height = 4;

    private static final KBL[][] layout = {
            {new KBL("keyboard-1"), new KBL("keyboard-2"), new KBL("keyboard-3"), new KBL("keyboard-4"), new KBL("keyboard-5"), new KBL("keyboard-6"), new KBL("keyboard-7"), new KBL("keyboard-8"), new KBL("keyboard-9"), new KBL("keyboard-0")},
            {new KBL("q"), new KBL("w"), new KBL("e"), new KBL("r"), new KBL("t"), new KBL("y"), new KBL("u"), new KBL("i"), new KBL("o"), new KBL("p")},
            {new KBL("a"), new KBL("s"), new KBL("d"), new KBL("f"), new KBL("g"), new KBL("h"), new KBL("j"), new KBL("k"), new KBL("l"), new KBL("semicolon")},
            {new KBL("z"), new KBL("x"), new KBL("c"), new KBL("v"), new KBL("b"), new KBL("n"), new KBL("m"), new KBL("comma"), new KBL("period"), new KBL("slash")},
    };

    private Integer port = 0;
    private final SteelSnake steelSnake = SteelSnake.getInstance();
    private final SimpleGson simpleGson;

    public KeyBoard() {
        simpleGson = steelSnake.getSimpleGson();
        discoverPort();
        setupSSEGame();
        bindFailEvent();
        bindKeys();
        bindClearEvent();
        clearKeyBoard();
    }

    public void triggerKey(String key, Color color) {
        JsonObject gameevent = new JsonObject();
        simpleGson.setObject(gameevent, "game", "STEELSNAKE");
        simpleGson.setObject(gameevent, "event", key);
        JsonObject keycolor = new JsonObject();
        keycolor.add("red", new JsonPrimitive(color.getRed()));
        keycolor.add("green", new JsonPrimitive(color.getGreen()));
        keycolor.add("blue", new JsonPrimitive(color.getBlue()));

        //simpleGson.setObject(gameevent, "data/frame/key-color/red", color.getRed());
        //simpleGson.setObject(gameevent, "data/frame/key-color/green", color.getGreen());
        //simpleGson.setObject(gameevent, "data/frame/key-color/blue", color.getBlue());
        simpleGson.setObject(gameevent, "data/frame/key-color", keycolor);

        sendRequest("game_event", gameevent);
    }

    public void triggerKeys(String[] keys, Color color) {
        JsonObject keyTrigger = new JsonObject();
        simpleGson.setObject(keyTrigger, "game", "STEELSNAKE");
        JsonArray eventsArray = new JsonArray();

        for (String key : keys) {
            JsonObject eventObject = new JsonObject();
            JsonObject keycolor = new JsonObject();
            keycolor.add("red", new JsonPrimitive(color.getRed()));
            keycolor.add("green", new JsonPrimitive(color.getGreen()));
            keycolor.add("blue", new JsonPrimitive(color.getBlue()));
            simpleGson.setObject(eventObject, "event", key.toUpperCase());
            System.out.println("keycolor1: " + eventObject);
            simpleGson.setObject(eventObject, "data/frame/key-color/red", color.getRed());
            System.out.println("keycolor2: " + eventObject);
            simpleGson.setObject(eventObject, "data/frame/key-color/green", color.getGreen());
            System.out.println("keycolor3: " + eventObject);
            simpleGson.setObject(eventObject, "data/frame/key-color/blue", color.getBlue());
            //simpleGson.setObject(eventObject, "data/frame/key-color", keycolor);

            eventsArray.add(eventObject);
            System.out.println("keycolor4: " + eventObject);
        }

        simpleGson.setObject(keyTrigger, "events", eventsArray);
        sendRequest("multiple_game_events", keyTrigger);
    }

    public void triggerFail() {
        steelSnake.setPaused(true);

        JsonObject triggerFail = new JsonObject();
        simpleGson.setObject(triggerFail, "game", "STEELSNAKE");
        simpleGson.setObject(triggerFail, "event", "FAIL");
        simpleGson.setObject(triggerFail, "data/value", new Random().nextInt(100));

        sendRequest("game_event", triggerFail);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearKeyBoard();
                steelSnake.setPaused(false);
            }
        }, 1825);
    }

    public void printConsoleField() {
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout[i].length; j++) {
                if (steelSnake.getSnake().contains(new Position(j, i))) {
                    System.out.print("⚬ ");
                } else if (steelSnake.getApple().equals(new Position(j, i))) {
                    System.out.print("★ ");
                } else {
                    System.out.print("▪ ");
                }

            }
            System.out.println();
        }

        System.out.println();
        System.out.println();
    }

    public void printField() {
        List<String> strings = new ArrayList<>();
        for (Position position : steelSnake.getSnake()) {
            KBL kbl = layout[position.getY()][position.getX()];
            strings.add(kbl.getAlternative());
        }

        KBL apple = layout[steelSnake.getApple().getY()][steelSnake.getApple().getX()];
        triggerKey(apple.getAlternative().toUpperCase(), new Color(0, 255, 21));

        triggerKeys(strings.toArray(new String[0]), new Color(255, 204, 18));
        if (steelSnake.getTail() != null) {
            KBL tail = layout[steelSnake.getTail().getY()][steelSnake.getTail().getX()];
            triggerKey(tail.getAlternative().toUpperCase(), new Color(0, 0, 0));
        }
    }

    private void discoverPort() {
        String programData = System.getenv("PROGRAMDATA");
        File file = new File(programData + "/SteelSeries/SteelSeries Engine 3/coreProps.json");
        try {
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader isReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isReader);
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }

            JsonObject addressObject = (JsonObject) JsonParser.parseString(sb.toString());
            String address = simpleGson.getObject(addressObject, "address", new TypeToken<String>(){}.getType());
            port = Integer.parseInt((address).substring(10));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSSEGame() {
        JsonObject setupGame = new JsonObject();
        simpleGson.setObject(setupGame, "game", "STEELSNAKE");
        simpleGson.setObject(setupGame, "game_display_name", "SteelSnake");
        simpleGson.setObject(setupGame, "developer", "Creepios");
        simpleGson.setObject(setupGame, "deinitialize_timer_length_ms", 1000);

        sendRequest("game_metadata", setupGame);
    }

    private void bindClearEvent() {
        JsonObject bindClear = new JsonObject();
        simpleGson.setObject(bindClear, "game", "STEELSNAKE");
        simpleGson.setObject(bindClear, "event", "CLEAR");
        simpleGson.setObject(bindClear, "min_value", 0);
        simpleGson.setObject(bindClear, "max_value", 100);
        simpleGson.setObject(bindClear, "icon_id", 1);

        JsonObject mainInfo = new JsonObject();
        simpleGson.setObject(mainInfo, "device-type", "keyboard");
        simpleGson.setObject(mainInfo,"zone", "all");
        simpleGson.setObject(mainInfo,"mode", "count");

        simpleGson.setObject(mainInfo,"color/red", 0);
        simpleGson.setObject(mainInfo,"color/green", 0);
        simpleGson.setObject(mainInfo,"color/blue", 0);

        JsonArray handlers = new JsonArray();
        handlers.add(mainInfo);
        simpleGson.setObject(bindClear,  "handlers", handlers);

        sendRequest("bind_game_event", bindClear);
    }

    public void clearKeyBoard() {
        JsonObject clearKeyBoard = new JsonObject();
        simpleGson.setObject(clearKeyBoard, "game", "STEELSNAKE");
        simpleGson.setObject(clearKeyBoard, "event", "CLEAR");
        simpleGson.setObject(clearKeyBoard, "data/value", new Random().nextInt(100));

        sendRequest("game_event", clearKeyBoard);
    }

    private void bindFailEvent() {
        JsonObject bindFail = new JsonObject();
        simpleGson.setObject(bindFail, "game", "STEELSNAKE");
        simpleGson.setObject(bindFail, "event", "FAIL");
        simpleGson.setObject(bindFail, "min_value", 0);
        simpleGson.setObject(bindFail, "max_value", 100);
        simpleGson.setObject(bindFail, "icon_id", 1);

        JsonObject mainInfo = new JsonObject();
        simpleGson.setObject(mainInfo, "device-type", "keyboard");
        simpleGson.setObject(mainInfo, "zone", "all");
        simpleGson.setObject(mainInfo, "mode", "percent");

        simpleGson.setObject(mainInfo, "color/red", 194);
        simpleGson.setObject(mainInfo, "color/green", 56);
        simpleGson.setObject(mainInfo, "color/blue", 48);

        simpleGson.setObject(mainInfo, "rate/frequency", 4);
        simpleGson.setObject(mainInfo, "rate/repeat_limit", 7);

        JsonArray handlers = new JsonArray();
        handlers.add(mainInfo);
        simpleGson.setObject(bindFail, "handlers", handlers);

        sendRequest("bind_game_event", bindFail);
    }

    private void bindKeys() {
        for (KBL[] chars : layout) {
            for (KBL letter : chars) {
                String key = letter.getKey();
                String alternative = letter.getAlternative().toUpperCase();
                JsonObject bindKeys = new JsonObject();
                simpleGson.setObject(bindKeys, "game", "STEELSNAKE");
                simpleGson.setObject(bindKeys, "event", alternative);
                simpleGson.setObject(bindKeys, "min_value", 0);
                simpleGson.setObject(bindKeys, "max_value", 100);
                simpleGson.setObject(bindKeys, "icon_id", 1);

                JsonObject mainInfo = new JsonObject();
                simpleGson.setObject(mainInfo, "device-type", "keyboard");
                simpleGson.setObject(mainInfo, "zone", key);
                simpleGson.setObject(mainInfo, "mode", "context-color");
                simpleGson.setObject(mainInfo, "context-frame-key", "key-color");

                JsonArray handlers = new JsonArray();
                handlers.add(mainInfo);
                simpleGson.setObject(bindKeys, "handlers", handlers);

                sendRequest("bind_game_event", bindKeys);
            }
        }
    }

    private void sendRequest(String urlPath, JsonObject object) {
        System.out.println(object);
        URL url = null;
        try {
            url = new URI("http://127.0.0.1:" + port + "/" + urlPath).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(object.toString());
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = rd.readLine()) != null) {
            }
            rd.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAIL: " + object.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
