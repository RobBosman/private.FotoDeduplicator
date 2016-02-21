package nl.bransom.foto;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DirScanner {

  public static Set<File> scanDir(final File rootDir) throws IOException {
    final Set<File> fileSet = new HashSet<>();
    doScanDir(rootDir, rootDir, fileSet);
    return fileSet;
  }

  private static void doScanDir(final File rootDir, final File file, final Set<File> fileSet) {
    if (file.isDirectory()) {
      Arrays.asList(file.listFiles()).stream().forEach(subFile -> doScanDir(rootDir, subFile, fileSet));
    } else {
      fileSet.add(file);
    }
  }

}
