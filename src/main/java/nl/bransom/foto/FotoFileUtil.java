package nl.bransom.foto;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FotoFileUtil {

  private static Charset UTF8 = Charset.forName("UTF8");
  private static String SEPARATOR = "|";
  private static String NEWLINE = "\r\n";
  private static byte[] NEWLINE_BYTES = NEWLINE.getBytes(UTF8);

  public static Map<FotoSpec, Set<String>> parse(final File outputFile) throws IOException {
    final String[] lineStrings = new String(Files.readAllBytes(Paths.get(outputFile.toURI())), UTF8).split(NEWLINE);

    final Map<FotoSpec, Set<String>> fotoSpecMap = new ConcurrentHashMap<>();
    Arrays.asList(lineStrings).stream().forEach(lineString -> {
      final int seperatorIndex = lineString.lastIndexOf(SEPARATOR);
      if (seperatorIndex >= 0) {
        final String path = lineString.substring(seperatorIndex + SEPARATOR.length());
        final FotoSpec fotoSpec = new FotoSpec(lineString.substring(0, seperatorIndex));
        Set<String> pathSet = fotoSpecMap.get(fotoSpec);
        if (pathSet == null) {
          pathSet = new HashSet<>();
          fotoSpecMap.put(fotoSpec, pathSet);
        }
        pathSet.add(path);
      }
    });
    return fotoSpecMap;
  }

  public static void writeFotoSpec(final File rootDir, final File fotoFile, final OutputStream os) {
    try {
      final FotoSpec fotoSpec = new FotoSpec(fotoFile);
      final String path = getPath(rootDir, fotoFile);
      final String lineString = fotoSpec.toString() + SEPARATOR + path;
      os.write(lineString.getBytes(UTF8));
      os.write(NEWLINE_BYTES);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String getPath(final File rootDir, final File fotoFile) {
    String path = fotoFile.getParent().replace(rootDir.getPath(), "");
    if (path.startsWith(File.separator)) {
      path = path.substring(File.separator.length());
    }
    if (path.endsWith(File.separator)) {
      path = path.substring(0, path.length() - File.separator.length());
    }
    return path;
  }

}
