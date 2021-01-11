/*
 * Copyright (C) Evergreen [2020 - 2021]
 * This program comes with ABSOLUTELY NO WARRANTY
 * This is free software, and you are welcome to redistribute it
 * under the certain conditions that can be found here
 * https://www.gnu.org/licenses/lgpl-3.0.en.html
 */

package com.evergreenclient.hudmod.settings.impl;

import com.evergreenclient.hudmod.settings.Setting;

public class IntegerSetting extends Setting {

    private int val;
    private final int min, max;
    private final String suffix;

    public IntegerSetting(String name, int val, int min, int max, String suffix) {
        super(name);
        this.val = val;
        this.min = min;
        this.max = max;
        this.suffix = suffix;
    }

    public int get() {
        return val;
    }

    public void set(int newVal) {
        this.val = newVal;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getSuffix() {
        return suffix;
    }

}