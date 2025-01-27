package com.example.touchtyped.model;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.interfaces.KeypressListener;

public class ExampleKeypressListener implements KeypressListener {

    /**
     * constructor that automatically registers this instance as a listener with the KeyboardInterface.
     * @param keyboardInterface is the keyboard interface to register with
     */
    public ExampleKeypressListener(KeyboardInterface keyboardInterface) {
        keyboardInterface.addKeypressListener(this);
    }

    @Override
    public void onKeypress(String key) {

        System.out.println(String.format("ExampleKeypressListener has received key: (%s)", key));

    }

}
