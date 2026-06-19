package com.example.hr.location;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.location.dto.LocationDtos.CoordinatesRequest;
import com.example.hr.location.dto.LocationDtos.CreateRequest;
import com.example.hr.location.dto.LocationDtos.ManagerRequest;
import com.example.hr.location.dto.LocationDtos.PhotoResponse;
import com.example.hr.location.dto.LocationDtos.Response;
import com.example.hr.location.dto.LocationDtos.UpdateRequest;
import com.example.hr.location.service.LocationPhotoService;
import com.example.hr.location.service.LocationPhotoService.DownloadResult;
import com.example.hr.location.service.LocationService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 거점 관리 API(LOC-001~006). 생성은 시스템관리자, 거점별 변경·사진은 거점관리자 범위 RBAC(LOC-003).
 * 사진은 백엔드 경유 업로드/다운로드(presigned 미사용).
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final LocationService locationService;
	private final LocationPhotoService photoService;
	private final AuthorizationService authorizationService;

	public LocationController(LocationService locationService, LocationPhotoService photoService,
			AuthorizationService authorizationService) {
		this.locationService = locationService;
		this.photoService = photoService;
		this.authorizationService = authorizationService;
	}

	/** 거점관리자(해당 거점 범위) 권한 요구(LOC-003). */
	private void requireLocationManager(Long actorId, Long locationId) {
		authorizationService.checkAllowed(actorId,
			new AccessRequest(Role.LOCATION_MANAGER, ScopeType.LOCATION, locationId));
	}

	@PostMapping
	public Response create(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody CreateRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(locationService.create(request.locationCode(), request.name(),
			request.address(), request.locationType(), request.managerId()));
	}

	/** 활성 거점 목록(LOC-005). 인증 사용자 공통 조회. */
	@GetMapping
	public List<Response> listActive() {
		return locationService.findActive().stream().map(Response::from).toList();
	}

	@PutMapping("/{id}")
	public Response update(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody UpdateRequest request) {
		requireLocationManager(actorId, id);
		return Response.from(locationService.update(id, request.name(), request.address(),
			request.contact(), request.fax(), request.locationType()));
	}

	@PutMapping("/{id}/coordinates")
	public Response coordinates(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody CoordinatesRequest request) {
		requireLocationManager(actorId, id);
		return Response.from(
			locationService.updateCoordinates(id, request.latitude(), request.longitude()));
	}

	@PutMapping("/{id}/manager")
	public Response manager(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody ManagerRequest request) {
		requireLocationManager(actorId, id);
		return Response.from(locationService.assignManager(id, request.managerId()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivate(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		requireLocationManager(actorId, id);
		locationService.deactivate(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/photos")
	public PhotoResponse uploadPhoto(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@RequestParam("file") MultipartFile file) throws IOException {
		requireLocationManager(actorId, id);
		return PhotoResponse.from(photoService.upload(id, actorId, file.getOriginalFilename(),
			file.getContentType(), file.getSize(), file.getInputStream()));
	}

	@GetMapping("/{id}/photos")
	public List<PhotoResponse> listPhotos(@PathVariable Long id) {
		return photoService.list(id).stream().map(PhotoResponse::from).toList();
	}

	@GetMapping("/{id}/photos/{photoId}")
	public ResponseEntity<InputStreamResource> downloadPhoto(@PathVariable Long id,
			@PathVariable Long photoId) {
		DownloadResult result = photoService.download(photoId);
		String contentType = result.meta().getContentType() != null
			? result.meta().getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"inline; filename=\"" + result.meta().getFileName() + "\"")
			.contentType(MediaType.parseMediaType(contentType))
			.body(new InputStreamResource(result.stream()));
	}

	@DeleteMapping("/{id}/photos/{photoId}")
	public ResponseEntity<Void> deletePhoto(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id, @PathVariable Long photoId) {
		requireLocationManager(actorId, id);
		photoService.delete(photoId);
		return ResponseEntity.noContent().build();
	}
}
