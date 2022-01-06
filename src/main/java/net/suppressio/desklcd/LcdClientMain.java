package net.suppressio.desklcd;

import org.lcdproc.lcdjava.LCD;
import org.lcdproc.lcdjava.LCDException;
import org.lcdproc.lcdjava.NumWidget;
import org.lcdproc.lcdjava.Screen;

import java.util.Calendar;

/**
 *
 */
public class LcdClientMain extends Thread {
    private final LCD lcd;

    private static final int numPos[] = {1, 4, 8, 11, 15, 18};
    private static final int colonPos[] = {7, 14};

    private final NumWidget[] numbers;
    private final NumWidget[] colons;

    private LcdClientMain(String host, int port) throws LCDException {
        // Connect to LCDd
        lcd = new LCD(host, port);
        System.out.println("Connected to LCDd: " + lcd);

        // Construct and activate a Screen
        Screen clockScreen = lcd.constructScreen("clock", Screen.PRIORITY_FOREGROUND, true);

        int numDigits;
        int numColons;
        if (lcd.getWidth() >= 20) {
            numDigits = 6;
            numColons = 2;
        } else {
            numDigits = 4;
            numColons = 1;
        }
        // Center the clock on the screen
        int xOffset = (lcd.getWidth() - (numPos[numDigits - 1] + 2)) / 2;

        // Add some Widgets to the screen
        numbers = new NumWidget[numDigits];
        for (int i = 0; i < numDigits; i++) {
            int x = numPos[i];
            NumWidget num = NumWidget.construct(clockScreen, x + xOffset, 0);
            boolean success = num.activate();
            assert success;
            numbers[i] = num;
        }

        colons = new NumWidget[numColons];
        for (int i = 0; i < numColons; i++) {
            int x = colonPos[i];
            NumWidget c = NumWidget.construct(clockScreen, x + xOffset, 10);
            boolean success = c.activate();
            assert success;
            colons[i] = c;
        }
    }

    @Override
    public void run() {
        // Update the display twice per second
        final int h = 500;
        long duration;
        long t = System.currentTimeMillis();

        try {
            while (!interrupted()) {
                Calendar now = Calendar.getInstance();
                int hours = now.get(Calendar.HOUR_OF_DAY);
                int minutes = now.get(Calendar.MINUTE);
                int seconds = now.get(Calendar.SECOND);
                int[] digits = new int[]{
                        hours / 10, hours % 10,
                        minutes / 10, minutes % 10,
                        seconds / 10, seconds % 10
                };
                for (int i = 0; i < numbers.length; i++) {
                    numbers[i].setNumber(digits[i]);
                }

                if (now.get(Calendar.MILLISECOND) > 500) {
                    for (NumWidget c : colons) {
                        c.setNumber(11); // space
                    }
                } else {
                    for (NumWidget c : colons) {
                        c.setNumber(10); // colon
                    }
                }

                t += h;
                duration = t - System.currentTimeMillis();
                if (duration > 0) {
                    sleep(duration);
                }
            }
        } catch (InterruptedException e) {
            //_log.debug("Interrupted");
        } finally {
            // Shut down the LCD both on requested termination (interrupt) and on error
            try {
                lcd.shutdown();
            } catch (LCDException e) {
                e.printStackTrace();
            }
        }
        //_log.debug("Terminating");
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java BigClock <LCD host> <LCD port>");
            System.exit(1);
        }

        // SLF4J-Simple default log level is INFO. Set it to DEBUG:
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        final LcdClientMain clock = new LcdClientMain(host, port);
        clock.start();

        // Let the clock shut down cleanly on exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clock.interrupt();
                try {
                    clock.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Press Ctrl+C to exit");
    }
}
