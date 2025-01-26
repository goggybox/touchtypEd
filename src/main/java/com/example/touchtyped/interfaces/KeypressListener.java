package com.example.touchtyped.interfaces;

/**
 * a listener interface for receiving keyboard presses.
 * a class that implements KeypressListener can register with the keyboard interface with the .addKeypressListener method
 * to then automatically run their onKeypress method whenever a keypress occurs.
 * To do this, a KeypressListener should receive the instance of the keyboard interface in its constructor, and run the
 * .addKeypressListener method.
 */
public interface KeypressListener {

    /**
     * a class implementing KeypressListener will have the onKeypress method be triggered when a keypress is registered.
     * @param key is the key that was pressed
     */
    void onKeypress(String key);

}
