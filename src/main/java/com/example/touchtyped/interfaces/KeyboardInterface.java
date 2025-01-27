package com.example.touchtyped.interfaces;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * implements a mock keyboard interface for use during development.
 * provides sending haptic commands, registering listeners, removing listeners, notifying listeners
 */
public class KeyboardInterface {

    private final List<KeypressListener> listeners;

    /**
     * constructor
     */
    public KeyboardInterface() {
        this.listeners = new ArrayList<>();
    }

    /**
     * attach an event handler to the specified scene to handle key presses and notify listeners
     * @param scene is the scene to attach to
     */
    public void attachToScene(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            String key = event.getText();
            if (!key.isEmpty()) {
                notifyListeners(key);
            } else {
                // check for special keys
                switch (event.getCode()) {
                    case BACK_SPACE:
                        notifyListeners("BACK_SPACE");
                        break;
                    case KeyCode.ENTER:
                        notifyListeners("ENTER");
                        break;
                    case ESCAPE:
                        notifyListeners("ESCAPE");
                        break;
                    case SHIFT:
                        notifyListeners("SHIFT");
                        break;
                    case CONTROL:
                        notifyListeners("CONTROL");
                        break;
                    case ALT:
                        notifyListeners("ALT");
                        break;
                    case LEFT:
                        notifyListeners("LEFT");
                        break;
                    case RIGHT:
                        notifyListeners("RIGHT");
                        break;
                    case UP:
                        notifyListeners("UP");
                        break;
                    case DOWN:
                        notifyListeners("DOWN");
                        break;
                    default:
                        notifyListeners("UNKNOWN");
                        break;
                }
            }
        });
    }

    /**
     * notifies all registered KeypressListeners of a keypress, calling their .onKeypress methods.
     */
    public void notifyListeners(String key) {
        for (KeypressListener listener : listeners) {
            listener.onKeypress(key);
        }
    }

    /**
     * registers a new listener to be notified of keypress events
     * @param listener is the listener to be registered
     */
    public void addKeypressListener(KeypressListener listener) {
        listeners.add(listener);
    }

    /**
     * removes a registered listener so that they are no longer notified of keypress events.
     * @param listener is the listener to be removed
     */
    public void removeKeypressListener(KeypressListener listener) {
        listeners.remove(listener);
    }

    /**
     * sends a command to the haptic motor beneath a key
     * TODO: still need to agree with hardware team on parameters
     * @param key is the key to vibrate
     * @param duration is the duration to vibrate (in milliseconds)
     * @param strength is the strength of the vibration.
     */
    public void sendHapticCommand(String key, int duration, int strength) {
        // for now, this method will simply output to the console.
        System.out.println(String.format("Key %s is vibrating for %d ms at strength %d", key, duration, strength));
    }

    /**
     * sends a command to the keyboard to turn the LED lights on for a certain amount of time.
     * @param duration is the duration to turn lights on for (in milliseconds)
     */
    public void activateLights(int duration) {
        // for now, this method will simply output to the console
        System.out.println(String.format("Turned LED lights on for %d milliseconds", duration));
    }

}
