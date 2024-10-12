package said.ahmad.javafx.tracker.system.hasher;

import lombok.Data;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FileGrouper {

    // Method to compute SHA-256 hash of a file
    private static String getFileHash(PathLayer filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = filePath.getInputFileStream()) {
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
        }
        // Convert hash bytes to hex format
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static GroupFileResult groupFilesByHash(List<PathLayer> filePaths) {
        Map<String, List<PathLayer>> hashToFileGroup = new HashMap<>();
        StringBuilder error = new StringBuilder();

        for (PathLayer filePath : filePaths) {
            if (!filePath.exists() || !filePath.isFile()) {
                error.append("File does not exist or is not a file: ").append(filePath);
                continue;
            }
            try {
                String fileHash = getFileHash(filePath);
                hashToFileGroup.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(filePath);
            } catch (NoSuchAlgorithmException | IOException e) {
                error.append("Error processing file: ").append(filePath).append("\n").append(e.getMessage());
                e.printStackTrace();
            }
        }
        var result = prettyPrintGroupFileMap(hashToFileGroup);
        return new GroupFileResult(result, error.toString());
    }

    public static String prettyPrintGroupFileMap(Map<String, List<PathLayer>> hashToFileGroup) {
        List<PathLayer> soloFiles = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        // Separate single file groups into "Different Solo"
        Map<String, List<PathLayer>> sortedGroups = new LinkedHashMap<>();
        int groupCount = 1;

        for (Map.Entry<String, List<PathLayer>> entry : hashToFileGroup.entrySet()) {
            if (entry.getValue().size() == 1) {
                soloFiles.addAll(entry.getValue());
            } else {
                sortedGroups.put("Group " + (char)('A' + groupCount - 1), entry.getValue());
                groupCount++;
            }
        }

        // Print non-Different Solo groups
        for (Map.Entry<String, List<PathLayer>> entry : sortedGroups.entrySet()) {
            String groupName = entry.getKey();
            List<PathLayer> files = entry.getValue();
            result.append(groupName).append(" --> ").append(files.size()).append(" <-- files\n");
            for (PathLayer file : files) {
                result.append(file.getName()).append("\n");
            }
            result.append("\n");
        }

        // Print "Different Solo" group for single-file entries
        if (!soloFiles.isEmpty()) {
            result.append("Group Different Solo --> ").append(soloFiles.size()).append(" <-- files\n");
            for (PathLayer file : soloFiles) {
                result.append(file.getName()).append("\n");
            }
            result.append("\n");
        }

        return result.toString();
    }
}
