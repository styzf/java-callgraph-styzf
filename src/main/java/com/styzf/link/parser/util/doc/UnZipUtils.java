package com.styzf.link.parser.util.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZipUtils {

    // 防止 zip 炸弹 (后面改成可配置)
    /** 压缩比阈值 */
    public static double thresholdRatio = 10;

    private static final Logger LOG = LoggerFactory.getLogger(UnZipUtils.class);

    private UnZipUtils() {}

    public static String text(String zipPath, String entryPath, Charset charset) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            ZipEntry entry = zipFile.getEntry(entryPath);
            if (entry == null) {
                return null;
            }
            return text(zipFile, entry, charset);
        } catch (Exception e) {
            String exceptionName = e.getClass().getName();
            String localizedMessage = e.getLocalizedMessage();
            LOG.error("{}: {} \tfile:///{} {}", exceptionName, localizedMessage, zipPath, entryPath);
            return null;
        }
    }

    private static String text(ZipFile zipFile, ZipEntry entry, Charset charset) {
        // TODO is / ?
        String zipFilePath = zipFile.getName();
        String entryPath = entry.getName();
        File outFile = new File(zipFilePath, entryPath);

        // for path injection vulnerabilities - zip slip vulnerabilities
        // like ../../../../../etc/password
        try {
            String unZipPath = outFile.getCanonicalPath();
            if (!unZipPath.startsWith(zipFilePath)) {
                LOG.error("Entry is outside of the target directory\n  unZipPath: {}\n  zipFilePath:\tfile:///{}",
                        unZipPath, zipFilePath);
                return null;
            }
        } catch (IOException e) {
            LOG.error("outFile.getCanonicalPath IOException\n  outFile.getPath(): {}\n  zipFilePath:\tfile:///{}",
                    outFile.getPath(), zipFilePath);
            return null;
        }

        if (entry.isDirectory()) {
            LOG.warn("entryPath should be file\tfile:///{} {}", zipFilePath, entryPath);
            return null;
        }

        int len;
        double totalSizeEntry = 0;
        char[] chars = new char[1024];
        final StringBuilder out = new StringBuilder();
        try (Reader reader = new InputStreamReader(zipFile.getInputStream(entry), charset)) {
            while ((len = reader.read(chars)) > 0) {
                totalSizeEntry += len;
                double compressionRatio = totalSizeEntry / entry.getCompressedSize();
                if (compressionRatio > thresholdRatio) {
                    LOG.error("ratio between compressed and uncompressed data is highly suspicious, " +
                            "looks like a Zip Bomb Attack");
                    return null;
                }
                out.append(chars, 0, len);
            }
        } catch (IOException e) {
            String path = zipFilePath.replace('\\', '/');
            LOG.error("unZipEntry IOException\tfile:///{}\n  {}", path, entry, e);
            return null;
        }
        return out.toString();
    }
}
