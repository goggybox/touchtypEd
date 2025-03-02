package com.example.touchtyped.interfaces;

import com.example.touchtyped.app.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import com.fazecast.jSerialComm.*;
import java.io.PrintWriter;
import java.util.Scanner;

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
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode keyCode = event.getCode();

            if (keyCode.isLetterKey() || keyCode.isDigitKey()) {
                String key = event.getText();
                notifyListeners(key.toUpperCase());
            } else {
                switch (keyCode) {
                    case BACK_SPACE:
                        notifyListeners("BACK_SPACE");
                        break;
                    case ENTER:
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
                    case TAB:
                        notifyListeners("TAB");
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
                    case SPACE:
                        notifyListeners(" ");
                        break;
                    case SEMICOLON:
                        notifyListeners("SEMICOLON");
                        break;
                    case QUOTE:
                        notifyListeners("QUOTE");
                        break;
                    case NUMBER_SIGN: // this key represents the key with # on a British keyboard
                        notifyListeners("HASHTAG");
                        break;
                    case OPEN_BRACKET:
                        notifyListeners("OPEN_BRACKET");
                        break;
                    case CLOSE_BRACKET:
                        notifyListeners("CLOSE_BRACKET");
                        break;
                    case COMMA:
                        notifyListeners("COMMA");
                        break;
                    case CAPS:
                        notifyListeners("CAPS");
                        break;
                    case PERIOD:
                        notifyListeners("PERIOD");
                        break;
                    case SLASH:
                        notifyListeners("SLASH");
                        break;
                    case BACK_SLASH:
                        notifyListeners("BACK_SLASH");
                        break;
                    case BACK_QUOTE:
                        notifyListeners("BACK_QUOTE");
                        break;
                    case EQUALS:
                        notifyListeners("EQUALS");
                        break;
                    case MINUS:
                        notifyListeners("MINUS");
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
        PrintWriter keyCommand = new PrintWriter(Application.ioPort.getOutputStream(),true);
        switch (key) {
            case "b":
                keyCommand.print(2);
                break;
            case "c":
                keyCommand.print(3);
                break;
            case "d":
                keyCommand.print(4);
                break;
            case "e":
                keyCommand.print(5);
                break;
            case "f":
                keyCommand.print(6);
                break;
            case "g":
                keyCommand.print(7);
                break;
            case "h":
                keyCommand.print(8);
                break;
        }

    }

    public void stopHaptic(){
        PrintWriter keyCommand = new PrintWriter(Application.ioPort.getOutputStream(),true);
        keyCommand.print(-1);
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
