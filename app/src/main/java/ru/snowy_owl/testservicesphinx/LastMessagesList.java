package ru.snowy_owl.testservicesphinx;

import android.text.TextUtils;

import java.util.ArrayList;

public class LastMessagesList extends ArrayList<String> {
    private final int mMaxSize;

    public LastMessagesList(int size) {
        mMaxSize = size;
    }

    public boolean add(String str) {
        boolean r = super.add(str);
        if (size() > mMaxSize) {
            removeRange(0, size() - mMaxSize - 1);
        }
        return r;
    }

    @Override
    public String toString() {
        return TextUtils.join("\n", this);
    }
}
