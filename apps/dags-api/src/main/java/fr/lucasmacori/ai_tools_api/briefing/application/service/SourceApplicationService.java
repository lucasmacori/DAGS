package fr.lucasmacori.ai_tools_api.briefing.application.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.CreateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.application.dto.UpdateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.service.SourceService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceApplicationService {

	private final SourceService sourceService;

	public List<Source> getSources(String userId) {
		return sourceService.getSources(userId);
	}

	public Source createSource(CreateSourceRequestBody request, String userId) {
		try {
			return sourceService.create(request.type(), request.title(), request.content(), userId);
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public Source updateSource(String sourceId, UpdateSourceRequestBody request) {
		if (!request.hasUpdates()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
		}

		try {
			return sourceService.update(sourceId, request.type(), request.title(), request.content())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source not found"));
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}

	public void deleteSource(String sourceId) {
		try {
			if (!sourceService.delete(sourceId)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source not found");
			}
		}
		catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
		}
	}
}
