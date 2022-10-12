package com.hmdp;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GeneralTest {

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.stream().map(Long::valueOf).collect(Collectors.toList());
    }
}
