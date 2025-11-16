package in.bhuvan.billingsoftware;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

	@CrossOrigin(origins = {"http://localhost:5173"})
	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("OK");
	}
} 