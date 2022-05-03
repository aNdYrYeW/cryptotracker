
//Standalone file
import java.awt.*;
import java.util.Arrays;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.JFrame;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.*;

public class Cryptotracker extends JFrame {
    Container c = getContentPane();
    public static Cryptotracker w = new Cryptotracker();
    String[] coinNames, oldCoinNames;
    double[] coinPrices, oldCoinPrices;
    boolean firstiter = true;

    // Main method which fetches API data every second, and draws a window full of
    // the prices.
    public static void main(String[] args) throws UnirestException {
        w.setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setVisible(true);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    w.GetAPIData();
                    w.Draw();
                } catch (UnirestException e) {
                }
            }
        }, 0, 1000);

    }

    // This is the main class which creates a window and formats it.

    public Cryptotracker() {
        super("Crypto Tracker - By Andrew Langan");
        c.setBackground(Color.WHITE);
        c.setLayout(new FlowLayout());
        c.setSize(Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // This is the method for handling the drawing of text to thescreen.
    public void Draw() {
        Graphics g = c.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        int i = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < coinNames.length / 8; y++) {
                if (coinPrices[i] != oldCoinPrices[i]) {
                    g2d.setPaint(Color.WHITE); // draws in the background color to "erase" the text so we only have to
                                               // change the updated values.
                    double value = (double) Math.round(oldCoinPrices[i] * 10000000.0)
                            / 10000000.0;
                    g2d.drawString(oldCoinNames[i] + ": " + value, (c.getWidth() / 8) * x,
                            (c.getHeight() / coinNames.length) * (y + 1) * 8);
                    g2d.setPaint(Color.GREEN); // draws the text green to signal that the value changed.
                    value = (double) Math.round(coinPrices[i] * 10000000.0) / 10000000.0;
                    g2d.drawString(coinNames[i] + ": " + value, (c.getWidth() / 8) * x,
                            (c.getHeight() / coinNames.length) * (y + 1) * 8);
                    final Integer fi = Integer.valueOf(i);
                    final Integer fx = Integer.valueOf(x);
                    final Integer fy = Integer.valueOf(y);
                    TimerTask tt = new TimerTask() {
                        // in three seconds, "erases" the text and then overwrites it in black.
                        public void run() {
                            g2d.setPaint(Color.WHITE);
                            double value = (double) Math.round(coinPrices[fi] * 10000000.0) / 10000000.0;
                            g2d.drawString(coinNames[fi] + ": " + value, (c.getWidth() / 8) * fx,
                                    (c.getHeight() / coinNames.length) * (fy + 1) * 8);
                            g2d.setPaint(Color.BLACK);
                            g2d.drawString(coinNames[fi] + ": " + value, (c.getWidth() / 8) * fx,
                                    (c.getHeight() / coinNames.length) * (fy + 1) * 8);
                        }
                    };
                    Timer t = new Timer();
                    t.schedule(tt, 3000);
                } else if (firstiter) {
                    // if this is the first run draw the text in black.
                    g2d.setPaint(Color.BLACK);
                    double value = (double) Math.round(coinPrices[i] * 10000000.0)
                            / 10000000.0;
                    g2d.drawString(coinNames[i] + ": " + value, (c.getWidth() / 8) * x,
                            (c.getHeight() / coinNames.length) * (y + 1) * 8);
                }
                i++;
            }
        }
        oldCoinNames = coinNames;
        oldCoinPrices = coinPrices;
        firstiter = false;
    }

    // This method fetches the current price of cryptocurrencies through Coinbase's
    // public API and directly modifies the String and double arrays of the
    // cryptocurrencies' price.
    public void GetAPIData() throws UnirestException {
        HttpResponse<JsonNode> coinRequest = Unirest.get("https://api.coinbase.com/v2/exchange-rates").asJson(); // coinbase
                                                                                                                 // url
        JSONObject coinResponse = new JSONObject(coinRequest.getBody());
        JSONObject coinJSONArray = coinResponse.getJSONArray("array").getJSONObject(0).getJSONObject("data")
                .getJSONObject("rates");
        coinNames = JSONObject.getNames(coinJSONArray); // convert coin names from JSON to an array.
        coinPrices = new double[coinNames.length];
        Arrays.sort(coinNames); // sort names to alphabetical order
        for (int i = 0; i < coinNames.length; i++) {
            coinPrices[i] = 1 / coinJSONArray.getDouble(coinNames[i]); // convert from price in currency to price in
                                                                       // USD.
        }
        if (firstiter) {
            // if this is the first run set the old prices so we know whether or not to
            // update.
            oldCoinNames = coinNames;
            oldCoinPrices = coinPrices;
        }
    }
}