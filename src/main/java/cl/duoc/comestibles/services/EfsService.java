package cl.duoc.comestibles.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EfsService {
    
    @Value("${efs.path}")
    private String efsPath;

    // guardar archivo
    public File saveToEfs(String filename, MultipartFile multipartFile) throws IOException {
        File dest = new File(efsPath, filename);
        File parentDir = dest.getParentFile();
        if(parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        multipartFile.transferTo(dest);
        return dest;
    }

    // leer archivo como byte[]
    public byte[] readFromEfs(String filename) throws IOException {
        File file = new File(efsPath, filename);
        if (!file.exists()) {
            throw new IOException("Archivo no encontrado: " + filename);
        }
        return Files.readAllBytes(file.toPath());
    }

    // listar archivos
    public List<String> listFiles() throws IOException {
        File dir = new File(efsPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directorio EFS no válido: " + efsPath);
        }
        return Arrays.stream(dir.listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    // eliminar archivo
    public void deleteFromEfs(String filename) throws IOException {
        File file = new File(efsPath, filename);
        if (file.exists() && !file.delete()) {
            throw new IOException("No se pudo eliminar el archivo: " + filename);
        }
    }
}
