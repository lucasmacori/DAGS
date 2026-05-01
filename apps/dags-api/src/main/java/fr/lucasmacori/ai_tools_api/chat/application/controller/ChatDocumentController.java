package fr.lucasmacori.ai_tools_api.chat.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.lucasmacori.ai_tools_api.chat.application.dto.UploadChatDocumentsResponse;
import fr.lucasmacori.ai_tools_api.chat.application.service.ChatDocumentApplicationService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat/document")
@RequiredArgsConstructor
class ChatDocumentController {

	private final ChatDocumentApplicationService applicationService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Mono<UploadChatDocumentsResponse> uploadDocuments(@RequestPart("files") Flux<FilePart> files) {
		return applicationService.uploadDocuments(files)
				.map(UploadChatDocumentsResponse::from);
	}

	@DeleteMapping(path = "/{documentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	Mono<Void> deleteDocument(@PathVariable String documentId) {
		return applicationService.deleteDocument(documentId);
	}
}
