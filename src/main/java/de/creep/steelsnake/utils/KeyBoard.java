// SteelSnake: File was created on 20.08.2020 by Creep (Discord: Creep#4924)

package de.creep.steelsnake.utils;

import com.sun.jndi.toolkit.url.Uri;
import de.creep.steelsnake.SteelSnake;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.IOUtils;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

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

    public KeyBoard() {
        discoverPort();
        setupSSEGame();
        bindFailEvent();
        bindKeys();
        bindClearEvent();
        clearKeyBoard();
    }

    public void triggerKey(String key, Color color) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("event", key);

        JSONObject dataObject = new JSONObject();
        JSONObject frameObject = new JSONObject();
        JSONObject keyColorObject = new JSONObject();
        keyColorObject.put("red", color.getRed());
        keyColorObject.put("green", color.getGreen());
        keyColorObject.put("blue", color.getBlue());
        frameObject.put("key-color", keyColorObject);
        dataObject.put("frame", frameObject);
        jsonObject.put("data", dataObject);

        sendRequest("game_event", jsonObject);
    }

    public void triggerKeys(String[] keys, Color color) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        JSONArray eventsArray = new JSONArray();

        for (String key : keys) {
            JSONObject eventObject = new JSONObject();
            eventObject.put("event", key.toUpperCase());

            JSONObject dataObject = new JSONObject();
            JSONObject frameObject = new JSONObject();
            JSONObject keyColorObject = new JSONObject();
            keyColorObject.put("red", color.getRed());
            keyColorObject.put("green", color.getGreen());
            keyColorObject.put("blue", color.getBlue());
            frameObject.put("key-color", keyColorObject);
            dataObject.put("frame", frameObject);
            eventObject.put("data", dataObject);
            eventsArray.add(eventObject);
        }

        jsonObject.put("events", eventsArray);
        sendRequest("multiple_game_events", jsonObject);
    }

    public void triggerFail() {
        steelSnake.setPaused(true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("event", "FAIL");

        JSONObject dataObject = new JSONObject();
        dataObject.put("value", new Random().nextInt(100));
        jsonObject.put("data", dataObject);

        sendRequest("game_event", jsonObject);
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
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
            JSONObject address = (JSONObject) new JSONParser().parse(sb.toString());
            port = Integer.parseInt(((String) address.get("address")).substring(10));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSSEGame() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("game_display_name", "SteelSnake");
        jsonObject.put("developer", "Creepios");
        jsonObject.put("deinitialize_timer_length_ms", 1000);
        sendRequest("game_metadata", jsonObject);
    }

    private void bindClearEvent() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("event", "CLEAR");
        jsonObject.put("min_value", 0);
        jsonObject.put("max_value", 100);
        jsonObject.put("icon_id", 1);

        JSONObject mainInfo = new JSONObject();
        mainInfo.put("device-type", "keyboard");
        mainInfo.put("zone", "all");
        mainInfo.put("mode", "count");

        JSONObject staticColor = new JSONObject();
        staticColor.put("red", 0);
        staticColor.put("green", 0);
        staticColor.put("blue", 0);
        mainInfo.put("color", staticColor);
        JSONArray handlers = new JSONArray();
        handlers.add(mainInfo);
        jsonObject.put("handlers", handlers);

        sendRequest("bind_game_event", jsonObject);
    }

    public void clearKeyBoard() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("event", "CLEAR");

        JSONObject dataObject = new JSONObject();
        dataObject.put("value", new Random().nextInt(100));
        jsonObject.put("data", dataObject);

        sendRequest("game_event", jsonObject);
    }

    private void bindFailEvent() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("game", "STEELSNAKE");
        jsonObject.put("event", "FAIL");
        jsonObject.put("min_value", 0);
        jsonObject.put("max_value", 100);
        jsonObject.put("icon_id", 1);

        JSONObject mainInfo = new JSONObject();
        mainInfo.put("device-type", "keyboard");
        mainInfo.put("zone", "all");
        mainInfo.put("mode", "percent");

        JSONObject staticColor = new JSONObject();
        staticColor.put("red", 194);
        staticColor.put("green", 56);
        staticColor.put("blue", 48);
        mainInfo.put("color", staticColor);

        JSONObject rateInfo = new JSONObject();
        rateInfo.put("frequency", 4);
        rateInfo.put("repeat_limit", 7);
        mainInfo.put("rate", rateInfo);

        JSONArray handlers = new JSONArray();
        handlers.add(mainInfo);
        jsonObject.put("handlers", handlers);

        sendRequest("bind_game_event", jsonObject);
    }

    private void bindKeys() {
        for (KBL[] chars : layout) {
            for (KBL letter : chars) {
                String key = letter.getKey();
                String alternative = letter.getAlternative().toUpperCase();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("game", "STEELSNAKE");
                jsonObject.put("event", alternative);
                jsonObject.put("min_value", 0);
                jsonObject.put("max_value", 100);
                jsonObject.put("icon_id", 1);

                JSONObject mainInfo = new JSONObject();
                mainInfo.put("device-type", "keyboard");
                mainInfo.put("zone", key);
                mainInfo.put("mode", "context-color");
                mainInfo.put("context-frame-key", "key-color");
                JSONArray handlers = new JSONArray();
                handlers.add(mainInfo);
                jsonObject.put("handlers", handlers);

                sendRequest("bind_game_event", jsonObject);
            }
        }
    }

    private void sendRequest(String urlPath, JSONObject object) {
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
            wr.writeBytes(object.toJSONString());
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = rd.readLine()) != null) {
            }
            rd.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("FAIL: " + object.toJSONString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
