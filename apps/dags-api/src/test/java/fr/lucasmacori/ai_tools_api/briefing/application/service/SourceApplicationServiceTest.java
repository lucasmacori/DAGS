package fr.lucasmacori.ai_tools_api.briefing.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import fr.lucasmacori.ai_tools_api.briefing.application.dto.CreateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.application.dto.UpdateSourceRequestBody;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.Source;
import fr.lucasmacori.ai_tools_api.briefing.domain.model.SourceType;
import fr.lucasmacori.ai_tools_api.briefing.domain.service.SourceService;

class SourceApplicationServiceTest {

	@Test
	void getSourcesDelegatesToDomainService() {
		SourceService sourceService = mock(SourceService.class);
		List<Source> expected = List.of(new Source("source-1", SourceType.PLAIN_TEXT, "Title", "Body", LocalDateTime.now(), LocalDateTime.now(), null));
		when(sourceService.getSources("user-1")).thenReturn(expected);
		SourceApplicationService applicationService = new SourceApplicationService(sourceService);

		List<Source> result = applicationService.getSources("user-1");

		assertEquals(expected, result);
	}

	@Test
	void createSourceDelegatesToDomainService() {
		SourceService sourceService = mock(SourceService.class);
		Source source = new Source("source-1", SourceType.PLAIN_TEXT, "Title", "Body", LocalDateTime.now(), LocalDateTime.now(), null);
		when(sourceService.create(SourceType.PLAIN_TEXT, "Title", "Body", "user-1")).thenReturn(source);
		SourceApplicationService applicationService = new SourceApplicationService(sourceService);

		Source result = applicationService.createSource(new CreateSourceRequestBody(SourceType.PLAIN_TEXT, "Title", "Body"), "user-1");

		assertEquals(source, result);
	}

	@Test
	void updateSourceReturnsNotFoundWhenSourceDoesNotExist() {
		SourceService sourceService = mock(SourceService.class);
		when(sourceService.update("source-1", null, null, "Updated")).thenReturn(Optional.empty());
		SourceApplicationService applicationService = new SourceApplicationService(sourceService);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> applicationService.updateSource("source-1", new UpdateSourceRequestBody(null, null, "Updated")));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void updateSourceRejectsEmptyPatch() {
		SourceService sourceService = mock(SourceService.class);
		SourceApplicationService applicationService = new SourceApplicationService(sourceService);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> applicationService.updateSource("source-1", new UpdateSourceRequestBody(null, null, null)));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	void deleteSourceDelegatesToDomainService() {
		SourceService sourceService = mock(SourceService.class);
		when(sourceService.delete("source-1")).thenReturn(true);
		SourceApplicationService applicationService = new SourceApplicationService(sourceService);

		applicationService.deleteSource("source-1");

		verify(sourceService).delete("source-1");
	}
}
