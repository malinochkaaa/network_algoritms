package org.example;


import peersim.Simulator;
import peersim.core.CommonState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] arg = new String[1];
        String config = "dij-random";
        arg[0] = String.format("config/%s.txt", config);
        long start = System.currentTimeMillis();
        Simulator.main(arg);
        long end = System.currentTimeMillis();
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long duration = (end - start) / 1000;
        String filename = String.format("criteria/results.txt");
        File f = new File(filename);
        List<String> list = null;
        try {
            Path tmp = Path.of("tmp.txt");
            list = Files.readAllLines(tmp);
            Files.delete(tmp);
            Files.delete(Path.of("tmp_coords.txt"));
        } catch (IOException e) {}
        int counter = 0;
        for (String s : list) {
            counter += Integer.parseInt(s);
        }
        int paths = list.size();
        float average = (float)counter/paths;
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            Files.write(Paths.get(filename), String.format("%s time:%d memory:%d nodes:%4f\n", config, duration, memory/1024, average).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {}
    }
}