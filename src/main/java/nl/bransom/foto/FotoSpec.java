package nl.bransom.foto;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;

public class FotoSpec {

  private static String SEPARATOR = "\t";
  private static final String DATE_FORMAT = "yyyy-MM-dd";

  private String name;
  private long sizeInBytes;
  private Date tagDate;
  private long exifHash;

  public FotoSpec(final File fotoFile) {
    name = fotoFile.getName();
    sizeInBytes = fotoFile.length();
    readExifData(fotoFile);
  }

  private void readExifData(final File fotoFile) {
    try {
      final StringBuilder sb = new StringBuilder();
      final Metadata metadata = ImageMetadataReader.readMetadata(fotoFile);
      final Directory exifData = metadata.getDirectory(ExifDirectory.class);
      if (exifData != null) {
        final Iterator<?> iter = exifData.getTagIterator();
        while (iter.hasNext()) {
          sb.append(iter.next());
        }
      }
      exifHash = sb.toString().hashCode();
      if (exifData.containsTag(ExifDirectory.TAG_DATETIME)) {
        tagDate = exifData.getDate(ExifDirectory.TAG_DATETIME);
      }
    } catch (ImageProcessingException e) {
      // ignore; probably MP4
    } catch (MetadataException e) {
      // ignore
    }
  }

  public FotoSpec(final String fotoSpecString) {
    final String[] split = fotoSpecString.split(SEPARATOR);
    name = split[0];
    sizeInBytes = Long.parseLong(split[1]);
    if (split.length >= 3) {
      exifHash = Long.parseLong(split[2]);
    }
    if (split.length >= 4) {
      try {
        tagDate = new SimpleDateFormat(DATE_FORMAT).parse(split[3]);
      } catch (ParseException e) {
        // ignore
      }
    }
  }

  public Date getTagDate() {
    return tagDate;
  }

  @Override
  public String toString() {
    return name + SEPARATOR + sizeInBytes + SEPARATOR + exifHash + SEPARATOR
        + (tagDate != null ? new SimpleDateFormat(DATE_FORMAT).format(tagDate) : "?");
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FotoSpec)) {
      return false;
    }
    final FotoSpec other = (FotoSpec) obj;
    return name.equals(other.name) && sizeInBytes == other.sizeInBytes && exifHash == other.exifHash;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += 31 * hash + name.hashCode();
    hash += 31 * hash + sizeInBytes;
    hash += 31 * hash + exifHash;
    return hash;
  }
}
