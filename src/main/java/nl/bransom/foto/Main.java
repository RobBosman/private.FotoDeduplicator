package nl.bransom.foto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.Holder;

public class Main {

  public static final File PHOTOSYNC_DIR = new File("C:\\Users\\Rob\\Documents\\photoSync\\");
  public static final File PHOTOSYNC_FOTOSPEC_FILE = new File("C:\\Users\\Rob\\Downloads\\photoSync.txt");
  public static final File BOSMANNAS_DIR = new File("\\\\BOSMANNAS\\photo\\");
  public static final File BOSMANNAS_FOTOSPEC_FILE = new File("C:\\Users\\Rob\\Downloads\\BosmanNAS.txt");

  private static String[] DELETE_DIRS = { "Recycle bin", "Auto Upload" };
  private static String[] IGNORE_DUPLICATE_DIRS = { DELETE_DIRS[0], DELETE_DIRS[1], "public", "_dup" };
  private static String[] IGNORE_WRONG_DATE_DIRS = { "0000-00-00 gescand" };

  public static void main(String[] args) throws IOException {
    {
      if (!PHOTOSYNC_FOTOSPEC_FILE.exists()) {
        scanDirAndStore(PHOTOSYNC_DIR, PHOTOSYNC_FOTOSPEC_FILE);
      }
      final Map<FotoSpec, Set<String>> photoSpecMap = FotoFileUtil.parse(PHOTOSYNC_FOTOSPEC_FILE);

      System.out.println("#################### photoSync.txt undeletes ######################");
      checkForUndeletes(photoSpecMap);

      System.out.println("#################### photoSync.txt duplicates #####################");
      checkForDuplicates(photoSpecMap);

      System.out.println("#################### photoSync.txt wrong years ####################");
      checkForWrongYears(photoSpecMap);
    }

    {
      if (!BOSMANNAS_FOTOSPEC_FILE.exists()) {
        scanDirAndStore(BOSMANNAS_DIR, BOSMANNAS_FOTOSPEC_FILE);
      }
      final Map<FotoSpec, Set<String>> photoSpecMap = FotoFileUtil.parse(BOSMANNAS_FOTOSPEC_FILE);

      System.out.println("#################### BosmanNAS.txt undeletes ######################");
      checkForUndeletes(photoSpecMap);

      System.out.println("#################### BosmanNAS.txt duplicates #####################");
      checkForDuplicates(photoSpecMap);

      System.out.println("#################### BosmanNAS.txt wrong years ####################");
      checkForWrongYears(photoSpecMap);
    }
  }

  public static void scanDirAndStore(final File rootDir, final File outputFile) throws IOException {
    try (final OutputStream os = new FileOutputStream(outputFile)) {
      DirScanner.scanDir(rootDir).stream()
          .forEach(photoFile -> FotoFileUtil.writeFotoSpec(rootDir, photoFile, os));
    }
  }

  public static void checkForUndeletes(final Map<FotoSpec, Set<String>> photoSpecMap) throws IOException {
    photoSpecMap.entrySet().stream().filter(entry -> isUndelete(entry.getValue()))
        .forEach(entry -> System.out.println(entry.getKey() + "\t" + entry.getValue()));
  }

  private static boolean isUndelete(final Set<String> paths) {
    final Holder<Boolean> isUndelete = new Holder<>(true);
    paths.stream().forEach(path -> {
      final int separatorIndex = path.indexOf(File.separator);
      final String firstDir = path.substring(0, (separatorIndex >= 0 ? separatorIndex : path.length()));
      boolean inDeleteDir = false;
      for (final String deleteDir : DELETE_DIRS) {
        if (firstDir.equals(deleteDir)) {
          inDeleteDir = true;
          break;
        }
      }
      if (!inDeleteDir) {
        isUndelete.value = false;
      }
    });
    return isUndelete.value;
  }

  public static void checkForDuplicates(final Map<FotoSpec, Set<String>> photoSpecMap) throws IOException {
    photoSpecMap.entrySet().stream().filter(entry -> isDuplicate(entry.getValue()))
        .forEach(entry -> System.out.println(entry.getKey() + "\t" + entry.getValue()));
  }

  private static boolean isDuplicate(final Set<String> paths) {
    final Holder<Integer> numIgnoredDirs = new Holder<>(0);
    paths.stream().forEach(path -> {
      final int separatorIndex = path.indexOf(File.separator);
      final String firstDir = path.substring(0, (separatorIndex >= 0 ? separatorIndex : path.length()));
      for (final String ignoreDir : IGNORE_DUPLICATE_DIRS) {
        if (firstDir.equals(ignoreDir)) {
          numIgnoredDirs.value++;
          break;
        }
      }
    });
    return paths.size() - numIgnoredDirs.value > 1;
  }

  public static void checkForWrongYears(final Map<FotoSpec, Set<String>> photoSpecMap) throws IOException {
    photoSpecMap.entrySet().stream().filter(entry -> isWrongYear(entry.getKey().getTagDate(), entry.getValue()))
        .forEach(entry -> System.out.println(entry.getKey() + "\t" + entry.getValue()));
  }

  private static boolean isWrongYear(final Date tagDate, final Set<String> paths) {
    if (tagDate == null) {
      return false;
    }

    final Holder<Boolean> isWrongYear = new Holder<>(false);
    final Calendar cal = Calendar.getInstance();
    cal.setTime(tagDate);
    final String tagYearStr = Integer.toString(cal.get(Calendar.YEAR));

    paths.stream().filter(path -> path.replaceAll("[^\\d*].*$", "").length() == 4).filter(path -> {
      for (final String ignoreDir : IGNORE_WRONG_DATE_DIRS) {
        if (path.startsWith(ignoreDir)) {
          return false;
        }
      }
      return true;
    }).forEach(path -> isWrongYear.value = !path.startsWith(tagYearStr));
    return isWrongYear.value;
  }
}
