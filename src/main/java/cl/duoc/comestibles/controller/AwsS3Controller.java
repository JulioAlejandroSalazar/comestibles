package cl.duoc.comestibles.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.pdf.PdfDocument;

import cl.duoc.comestibles.dto.S3ObjectDto;
import cl.duoc.comestibles.services.AwsS3Service;
import cl.duoc.comestibles.services.EfsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class AwsS3Controller {

	private final AwsS3Service awsS3Service;

	private final EfsService efsService;

	// Listar objetos en un bucket
	@GetMapping("/{bucket}/objects")
	public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {

		List<S3ObjectDto> dtoList = awsS3Service.listObjects(bucket);
		return ResponseEntity.ok(dtoList);
	}

	// Obtener objeto como stream
	@GetMapping("/{bucket}/object/stream")
	public ResponseEntity<InputStreamResource> streamObject(@PathVariable String bucket, @RequestParam("key") String key) {
		ResponseInputStream<GetObjectResponse> s3Stream = awsS3Service.getObjectInputStream(bucket, key);

		String contentType = s3Stream.response().contentType();
		if (contentType == null || contentType.isBlank()) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"inline; filename=" + key.substring(key.lastIndexOf('/') + 1))
				.contentType(MediaType.parseMediaType(contentType))
				.body(new InputStreamResource(s3Stream));
	}


	// Descargar archivo como byte[]
	@GetMapping("/{bucket}/object")
	public ResponseEntity<byte[]> downloadObject(@PathVariable String bucket, @RequestParam("key") String key) {
		byte[] fileBytes = awsS3Service.downloadAsBytes(bucket, key);
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key.substring(key.lastIndexOf('/') + 1))
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(fileBytes);
	}	

	// Subir archivo
	@PostMapping("/{bucket}/object/{key}")
	public ResponseEntity<Void> uploadObject(@PathVariable String bucket, @PathVariable String key,
											 @RequestParam("file") MultipartFile file) {
		try {
			File savedFile = efsService.saveToEfs(key, file);	
			awsS3Service.upload(bucket, key, savedFile);
	
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/{bucket}/object/{key}/pdf")
    public ResponseEntity<String> uploadPdf(@PathVariable String bucket, @PathVariable String key,
                                            @RequestParam("file") MultipartFile file) {
        try {
            if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                return ResponseEntity.badRequest().body("El archivo debe ser un PDF");
            }

            awsS3Service.upload(bucket, key, file.getBytes(), "application/pdf");

            return ResponseEntity.ok("PDF subido correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al subir PDF");
        }
    }	

	// Mover objeto dentro del mismo bucket
	@PostMapping("/{bucket}/move")
	public ResponseEntity<Void> moveObject(@PathVariable String bucket, @RequestParam String sourceKey,
			@RequestParam String destKey) {
		awsS3Service.moveObject(bucket, sourceKey, destKey);
		return ResponseEntity.ok().build();
	}

	// Borrar objeto
	@DeleteMapping("/{bucket}/object/**")
	public ResponseEntity<Void> deleteObject(@PathVariable String bucket, HttpServletRequest request) {

		String path = request.getRequestURI();
		String key = path.substring(path.indexOf("/object/") + 8);
		awsS3Service.deleteObject(bucket, key);
		return ResponseEntity.noContent().build();
	}

	
	// actualizar objeto
	@PutMapping("/{bucket}/object/**")
	public ResponseEntity<String> updateObject(
		@PathVariable String bucket,
		@RequestParam("file") MultipartFile file,
		HttpServletRequest request) {
		
		try {
			if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
				return ResponseEntity.badRequest().body("El archivo debe ser un PDF");
			}
			
			// Obtengo el path completo
			String path = request.getRequestURI();
			String key = path.substring(path.indexOf("/object/") + 8); // quita "/object/"
			
			awsS3Service.upload(bucket, key, file.getBytes(), file.getContentType());
			return ResponseEntity.ok("Archivo actualizado correctamente");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Error al actualizar archivo");
		}
	}
	
	
	
	




	
	// EFS

	// descargar archivo
	@GetMapping("/efs/object/{filename}")
	public ResponseEntity<byte[]> downloadFromEfs(@PathVariable String filename) {
		try {
			byte[] fileBytes = efsService.readFromEfs(filename);
			return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(fileBytes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	// listar archivos
	@GetMapping("/efs/objects")
	public ResponseEntity<List<String>> listFilesInEfs() {
		try {
			List<String> filenames = efsService.listFiles();
			return ResponseEntity.ok(filenames);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
	
	// borrar archivo
	@DeleteMapping("/efs/object/{filename}")
	public ResponseEntity<Void> deleteFromEfs(@PathVariable String filename) {
		try {
			efsService.deleteFromEfs(filename);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}


}
