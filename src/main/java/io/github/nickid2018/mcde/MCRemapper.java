package io.github.nickid2018.mcde;

import java.io.File;
import java.util.zip.ZipFile;

public class MCRemapper {

    // First arg = MC Binary File
    // Second arg = MC Mapping
    // Third arg = Destination
    public static void main(String[] args) throws Exception {
        try (ZipFile file = new ZipFile(new File(args[0]))) {
            FileProcessor.process(file, new File(args[1]), new File(args[2]));
        }
    }
}
